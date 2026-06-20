# Integration Architecture

The `integration` context isolates all communication with **external** systems (ERP/financials, payment gateways, e-invoicing, GSA catalogs, identity providers, email/SMS). No other context talks to the outside world directly — this keeps external volatility behind a stable boundary.

## Ports & Adapters (Hexagonal)

```
Procurement service ──(domain port)──▶ IntegrationGateway (interface, owned by integration.api)
                                              │
                                  ┌───────────┴────────────┐
                              ErpAdapter            PaymentAdapter   ... (infrastructure)
                                  │                         │
                              SAP/Oracle             Payment rails
```

- Each external system = one adapter implementing a port interface declared in `integration` (exposed via its `api` package for other contexts to depend on the **interface only**).
- Adapters translate between the platform's canonical model and the vendor's wire format (anti-corruption layer).

## Reliability patterns (built-in, reusable)

| Concern              | Mechanism                                                        |
|----------------------|-----------------------------------------------------------------|
| Outbound delivery    | `IntegrationMessage` aggregate + outbox + retry with backoff    |
| Idempotency          | Client-supplied idempotency keys; dedupe table                  |
| Resilience           | Resilience4j circuit breaker + bulkhead + timeout per adapter   |
| Async                | Kafka for fire-and-forget; request/reply via reply topics       |
| Inbound webhooks     | Signature-verified controller → normalized inbound event        |
| Poison messages      | Dead-letter topic `*.dlq` + replay tooling                      |
| Observability        | Correlation ID propagated; per-adapter metrics & traces         |

## Adding a new integration (agent recipe)

1. Define the canonical port interface in `integration/api`.
2. Implement an adapter in `integration/infrastructure` (annotate `@IntegrationAdapter`).
3. Wrap calls with the provided `ResilientClient` template (CB + retry + timeout preset).
4. Map external errors to `IntegrationException` subtypes — never leak vendor types upward.
5. Register endpoint config (URL, auth, timeouts) as tenant-scoped data, not constants.

## Canonical inbound/outbound contracts

Versioned DTOs in `integration/api`. External schema changes are absorbed in the adapter; the canonical contract changes only via the normal additive-evolution rules used for events.
