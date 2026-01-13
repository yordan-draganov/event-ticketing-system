import React from 'react';
import { useNavigate } from 'react-router-dom';

export const CheckoutHeader: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="bg-white/80 backdrop-blur-md border-b sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 py-3">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-gray-600 hover:text-blue-600 transition-colors font-medium"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back
        </button>
      </div>
    </div>
  );
};

