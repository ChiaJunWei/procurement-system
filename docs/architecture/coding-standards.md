# Coding Standards (read before writing code)

These rules exist so that **hundreds of features added by different agents over 10 years stay consistent**. They are enforced by ArchUnit + Checkstyle + CI where possible.

## Architectural rules (ArchUnit-enforced)

1. **Dependency direction:** `api → application → domain`; `infrastructure → {domain, application}`. `domain` depends only on `shared-kernel/domain`. No cycles.
2. **No cross-module internals:** `gov.procure.<X>` may not import `gov.procure.<Y>.{domain,application,infrastructure}`. Cross-context calls go through `<Y>.api` interfaces or Kafka events only.
3. **No cross-schema SQL:** a module's repositories touch only its own schema. No FK across schemas.
4. **Aggregates reference other aggregates by ID**, never by object.
5. **Persistence types stay in `infrastructure`:** no JPA annotations leaking into `domain`. (Domain is persistence-agnostic; mapping happens in adapters.)

## Layer responsibilities

| Layer          | May contain                                              | Must NOT contain                          |
|----------------|---------------------------------------------------------|-------------------------------------------|
| `domain`       | aggregates, VOs, domain events, repo **ports**, domain services | Spring, JPA, HTTP, framework annotations |
| `application`  | use-case services, commands/queries, `@Transactional`   | controllers, JPA entities                 |
| `infrastructure`| JPA entities + mappers, repo adapters, Kafka, clients  | business rules                            |
| `api`          | controllers, request/response DTOs, cross-context interfaces | persistence, business logic           |

## Conventions

- **Commands/Queries (CQRS-lite):** writes via `*Command` → `*CommandService`; reads via `*Query` → `*QueryService`/read models. Don't return aggregates from controllers — return DTOs.
- **Identifiers:** strongly typed `record FooId(UUID value)`. No bare `UUID`/`String` IDs in signatures.
- **Money:** always `shared.domain.Money` (amount + currency). Never `double`/`float` for money.
- **Time:** `Instant` for timestamps (UTC); `LocalDate` for business dates. Inject `Clock`.
- **Null:** prefer `Optional` at boundaries; `@NonNull` by default; validate at the edge.
- **Errors:** throw domain exceptions (`DomainException` subtypes); a global `@RestControllerAdvice` maps them to RFC-7807 `ProblemDetail`. Never leak stack traces.
- **Events:** name `<Aggregate><PastTenseVerb>`; immutable records; register on the aggregate, append to outbox in the service.
- **Validation:** Bean Validation on DTOs (edge) + invariants in the aggregate (core). Both, not either.
- **Tenancy:** never write `WHERE tenant_id = ?` by hand — rely on RLS; for background work wrap in `TenantContext.runAs(...)`.

## Testing

- **Domain:** pure unit tests, no Spring.
- **Application:** slice tests with in-memory/port mocks.
- **Infrastructure/integration:** Testcontainers (PostgreSQL, Kafka, Redis).
- **Architecture:** ArchUnit fitness functions in `shared-kernel` test source run for every module.
- Every new aggregate behavior method needs a test asserting its invariant.

## Naming & style

- Packages lowercase, singular context name. Classes `PascalCase`, methods `camelCase`.
- One aggregate root per file; value objects may share a file if tightly related.
- Public API of a module = its `api` package only. Everything else is `package-private` where feasible.
- Format with Spotless (Google Java Format). Lint frontend with ESLint + Prettier + strict `tsconfig`.

## Frontend (Next.js)

- **Feature-sliced:** `src/features/<feature>/{api,components,hooks,types}`; shared UI in `src/components`.
- Server Components by default; Client Components only when interactive.
- Data access via typed clients in `features/*/api` (generated from OpenAPI). No `fetch` scattered in components.
- Forms: React Hook Form + Zod schemas mirroring backend validation.

## The Golden Rule for Agents

**Find the nearest analogous slice and copy its shape.** The canonical reference is `Create Purchase Requisition` ([reference-vertical-slice.md](reference-vertical-slice.md)). Do not introduce a new pattern when an existing one fits.
