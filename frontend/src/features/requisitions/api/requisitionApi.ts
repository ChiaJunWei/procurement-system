// Typed API client for the Requisitions feature. ALL network access for this feature lives here
// — components never call fetch directly (see coding-standards.md). The shared apiClient injects
// the Keycloak bearer token and the correlation id; the tenant is derived server-side from the JWT.
import { apiClient } from "@/lib/apiClient";
import type {
  CreateRequisitionInput,
  CreateRequisitionResponse,
} from "../types/requisition";

const BASE = "/api/v1/procurement/requisitions";

export async function createRequisition(
  input: CreateRequisitionInput,
): Promise<CreateRequisitionResponse> {
  return apiClient.post<CreateRequisitionResponse>(BASE, input);
}
