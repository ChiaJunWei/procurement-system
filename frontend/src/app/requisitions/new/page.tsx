// Route: /requisitions/new. Server Component shell that resolves the authenticated user
// (server-side session) and renders the interactive form. Keeps secrets/session on the server.
import { getCurrentUser } from "@/lib/auth";
import { CreateRequisitionForm } from "@/features/requisitions/components/CreateRequisitionForm";

export default async function NewRequisitionPage() {
  const user = await getCurrentUser();

  return (
    <main>
      <h1>New Purchase Requisition</h1>
      <CreateRequisitionForm requesterId={user.id} />
    </main>
  );
}
