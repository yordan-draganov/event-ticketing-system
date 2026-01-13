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
    <div className="fixed bottom-6 left-1/2 -translate-x-1/2 w-[calc(100%-2rem)] max-w-4xl z-50 animate-in fade-in slide-in-from-bottom-8 duration-500">
      <div className="bg-gray-900 rounded-2xl shadow-2xl p-6 text-white flex flex-col md:flex-row items-center justify-between gap-6 border border-white/10 backdrop-blur-lg">
        <div className="flex items-center gap-6">
          <div className="hidden sm:flex items-center justify-center w-14 h-14 bg-blue-600 rounded-xl shadow-inner">
            <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z" />
            </svg>
          </div>
          <div className="text-center md:text-left">
            <p className="text-blue-400 text-xs font-black uppercase tracking-widest mb-1">Reservation Summary</p>
            <h3 className="text-2xl font-black leading-none tracking-tight">
              ${total.toFixed(2)}
              <span className="ml-3 text-sm font-medium text-gray-400">
                for {ticketCount} ticket{ticketCount > 1 ? 's' : ''}
              </span>
            </h3>
          </div>
        </div>
        <button
          onClick={onCheckout}
          className="w-full md:w-auto px-10 py-4 bg-white text-gray-900 rounded-xl font-bold hover:bg-blue-50 transition-all active:scale-95 shadow-xl"
        >
          Checkout Now
        </button>
      </div>
    </div>
  );
};

