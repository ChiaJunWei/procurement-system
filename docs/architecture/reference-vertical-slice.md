# Reference Vertical Slice — Create Purchase Requisition

This is the **canonical pattern**. To add any new write feature, copy this slice's shape into the
relevant module. It exercises every architectural layer end-to-end.

## Flow

```
Next.js form (CreateRequisitionForm.tsx)
  → Zod validation (same rules as backend)
  → requisitionApi.createRequisition()           [features/requisitions/api]
      → POST /api/v1/procurement/requisitions     (Bearer JWT: sub, agency_id, roles)

Spring Boot
  TenantContextFilter      → sets TenantContext from JWT agency_id claim
  @PreAuthorize            → coarse authority check (procurement:requisition:create)
  PurchaseRequisitionController
      → CreateRequisitionRequest (Bean Validation at the edge)
      → maps DTO → CreatePurchaseRequisitionCommand
  CreatePurchaseRequisitionService  @Transactional   ← the one transaction boundary
      1. PurchaseRequisition.create(...)   → invariants enforced, domain event registered
      2. repository.save(...)              → RLS sets app.current_tenant; row written
      3. workflowEngine.start(...)         → lifecycle instance in DRAFT
      4. outboxAppender.append(event)      → outbox row in SAME tx  (atomic state+event)
  commit

OutboxRelay (async)        → publishes PurchaseRequisitionCreated to Kafka
  ├─ Audit consumer        → immutable, hash-chained audit_record
  ├─ Reporting consumer    → read-model projection
  └─ Workflow consumer     → (where wired) advances dependent flows
```

## File map (what to copy)

| Layer            | File                                                                                  |
|------------------|---------------------------------------------------------------------------------------|
| UI page          | `frontend/src/app/requisitions/new/page.tsx`                                          |
| UI component     | `frontend/src/features/requisitions/components/CreateRequisitionForm.tsx`             |
| UI hook/api/types| `frontend/src/features/requisitions/{hooks,api,types}`                                |
| Controller       | `backend/modules/procurement/.../api/PurchaseRequisitionController.java`              |
| Request/Response | `backend/modules/procurement/.../api/CreateRequisitionRequest.java` / `...Response`   |
| Command          | `backend/modules/procurement/.../application/CreatePurchaseRequisitionCommand.java`   |
| Use-case service | `backend/modules/procurement/.../application/CreatePurchaseRequisitionService.java`   |
| Aggregate        | `backend/modules/procurement/.../domain/PurchaseRequisition.java`                     |
| Entities/VOs     | `backend/modules/procurement/.../domain/{LineItem,Money,RequisitionNumber,...}.java`  |
| Domain event     | `backend/modules/procurement/.../domain/PurchaseRequisitionCreated.java`              |
| Repository port  | `backend/modules/procurement/.../domain/PurchaseRequisitionRepository.java`           |
| Repository adapter| `backend/modules/procurement/.../infrastructure/JpaPurchaseRequisitionRepository.java`|
| JPA entity+mapper| `backend/modules/procurement/.../infrastructure/{PurchaseRequisitionEntity,...Mapper}`|
| Migration        | `backend/bootstrap/.../db/migration/V2__procurement_purchase_requisition.sql`         |
| Domain test      | `backend/modules/procurement/src/test/.../PurchaseRequisitionTest.java`               |

## Checklist for a new write feature

1. **Domain first.** Add/extend the aggregate; put invariants in behavior methods; register a
   `<Aggregate><Verb>ed` event. Write the domain unit test (one per invariant).
2. **Port.** Add any new repository method to the domain port.
3. **Application.** Add a `*Command` + `*Service` with the `@Transactional` boundary. Save, drive
   workflow, append event to outbox. Never touch audit directly.
4. **Infrastructure.** Implement the port adapter + JPA mapping. Add a Flyway migration with the
   `tenant_id` column **and RLS policies** (copy V2).
5. **API.** Add a thin controller + validated DTOs + `@PreAuthorize`. Map DTO ⇄ command.
6. **Frontend.** Add a feature slice: Zod types, api client fn, mutation hook, component, route.
7. **Verify.** `./gradlew check` runs unit + ArchUnit + Testcontainers integration tests.

## What you must NOT do

- Don't write audit code in the feature — Audit consumes events.
- Don't add `WHERE tenant_id = ?` — RLS handles it. (Background jobs: `TenantContext.runAs`.)
- Don't import another context's `domain`/`application`/`infrastructure` — use its `api` or events.
- Don't put business rules in the controller or the JPA entity.
- Don't invent a new pattern when this one fits.
