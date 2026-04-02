import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { ApiClient } from '../../services/api.client';
import type { EventResponse, SeatResponse } from '../../generated/api';

interface CheckoutState {
  event: EventResponse;
  selectedSeats: SeatResponse[];
  totalPrice: number;
}

interface PaymentFormProps {
  checkoutData: CheckoutState;
  onSuccess: () => void;
}

const extractErrorMessage = (error: unknown, fallback: string): string => {
  if (!error) return fallback;

  const anyError = error as any;

  if (anyError.response) {
    const data = anyError.response.data || anyError.response;
    if (typeof data === 'string') {
      return data;
    }
    if (data?.message) {
      return data.message;
    }
    if (data?.error) {
      return data.error;
    }
  }

  if (anyError.message) {
    return anyError.message as string;
  }

  return fallback;
};

export const PaymentForm: React.FC<PaymentFormProps> = ({ checkoutData, onSuccess }) => {
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();

  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements) return;

    let userName: string | null = null;
    try {
      userName = localStorage.getItem('userName');
    } catch (storageError) {
      console.error('Failed to read authentication state before payment:', storageError);
      setError("We couldn't access your session. Please sign in again.");
      setLoading(false);
      return;
    }

    if (!userName) {
      setError("Please log in to complete your purchase.");
      setTimeout(() => navigate('/'), 2000);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const paymentResponse = await ApiClient.createPaymentIntent({
        eventId: checkoutData.event.id || '',
        seatIds: checkoutData.selectedSeats.map((seat) => seat.id || '').filter((id) => id !== ''),
      });

      if (!paymentResponse.clientSecret) {
        throw new Error("Failed to initialize payment. Please try again.");
      }

      const cardElement = elements.getElement(CardElement);
      if (!cardElement) throw new Error("Card element not found");

      const { error: stripeError, paymentIntent } = await stripe.confirmCardPayment(
        paymentResponse.clientSecret,
        {
          payment_method: {
            card: cardElement,
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
<<<<<<< fix/ui
          console.error('Error details:', {
            message: confirmErr.message,
            response: confirmErr.response,
            stack: confirmErr.stack,
          });

          let errorMessage = "Payment was successful, but there was an issue creating your tickets.";

          if (confirmErr.response) {
            const errorData = confirmErr.response.data || confirmErr.response;
            if (errorData.message) {
              errorMessage = errorData.message;
            } else if (errorData.error) {
              errorMessage = errorData.error;
            }
          } else if (confirmErr.message) {
            errorMessage = confirmErr.message;
          }

          errorMessage += ` Please contact support with payment ID: ${paymentIntent.id}`;
=======
          const baseMessage = extractErrorMessage(
            confirmErr,
            'Payment was successful, but there was an issue creating your tickets.'
          );

          const errorMessage = `${baseMessage} Please contact support with payment ID: ${paymentIntent.id}`;
>>>>>>> main

          throw new Error(errorMessage);
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
    <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-xl p-6 sm:p-8 lg:p-10">
      <div className="mb-6 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Payment Details</h2>
          <p className="mt-1 text-sm text-gray-600">Enter your card information to complete the purchase.</p>
        </div>
        <p className="text-lg font-bold text-blue-600">Total: ${checkoutData.totalPrice.toFixed(2)}</p>
      </div>

      <div className="mb-6">
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          Card Information
        </label>
        <div className="w-full rounded-xl border-2 border-gray-200 bg-gray-50 p-4 sm:p-5">
          <CardElement
            options={{
              style: {
                base: {
                  fontSize: '16px',
                  color: '#1f2937',
                  '::placeholder': {
                    color: '#9ca3af',
                  },
                },
                invalid: {
                  color: '#ef4444',
                },
              },
            }}
          />
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

      <p className="mt-4 px-2 text-center text-xs text-gray-500">
        Your payment is secured by Stripe. We never store your card details.
      </p>
    </form>
  );
};
