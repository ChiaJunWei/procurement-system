# Multi-Tenancy Strategy

**Model:** Shared schema + `tenant_id` column + PostgreSQL Row-Level Security (RLS). Tenant = Agency. See [ADR-0002](../adr/0002-multi-tenancy.md).

## Request lifecycle

```
HTTP request
  → TenantContextFilter            # extracts tenant from JWT claim `agency_id`
      → TenantContext.set(tenantId)  (ThreadLocal, also bound to Reactor/virtual-thread scope)
        → @Transactional opens connection
            → ConnectionPreparer runs: SET app.current_tenant = '<uuid>'
              → RLS policies filter every statement automatically
        → response
  → TenantContext.clear()          # always, in finally
```

## Database enforcement

Every tenant-scoped table:

```sql
ALTER TABLE procurement.purchase_requisition ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON procurement.purchase_requisition
  USING (tenant_id = current_setting('app.current_tenant')::uuid)
  WITH CHECK (tenant_id = current_setting('app.current_tenant')::uuid);
```

- `USING` filters reads/updates/deletes; `WITH CHECK` blocks inserting/updating rows into another tenant.
- The application connects as a **non-superuser role** that does NOT have `BYPASSRLS`, so isolation cannot be forgotten.
- Cross-agency reporting uses a dedicated `reporting_role` (granted `BYPASSRLS`) and is only reachable from the Reporting module's read-only datasource.

## Propagation beyond the DB

| Layer  | Mechanism                                              |
|--------|-------------------------------------------------------|
| Kafka  | `tenant_id` header on every message; consumers re-establish `TenantContext` before processing |
| Redis  | Key namespace prefix `t:{tenantId}:...`               |
| S3     | Object key prefix `tenants/{tenantId}/...` + bucket policy |
| Logs   | `tenantId` in MDC for every log line                  |

## Failure mode = fail closed

If `app.current_tenant` is unset, RLS evaluates `current_setting(...)` → error/empty, returning **zero rows** rather than leaking. Background jobs and event consumers MUST call `TenantContext.set(...)` (or `TenantContext.runAs(tenantId, () -> ...)`) explicitly.

## Implementation pointers

- `shared.tenant.TenantContext` — ThreadLocal holder + `runAs` helper.
- `shared.tenant.TenantConnectionPreparer` — sets the GUC on connection checkout.
- `shared.tenant.TenantContextFilter` — Spring filter extracting the claim.
