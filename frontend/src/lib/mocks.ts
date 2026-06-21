// Typed mock API used when NEXT_PUBLIC_USE_MOCK === "true". Lets the whole UI build, render, and be
// clicked through without a running backend/Keycloak. Each handler mirrors the real endpoint's
// contract, so swapping mock mode off requires no feature-code changes.
//
// To mock a new endpoint, register a handler keyed by "METHOD /path" (path may use :params).
type MockHandler = (body: unknown) => unknown;

const handlers = new Map<string, MockHandler>();

function register(method: string, path: string, handler: MockHandler): void {
  handlers.set(`${method.toUpperCase()} ${path}`, handler);
}

// --- Procurement: create purchase requisition -------------------------------
register("POST", "/api/v1/procurement/requisitions", () => ({
  requisitionId: crypto.randomUUID(),
}));

export async function resolveMock<T>(method: string, path: string, body?: unknown): Promise<T> {
  const handler = handlers.get(`${method.toUpperCase()} ${path}`);
  if (!handler) {
    throw new Error(`No mock handler registered for ${method} ${path}`);
  }
  // Simulate network latency so loading states are exercised in dev.
  await new Promise((resolve) => setTimeout(resolve, 250));
  return handler(body) as T;
}
