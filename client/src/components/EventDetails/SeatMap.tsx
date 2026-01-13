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
    <div className="bg-white rounded-2xl sm:rounded-3xl shadow-xl shadow-gray-200/50 p-4 sm:p-6 md:p-8 lg:p-12 border border-gray-100 relative overflow-hidden">
      <div className="max-w-md mx-auto mb-12 sm:mb-16 md:mb-20 relative">
        <div className="w-full h-2 sm:h-3 bg-gradient-to-r from-gray-200 via-gray-400 to-gray-200 rounded-full blur-sm opacity-50" />
        <div className="relative -top-1 w-full h-10 sm:h-12 bg-gray-800 rounded-b-[80px] sm:rounded-b-[100px] shadow-2xl flex items-center justify-center">
          <span className="text-gray-400 text-[10px] sm:text-xs font-black tracking-[0.2em] sm:tracking-[0.3em] uppercase">Stage</span>
        </div>
      </div>

      <div className="overflow-x-auto pb-6 sm:pb-8 -mx-2 px-2">
        <div className="flex flex-col items-center gap-2 sm:gap-3 md:gap-4 min-w-max">
          {Object.keys(groupedSeats).sort().map(row => (
            <div key={row} className="flex items-center gap-2 sm:gap-4 md:gap-6">
              <span className="w-4 sm:w-6 text-[10px] sm:text-xs font-black text-gray-300 text-right">{row}</span>
              
              <div className="flex gap-1.5 sm:gap-2 md:gap-2.5">
                {groupedSeats[row]
                  .sort((a, b) => (a.seatNumber || 0) - (b.seatNumber || 0))
                  .map((seat, idx) => (
                    <button
                      key={seat.id}
                      onClick={() => seat.id && onSeatToggle(seat.id, seat.isAvailable)}
                      disabled={!seat.isAvailable}
                      className={`
                        w-7 h-7 sm:w-8 sm:h-8 md:w-9 md:h-9 rounded-md sm:rounded-lg text-[9px] sm:text-[10px] md:text-[11px] font-bold transition-all duration-200
                        ${seat.id && selectedSeats.has(seat.id)
                          ? 'bg-blue-600 text-white shadow-lg shadow-blue-200 scale-110 -translate-y-1'
                          : seat.isAvailable
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-100 hover:border-emerald-400 hover:bg-emerald-100 active:scale-95'
                          : 'bg-gray-100 text-gray-300 cursor-not-allowed'
                        }
                        ${(idx + 1) % 10 === 0 ? 'mr-3 sm:mr-4 md:mr-6' : ''}
                      `}
                    >
                      {seat.seatNumber}
                    </button>
                  ))}
              </div>
              
              <span className="w-4 sm:w-6 text-[10px] sm:text-xs font-black text-gray-300">{row}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="flex flex-wrap justify-center gap-4 sm:gap-6 md:gap-8 mt-8 sm:mt-10 md:mt-12 pt-6 sm:pt-8 border-t border-gray-50">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 sm:w-4 sm:h-4 bg-emerald-50 border border-emerald-100 rounded flex-shrink-0" />
          <span className="text-[10px] sm:text-xs font-bold text-gray-400 uppercase tracking-tighter">Available</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 sm:w-4 sm:h-4 bg-blue-600 rounded flex-shrink-0" />
          <span className="text-[10px] sm:text-xs font-bold text-gray-400 uppercase tracking-tighter">Selected</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 sm:w-4 sm:h-4 bg-gray-100 rounded flex-shrink-0" />
          <span className="text-[10px] sm:text-xs font-bold text-gray-400 uppercase tracking-tighter">Sold Out</span>
        </div>
      </div>
    </div>
  );
};