# ADR-0004: First-Class Workflow & Policy Engines

- **Status:** Accepted
- **Date:** 2026-06-20

## Context

Procurement is governed by agency-specific, frequently-changing approval chains and spending rules (thresholds, delegation of authority, conflict-of-interest checks). Hard-coding these into each feature would make the hundreds of planned features unmaintainable.

## Decision

Provide two reusable framework engines in `shared-kernel`, configured by data (not code):

- **Workflow engine** — a generic, persistent state machine. Any aggregate becomes "workflowable" by declaring a `WorkflowDefinition` (states, transitions, guards, actions). Approvals, escalations, and SLAs are first-class. See [workflow-engine.md](../architecture/workflow-engine.md).
- **Policy engine** — declarative rule evaluation returning `Permit/Deny/NotApplicable` with obligations. Used for both authorization (ABAC) and business rules (spend thresholds). See [policy-engine.md](../architecture/policy-engine.md).

Both are extension points: agents add a definition or a rule, never new engine plumbing.

## Consequences

- New approval flows = new `WorkflowDefinition` rows, zero code in most cases.
- Policy changes are auditable, testable in isolation, and tenant-overridable.
- Engines are deliberately generic; misuse risk mitigated by clear contracts + reference usage in the Procurement slice.
