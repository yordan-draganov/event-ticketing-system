import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout/Layout';
import { Home } from './pages/Home/Home';
import { EventDetails } from './pages/EventDetails/EventDetails';
import { MyTickets } from './pages/MyTickets/MyTickets';
import { Checkout } from './pages/Checkout/Checkout';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="events/:eventId" element={<EventDetails />} />
          <Route path="my-tickets" element={<MyTickets />} />
          <Route path="checkout" element={<Checkout />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;