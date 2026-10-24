"use client";

import { createTheme } from "@mui/material/styles";

const theme = createTheme({
  palette: {
    primary: { main: "#00796b" },
    background: { default: "#f4f6f8" },
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: "Roboto, system-ui, -apple-system, 'Segoe UI', Arial, sans-serif",
  },
});

export default theme;
