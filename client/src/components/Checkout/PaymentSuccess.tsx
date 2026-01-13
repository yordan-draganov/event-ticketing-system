import React from 'react';

export const PaymentSuccess: React.FC = () => {
  return (
    <div className="bg-white rounded-2xl shadow-xl p-8 md:p-12 text-center">
      <div className="mb-6">
        <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg className="w-12 h-12 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h3 className="text-2xl font-bold text-gray-900 mb-2">Payment Successful!</h3>
        <p className="text-gray-600">Your tickets have been confirmed. Check your email for details.</p>
      </div>
      <p className="text-sm text-gray-500">Redirecting to home page...</p>
    </div>
  );
};

