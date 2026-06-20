# ADR-0003: Event-Driven Integration with Transactional Outbox

- **Status:** Accepted
- **Date:** 2026-06-20

## Context

Bounded contexts must stay decoupled but react to each other (e.g. Audit records every state change; Reporting builds projections; Workflow advances on domain events). Dual-writing to PostgreSQL and Kafka in one transaction is impossible without a 2PC we don't want.

## Decision

Use the **Transactional Outbox** pattern:

1. A use-case service mutates aggregate state **and** inserts an `outbox_event` row in the **same DB transaction**.
2. A relay (`OutboxRelay`, polling + `LISTEN/NOTIFY`) publishes committed outbox rows to Kafka and marks them dispatched.
3. Consumers are **idempotent** (dedupe on `event_id`) and run with at-least-once delivery.

Events are versioned, immutable facts named `<Aggregate><PastTenseVerb>` (e.g. `PurchaseRequisitionCreated`). Schema is governed by a registry; new fields are additive (backward compatible). See [event-architecture.md](../architecture/event-architecture.md).

## Consequences

- Atomicity between state and event without distributed transactions.
- At-least-once → consumers MUST be idempotent (enforced by `IdempotentConsumer` helper).
- Audit and Reporting become pure event consumers — no synchronous coupling.
