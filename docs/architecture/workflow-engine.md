# Workflow Engine Framework

A generic, data-driven, persistent **state machine** used by any aggregate that needs lifecycle/approval orchestration. See [ADR-0004](../adr/0004-workflow-and-policy-engines.md). Lives in `shared.workflow`.

## Concepts

| Concept              | Meaning                                                                 |
|----------------------|-------------------------------------------------------------------------|
| `WorkflowDefinition` | Versioned, tenant-overridable config: states, transitions, guards, actions. Stored as data. |
| `WorkflowState`      | A named state (e.g. `DRAFT`, `PENDING_APPROVAL`, `APPROVED`).            |
| `Transition`         | `from → to` triggered by an `event`, gated by `guards`, firing `actions`.|
| `Guard`              | Predicate (`WorkflowContext → boolean`). Often delegates to the Policy engine. |
| `Action`             | Side effect on entry/exit/transition (notify, assign task, publish event). |
| `WorkflowInstance`   | Runtime state of one aggregate moving through a definition.              |
| `TaskAssignment`     | A human task (approval) with assignee, SLA, escalation.                  |

## How a feature uses it

1. Register a definition (data, usually a migration or admin UI), e.g. `purchase-requisition.approval`:

```yaml
definitionKey: purchase-requisition.approval
version: 1
initialState: DRAFT
states: [DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, CANCELLED]
transitions:
  - { event: SUBMIT,   from: DRAFT,            to: PENDING_APPROVAL, guards: [hasLineItems, withinBudget] }
  - { event: APPROVE,  from: PENDING_APPROVAL, to: APPROVED,         guards: [isAuthorizedApprover] }
  - { event: REJECT,   from: PENDING_APPROVAL, to: REJECTED }
  - { event: CANCEL,   from: [DRAFT, PENDING_APPROVAL], to: CANCELLED }
approvalChains:                       # resolved dynamically from Policy/Agency context
  - { state: PENDING_APPROVAL, resolver: thresholdBasedApprovers, slaHours: 48, escalateTo: supervisor }
```

2. The aggregate delegates transitions:

```java
WorkflowResult result = workflowEngine.fire(instanceId, WorkflowEvent.SUBMIT, ctx);
this.status = RequisitionStatus.from(result.newState());
```

3. Guards/actions are looked up from a Spring registry by name, so agents add a `@Component("withinBudget") implements WorkflowGuard` and reference it by key — **no engine changes**.

## Extension points (the only things a feature adds)

- A `WorkflowDefinition` (config/data).
- Named `WorkflowGuard` / `WorkflowAction` beans.
- An `ApproverResolver` bean if approval routing is custom.

## Guarantees

- Transitions are atomic with the aggregate save (same transaction).
- Every transition emits a `WorkflowTransitioned` event → Audit.
- SLA breaches schedule escalation actions via the scheduler.
- Definitions are versioned; in-flight instances keep their pinned version.

## Framework classes

`WorkflowEngine` (port), `DefaultWorkflowEngine`, `WorkflowDefinition`, `WorkflowInstance`, `Transition`, `WorkflowGuard`, `WorkflowAction`, `ApproverResolver`, `WorkflowRegistry`, `TaskAssignment`.
