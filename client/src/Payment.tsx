import React, { useState } from 'react';
import { CardElement, Elements, useStripe, useElements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';

const stripePromise = loadStripe('pk_test_token');

interface PaymentProps {
  eventId: string;
  seatIds: string[]; 
  totalPrice: number;
}

interface PaymentPayload {
  eventId: string;
  seatIds: string[];
}

interface ConfirmPayload {
  paymentIntentId: string;
}

const CheckoutForm: React.FC<PaymentProps> = ({ eventId, seatIds, totalPrice }) => {
  const stripe = useStripe();
  const elements = useElements();
  
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<boolean>(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!stripe || !elements) return;

    if (!eventId || !seatIds || seatIds.length === 0) {
      setError("Missing Event ID or Selected Seats. Please go back and select seats.");
      return;
    }

    let token = localStorage.getItem('token');
    console.log(token);
  
    if (!token || token === "null") {
      setError("Authentication failed: Please log in again.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const payload: PaymentPayload = {
        eventId: eventId.toString(),
        seatIds: seatIds.map(id => id.toString())
      };

      const intentResponse = await fetch('http://localhost:8081/api/payments/create-intent', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });

      if (!intentResponse.ok) {
        const errorData = await intentResponse.json().catch(() => ({}));
        throw new Error(errorData.message || "Failed to create payment intent.");
      }

      const { clientSecret } = await intentResponse.json();

      const cardElement = elements.getElement(CardElement);
      if (!cardElement) throw new Error("Card element not found");

      const { error: stripeError, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
        payment_method: { card: cardElement }
      });

      if (stripeError) throw new Error(stripeError.message);

      if (paymentIntent?.status === 'succeeded') {
        const confirmPayload: ConfirmPayload = {
          paymentIntentId: paymentIntent.id
        };

        const confirmResponse = await fetch('http://localhost:8081/api/payments/confirm', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(confirmPayload)
        });

        if (!confirmResponse.ok) {
          throw new Error("Payment verified, but ticket generation failed.");
        }

        setSuccess(true);
      }
    } catch (err: any) {
      setError(err.message || "An unexpected error occurred.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ maxWidth: '400px', margin: '20px auto', padding: '20px', border: '1px solid #ddd', borderRadius: '8px' }}>
      <h3>Total Price: ${totalPrice}</h3>
      <div style={{ padding: '12px', border: '1px solid #ccc', borderRadius: '4px', marginBottom: '15px', backgroundColor: '#fff' }}>
        <CardElement options={{ style: { base: { fontSize: '16px' } } }} />
      </div>

      {error && <div style={{ color: 'red', marginBottom: '10px', fontSize: '14px' }}>{error}</div>}
      
      {success ? (
        <div style={{ color: 'green', textAlign: 'center' }}>✓ Payment Successful! Check your email.</div>
      ) : (
        <button 
          disabled={!stripe || loading} 
          style={{ width: '100%', padding: '10px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
        >
          {loading ? 'Processing...' : `Pay $${totalPrice}`}
        </button>
      )}
    </form>
  );
};

const Payment: React.FC<PaymentProps> = (props) => {
  return (
    <Elements stripe={stripePromise}>
      <CheckoutForm {...props} />
    </Elements>
  );
};

export default Payment;