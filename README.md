# Government Procurement Platform

A **modular monolith** built for 20+ agencies, 50,000+ users, and 5M+ transactions/year over a 10+ year lifetime.

The architecture is optimized for **safe, incremental extension by autonomous coding agents**. Every new feature follows the same vertical-slice pattern demonstrated by the reference implementation (`Create Purchase Requisition`). No feature should require cross-cutting refactoring.

## Why a Modular Monolith (not microservices)

We get the developer velocity, transactional consistency, and operational simplicity of a monolith, while enforcing **bounded-context boundaries in code** so that any module can be extracted into a service later with minimal blast radius. See [ADR-0001](docs/adr/0001-modular-monolith.md).

## Tech Stack

| Concern        | Technology               |
|----------------|--------------------------|
| Frontend       | Next.js + TypeScript     |
| Backend        | Java 21, Spring Boot 3   |
| Database       | PostgreSQL               |
| Messaging      | Kafka                    |
| Cache          | Redis                    |
| AuthN/AuthZ    | Keycloak (OIDC)          |
| Object Storage | S3                       |
| Orchestration  | Kubernetes               |

## Bounded Contexts

| Context            | Responsibility                                                        | Module        |
|--------------------|----------------------------------------------------------------------|---------------|
| Identity           | Users, roles, sessions, Keycloak integration                         | `identity`    |
| Agency Management  | Agencies, org units, delegation hierarchies, budgets                 | `agency`      |
| **Procurement**    | Requisitions, solicitations, bids, awards (**reference module**)      | `procurement` |
| Vendor             | Vendor registration, qualification, performance                      | `vendor`      |
| Contract           | Contract authoring, lifecycle, obligations                          | `contract`    |
| Workflow           | Generic state-machine + approval orchestration engine                | `workflow`    |
| Policy             | Declarative business-rule / authorization policy engine             | `policy`      |
| Audit              | Immutable audit log, compliance trail                                | `audit`       |
| Integration        | Outbound/inbound adapters (ERP, payment, e-invoicing)               | `integration` |
| Reporting          | Read models, analytics projections, exports                         | `reporting`   |

Each context maps 1:1 to a Gradle module and a PostgreSQL schema. Contexts communicate **only** via (a) published domain events on Kafka, or (b) explicit synchronous APIs exposed through a context's `api` package — never by reaching into another context's tables or internal classes.

## Repository Layout

```
.
├── docs/                       # ADRs + architecture references (read these first)
├── backend/
│   ├── buildSrc/               # Shared Gradle build logic / conventions
│   ├── shared-kernel/          # Cross-context primitives (NO business logic)
│   ├── modules/                # One Gradle module per bounded context
│   │   └── procurement/        # ⭐ Reference module — copy this pattern
│   └── bootstrap/              # The single deployable Spring Boot app (wires modules)
└── frontend/                   # Next.js app (feature-sliced)
```

## Module internal structure (DDD layered)

```
modules/<context>/src/main/java/gov/procure/<context>/
├── domain/          # Aggregates, entities, value objects, domain events, repository PORTS
├── application/     # Use-case services, commands, queries, transaction boundaries
├── infrastructure/  # Repository adapters (JPA), event publishers, external clients
├── api/             # REST controllers, DTOs, public cross-context interfaces
└── config/          # Spring wiring for this module
```

The **dependency rule** is strictly inward: `api → application → domain` and `infrastructure → domain`. The `domain` package depends on nothing but `shared-kernel/domain`. Enforced by ArchUnit (see `shared-kernel` tests) and [Coding Standards](docs/architecture/coding-standards.md).

## For Autonomous Agents — Start Here

1. Read [docs/architecture/coding-standards.md](docs/architecture/coding-standards.md).
2. Read the reference slice: [docs/architecture/reference-vertical-slice.md](docs/architecture/reference-vertical-slice.md).
3. To add a feature, **copy the slice pattern** in the relevant module. Do not invent new patterns.
4. Run `./gradlew check` — ArchUnit + tests enforce the boundaries for you.

## Documentation Index

- Architecture overview & contexts → this file
- [Domain model & aggregates](docs/architecture/domain-model.md)
- [Multi-tenancy strategy](docs/architecture/multi-tenancy.md)
- [Event architecture](docs/architecture/event-architecture.md)
- [Workflow engine](docs/architecture/workflow-engine.md)
- [Policy engine](docs/architecture/policy-engine.md)
- [Integration architecture](docs/architecture/integration-architecture.md)
- [Security architecture](docs/architecture/security-architecture.md)
- [Coding standards](docs/architecture/coding-standards.md)
- [Reference vertical slice](docs/architecture/reference-vertical-slice.md)
- [ADRs](docs/adr/)
# procurement-system
