// Base URL for API calls
// Look for environment variable first, fall back to localhost if not provided
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

// Helper function to create full API URLs
export const getApiUrl = (endpoint: string) => {
  // Make sure endpoint starts with a slash
  const formattedEndpoint = endpoint.startsWith("/")
    ? endpoint
    : `/${endpoint}`;
  return `${API_BASE_URL}${formattedEndpoint}`;
};
