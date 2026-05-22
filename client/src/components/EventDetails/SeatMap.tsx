import React from 'react';
import EventSeatIcon from '@mui/icons-material/EventSeat';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
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
  const rowKeys = Object.keys(groupedSeats).sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));
  const availableCount = seats.filter((seat) => seat.isAvailable).length;

  return (
    <div className="relative overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-[0_24px_70px_-36px_rgba(15,23,42,0.4)]">
      <div className="flex flex-col gap-4 border-b border-slate-100 bg-slate-50/80 px-4 py-5 sm:flex-row sm:items-center sm:justify-between sm:px-6 md:px-8">
        <div>
          <h3 className="text-xl font-extrabold text-slate-950">Seat Map</h3>
          <p className="mt-1 text-sm text-slate-500">Pick any available seat from the current section.</p>
        </div>

        <div className="grid grid-cols-3 gap-2 text-center sm:min-w-80">
          <div className="rounded-lg border border-slate-200 bg-white px-3 py-2">
            <p className="text-[10px] font-black uppercase tracking-wider text-slate-400">Rows</p>
            <p className="text-lg font-black text-slate-900">{rowKeys.length}</p>
          </div>
          <div className="rounded-lg border border-emerald-100 bg-emerald-50 px-3 py-2">
            <p className="text-[10px] font-black uppercase tracking-wider text-emerald-600">Open</p>
            <p className="text-lg font-black text-emerald-700">{availableCount}</p>
          </div>
          <div className="rounded-lg border border-blue-100 bg-blue-50 px-3 py-2">
            <p className="text-[10px] font-black uppercase tracking-wider text-blue-600">Chosen</p>
            <p className="text-lg font-black text-blue-700">{selectedSeats.size}</p>
          </div>
        </div>
      </div>

      <div className="p-4 sm:p-6 md:p-8 lg:p-10">
        <div className="mx-auto mb-10 max-w-xl sm:mb-12">
          <div className="h-2 rounded-full bg-gradient-to-r from-transparent via-slate-400 to-transparent opacity-60 blur-[1px]" />
          <div className="relative -top-1 mx-auto flex h-12 w-[92%] items-center justify-center rounded-b-[120px] bg-slate-900 shadow-[0_20px_45px_-20px_rgba(15,23,42,0.75)] sm:h-14">
            <span className="text-[10px] font-black uppercase tracking-[0.35em] text-slate-300">Stage</span>
          </div>
        </div>

        <div className="-mx-4 overflow-x-auto px-4 pb-4 sm:-mx-6 sm:px-6">
          <div className="mx-auto flex min-w-max flex-col items-center gap-2.5 sm:gap-3">
            {rowKeys.map(row => (
              <div key={row} className="grid grid-cols-[2rem_auto_2rem] items-center gap-2 sm:grid-cols-[2.5rem_auto_2.5rem] sm:gap-4">
                <span className="rounded-md bg-slate-100 px-2 py-1 text-center text-[11px] font-black text-slate-500">{row}</span>

                <div className="flex items-center gap-1.5 rounded-xl border border-slate-100 bg-slate-50 px-2 py-2 sm:gap-2 sm:px-3">
                {groupedSeats[row]
                  .sort((a, b) => (a.seatNumber || 0) - (b.seatNumber || 0))
                  .map((seat, idx) => (
                    <button
                      key={seat.id}
                      onClick={() => seat.id && onSeatToggle(seat.id, seat.isAvailable)}
                      disabled={!seat.isAvailable}
                      title={`Row ${row}, seat ${seat.seatNumber}${seat.isAvailable ? '' : ' unavailable'}`}
                      className={`
                        group relative flex h-9 w-9 flex-col items-center justify-center rounded-lg border text-[10px] font-black transition-all duration-200 sm:h-10 sm:w-10 sm:text-[11px] md:h-11 md:w-11
                        ${seat.id && selectedSeats.has(seat.id)
                          ? 'border-blue-600 bg-blue-600 text-white shadow-lg shadow-blue-200 -translate-y-1'
                          : seat.isAvailable
                          ? 'border-emerald-200 bg-white text-emerald-700 hover:border-emerald-500 hover:bg-emerald-50 hover:-translate-y-0.5 active:scale-95'
                          : 'cursor-not-allowed border-slate-200 bg-slate-200 text-slate-400'
                        }
                        ${(idx + 1) % 10 === 0 ? 'mr-3 sm:mr-5' : ''}
                      `}
                    >
                      <span className={`absolute left-1 right-1 top-1 h-1 rounded-full ${seat.id && selectedSeats.has(seat.id) ? 'bg-blue-300' : seat.isAvailable ? 'bg-emerald-200' : 'bg-slate-300'}`} />
                      {seat.seatNumber}
                    </button>
                  ))}
                </div>

                <span className="rounded-md bg-slate-100 px-2 py-1 text-center text-[11px] font-black text-slate-500">{row}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="mt-6 flex flex-wrap justify-center gap-3 border-t border-slate-100 pt-6 sm:gap-4">
          <div className="flex items-center gap-2 rounded-lg border border-emerald-100 bg-emerald-50 px-3 py-2">
            <EventSeatIcon className="text-emerald-600" fontSize="small" />
            <span className="text-xs font-black uppercase tracking-wide text-emerald-700">Available</span>
          </div>
          <div className="flex items-center gap-2 rounded-lg border border-blue-100 bg-blue-50 px-3 py-2">
            <CheckCircleIcon className="text-blue-600" fontSize="small" />
            <span className="text-xs font-black uppercase tracking-wide text-blue-700">Selected</span>
          </div>
          <div className="flex items-center gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2">
            <BlockIcon className="text-slate-400" fontSize="small" />
            <span className="text-xs font-black uppercase tracking-wide text-slate-500">Sold out</span>
          </div>
        </div>
      </div>
    </div>
  );
};