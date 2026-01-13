import React from 'react';

interface CheckoutSummaryProps {
  total: number;
  ticketCount: number;
  onCheckout: () => void;
}

export const CheckoutSummary: React.FC<CheckoutSummaryProps> = ({
  total,
  ticketCount,
  onCheckout,
}) => {
  if (ticketCount === 0) return null;

  return (
    <div className="fixed bottom-4 sm:bottom-6 left-4 right-4 sm:left-1/2 sm:-translate-x-1/2 w-auto sm:w-[calc(100%-2rem)] max-w-4xl z-50 animate-in fade-in slide-in-from-bottom-8 duration-500">
      <div className="bg-gray-900 rounded-xl sm:rounded-2xl shadow-2xl p-4 sm:p-6 text-white flex flex-col sm:flex-row items-center justify-between gap-4 sm:gap-6 border border-white/10 backdrop-blur-lg">
        <div className="flex items-center gap-3 sm:gap-6 w-full sm:w-auto">
          <div className="hidden sm:flex items-center justify-center w-12 h-12 md:w-14 md:h-14 bg-blue-600 rounded-xl shadow-inner flex-shrink-0">
            <svg className="w-6 h-6 md:w-7 md:h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
            </svg>
          </div>
          <div className="flex-1 text-center sm:text-left min-w-0">
            <p className="text-blue-400 text-[10px] sm:text-xs font-black uppercase tracking-widest mb-1">
              Reservation Summary
            </p>
            <div className="flex flex-col sm:flex-row sm:items-baseline sm:gap-3">
              <h3 className="text-xl sm:text-2xl font-black leading-none tracking-tight">
                ${total.toFixed(2)}
              </h3>
              <span className="text-xs sm:text-sm font-medium text-gray-400 mt-1 sm:mt-0">
                for {ticketCount} ticket{ticketCount > 1 ? 's' : ''}
              </span>
            </div>
          </div>
        </div>
        <button
          onClick={onCheckout}
          className="w-full sm:w-auto px-6 sm:px-10 py-3 sm:py-4 bg-white text-gray-900 rounded-lg sm:rounded-xl font-bold text-sm sm:text-base hover:bg-blue-50 transition-all active:scale-95 shadow-xl whitespace-nowrap"
        >
          Checkout Now
        </button>
      </div>
    </div>
  );
};