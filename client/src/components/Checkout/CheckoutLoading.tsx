import React from 'react';

export const CheckoutLoading: React.FC = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
        <p className="text-xl text-gray-500">Loading checkout...</p>
      </div>
    </div>
  );
};

