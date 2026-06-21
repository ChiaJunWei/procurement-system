-- Mock-profile schema init (H2). Runs AFTER Hibernate generates tables/schemas.
-- The per-tenant requisition number sequence is allocated via raw JDBC in prod (Flyway-created);
-- recreate it here so the create-requisition flow works on H2.
CREATE SEQUENCE IF NOT EXISTS procurement.requisition_number_seq START WITH 1;
