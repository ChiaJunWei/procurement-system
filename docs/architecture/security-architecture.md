# Security Architecture

Government-grade: defense in depth, least privilege, full auditability, 10-year retention.

## Authentication — Keycloak (OIDC)

- Keycloak is the single IdP. Frontend uses **Authorization Code + PKCE**; backend validates JWTs as an OAuth2 **Resource Server**.
- One Keycloak **realm** for the platform; each agency is an **organization/group** within it. The JWT carries `agency_id`, `sub` (→ `KeycloakSubject`), `roles`, and `delegations`.
- Supports federation (agency SSO, smart-card/PIV via Keycloak brokering) without app changes.

## Authorization — layered

| Layer            | Enforces                                    | Mechanism                                |
|------------------|---------------------------------------------|------------------------------------------|
| Edge / gateway   | Coarse route auth, rate limiting            | Spring Security + gateway                 |
| Method           | Role/scope required for an endpoint         | `@PreAuthorize("hasAuthority('procurement:requisition:create')")` |
| Domain (ABAC)    | Fine-grained, attribute/obligation rules    | **Policy engine** (see policy-engine.md) |
| Data             | Tenant row isolation                        | **PostgreSQL RLS** (see multi-tenancy.md)|

Permissions are named `<context>:<resource>:<action>` and managed as roles in Keycloak, mapped to authorities.

## Data protection

- **In transit:** TLS everywhere; mTLS between pods (service mesh).
- **At rest:** PostgreSQL TDE / encrypted EBS; S3 SSE-KMS; Redis encryption.
- **Field-level:** PII/sensitive fields (TaxId, bank details) encrypted via a `@Encrypted` JPA converter backed by KMS-managed keys with rotation.
- **Secrets:** never in code/config; Kubernetes Secrets sourced from a vault (e.g. Vault/KMS).

## Audit & non-repudiation

- The Audit context consumes **every** domain event → append-only, hash-chained `AuditRecord` (tamper-evident).
- Security-relevant events (login, permission change, data export, policy override) are first-class.
- Retention: 10+ years, WORM storage tier for archived audit.

## Application hardening

- Input validation at the API edge (Bean Validation on DTOs); output encoding in the frontend.
- Parameterized queries only (JPA/criteria); no string-built SQL.
- CSRF protection for browser flows; strict CORS allowlist; security headers (CSP, HSTS).
- Dependency scanning (OWASP), SAST/DAST in CI, image scanning before deploy.
- Rate limiting & anomaly detection on auth and export endpoints.

## Principle of least privilege

- App DB role: no `BYPASSRLS`, no DDL at runtime.
- Per-module Kafka ACLs (a module may only produce to its own topics).
- S3 bucket policies scoped by tenant prefix.
