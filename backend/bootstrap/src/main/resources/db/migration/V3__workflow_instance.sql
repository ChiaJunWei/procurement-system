-- ============================================================================
-- V3: Workflow engine runtime state. One row per aggregate instance moving through
-- a definition. Tenant-scoped with RLS like every business table.
-- ============================================================================

CREATE TABLE workflow.workflow_instance (
    id                 UUID PRIMARY KEY,
    tenant_id          UUID        NOT NULL,
    definition_key     TEXT        NOT NULL,
    definition_version INT         NOT NULL,
    aggregate_id       UUID        NOT NULL,
    current_state      TEXT        NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    lock_version       BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT uq_workflow_instance_aggregate UNIQUE (aggregate_id)
);
CREATE INDEX idx_workflow_instance_tenant ON workflow.workflow_instance (tenant_id, definition_key);

ALTER TABLE workflow.workflow_instance ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON workflow.workflow_instance
    USING (tenant_id = current_setting('app.current_tenant', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::uuid);
