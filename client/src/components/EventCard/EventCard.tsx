import React from 'react';
import type { EventResponse } from '../../generated/api';
import { formatDateShort } from '../../utils/dateUtils';

interface EventCardProps {
  event: EventResponse;
  onViewDetails: () => void;
}

export const EventCard: React.FC<EventCardProps> = ({ event, onViewDetails }) => {
  return (
    <article 
      onClick={onViewDetails}
      className="group bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-xl hover:shadow-blue-500/10 transition-all duration-300 overflow-hidden cursor-pointer"
    >
      <div className="relative h-52 overflow-hidden">
        <img
          src={event.image || 'https://placehold.co/400x200?text=Event'}
          alt={event.title}
          className="w-full h-full object-cover transform group-hover:scale-110 transition-transform duration-500"
          onError={(e) => {
            e.currentTarget.src = 'https://placehold.co/400x200?text=Event';
          }}
        />
        <div className="absolute top-4 left-4">
          <span className="px-3 py-1 bg-white/90 backdrop-blur-md text-gray-900 text-[10px] font-black uppercase tracking-wider rounded-lg shadow-sm">
            {event.category}
          </span>
        </div>
        <div className="absolute bottom-4 right-4 bg-blue-600 text-white px-3 py-1 rounded-lg shadow-lg">
          <p className="text-xs font-bold">{formatDateShort(event.date)}</p>
        </div>
      </div>

      <div className="p-6">
        <div className="mb-4">
          <h3 className="text-xl font-bold text-gray-900 line-clamp-1 group-hover:text-blue-600 transition-colors">
            {event.title}
          </h3>
          <div className="flex items-center gap-2 mt-2 text-gray-500 text-sm">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            </svg>
            <span className="truncate">{event.location}</span>
          </div>
        </div>

        <p className="text-gray-500 text-sm line-clamp-2 mb-6 leading-relaxed">
          {event.description}
        </p>

        <div className="flex justify-between items-end">
          <div>
            <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">Tickets from</p>
            <p className="text-2xl font-black text-gray-900">${event.minPrice}</p>
          </div>
          <div className="text-right">
            <span className={`text-xs font-bold px-2 py-1 rounded-md ${
              (event.availableSeats || 0) < 20 ? 'bg-red-50 text-red-600' : 'bg-emerald-50 text-emerald-600'
            }`}>
              {event.availableSeats} left
            </span>
          </div>
        </div>
      </div>
    </article>
  );
};