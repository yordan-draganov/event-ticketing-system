import { ThemeProvider, CssBaseline } from "@mui/material";
import Home from "./pages/Home";
import theme from "./theme.ts";

function App() {
  const handleSearch = (query: string): void => {
    console.log("Searching for:", query);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Home appName="MyApp" onSearch={handleSearch} />
    </ThemeProvider>
  );
}

export default App;
