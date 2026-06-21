package gov.procure.bootstrap.mock;

import java.util.UUID;

/**
 * Fixed dev identity for the mock profile. The UUIDs intentionally match the frontend's stubbed
 * dev user (frontend/src/lib/auth.ts) so end-to-end calls satisfy the procurement create policy
 * (requester == subject, tenant == agency). See MOCKS.md.
 */
final class MockIdentity {
    static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID AGENCY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private MockIdentity() {}
}
