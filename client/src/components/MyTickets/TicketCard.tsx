import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { TicketResponse } from '../../generated/api';
import { formatDateLong, formatTime } from '../../utils/dateUtils';

interface TicketCardProps {
  ticket: TicketResponse;
}

export const TicketCard: React.FC<TicketCardProps> = ({ ticket }) => {
  const navigate = useNavigate();

  const getStatusColor = (status: string): string => {
    switch (status) {
      case 'confirmed':
        return 'bg-green-100 text-green-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      case 'pending':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="bg-white rounded-lg shadow hover:shadow-lg transition overflow-hidden">
      <div className="md:flex">
        <div className="md:w-1/3">
          <img
            src={ticket.eventImage || 'https://via.placeholder.com/400x300?text=Event'}
            alt={ticket.eventTitle}
            className="w-full h-48 md:h-full object-cover"
          />
        </div>
        <div className="md:w-2/3 p-6">
          <div className="flex items-start justify-between mb-4">
            <div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">
                {ticket.eventTitle}
              </h3>
              <span className={`inline-block px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(ticket.status)}`}>
                {ticket.status.toUpperCase()}
              </span>
            </div>
          </div>

          <div className="space-y-2 text-sm text-gray-600 mb-4">
            <div className="flex items-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span>{formatDateLong(ticket.eventDate)} • {formatTime(ticket.startTime || '')} - {formatTime(ticket.endTime || '')}</span>
            </div>
            <div className="flex items-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              </svg>
              <span>{ticket.eventLocation}</span>
            </div>
            <div className="flex items-center gap-2">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
              <span>Section {ticket.sectionName} • {ticket.seatCount} seat{ticket.seatCount !== 1 ? 's' : ''}</span>
            </div>
          </div>

          <div className="flex items-center justify-between pt-4 border-t">
            <div>
              <span className="text-sm text-gray-500">Total Price</span>
              <p className="text-xl font-bold text-blue-600">${ticket.totalPrice}</p>
            </div>
            <button
              onClick={() => navigate(`/tickets/${ticket.id}`)}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium"
            >
              View Ticket
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

