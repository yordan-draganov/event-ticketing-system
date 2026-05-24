import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CardCvcElement, CardExpiryElement, CardNumberElement, useStripe, useElements } from '@stripe/react-stripe-js';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import ShieldOutlinedIcon from '@mui/icons-material/ShieldOutlined';
import { ApiClient } from '../../services/api.client';
import type { EventResponse, PaymentResponse, SeatResponse } from '../../generated/api';

interface CheckoutState {
  event: EventResponse;
  selectedSeats: SeatResponse[];
  totalPrice: number;
}

interface PaymentFormProps {
  checkoutData: CheckoutState;
  paymentSetup: PaymentResponse;
  onSuccess: () => void;
}

type ErrorPayload = {
  message?: unknown;
  error?: unknown;
};

const extractErrorMessage = (error: unknown, fallback: string): string => {
  if (!error) return fallback;

  const possibleError = error as { response?: unknown; message?: unknown };

  if (possibleError.response) {
    const response = possibleError.response as { data?: unknown };
    const data = (response.data || possibleError.response) as ErrorPayload | string;
    if (typeof data === 'string') {
      return data;
    }
    if (typeof data.message === 'string') {
      return data.message;
    }
    if (typeof data.error === 'string') {
      return data.error;
    }
  }

  if (typeof possibleError.message === 'string') {
    return possibleError.message;
  }

  return fallback;
};

const stripeFieldStyle = {
  base: {
    fontSize: '16px',
    color: '#111827',
    fontWeight: '500',
    '::placeholder': {
      color: '#9ca3af',
    },
  },
  invalid: {
    color: '#dc2626',
  },
};

