import Link from "next/link";

// Minimal detail route so post-create navigation resolves. A full build fetches the requisition
// via a typed query hook (features/requisitions/api) and renders status + line items.
export default async function RequisitionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return (
    <main>
      <h1>Purchase Requisition</h1>
      <p>
        Created requisition <code>{id}</code> (status: <strong>DRAFT</strong>).
      </p>
      <p>
        <Link href="/requisitions/new">Create another</Link>
      </p>
    </main>
  );
}
