import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout/Layout';
import { Home } from './pages/Home/Home';
import { EventDetails } from './pages/EventDetails/EventDetails';
import { MyTickets } from './pages/MyTickets/MyTickets';
import { Checkout } from './pages/Checkout/Checkout';
import { ErrorBoundary } from './ErrorBoundary';
import { AdminDashboard } from './pages/AdminDashboard/AdminDashboard';

function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Home />} />
            <Route path="events/:eventId" element={<EventDetails />} />
            <Route path="my-tickets" element={<MyTickets />} />
            <Route path="checkout" element={<Checkout />} />
            <Route path="admin/dashboard" element={<AdminDashboard />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ErrorBoundary>
  );
}

export default App;