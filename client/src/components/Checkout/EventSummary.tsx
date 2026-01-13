import React from 'react';
import type { EventResponse } from '../../generated/api';
import { formatDateLong, formatTime } from '../../utils/dateUtils';

interface EventSummaryProps {
  event: EventResponse;
}

export const EventSummary: React.FC<EventSummaryProps> = ({ event }) => {
  return (
    <div className="bg-white rounded-2xl shadow-xl p-8">
      <h2 className="text-xl font-bold text-gray-900 mb-6">Event Details</h2>
      <div className="flex gap-6">
        <img
          src={event.image || 'https://placehold.co/200x200?text=Event'}
          alt={event.title}
          className="w-32 h-32 object-cover rounded-xl"
          onError={(e) => {
            e.currentTarget.src = 'https://placehold.co/200x200?text=Event';
          }}
        />
        <div className="flex-1">
          <h3 className="text-2xl font-bold text-gray-900 mb-3">{event.title}</h3>
          <div className="space-y-2 text-gray-600">
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span>{formatDateLong(event.date)}</span>
            </div>
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{formatTime(event.startTime || '')} - {formatTime(event.endTime || '')}</span>
            </div>
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              </svg>
              <span>{event.location}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

