import React from 'react';

interface OrderSummaryProps {
  totalPrice: number;
}

export const OrderSummary: React.FC<OrderSummaryProps> = ({ totalPrice }) => {
  return (
    <div className="bg-white rounded-2xl shadow-xl p-6 mb-6">
      <h3 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h3>
      <div className="space-y-3 mb-4">
        <div className="flex justify-between text-gray-600">
          <span>Subtotal</span>
          <span>${totalPrice.toFixed(2)}</span>
        </div>
        <div className="flex justify-between text-gray-600">
          <span>Fees</span>
          <span>$0.00</span>
        </div>
        <div className="border-t border-gray-200 pt-3 flex justify-between">
          <span className="text-lg font-bold text-gray-900">Total</span>
          <span className="text-2xl font-extrabold text-blue-600">${totalPrice.toFixed(2)}</span>
        </div>
      </div>
    </div>
  );
};

