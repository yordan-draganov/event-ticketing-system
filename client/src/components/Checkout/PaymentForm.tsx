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

export const PaymentForm: React.FC<PaymentFormProps> = ({ checkoutData, onSuccess }) => {
  const stripe = useStripe();
  const elements = useElements();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements) return;

    const token = localStorage.getItem('token');
    if (!token || token === "null") {
      setError("Please log in to complete your purchase.");
      setTimeout(() => navigate('/'), 2000);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const paymentResponse = await ApiClient.createPaymentIntent({
        eventId: checkoutData.event.id || '',
        seatIds: checkoutData.selectedSeats.map(seat => seat.id || '').filter(id => id !== '')
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
          }
        }
      );

      if (stripeError) {
        throw new Error(stripeError.message || "Payment failed. Please try again.");
      }

      if (paymentIntent?.status === 'succeeded' && paymentIntent.id) {
        try {
          console.log('Confirming payment with backend, paymentIntentId:', paymentIntent.id);
          console.log('Event ID:', checkoutData.event.id);
          console.log('Seat IDs:', checkoutData.selectedSeats.map(seat => seat.id).filter(id => id));
          
          const confirmResponse = await ApiClient.confirmPayment(paymentIntent.id);
          console.log('Payment confirmed, ticket creation response:', confirmResponse);
          
          await new Promise(resolve => setTimeout(resolve, 500));
          
          onSuccess();
          
          setTimeout(() => {
            navigate('/', { state: { paymentSuccess: true } });
          }, 3000);
        } catch (confirmErr: any) {
          console.error('Error confirming payment and creating tickets:', confirmErr);
          console.error('Error details:', {
            message: confirmErr.message,
            response: confirmErr.response,
            stack: confirmErr.stack
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
          
          throw new Error(errorMessage);
        }
      } else {
        throw new Error("Payment was not completed successfully.");
      }
    } catch (err: any) {
      console.error('Payment error:', err);
      setError(err.message || "An unexpected error occurred. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-xl p-4 sm:p-6 md:p-8 lg:p-12">
      <h2 className="text-xl sm:text-2xl font-bold text-gray-900 mb-4 sm:mb-6">Payment Details</h2>
      
      <div className="mb-4 sm:mb-6">
        <label className="block text-sm font-semibold text-gray-700 mb-2">
          Card Information
        </label>
        <div className="p-3 sm:p-4 border-2 border-gray-200 rounded-xl bg-gray-50">
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
        <div className="mb-4 sm:mb-6 p-3 sm:p-4 bg-red-50 border border-red-200 rounded-xl">
          <div className="flex items-start gap-2">
            <svg className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p className="text-sm text-red-700 font-medium">{error}</p>
          </div>
        </div>
      )}

      <button
        type="submit"
        disabled={!stripe || loading}
        className="w-full py-3 sm:py-4 bg-blue-600 text-white rounded-xl font-bold text-base sm:text-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-all shadow-lg hover:shadow-xl active:scale-[0.98]"
      >
        {loading ? (
          <span className="flex items-center justify-center gap-2">
            <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Processing Payment...
          </span>
        ) : (
          `Pay $${checkoutData.totalPrice.toFixed(2)}`
        )}
      </button>

      <p className="mt-3 sm:mt-4 text-xs text-gray-500 text-center px-2">
        Your payment is secured by Stripe. We never store your card details.
      </p>
    </form>
  );
};