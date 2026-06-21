// Authentication seam. Today it is a STUB so the app runs standalone; the contract is what the rest
// of the app depends on, so wiring Keycloak (Authorization Code + PKCE via next-auth) later means
// replacing only this file — no feature code changes.
//
// Production plan: getCurrentUser() reads the server-side session; getAccessToken() returns the
// Keycloak access token to attach as a bearer in apiClient.

export interface CurrentUser {
  id: string;
  agencyId: string;
  roles: string[];
}

// Deterministic dev identity. Matches the backend's expectations (sub, agency_id, roles).
const DEV_USER: CurrentUser = {
  id: "11111111-1111-1111-1111-111111111111",
  agencyId: "22222222-2222-2222-2222-222222222222",
  roles: ["procurement:requisition:create"],
};

/** Server-side: resolve the authenticated user (stubbed). */
export async function getCurrentUser(): Promise<CurrentUser> {
  return DEV_USER;
}

/** Client/server: bearer token for API calls (stubbed; null in mock mode). */
export async function getAccessToken(): Promise<string | null> {
  return null;
}
