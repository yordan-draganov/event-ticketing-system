import { ThemeProvider, CssBaseline } from "@mui/material";
import HomePage from "./pages/HomePage/Home.tsx";
// import Payment from "./Payment.tsx"
import theme from "./theme.ts";

function App() {
  // const eventId = "eventID"
  // const seatIds = ["seatID"]
  // const totalPrice = 0

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <HomePage />
      {/* <Payment
        eventId={eventId}
        seatIds={seatIds}
        totalPrice={totalPrice}
      /> */}
    </ThemeProvider>
  );
}

export default App;