export const PaymentForm: React.FC<PaymentFormProps> = ({ checkoutData, paymentSetup, onSuccess }) => {
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();

  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (loading) return;
    if (!stripe || !elements) return;

    setLoading(true);
    setError(null);

    try {
      await ApiClient.getCurrentUser();
    } catch (authError) {
      console.error('Failed to verify authentication before payment:', authError);
      setError("Please log in to complete your purchase.");
      setTimeout(() => navigate('/'), 2000);
      setLoading(false);
      return;
    }

    try {
      if (!paymentSetup.clientSecret || !paymentSetup.paymentIntentId) {
        throw new Error("Failed to initialize payment. Please try again.");
      }

      const cardNumberElement = elements.getElement(CardNumberElement);
      if (!cardNumberElement) throw new Error("Card element not found");

      const { error: stripeError, paymentIntent } = await stripe.confirmCardPayment(
        paymentSetup.clientSecret,
        {
          payment_method: {
            card: cardNumberElement,
          },
        }
      );

      if (stripeError) {
        throw new Error(stripeError.message || "Payment failed. Please try again.");
      }

      if (paymentIntent?.status === 'succeeded' && paymentIntent.id) {
        try {
          console.log('Confirming payment with backend, paymentIntentId:', paymentIntent.id);
          console.log('Event ID:', checkoutData.event.id);
          console.log('Seat IDs:', checkoutData.selectedSeats.map((seat) => seat.id).filter((id) => id));

          const confirmResponse = await ApiClient.confirmPayment(paymentIntent.id);
          console.log('Payment confirmed, ticket creation response:', confirmResponse);

          await new Promise((resolve) => setTimeout(resolve, 500));

          onSuccess();

          setTimeout(() => {
            navigate('/', { state: { paymentSuccess: true } });
          }, 3000);
        } catch (confirmErr: unknown) {
          console.error('Error confirming payment and creating tickets:', confirmErr);
          const message = extractErrorMessage(confirmErr, "Payment succeeded, but ticket creation failed. Please contact support.");
          setError(message);
          return;
        }
      } else {
        throw new Error("Payment was not completed successfully.");
      }
    } catch (err: unknown) {
      console.error('Payment error:', err);
      const message = extractErrorMessage(err, "An unexpected error occurred. Please try again.");
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-[0_24px_70px_-36px_rgba(15,23,42,0.45)]">
      <div className="border-b border-slate-100 bg-slate-50/80 px-6 py-5 sm:px-8">
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="text-2xl font-bold text-slate-950">Payment Details</h2>
            <p className="mt-1 text-sm text-slate-600">Choose a payment method and enter your card information.</p>
          </div>
          <p className="text-lg font-bold text-blue-700">Total: ${checkoutData.totalPrice.toFixed(2)}</p>
        </div>
      </div>

      <div className="p-6 sm:p-8 lg:p-10">
        <div className="mb-6">
          <p className="mb-3 text-sm font-bold text-slate-800">Payment method</p>
          <div className="grid gap-3 sm:grid-cols-2">
            <button
              type="button"
              aria-pressed="true"
              className="flex min-h-20 items-center gap-3 rounded-lg border-2 border-blue-600 bg-blue-50 px-4 py-3 text-left shadow-sm ring-4 ring-blue-100/70"
            >
              <span className="flex h-10 w-10 items-center justify-center rounded-md bg-blue-600 text-white">
                <CreditCardIcon fontSize="small" />
              </span>
              <span>
                <span className="block text-base font-extrabold text-blue-800">Card</span>
                <span className="block text-xs font-semibold text-blue-600">Visa, Mastercard, Amex</span>
              </span>
            </button>

            <div className="flex min-h-20 items-center gap-3 rounded-lg border border-slate-200 bg-white px-4 py-3 text-left shadow-sm">
              <span className="flex h-10 w-10 items-center justify-center rounded-md bg-slate-100 text-slate-500">
                <ShieldOutlinedIcon fontSize="small" />
              </span>
              <span>
                <span className="block text-base font-extrabold text-slate-700">Secure payment</span>
                <span className="block text-xs font-semibold text-slate-500">Protected by Stripe</span>
              </span>
            </div>
          </div>
        </div>

        <div className="mb-5 flex items-center gap-2 rounded-lg border border-emerald-100 bg-emerald-50 px-4 py-3 text-emerald-800">
          <LockOutlinedIcon className="text-emerald-600" fontSize="small" />
          <p className="text-sm font-bold">Secure, encrypted checkout</p>
        </div>

        <div className="mb-6 space-y-4">
          <div>
            <label className="mb-2 block text-sm font-bold text-slate-800">
              Card number
            </label>
            <div className="rounded-lg border border-slate-300 bg-white px-4 py-4 shadow-sm transition focus-within:border-blue-600 focus-within:ring-4 focus-within:ring-blue-100">
              <CardNumberElement options={{ style: stripeFieldStyle, showIcon: true }} />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-bold text-slate-800">
                Expiry date
              </label>
              <div className="rounded-lg border border-slate-300 bg-white px-4 py-4 shadow-sm transition focus-within:border-blue-600 focus-within:ring-4 focus-within:ring-blue-100">
                <CardExpiryElement options={{ style: stripeFieldStyle }} />
              </div>
            </div>

            <div>
              <label className="mb-2 block text-sm font-bold text-slate-800">
                Security code
              </label>
              <div className="rounded-lg border border-slate-300 bg-white px-4 py-4 shadow-sm transition focus-within:border-blue-600 focus-within:ring-4 focus-within:ring-blue-100">
                <CardCvcElement options={{ style: stripeFieldStyle }} />
              </div>
            </div>
          </div>
        </div>

        {error && (
          <div className="mb-6 rounded-xl border border-red-200 bg-red-50 p-4">
            <div className="flex items-start gap-2">
              <svg className="mt-0.5 h-5 w-5 flex-shrink-0 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-sm font-medium text-red-700">{error}</p>
            </div>
          </div>
        )}

        <button
          type="submit"
          disabled={!stripe || loading}
          className="w-full rounded-xl bg-blue-600 py-4 text-base font-bold text-white shadow-lg transition-all hover:bg-blue-700 hover:shadow-xl disabled:cursor-not-allowed disabled:bg-gray-400 sm:text-lg"
        >
          {loading ? (
            <span className="flex items-center justify-center gap-2">
              <svg className="h-5 w-5 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Processing Payment...
            </span>
          ) : (
            `Pay $${checkoutData.totalPrice.toFixed(2)}`
          )}
        </button>

        <p className="mt-4 px-2 text-center text-xs text-slate-500">
          Your payment is secured by Stripe. We never store your card details.
        </p>
      </div>
    </form>
  );
};
