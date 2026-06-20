"use client";
// Reference feature component: a Client Component because it is interactive. Validation uses the
// shared Zod schema (same rules as the backend). Submission goes through the feature hook → api
// client. This is the pattern new form features should copy.
import { useFieldArray, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import {
  createRequisitionSchema,
  type CreateRequisitionInput,
} from "../types/requisition";
import { useCreateRequisition } from "../hooks/useCreateRequisition";

const EMPTY_LINE = {
  description: "",
  quantity: 1,
  unitOfMeasure: "",
  estimatedUnitPrice: 0,
  budgetId: "",
  accountingCode: "",
};

export function CreateRequisitionForm({ requesterId }: { requesterId: string }) {
  const router = useRouter();
  const mutation = useCreateRequisition();

  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CreateRequisitionInput>({
    resolver: zodResolver(createRequisitionSchema),
    defaultValues: {
      requesterId,
      currency: "USD",
      justification: "",
      lineItems: [EMPTY_LINE],
    },
  });

  const { fields, append, remove } = useFieldArray({ control, name: "lineItems" });

  const onSubmit = handleSubmit(async (values) => {
    const { requisitionId } = await mutation.mutateAsync(values);
    router.push(`/requisitions/${requisitionId}`);
  });

  return (
    <form onSubmit={onSubmit} className="space-y-6">
      <section>
        <label htmlFor="justification">Justification</label>
        <textarea id="justification" {...register("justification")} rows={3} />
        {errors.justification && <p role="alert">{errors.justification.message}</p>}
      </section>

      <section>
        <h2>Line items</h2>
        {fields.map((field, i) => (
          <fieldset key={field.id}>
            <input placeholder="Description" {...register(`lineItems.${i}.description`)} />
            <input type="number" step="any" placeholder="Qty" {...register(`lineItems.${i}.quantity`)} />
            <input placeholder="Unit" {...register(`lineItems.${i}.unitOfMeasure`)} />
            <input type="number" step="any" placeholder="Unit price" {...register(`lineItems.${i}.estimatedUnitPrice`)} />
            <input placeholder="Budget ID" {...register(`lineItems.${i}.budgetId`)} />
            <input placeholder="Accounting code" {...register(`lineItems.${i}.accountingCode`)} />
            {fields.length > 1 && (
              <button type="button" onClick={() => remove(i)}>Remove</button>
            )}
          </fieldset>
        ))}
        <button type="button" onClick={() => append(EMPTY_LINE)}>Add line item</button>
        {errors.lineItems?.root && <p role="alert">{errors.lineItems.root.message}</p>}
      </section>

      {mutation.isError && <p role="alert">Failed to create requisition. Please retry.</p>}

      <button type="submit" disabled={isSubmitting || mutation.isPending}>
        {mutation.isPending ? "Submitting…" : "Create requisition"}
      </button>
    </form>
  );
}
