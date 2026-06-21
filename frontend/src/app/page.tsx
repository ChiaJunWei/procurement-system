import Link from "next/link";

export default function HomePage() {
  return (
    <main>
      <h1>Government Procurement Platform</h1>
      <p>
        Reference frontend slice. The Procurement context exposes the{" "}
        <strong>Create Purchase Requisition</strong> feature.
      </p>
      <p>
        <Link href="/requisitions/new">→ New Purchase Requisition</Link>
      </p>
    </main>
  );
}
