# Policy Engine Framework

Declarative rule evaluation for **both** authorization (ABAC) and business rules (spend thresholds, conflict-of-interest, delegation of authority). See [ADR-0004](../adr/0004-workflow-and-policy-engines.md). Lives in `shared.policy`.

## Model (PDP/PEP, ABAC-style)

```
PEP (Policy Enforcement Point)  →  PDP (Policy Decision Point)  →  Decision
   e.g. a service guard              evaluates a PolicySet         PERMIT | DENY | NOT_APPLICABLE (+ obligations)
```

```java
PolicyRequest req = PolicyRequest.builder()
    .subject(currentUser)                  // roles, agency, attributes
    .action("procurement:requisition:submit")
    .resource(requisition)                 // tenantId, amount, category
    .environment(now, ip)
    .build();

PolicyDecision decision = policyEngine.evaluate(req);
if (decision.isDenied()) throw new PolicyViolationException(decision.reasons());
decision.obligations().forEach(obligationHandler::apply);  // e.g. "require second approver"
```

## Rule definition (data-driven, tenant-overridable)

```yaml
policySet: procurement.requisition.submit
combiningAlgorithm: deny-overrides
rules:
  - id: requester-must-own-agency
    effect: DENY
    condition: "subject.agencyId != resource.tenantId"
  - id: high-value-needs-second-approval
    effect: PERMIT
    condition: "resource.amount > 50000"
    obligations: [ { type: REQUIRE_ADDITIONAL_APPROVAL, role: FINANCE_DIRECTOR } ]
  - id: default-permit-own-draft
    effect: PERMIT
    condition: "subject.id == resource.requesterId"
```

- Conditions are evaluated by a sandboxed SpEL/expression evaluator over a typed attribute model.
- `combiningAlgorithm`: `deny-overrides`, `permit-overrides`, `first-applicable`.
- Tenants may layer overrides; resolution is base policy + tenant overrides merged deterministically.

## Why one engine for authz AND business rules

Both are "given attributes, decide permit/deny with obligations." Unifying them means a single audited, testable, hot-reloadable place for compliance logic — critical for a 10-year government system.

## Extension points

- Add a `PolicySet` (data).
- Add a custom `AttributeResolver` bean to expose new attributes to conditions.
- Add an `ObligationHandler` for a new obligation type.

## Framework classes

`PolicyEngine` (port), `DefaultPolicyEngine`, `PolicySet`, `Rule`, `PolicyRequest`, `PolicyDecision`, `Obligation`, `AttributeResolver`, `ObligationHandler`, `CombiningAlgorithm`.

## Integration with Workflow

Workflow `Guard`s typically delegate to the Policy engine, so approval routing and authorization share one rule base.
