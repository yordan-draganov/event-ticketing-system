import React from 'react';
import { useNavigate } from 'react-router-dom';

export const EmptyState: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="text-center py-16">
      <svg className="w-24 h-24 mx-auto text-gray-300 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
      </svg>
      <h3 className="text-xl font-semibold text-gray-700 mb-2">No Tickets Yet</h3>
      <p className="text-gray-500 mb-6">You haven't purchased any tickets yet.</p>
      <button
        onClick={() => navigate('/events')}
        className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
      >
        Browse Events
      </button>
    </div>
  );
};

