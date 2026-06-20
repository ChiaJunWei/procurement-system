# ADR-0002: Multi-Tenancy via Shared Schema + Row-Level Security

- **Status:** Accepted
- **Date:** 2026-06-20

## Context

Tenants are **agencies** (20+, growing). Data must be strictly isolated per agency, but cross-agency reporting and shared catalogs are required. We must support 5M tx/year and 10-year retention without exploding operational cost.

## Options considered

1. **Database-per-tenant** — strongest isolation, highest ops cost (20+ DBs, 20× migrations), poor for cross-agency reporting. Rejected.
2. **Schema-per-tenant** — moderate isolation, migration fan-out grows with tenants, connection-pool pressure. Rejected.
3. **Shared schema + `tenant_id` + PostgreSQL Row-Level Security (RLS)** — single migration set, native DB-enforced isolation, easy cross-tenant reporting via privileged role. **Chosen.**

## Decision

- Every tenant-scoped table carries a non-null `tenant_id UUID`.
- PostgreSQL **RLS policies** filter every query by the session GUC `app.current_tenant`, set per-request by `TenantContext` before any DB access.
- A separate `reporting_role` may bypass RLS for cross-agency analytics.
- `tenant_id` is propagated through Kafka headers and Redis key prefixes.

See [multi-tenancy.md](../architecture/multi-tenancy.md).

## Consequences

- Isolation is enforced by the database even if application code forgets a `WHERE` clause — defense in depth.
- A forgotten `SET app.current_tenant` fails closed (RLS returns zero rows), surfacing bugs loudly.
- Future "extract a noisy tenant to its own DB" remains possible because the tenant boundary is explicit.
