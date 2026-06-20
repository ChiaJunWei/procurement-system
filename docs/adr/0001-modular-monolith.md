# ADR-0001: Modular Monolith over Microservices

- **Status:** Accepted
- **Date:** 2026-06-20
- **Deciders:** Principal Architecture

## Context

20+ agencies, 50k users, 5M tx/year, 10+ year lifetime. The team will grow and a large share of future development will be done by autonomous coding agents that must extend the system **without coordinating distributed deployments**. We need strong consistency for financial/procurement transactions and low operational overhead early, but the freedom to scale and extract services later.

## Decision

Build a **modular monolith**: a single deployable Spring Boot application composed of independently-compiled Gradle modules, one per bounded context. Boundaries are enforced *in code* (package structure + ArchUnit + per-schema data ownership) so each module is a pre-cut "service" awaiting extraction.

## Consequences

**Positive**
- Single transaction boundary available within a context; no distributed-transaction complexity for the common case.
- One build, one deploy, one debugger — agents iterate fast.
- Refactors across contexts are compiler-checked.
- Extraction path is mechanical: a module already owns its schema and talks via events/APIs.

**Negative**
- Risk of accidental coupling — mitigated by ArchUnit fitness functions failing the build.
- All modules scale together at the JVM level — mitigated by stateless app + horizontal pods; hot contexts (Reporting) get read-replica projections.

## Extraction readiness checklist (per module)

- [ ] Owns a dedicated PostgreSQL schema; no cross-schema joins or FKs.
- [ ] Publishes/consumes only via Kafka events or `api`-package interfaces.
- [ ] No compile-time dependency on another module's `domain`/`application`/`infrastructure`.
