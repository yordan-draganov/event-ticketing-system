import React from 'react';
import type { SeatResponse } from '../../generated/api';

interface TicketDetailsProps {
  sectionName: string;
  pricePerTicket: number;
  seats: SeatResponse[];
}

export const TicketDetails: React.FC<TicketDetailsProps> = ({ sectionName, pricePerTicket, seats }) => {
  return (
    <div className="bg-white rounded-2xl shadow-xl p-8">
      <h2 className="text-xl font-bold text-gray-900 mb-6">Ticket Details</h2>
      <div className="space-y-4">
        <div className="flex items-center justify-between py-4 border-b border-gray-100">
          <div>
            <p className="font-semibold text-gray-900">{sectionName}</p>
            <p className="text-sm text-gray-500">
              {seats.length} ticket{seats.length > 1 ? 's' : ''}
            </p>
          </div>
          <p className="font-bold text-gray-900">${pricePerTicket.toFixed(2)} each</p>
        </div>
        {seats.map((seat, index) => (
          <div key={seat.id || index} className="flex items-center justify-between py-2 text-sm text-gray-600">
            <span>Seat {seat.rowLabel}{seat.seatNumber}</span>
            <span>${pricePerTicket.toFixed(2)}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

