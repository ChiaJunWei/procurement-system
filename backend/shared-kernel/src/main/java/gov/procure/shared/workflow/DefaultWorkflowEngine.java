package gov.procure.shared.workflow;

import gov.procure.shared.event.OutboxAppender;
import gov.procure.shared.tenant.TenantContext;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reference implementation of the {@link WorkflowEngine}. Generic and data-driven: it resolves the
 * applicable transition from a {@link WorkflowDefinition}, evaluates named guards, runs named
 * actions, persists the new state (optimistic-locked) atomically with the caller's transaction, and
 * appends a {@link WorkflowTransitioned} event to the outbox. Features add definitions + guard/
 * action beans — never touch this class.
 */
@Service
public class DefaultWorkflowEngine implements WorkflowEngine {

    private final WorkflowDefinitionRegistry definitions;
    private final WorkflowInstanceRepository instances;
    private final Map<String, WorkflowGuard> guards;
    private final Map<String, WorkflowAction> actions;
    private final OutboxAppender outbox;
    private final Clock clock;

    public DefaultWorkflowEngine(WorkflowDefinitionRegistry definitions,
                                 WorkflowInstanceRepository instances,
                                 Map<String, WorkflowGuard> guards,
                                 Map<String, WorkflowAction> actions,
                                 OutboxAppender outbox,
                                 Clock clock) {
        this.definitions = definitions;
        this.instances = instances;
        this.guards = guards;
        this.actions = actions;
        this.outbox = outbox;
        this.clock = clock;
    }

    @Override
    @Transactional
    public WorkflowInstanceRef start(String definitionKey, UUID aggregateId, WorkflowContext context) {
        WorkflowDefinition def = definitions.latest(definitionKey);
        var entity = new WorkflowInstanceEntity(
            UUID.randomUUID(), context.tenantId().value(), def.key(), def.version(),
            aggregateId, def.initialState().name(), clock.instant());
        instances.save(entity);
        return new WorkflowInstanceRef(entity.id(), aggregateId, def.initialState());
    }

    @Override
    @Transactional
    public WorkflowResult fire(UUID aggregateId, WorkflowEvent event, WorkflowContext context) {
        WorkflowInstanceEntity instance = instances.findByAggregateId(aggregateId)
            .orElseThrow(() -> new WorkflowException("No workflow instance for aggregate " + aggregateId));
        WorkflowDefinition def = definitions.get(instance.definitionKey(), instance.definitionVersion());
        WorkflowState current = WorkflowState.of(instance.currentState());

        Transition transition = def.transitionFor(current, event).orElseThrow(() ->
            new WorkflowException("workflow.illegal-transition",
                "No transition for event %s in state %s (definition %s)"
                    .formatted(event.name(), current.name(), def.key())));

        for (String guardName : transition.guards()) {
            WorkflowGuard guard = guards.get(guardName);
            if (guard == null) {
                throw new WorkflowException("Unknown workflow guard bean: " + guardName);
            }
            if (!guard.isSatisfied(context)) {
                throw new WorkflowException("workflow.guard-failed",
                    "Guard '%s' rejected event %s".formatted(guardName, event.name()));
            }
        }

        transition.actions().forEach(actionName -> {
            WorkflowAction action = actions.get(actionName);
            if (action == null) {
                throw new WorkflowException("Unknown workflow action bean: " + actionName);
            }
            action.execute(context);
        });

        instance.moveTo(transition.to().name(), clock.instant());
        instances.save(instance);

        outbox.append(new WorkflowTransitioned(
            UUID.randomUUID(), clock.instant(), aggregateId, context.tenantId(),
            def.key(), current.name(), transition.to().name(), event.name()));

        return new WorkflowResult(current, transition.to(), transition.actions());
    }

    @Override
    public WorkflowState currentState(UUID aggregateId) {
        return instances.findByAggregateId(aggregateId)
            .map(i -> WorkflowState.of(i.currentState()))
            .orElseThrow(() -> new WorkflowException("No workflow instance for aggregate " + aggregateId));
    }
}
