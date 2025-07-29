import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const DOMAIN = env.DOMAIN || "got.shimmythe.dev";

  return {
    plugins: [react(), tailwindcss()],
    assetsInclude: ["**/*.wav", "**/*.mp3", "**/*.ogg"],

    server: {
      host: true, // bind 0.0.0.0 inside Docker
      allowedHosts: [DOMAIN], // allow your public host header
      hmr: {
        host: DOMAIN, // ensure HMR uses your domain
        protocol: "wss", // site is HTTPS at the edge (Caddy)
        clientPort: 443, // browser connects on 443
      },
    },

    // (optional but nice for `vite preview`)
    preview: {
      host: true,
      allowedHosts: [DOMAIN],
    },
  };
});
