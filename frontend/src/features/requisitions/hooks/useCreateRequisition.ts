"use client";
// Mutation hook encapsulating the create-requisition use case. Keeps server-state concerns
// (loading, error, cache invalidation) out of components.
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createRequisition } from "../api/requisitionApi";
import type { CreateRequisitionInput } from "../types/requisition";

export function useCreateRequisition() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateRequisitionInput) => createRequisition(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["requisitions"] });
    },
  });
}
