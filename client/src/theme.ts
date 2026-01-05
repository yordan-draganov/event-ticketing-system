import { createTheme } from "@mui/material/styles";

const theme = createTheme({
  palette: {
    primary: {
      main: "#2563eb", 
    },
    secondary: {
      main: "#9333ea", 
    },
  },
  typography: {
    fontFamily: "Inter, system-ui, sans-serif",
  },
});

export default theme;
