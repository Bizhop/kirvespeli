import { createTheme } from "@mui/material"
import "@fontsource/abel/latin-400.css"
import "@fontsource/alegreya-sans-sc/latin-700.css"

export default createTheme({
  palette: {
    mode: "dark",
    primary: { main: "#d9e6a5", contrastText: "#263608" },
    secondary: { main: "#fff3bd" },
    background: { default: "#2d4a0b", paper: "#465c0b" },
    text: { primary: "#ffffff", secondary: "#e0e8cd" },
  },
  typography: {
    fontFamily: '"Abel", sans-serif',
    ...Object.fromEntries(
      ["h1", "h2", "h3", "h4", "h5", "h6"].map(h => [
        h,
        { fontFamily: '"Alegreya Sans SC", sans-serif', fontWeight: 700 },
      ]),
    ),
    button: { textTransform: "none", fontSize: "1rem" },
  },
  shape: { borderRadius: 12 },
  components: {
    MuiPaper: {
      styleOverrides: { root: { backgroundImage: "none", border: "1px solid #ffffff66" } },
    },
    MuiTableCell: { styleOverrides: { stickyHeader: { backgroundColor: "#42550d" } } },
    MuiButton: { defaultProps: { disableElevation: true } },
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          background: "radial-gradient(ellipse at 50% 30%, #6a8314, #2d4a0b)",
          backgroundAttachment: "fixed",
        },
      },
    },
  },
})
