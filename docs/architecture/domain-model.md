# Domain Model & Aggregate Roots

DDD tactical patterns. Each bounded context owns its aggregates. Aggregates are the **transactional consistency boundary** — one aggregate per transaction; cross-aggregate consistency is eventual, via domain events.

## Aggregate roots by context

| Context     | Aggregate Root(s)                              | Key Value Objects                                  |
|-------------|-----------------------------------------------|----------------------------------------------------|
| Identity    | `UserAccount`                                  | `Email`, `KeycloakSubject`, `RoleAssignment`       |
| Agency      | `Agency`, `OrgUnit`, `Budget`                  | `AgencyCode`, `FiscalYear`, `Money`                |
| Procurement | **`PurchaseRequisition`**, `Solicitation`, `Bid`, `Award` | `Money`, `LineItem`, `RequisitionNumber`, `BudgetReference` |
| Vendor      | `Vendor`                                       | `TaxId`, `QualificationStatus`, `Capability`       |
| Contract    | `Contract`                                     | `ContractTerm`, `Obligation`, `Milestone`          |
| Workflow    | `WorkflowInstance`                             | `WorkflowState`, `Transition`, `TaskAssignment`    |
| Policy      | `PolicySet`                                    | `Rule`, `Effect`, `Obligation`                     |
| Audit       | `AuditRecord` (append-only)                    | `ActorRef`, `ResourceRef`, `Change`                |
| Integration | `IntegrationMessage`                           | `Endpoint`, `DeliveryStatus`                       |
| Reporting   | (read models only — projections, no aggregates)| —                                                  |

## Rules every aggregate follows

1. **Reference other aggregates by ID only** (`AgencyId`, never `Agency`). Keeps boundaries clean and extraction-safe.
2. **State changes go through behavior methods** that enforce invariants and **record domain events** (`AbstractAggregateRoot.registerEvent`). No public setters.
3. **Value objects are immutable** Java `record`s with validation in the compact constructor.
4. **Every tenant-scoped aggregate carries a `TenantId`** (`shared.tenant.TenantId`).
5. Aggregates extend `shared.domain.AbstractAggregateRoot<ID>` and use a strongly-typed identifier (`record PurchaseRequisitionId(UUID value)`).

## Reference: `PurchaseRequisition` aggregate

```
PurchaseRequisition (root)
├── id: PurchaseRequisitionId
├── tenantId: TenantId            # owning agency
├── requisitionNumber: RequisitionNumber
├── requesterId: UserId
├── status: RequisitionStatus     # DRAFT → SUBMITTED → APPROVED → ... (driven by Workflow engine)
├── justification: String
├── lineItems: List<LineItem>     # entities owned by the root
├── totalEstimatedCost: Money     # derived invariant = Σ lineItems
└── audit: created/modified metadata

LineItem (entity, part of aggregate)
├── description, quantity, unitOfMeasure
├── estimatedUnitPrice: Money
└── budgetReference: BudgetReference   # ID-reference into Agency context
```

Invariants enforced in the aggregate: at least one line item to submit; `totalEstimatedCost` is always Σ of line items; only `DRAFT` requisitions are editable; status transitions are delegated to the Workflow engine.

See the full implementation in `modules/procurement/.../domain/` and [reference-vertical-slice.md](reference-vertical-slice.md).
