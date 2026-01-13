import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import type { EventResponse, SectionResponse, SeatResponse } from '../../generated/api';
import { CheckoutHeader } from '../../components/Checkout/CheckoutHeader';
import { EventSummary } from '../../components/Checkout/EventSummary';
import { TicketDetails } from '../../components/Checkout/TicketDetails';
import { OrderSummary } from '../../components/Checkout/OrderSummary';
import { PaymentForm } from '../../components/Checkout/PaymentForm';
import { PaymentSuccess } from '../../components/Checkout/PaymentSuccess';
import { CheckoutLoading } from '../../components/Checkout/CheckoutLoading';
import { CheckoutError } from '../../components/Checkout/CheckoutError';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);

interface CheckoutState {
  event: EventResponse;
  sections: SectionResponse[];
  selectedSeats: SeatResponse[];
  selectedSection: SectionResponse;
  totalPrice: number;
}

export const Checkout: React.FC = () => {
  const location = useLocation();
  const [checkoutData, setCheckoutData] = useState<CheckoutState | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [paymentSuccess, setPaymentSuccess] = useState(false);

  useEffect(() => {
    const state = location.state as CheckoutState | null;
    
    if (!state || !state.event || !state.selectedSeats || state.selectedSeats.length === 0) {
      setError("Invalid checkout data. Please select seats first.");
      setLoading(false);
      return;
    }

    const fetchSeatDetails = async () => {
      try {
        const seatsWithDetails = await Promise.all(
          state.selectedSeats.map(async (seat) => {
            if (!seat.id) return seat;
            return seat;
          })
        );

        setCheckoutData({
          ...state,
          selectedSeats: seatsWithDetails
        });
      } catch (err) {
        console.error('Error fetching seat details:', err);
        setCheckoutData(state);
      } finally {
        setLoading(false);
      }
    };

    fetchSeatDetails();
  }, [location]);

  if (loading) {
    return <CheckoutLoading />;
  }

  if (error || !checkoutData) {
    return <CheckoutError error={error || "Invalid checkout data"} />;
  }

  const sectionName = checkoutData.selectedSection?.name || 'Unknown Section';
  const pricePerTicket = checkoutData.selectedSection?.price ? Number(checkoutData.selectedSection.price) : 0;

  return (
    <div className="min-h-screen bg-gray-50">
      <CheckoutHeader />

      <div className="max-w-6xl mx-auto px-4 py-12">
        <div className="mb-8 text-center">
          <h1 className="text-4xl font-extrabold text-gray-900 mb-2">Checkout</h1>
          <p className="text-gray-600">Review your order and complete payment</p>
        </div>

        <div className="grid lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            <EventSummary event={checkoutData.event} />
            <TicketDetails 
              sectionName={sectionName}
              pricePerTicket={pricePerTicket}
              seats={checkoutData.selectedSeats}
            />
          </div>

          <div className="lg:col-span-1">
            <div className="sticky top-24">
              <OrderSummary totalPrice={checkoutData.totalPrice} />

              {paymentSuccess ? (
                <PaymentSuccess />
              ) : (
                <Elements stripe={stripePromise}>
                  <PaymentForm 
                    checkoutData={checkoutData} 
                    onSuccess={() => setPaymentSuccess(true)}
                  />
                </Elements>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

