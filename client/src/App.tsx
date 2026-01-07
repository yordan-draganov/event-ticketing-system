import { ThemeProvider, CssBaseline } from "@mui/material";
import Home from "./pages/Home";
import Payment from "./Payment.tsx"
import theme from "./theme.ts";

function App() {
  const handleSearch = (query: string): void => {
    console.log("Searching for:", query);
  };

  const eventId = "eventID"
  const seatIds = ["seatID"]
  const totalPrice = 0

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Home appName="MyApp" onSearch={handleSearch} />
      <Payment
        eventId={eventId}
        seatIds={seatIds}
        totalPrice={totalPrice}
      />
    </ThemeProvider>
  );
}

export default App;
