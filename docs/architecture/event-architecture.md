# Event Architecture

See [ADR-0003](../adr/0003-event-architecture.md). Inter-context communication is **events-first**.

## Event taxonomy

| Type           | Scope            | Transport        | Example                          |
|----------------|------------------|------------------|----------------------------------|
| Domain event   | within a context | in-process (Spring `ApplicationEventPublisher`) | aggregate notifies its own services |
| Integration event | across contexts | Kafka (via outbox) | `PurchaseRequisitionCreated`     |

A domain event becomes an integration event when it must cross a boundary; the use-case service writes it to the outbox.

## Transactional Outbox flow

```
@Transactional
service.handle(command):
   aggregate.mutate()                       # registers domain event(s)
   repository.save(aggregate)               # state → DB
   outbox.append(IntegrationEvent.from(e))  # event → outbox table (SAME tx)
# commit

OutboxRelay (separate thread, LISTEN/NOTIFY + poll):
   for row in outbox where dispatched_at is null:
       kafka.publish(topic(row), key=row.aggregateId, headers={tenant_id, event_id})
       row.dispatched_at = now()
```

## Topic & naming conventions

- Topic: `procurement.purchase-requisition.events.v1` → `<context>.<aggregate>.events.v<major>`.
- Event name: `<Aggregate><PastTenseVerb>` (`PurchaseRequisitionCreated`, `PurchaseRequisitionSubmitted`).
- Partition key: aggregate ID (preserves per-aggregate ordering).
- Headers (always): `event_id` (UUID), `event_type`, `event_version`, `tenant_id`, `occurred_at`, `correlation_id`.

## Envelope

```json
{
  "eventId": "uuid",
  "eventType": "PurchaseRequisitionCreated",
  "eventVersion": 1,
  "aggregateId": "uuid",
  "tenantId": "uuid",
  "occurredAt": "2026-06-20T10:00:00Z",
  "correlationId": "uuid",
  "payload": { /* event-specific, additive evolution only */ }
}
```

## Consumer contract

- **Idempotent**: dedupe on `event_id` via `processed_event` table (`IdempotentConsumer` helper).
- **Tenant-aware**: `TenantContext.runAs(header.tenant_id, ...)`.
- **Schema-tolerant**: ignore unknown fields; never break on additive changes. Breaking change ⇒ new `vN+1` topic + dual-publish migration window.

## Standard consumers (built-in)

- **Audit** consumes *all* `*.events.*` topics → immutable `AuditRecord`.
- **Reporting** consumes relevant topics → denormalized read models.
- **Workflow** consumes lifecycle events to advance instances where wired.

## Framework classes (`shared-kernel`)

`IntegrationEvent`, `OutboxEvent` (JPA), `OutboxAppender`, `OutboxRelay`, `EventPublisher`, `IdempotentConsumer`, `EventEnvelope`.
