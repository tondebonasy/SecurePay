import { createTheme, alpha } from "@mui/material/styles";
export const makeTheme = (mode: "light" | "dark") => {
  const dark = mode === "dark",
    border = dark ? "#2F4058" : "#E1DACB";
  return createTheme({
    palette: {
      mode,
      primary: { main: dark ? "#D7B66A" : "#13233B" },
      secondary: { main: "#B8924F" },
      background: {
        default: dark ? "#0E1A2D" : "#F7F4EE",
        paper: dark ? "#15243B" : "#FFFFFF",
      },
      text: {
        primary: dark ? "#F3F6FA" : "#13233B",
        secondary: dark ? "#B7C2D1" : "#3D4C61",
      },
      divider: border,
      success: { main: "#1F7A4D" },
      warning: { main: "#B7791F" },
      error: { main: "#B42318" },
      info: { main: "#1D5FA8" },
    },
    shape: { borderRadius: 14 },
    typography: {
      fontFamily: '"Manrope", "Segoe UI", sans-serif',
      fontSize: 13,
      allVariants: { fontVariantNumeric: "tabular-nums" },
      h4: {
        fontSize: "1.85rem",
        fontWeight: 750,
        letterSpacing: "-.045em",
        lineHeight: 1.25,
      },
      h5: { fontSize: "1.35rem", fontWeight: 700, letterSpacing: "-.025em" },
      h6: { fontSize: "1rem", fontWeight: 700, letterSpacing: "-.02em" },
      body1: { lineHeight: 1.7 },
      body2: { lineHeight: 1.65 },
      button: { textTransform: "none", fontWeight: 650, letterSpacing: 0 },
      overline: { fontSize: 10, fontWeight: 700, letterSpacing: ".14em" },
    },
    components: {
      MuiCard: {
        styleOverrides: {
          root: {
            border: `1px solid ${border}`,
            boxShadow: dark
              ? "none"
              : "0 2px 3px rgba(23,36,59,.015), 0 8px 24px rgba(23,36,59,.025)",
            backgroundImage: "none",
            overflow: "hidden",
          },
        },
      },
      MuiCardContent: {
        styleOverrides: {
          root: {
            padding: 24,
            "&:last-child": { paddingBottom: 24 },
            "@media(max-width:600px)": {
              padding: 18,
              "&:last-child": { paddingBottom: 18 },
            },
          },
        },
      },
      MuiButton: {
        defaultProps: { disableElevation: true },
        styleOverrides: {
          root: {
            borderRadius: 9,
            minHeight: 40,
            padding: "9px 16px",
            transition:
              "background-color .18s, box-shadow .18s, transform .18s",
          },
          containedPrimary: {
            boxShadow: dark ? "none" : "0 3px 7px #2459E020",
            "&:hover": {
              boxShadow: "0 5px 12px #2459E030",
              transform: "translateY(-1px)",
            },
          },
          outlined: { borderColor: border },
        },
      },
      MuiOutlinedInput: {
        styleOverrides: {
          root: {
            borderRadius: 9,
            backgroundColor: dark ? "#101B2C" : "#FFFFFF",
            fontSize: 13,
            "& fieldset": { borderColor: border },
            "&:hover fieldset": { borderColor: dark ? "#597294" : "#B5C2D5" },
            "&.Mui-focused": { boxShadow: "0 0 0 3px #3B82F612" },
          },
        },
      },
      MuiInputLabel: { styleOverrides: { root: { fontSize: 13 } } },
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: 7,
            fontWeight: 600,
            fontSize: 11.5,
            height: 27,
          },
          outlined: { borderColor: border },
        },
      },
      MuiTableHead: {
        styleOverrides: {
          root: { backgroundColor: dark ? "#101A29" : "#F8FAFD" },
        },
      },
      MuiTableCell: {
        styleOverrides: {
          root: { borderColor: border, padding: "16px 20px", fontSize: 13 },
          head: {
            fontSize: 11,
            fontWeight: 700,
            letterSpacing: ".06em",
            textTransform: "uppercase",
            color: dark ? "#A0AEC3" : "#77869B",
            whiteSpace: "nowrap",
          },
        },
      },
      MuiTableRow: {
        styleOverrides: {
          root: {
            "&.MuiTableRow-hover:hover": {
              backgroundColor: alpha("#3B82F6", 0.035),
            },
          },
        },
      },
      MuiDialog: {
        styleOverrides: {
          paper: {
            borderRadius: 18,
            backgroundImage: "none",
            boxShadow: "0 32px 100px #00000030",
            border: `1px solid ${border}`,
          },
        },
      },
      MuiDialogTitle: {
        styleOverrides: {
          root: {
            fontSize: 19,
            fontWeight: 700,
            padding: "24px 28px",
            borderBottom: `1px solid ${border}`,
          },
        },
      },
      MuiDialogContent: {
        styleOverrides: { root: { padding: "24px 28px !important" } },
      },
      MuiDialogActions: {
        styleOverrides: {
          root: { padding: "16px 24px", borderTop: `1px solid ${border}` },
        },
      },
      MuiAlert: {
        styleOverrides: {
          root: { borderRadius: 10, fontSize: 12.5 },
          standardInfo: { backgroundColor: dark ? "#152C40" : "#F0F7FF" },
        },
      },
      MuiSkeleton: { styleOverrides: { root: { transform: "none" } } },
      MuiTabs: {
        styleOverrides: { indicator: { height: 3, borderRadius: 3 } },
      },
      MuiTab: {
        styleOverrides: {
          root: { textTransform: "none", fontWeight: 600, minHeight: 48 },
        },
      },
      MuiCssBaseline: {
        styleOverrides: {
          body: { WebkitFontSmoothing: "antialiased" },
          "::selection": { background: "#2563EB25" },
          "*:focus-visible": { outline: "3px solid #06B6D4", outlineOffset: 3 },
          "@media(prefers-reduced-motion:reduce)": {
            "*,*::before,*::after": {
              animation: "none !important",
              transition: "none !important",
            },
          },
          "@media print": {
            "nav,header,button": { display: "none !important" },
            main: { margin: 0, padding: 0 },
          },
        },
      },
    },
  });
};
export default makeTheme("light");
