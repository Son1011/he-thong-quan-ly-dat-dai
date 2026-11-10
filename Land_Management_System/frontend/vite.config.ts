import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          react: ["react", "react-dom", "react-router-dom"],
          mui: ["@mui/material", "@mui/icons-material", "@emotion/react", "@emotion/styled"],
          charts: ["chart.js", "react-chartjs-2"],
          html2canvas: ["html2canvas"],
          jspdf: ["jspdf"],
        },
      },
    },
  },
  server: {
    proxy: {
      "/api": {
        // Local dev: backend hiện đang chạy trên localhost:8080 với context path /api.
        target: "http://127.0.0.1:8080",
        changeOrigin: true,
        // Giữ nguyên /api khi chuyển đến backend Spring Boot.
      },
    },
  },
});
