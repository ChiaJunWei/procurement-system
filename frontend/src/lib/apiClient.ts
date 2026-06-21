// The single HTTP boundary for the whole app. Feature code calls typed functions in
// features/*/api which delegate here — components never call fetch directly (coding-standards.md).
//
// Auth: injects the bearer token from lib/auth (a stub today; swap in Keycloak/next-auth later
// without touching any feature code).
//
// Mock mode: when NEXT_PUBLIC_USE_MOCK === "true", requests resolve against the registered mock
// handlers in lib/mocks instead of hitting the backend, so the UI builds and runs standalone.
import { getAccessToken } from "./auth";
import { resolveMock } from "./mocks";

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";
const USE_MOCK = process.env.NEXT_PUBLIC_USE_MOCK === "true";

export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string | undefined,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  if (USE_MOCK) {
    return resolveMock<T>(method, path, body);
  }

  const token = await getAccessToken();
  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
    cache: "no-store",
  });

  if (!res.ok) {
    // Backend returns RFC-7807 ProblemDetail; surface its code/detail when present.
    const problem = await res.json().catch(() => ({}) as Record<string, unknown>);
    throw new ApiError(
      res.status,
      typeof problem.code === "string" ? problem.code : undefined,
      typeof problem.detail === "string" ? problem.detail : res.statusText,
    );
  }

  return (res.status === 204 ? undefined : await res.json()) as T;
}

export const apiClient = {
  get: <T>(path: string) => request<T>("GET", path),
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, body),
  put: <T>(path: string, body?: unknown) => request<T>("PUT", path, body),
  delete: <T>(path: string) => request<T>("DELETE", path),
};
