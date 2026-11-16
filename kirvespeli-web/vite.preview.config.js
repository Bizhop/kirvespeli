import { fileURLToPath } from "node:url"
import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"

const projectDir = fileURLToPath(new URL(".", import.meta.url))

// Development-only component gallery (src/preview). Never part of the production build.
export default defineConfig({
  root: "src/preview",
  envDir: projectDir,
  plugins: [react()],
  server: {
    port: 1235,
    strictPort: true,
    fs: { allow: [projectDir] },
  },
  build: {
    outDir: "../../dist-preview",
    emptyOutDir: true,
  },
})
