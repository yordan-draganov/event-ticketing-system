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
    <div className="min-h-screen bg-slate-50">
      <CheckoutHeader />

      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8 lg:py-14">
        <div className="mb-10 rounded-[2rem] border border-slate-200/80 bg-gradient-to-br from-white via-slate-50 to-blue-50 px-6 py-8 shadow-[0_24px_60px_-32px_rgba(15,23,42,0.35)] sm:px-8 lg:px-10">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-2xl">
              <span className="inline-flex items-center rounded-full border border-blue-100 bg-blue-50 px-3 py-1 text-sm font-semibold text-blue-700">
                Secure checkout
              </span>
              <h1 className="mt-4 text-4xl font-extrabold tracking-tight text-slate-900 sm:text-5xl">
                Complete your booking
              </h1>
              <p className="mt-3 text-base text-slate-600 sm:text-lg">
                Review your seats, confirm the order details, and finish payment in one place.
              </p>
            </div>

            <div className="grid gap-3 sm:grid-cols-3">
              <div className="rounded-2xl border border-slate-200 bg-white/80 px-4 py-3 shadow-sm backdrop-blur">
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Tickets</p>
                <p className="mt-1 text-2xl font-bold text-slate-900">{checkoutData.selectedSeats.length}</p>
              </div>
              <div className="rounded-2xl border border-slate-200 bg-white/80 px-4 py-3 shadow-sm backdrop-blur">
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Section</p>
                <p className="mt-1 text-lg font-bold text-slate-900">{sectionName}</p>
              </div>
              <div className="rounded-2xl border border-slate-200 bg-white/80 px-4 py-3 shadow-sm backdrop-blur">
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">Total</p>
                <p className="mt-1 text-2xl font-bold text-blue-700">${checkoutData.totalPrice.toFixed(2)}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid gap-8 lg:grid-cols-12 lg:items-start">
          <div className="space-y-6 lg:col-span-7">
            <EventSummary event={checkoutData.event} />
            <TicketDetails 
              sectionName={sectionName}
              pricePerTicket={pricePerTicket}
              seats={checkoutData.selectedSeats}
            />
          </div>

          <div className="lg:col-span-5">
            <div className="sticky top-24">
              <OrderSummary totalPrice={checkoutData.totalPrice} />
            </div>
          </div>

          <div className="lg:col-span-12">
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
  );
};
