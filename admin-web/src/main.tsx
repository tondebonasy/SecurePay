import "./framework.css";
import React, { useMemo, useState, useEffect } from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { ThemeProvider, CssBaseline } from "@mui/material";
import { makeTheme } from "./theme";
import App from "./ui/App";
import { ThemeMode } from "./themeMode";
function Root() {
  const [mode, setMode] = useState<"light" | "dark">(
    localStorage.getItem("securepay-theme") === "dark" ? "dark" : "light",
  );
  useEffect(() => {
    document.documentElement.dataset.theme = mode;
  }, [mode]);
  const theme = useMemo(() => makeTheme(mode), [mode]);
  return (
    <ThemeMode.Provider
      value={{
        mode,
        toggle: () =>
          setMode((m) => {
            const next = m === "light" ? "dark" : "light";
            localStorage.setItem("securepay-theme", next);
            return next;
          }),
      }}
    >
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </ThemeProvider>
    </ThemeMode.Provider>
  );
}
ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <Root />
  </React.StrictMode>,
);
