import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { EventResponse } from '../../generated/api';
import { formatDateLong, formatTime } from '../../utils/dateUtils';

interface EventInfoProps {
  event: EventResponse;
}

export const EventInfo: React.FC<EventInfoProps> = ({ event }) => {
  const navigate = useNavigate();

  return (
    <>
      <div className="bg-white/80 backdrop-blur-md border-b sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 py-3">
          <button
            onClick={() => navigate('/')}
            className="flex items-center gap-2 text-gray-600 hover:text-blue-600 transition-colors font-medium text-sm sm:text-base"
          >
            <svg className="w-4 h-4 sm:w-5 sm:h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            Back to Events
          </button>
        </div>
      </div>

      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-6 sm:py-8 md:py-10">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 sm:gap-8 md:gap-10 items-start">
            <div className="relative group order-1">
              <img
                src={event.image || 'https://placehold.co/600x400?text=Event'}
                alt={event.title}
                className="w-full h-64 sm:h-80 md:h-96 lg:h-[400px] object-cover rounded-2xl shadow-lg group-hover:shadow-xl transition-shadow"
                onError={(e) => {
                  e.currentTarget.src = 'https://placehold.co/600x400?text=Event';
                }}
              />
              <div className="absolute top-3 left-3 sm:top-4 sm:left-4">
                <span className="px-3 py-1 sm:px-4 sm:py-1.5 bg-blue-600 text-white rounded-full text-xs sm:text-sm font-semibold shadow-md">
                  {event.category}
                </span>
              </div>
            </div>

            <div className="flex flex-col h-full order-2">
              <h1 className="text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-extrabold text-gray-900 mb-4 sm:mb-6 tracking-tight leading-tight">
                {event.title}
              </h1>
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4 md:gap-6 mb-6 sm:mb-8">
                <div className="flex items-center gap-3 sm:gap-4 p-3 sm:p-4 bg-gray-50 rounded-xl">
                  <div className="p-2 bg-white rounded-lg shadow-sm text-blue-600 flex-shrink-0">
                    <svg className="w-5 h-5 sm:w-6 sm:h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="text-[10px] sm:text-xs text-gray-400 font-bold uppercase tracking-wider">Date</p>
                    <p className="text-sm sm:text-base text-gray-700 font-medium truncate">{formatDateLong(event.date)}</p>
                  </div>
                </div>

                <div className="flex items-center gap-3 sm:gap-4 p-3 sm:p-4 bg-gray-50 rounded-xl">
                  <div className="p-2 bg-white rounded-lg shadow-sm text-blue-600 flex-shrink-0">
                    <svg className="w-5 h-5 sm:w-6 sm:h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="text-[10px] sm:text-xs text-gray-400 font-bold uppercase tracking-wider">Time</p>
                    <p className="text-sm sm:text-base text-gray-700 font-medium truncate">
                      {formatTime(event.startTime || '')} - {formatTime(event.endTime || '')}
                    </p>
                  </div>
                </div>
              </div>

              <div className="space-y-3 sm:space-y-4 mb-6 sm:mb-8">
                <div className="flex items-start gap-3 text-gray-600">
                  <svg className="w-5 h-5 text-gray-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  </svg>
                  <span className="font-medium text-sm sm:text-base break-words">{event.location}</span>
                </div>
                {event.organizer && (
                  <div className="flex items-start gap-3 text-gray-600">
                    <svg className="w-5 h-5 text-gray-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <span className="text-sm sm:text-base">
                      Hosted by <span className="font-semibold text-gray-900">{event.organizer}</span>
                    </span>
                  </div>
                )}
              </div>

              <div className="mt-auto pt-4 sm:pt-6 border-t border-gray-100 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div>
                  <p className="text-xs sm:text-sm text-gray-400 font-bold uppercase tracking-wider">Tickets From</p>
                  <p className="text-2xl sm:text-3xl font-black text-blue-600">${event.minPrice}</p>
                </div>
                <div className="sm:text-right">
                  <p className="text-xs sm:text-sm text-gray-400 font-bold uppercase tracking-wider">Availability</p>
                  <p className="text-base sm:text-lg font-bold text-gray-900">{event.availableSeats} seats left</p>
                </div>
              </div>
            </div>
          </div>

          {event.longDescription && (
            <div className="mt-8 sm:mt-10 md:mt-12 p-4 sm:p-6 md:p-8 bg-gray-50 rounded-2xl">
              <h2 className="text-xl sm:text-2xl font-bold text-gray-900 mb-3 sm:mb-4">About the Event</h2>
              <p className="text-sm sm:text-base text-gray-600 leading-relaxed whitespace-pre-line">{event.longDescription}</p>
            </div>
          )}
        </div>
      </div>
    </>
  );
};