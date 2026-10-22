import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
export default defineConfig({
  plugins: [react()],
  test: {
    maxWorkers: 1,
    environment: "jsdom",
    globals: true,
    setupFiles: ["./src/testSetup.ts"],
  },
});
