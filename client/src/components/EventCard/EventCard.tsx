import React from 'react';
import type { EventResponse } from '../../generated/api';

interface EventCardProps {
  event: EventResponse;
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
};

const formatTime = (timeStr?: string): string => {
  if (!timeStr) return '';
  return timeStr.substring(0, 5);
};

export const EventCard: React.FC<EventCardProps> = ({ event }) => {
  return (
    <article className="bg-white rounded-lg shadow hover:shadow-xl transition-shadow overflow-hidden">
      <img
        src={event.image || 'https://via.placeholder.com/400x200?text=Event'}
        alt={event.title}
        className="w-full h-48 object-cover"
      />
      <div className="p-5">
        <div className="flex justify-between items-start mb-3">
          <h3 className="text-lg font-bold text-gray-900 line-clamp-2 flex-1">
            {event.title}
          </h3>
          <span className="ml-2 px-2 py-1 bg-blue-100 text-blue-700 text-xs font-medium rounded-full flex-shrink-0">
            {event.category}
          </span>
        </div>

        <p className="text-sm text-gray-600 mb-2">
          📅 {formatDate(event.date)} • {formatTime(event.startTime)} - {formatTime(event.endTime)}
        </p>

        <p className="text-sm text-gray-600 mb-3">
          📍 {event.location}
        </p>

        <p className="text-sm text-gray-700 line-clamp-2 mb-4">
          {event.description}
        </p>

        <div className="flex justify-between items-center mb-4">
          <span className="text-lg font-semibold text-blue-600">
            ${event.minPrice} - ${event.maxPrice}
          </span>
          <span className="text-sm text-gray-600">
            {event.availableSeats} seats left
          </span>
        </div>

        <button className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition font-medium">
          View Details
        </button>
      </div>
    </article>
  );
};
