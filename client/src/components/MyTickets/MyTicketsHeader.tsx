import React from 'react';
import { useNavigate } from 'react-router-dom';

export const MyTicketsHeader: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="bg-white border-b sticky top-0 z-30">
      <div className="max-w-7xl mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 text-gray-600 hover:text-gray-900"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back
          </button>
          <h1 className="text-2xl font-bold text-gray-900">My Tickets</h1>
          <div className="w-20"></div>
        </div>
      </div>
    </div>
  );
};

