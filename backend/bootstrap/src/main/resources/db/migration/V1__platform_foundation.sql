-- ============================================================================
-- V1: Platform foundation — schemas, roles, shared infrastructure tables.
-- One schema per bounded context (extraction-ready). RLS-based multi-tenancy.
-- ============================================================================

-- --- Bounded-context schemas (data ownership boundary) ---------------------
CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS agency;
CREATE SCHEMA IF NOT EXISTS procurement;
CREATE SCHEMA IF NOT EXISTS vendor;
CREATE SCHEMA IF NOT EXISTS contract;
CREATE SCHEMA IF NOT EXISTS workflow;
CREATE SCHEMA IF NOT EXISTS policy;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS integration;
CREATE SCHEMA IF NOT EXISTS reporting;

-- --- Application role: NO BYPASSRLS, so tenant isolation cannot be skipped --
-- (Created by infra/IaC in real envs; shown here for completeness.)
-- CREATE ROLE app_user LOGIN PASSWORD '***' NOBYPASSRLS;
-- CREATE ROLE reporting_role LOGIN PASSWORD '***' BYPASSRLS;

-- --- Transactional outbox (shared infra; one table, partitioned by month) ---
CREATE TABLE integration.outbox_event (
    id              UUID PRIMARY KEY,
    tenant_id       UUID        NOT NULL,
    aggregate_id    UUID        NOT NULL,
    event_type      TEXT        NOT NULL,
    event_version   INT         NOT NULL,
    topic           TEXT        NOT NULL,
    payload         JSONB       NOT NULL,
    correlation_id  UUID,
    occurred_at     TIMESTAMPTZ NOT NULL,
    dispatched_at   TIMESTAMPTZ
);
CREATE INDEX idx_outbox_undispatched ON integration.outbox_event (occurred_at)
    WHERE dispatched_at IS NULL;

-- --- Idempotent consumer dedupe -------------------------------------------
CREATE TABLE integration.processed_event (
    event_id     UUID        NOT NULL,
    consumer     TEXT        NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, consumer)
);

-- --- Immutable, hash-chained audit log ------------------------------------
CREATE TABLE audit.audit_record (
    id            UUID PRIMARY KEY,
    tenant_id     UUID        NOT NULL,
    occurred_at   TIMESTAMPTZ NOT NULL,
    actor_id      UUID,
    action        TEXT        NOT NULL,
    resource_type TEXT        NOT NULL,
    resource_id   UUID,
    event_type    TEXT        NOT NULL,
    payload       JSONB       NOT NULL,
    prev_hash     TEXT,
    record_hash   TEXT        NOT NULL
);
CREATE INDEX idx_audit_resource ON audit.audit_record (resource_type, resource_id);
CREATE INDEX idx_audit_tenant_time ON audit.audit_record (tenant_id, occurred_at);

-- Append-only enforcement: revoke UPDATE/DELETE in real envs.
-- REVOKE UPDATE, DELETE ON audit.audit_record FROM app_user;
