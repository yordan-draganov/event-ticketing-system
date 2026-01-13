import React from 'react';
import { useNavigate } from 'react-router-dom';

interface CheckoutErrorProps {
  error: string;
}

export const CheckoutError: React.FC<CheckoutErrorProps> = ({ error }) => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center bg-white rounded-2xl shadow-xl p-8 max-w-md">
        <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
        <h3 className="text-xl font-bold text-gray-900 mb-2">Checkout Error</h3>
        <p className="text-gray-600 mb-6">{error || "Invalid checkout data"}</p>
        <button
          onClick={() => navigate('/')}
          className="px-6 py-3 bg-blue-600 text-white rounded-xl font-semibold hover:bg-blue-700 transition-colors"
        >
          Back to Events
        </button>
      </div>
    </div>
  );
};

