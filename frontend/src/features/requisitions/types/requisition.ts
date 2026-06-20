// Types for the Requisitions feature. Mirror the backend DTOs; in production these are
// generated from the OpenAPI spec so they never drift. The Zod schema is the single source
// of validation truth on the client and mirrors backend Bean Validation.
import { z } from "zod";

export const lineItemSchema = z.object({
  description: z.string().min(1, "Description is required"),
  quantity: z.coerce.number().positive("Quantity must be positive"),
  unitOfMeasure: z.string().optional(),
  estimatedUnitPrice: z.coerce.number().min(0, "Price cannot be negative"),
  budgetId: z.string().uuid("Select a budget"),
  accountingCode: z.string().min(1, "Accounting code is required"),
});

export const createRequisitionSchema = z.object({
  requesterId: z.string().uuid(),
  justification: z.string().min(1, "Justification is required").max(2000),
  currency: z.string().length(3),
  lineItems: z.array(lineItemSchema).min(1, "Add at least one line item"),
});

export type LineItemInput = z.infer<typeof lineItemSchema>;
export type CreateRequisitionInput = z.infer<typeof createRequisitionSchema>;

export interface CreateRequisitionResponse {
  requisitionId: string;
}
