/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // The backend is the source of truth for data; the frontend is a thin, typed client.
  // API base + mock mode are environment-driven (see src/lib/apiClient.ts).
};

export default nextConfig;
