import { fileURLToPath } from "node:url"
import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"

const projectDir = fileURLToPath(new URL(".", import.meta.url))

// Application: src/index.html -> dist/. Component previews use vite.preview.config.ts.
export default defineConfig({
  root: "src",
  envDir: projectDir,
  cacheDir: `${projectDir}node_modules/.vite`,
  plugins: [react()],
  server: {
    port: 1234,
    strictPort: true,
    // Deno installs font assets under the project node_modules, outside the src root.
    fs: { allow: [projectDir] },
  },
  build: {
    outDir: "../dist",
    emptyOutDir: true,
    target: "baseline-widely-available",
    // Keep every image a separate cacheable file (small SVGs would otherwise be inlined into the JS).
    assetsInlineLimit: 0,
  },
})
