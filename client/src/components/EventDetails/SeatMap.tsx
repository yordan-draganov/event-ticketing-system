import React from 'react';
import type { SeatResponse } from '../../generated/api';
import { groupSeatsByRow } from '../../utils/eventUtils';

interface SeatMapProps {
  seats: SeatResponse[];
  selectedSeats: Set<string>;
  onSeatToggle: (seatId: string, isAvailable?: boolean) => void;
}

export const SeatMap: React.FC<SeatMapProps> = ({
  seats,
  selectedSeats,
  onSeatToggle,
}) => {
  const groupedSeats = groupSeatsByRow(seats);

  return (
    <div className="bg-white rounded-3xl shadow-xl shadow-gray-200/50 p-8 md:p-12 border border-gray-100 relative overflow-hidden">
      <div className="max-w-md mx-auto mb-20 relative">
        <div className="w-full h-3 bg-gradient-to-r from-gray-200 via-gray-400 to-gray-200 rounded-full blur-sm opacity-50" />
        <div className="relative -top-1 w-full h-12 bg-gray-800 rounded-b-[100px] shadow-2xl flex items-center justify-center">
          <span className="text-gray-400 text-xs font-black tracking-[0.3em] uppercase">Stage</span>
        </div>
      </div>

      <div className="overflow-x-auto pb-8">
        <div className="flex flex-col items-center gap-4 min-w-max">
          {Object.keys(groupedSeats).sort().map(row => (
            <div key={row} className="flex items-center gap-6">
              <span className="w-6 text-xs font-black text-gray-300">{row}</span>
              <div className="flex gap-2.5">
                {groupedSeats[row]
                  .sort((a, b) => (a.seatNumber || 0) - (b.seatNumber || 0))
                  .map((seat, idx) => (
                    <button
                      key={seat.id}
                      onClick={() => seat.id && onSeatToggle(seat.id, seat.isAvailable)}
                      disabled={!seat.isAvailable}
                      className={`
                        w-9 h-9 rounded-lg text-[11px] font-bold transition-all duration-200
                        ${seat.id && selectedSeats.has(seat.id)
                          ? 'bg-blue-600 text-white shadow-lg shadow-blue-200 scale-110 -translate-y-1'
                          : seat.isAvailable
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-100 hover:border-emerald-400 hover:bg-emerald-100'
                          : 'bg-gray-100 text-gray-300 cursor-not-allowed'
                        }
                        ${(idx + 1) % 10 === 0 ? 'mr-6' : ''}
                      `}
                    >
                      {seat.seatNumber}
                    </button>
                  ))}
              </div>
              <span className="w-6 text-xs font-black text-gray-300">{row}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="flex justify-center gap-8 mt-12 pt-8 border-t border-gray-50">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-emerald-50 border border-emerald-100 rounded" />
          <span className="text-xs font-bold text-gray-400 uppercase tracking-tighter">Available</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-blue-600 rounded" />
          <span className="text-xs font-bold text-gray-400 uppercase tracking-tighter">Selected</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-gray-100 rounded" />
          <span className="text-xs font-bold text-gray-400 uppercase tracking-tighter">Sold Out</span>
        </div>
      </div>
    </div>
  );
};

