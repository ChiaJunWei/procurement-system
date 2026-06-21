# MOCKS — what's faked to get a runnable model

This file is the **single source of truth for everything stubbed/mocked** so the platform runs
end-to-end with no external services. Each item says what's real, what's faked, and how to make it
real. Remove items from this list as they're replaced.

> TL;DR: the **frontend** runs fully mocked (`NEXT_PUBLIC_USE_MOCK=true`), and the **backend** runs
> under the `mock` Spring profile with H2 + no Kafka/Keycloak/Redis/S3. Both are independently
> runnable today.

## How to run the runnable model

**Backend** (in-memory, no Docker/Postgres/Kafka/Keycloak):
```bash
cd backend
./gradlew :bootstrap:bootRun --args='--spring.profiles.active=mock'
# health: http://localhost:8080/actuator/health   (set SERVER_PORT to change)
# H2 console: http://localhost:8080/h2-console  (JDBC URL: jdbc:h2:mem:procurement)
```

**Frontend** (mock API, stub auth):
```bash
cd frontend
npm install
npm run dev          # http://localhost:3000  → /requisitions/new
```

Smoke test the backend slice:
```bash
curl -X POST http://localhost:8080/api/v1/procurement/requisitions \
  -H 'Content-Type: application/json' \
  -d '{"requesterId":"11111111-1111-1111-1111-111111111111","justification":"demo","currency":"USD",
       "lineItems":[{"description":"Laptop","quantity":1,"estimatedUnitPrice":1000.00,
                     "budgetId":"33333333-3333-3333-3333-333333333333","accountingCode":"A"}]}'
# → 201 {"requisitionId":"..."}  (workflow instance + outbox event also written; relay logs [MOCK-KAFKA])
```

---

## Backend mocks (active only under the `mock` profile)

All real implementations are annotated `@Profile("!mock")`; the fakes below are `@Profile("mock")`
and live in `backend/bootstrap/src/main/java/gov/procure/bootstrap/mock/`.

| # | Concern | Real (prod) | Mocked (mock profile) | How to make it real |
|---|---------|-------------|------------------------|---------------------|
| B1 | **Database** | PostgreSQL + Flyway migrations (`V1`–`V3`) | H2 in-memory; schema via Hibernate `ddl-auto=create-drop` | Run Postgres, drop the `mock` profile; Flyway applies migrations |
| B2 | **Row-Level Security (tenant isolation)** | Postgres RLS policies + `app_user` (no `BYPASSRLS`) | **Not enforced** — H2 has no RLS; `TenantConnectionPreparer.set_config` is a no-op effect | Use Postgres + the restricted DB role (see `multi-tenancy.md`) |
| B3 | **Messaging** | Kafka via `KafkaEventPublisher` | `LoggingEventPublisher` logs `[MOCK-KAFKA]` instead of producing | Run Kafka, drop `mock` (Kafka autoconfig is excluded in mock) |
| B4 | **Audit trail consumer** | `AuditEventListener` consumes all `*.events.*` → hash-chained `audit_record` | **Disabled** (no Kafka). Outbox events are published (logged) but **not consumed**, so no audit rows are written | Enable Kafka; the listener is `@Profile("!mock")` and activates automatically |
| B5 | **AuthN / JWT** | OAuth2 Resource Server validating Keycloak JWTs (`SecurityConfig`) | `MockSecurityConfig` permits **all** requests; OAuth2 autoconfig excluded | Configure Keycloak issuer-uri, drop `mock` |
| B6 | **Current user** | `JwtCurrentUserProvider` (reads `sub`/`agency_id`/`roles` from JWT) | `MockCurrentUserProvider` → fixed dev user `1111…` / agency `2222…` | Provided automatically once JWT auth is on |
| B7 | **Tenant resolution** | `TenantContextFilter` (from `agency_id` claim) | `MockTenantFilter` → tenant from `X-Tenant-Id` header, else dev agency `2222…` | Same as B5/B6 |
| B8 | **Requisition number sequence** | Flyway-created `procurement.requisition_number_seq` | Recreated for H2 in `bootstrap/src/main/resources/mock/mock-schema.sql` | Provided by Flyway in prod |
| B9 | **Redis cache** | Intended for caching/sessions | **Not wired** (no `spring-boot-starter-data-redis` dependency); `application.yml` has config but it's inert | Add the starter + cache beans |
| B10 | **S3 object storage** | Document/attachment storage | **Not implemented at all** | Add an S3 adapter in the `integration` context |

### Backend stubs that are NOT profile-gated (incomplete, not "mocked")
| # | Item | State | Where |
|---|------|-------|-------|
| B11 | Workflow guards `withinBudget` / `isAuthorizedApprover` | Read context attributes with permissive defaults (no real budget/authority check) | `ProcurementWorkflowConfig` |
| B12 | Workflow actions `notifyApprovers` / `notifyRequester` | Log only (no notification sent) | `ProcurementWorkflowConfig` |
| B13 | 8 of 10 bounded contexts (identity, agency, vendor, contract, workflow*, policy*, integration, reporting) | Not implemented; commented out in `settings.gradle.kts` (*engines exist in shared-kernel) | `backend/settings.gradle.kts` |
| B14 | Aggregate read/query side (`GET` requisitions, projections) | Not implemented (write side only) | — |

---

## Frontend mocks

| # | Concern | Real (prod) | Mocked | How to make it real |
|---|---------|-------------|--------|---------------------|
| F1 | **API calls** | `apiClient` → Spring API with bearer token | When `NEXT_PUBLIC_USE_MOCK=true`, requests resolve against `src/lib/mocks.ts` handlers (create returns a random UUID) | Set `NEXT_PUBLIC_USE_MOCK=false` + `NEXT_PUBLIC_API_BASE_URL` |
| F2 | **Authentication** | Keycloak (Authorization Code + PKCE), e.g. via next-auth | `src/lib/auth.ts` returns a fixed dev user; `getAccessToken()` returns `null` | Implement the two functions in `auth.ts` only — no feature code changes |
| F3 | **Requisition detail page** | Fetch requisition via a typed query hook | `src/app/requisitions/[id]/page.tsx` shows a static placeholder | Add a `GET` query hook once B14 exists |
| F4 | **Styling** | Design system / Tailwind | Plain CSS in `globals.css` | Adopt the chosen UI kit |
| F5 | **Tests** | Vitest + Playwright | None yet | Add unit + e2e tests |

---

## Known correctness notes (fixed while building the runnable model)
- **Outbox ordering bug (fixed):** the repository adapter used to clear domain events before the
  service drained them to the outbox, so no events were ever published. Event lifecycle now lives in
  the application service; the adapter only persists state.
- **Policy default-deny (fixed):** `PolicyEngine.enforce` now rejects `NOT_APPLICABLE` (no matching
  permit), not just explicit `DENY` — fail-closed posture appropriate for government.
