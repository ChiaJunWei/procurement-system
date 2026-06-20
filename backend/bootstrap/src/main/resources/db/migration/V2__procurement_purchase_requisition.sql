-- ============================================================================
-- V2: Procurement — Purchase Requisition aggregate (reference slice).
-- Demonstrates the standard table pattern: tenant_id column + RLS policies.
-- ============================================================================

CREATE TABLE procurement.purchase_requisition (
    id                     UUID PRIMARY KEY,
    tenant_id              UUID         NOT NULL,
    requisition_number     TEXT         NOT NULL,
    requester_id           UUID         NOT NULL,
    justification          TEXT         NOT NULL,
    status                 TEXT         NOT NULL,
    currency               CHAR(3)      NOT NULL,
    total_estimated_amount NUMERIC(18,2) NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_requisition_number UNIQUE (tenant_id, requisition_number)
);

CREATE TABLE procurement.requisition_line_item (
    id                   UUID PRIMARY KEY,
    requisition_id       UUID         NOT NULL REFERENCES procurement.purchase_requisition(id) ON DELETE CASCADE,
    tenant_id            UUID         NOT NULL,
    description          TEXT         NOT NULL,
    quantity             NUMERIC(18,4) NOT NULL CHECK (quantity > 0),
    unit_of_measure      TEXT,
    estimated_unit_price NUMERIC(18,2) NOT NULL CHECK (estimated_unit_price >= 0),
    budget_id            UUID         NOT NULL,
    accounting_code      TEXT         NOT NULL
);
CREATE INDEX idx_lineitem_requisition ON procurement.requisition_line_item (requisition_id);

-- Per-tenant requisition number sequence (illustrative; production scopes per tenant/year).
CREATE SEQUENCE procurement.requisition_number_seq START 1;

-- --- Row-Level Security: tenant isolation enforced by the database ----------
ALTER TABLE procurement.purchase_requisition  ENABLE ROW LEVEL SECURITY;
ALTER TABLE procurement.requisition_line_item ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON procurement.purchase_requisition
    USING (tenant_id = current_setting('app.current_tenant', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::uuid);

CREATE POLICY tenant_isolation ON procurement.requisition_line_item
    USING (tenant_id = current_setting('app.current_tenant', true)::uuid)
    WITH CHECK (tenant_id = current_setting('app.current_tenant', true)::uuid);

CREATE INDEX idx_pr_tenant_status ON procurement.purchase_requisition (tenant_id, status);
CREATE INDEX idx_pr_tenant_requester ON procurement.purchase_requisition (tenant_id, requester_id);
