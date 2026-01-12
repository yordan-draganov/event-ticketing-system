import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ApiClient } from '../../services/api.client';
import type { EventResponse, SectionResponse, SeatResponse } from '../../generated/api';

export const EventDetails: React.FC = () => {
  const { eventId } = useParams<{ eventId: string }>();
  const navigate = useNavigate();

  const [event, setEvent] = useState<EventResponse | null>(null);
  const [sections, setSections] = useState<SectionResponse[]>([]);
  const [seats, setSeats] = useState<SeatResponse[]>([]);
  const [selectedSection, setSelectedSection] = useState<string | null>(null);
  const [selectedSeats, setSelectedSeats] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (eventId) {
      fetchEventDetails();
    }
  }, [eventId]);

  useEffect(() => {
    if (selectedSection) {
      fetchSeats(selectedSection);
    }
  }, [selectedSection]);

  const fetchEventDetails = async () => {
    if (!eventId) return;
    
    try {
      setLoading(true);
      const [eventData, sectionsData] = await Promise.all([
        ApiClient.getEventById(eventId),
        ApiClient.getSectionsByEvent(eventId),
      ]);
      setEvent(eventData);
      setSections(sectionsData);
      if (sectionsData.length > 0 && sectionsData[0].id) {
        setSelectedSection(sectionsData[0].id);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load event details');
    } finally {
      setLoading(false);
    }
  };

  const fetchSeats = async (sectionId: string) => {
    try {
      const seatsData = await ApiClient.getSeatsBySection(sectionId);
      setSeats(seatsData);
    } catch (err) {
      console.error('Failed to load seats:', err);
    }
  };

  const toggleSeat = (seatId: string, isAvailable?: boolean) => {
    if (!isAvailable) return;

    const newSelected = new Set(selectedSeats);
    if (newSelected.has(seatId)) {
      newSelected.delete(seatId);
    } else {
      newSelected.add(seatId);
    }
    setSelectedSeats(newSelected);
  };

  const calculateTotal = (): number => {
    const section = sections.find(s => s.id === selectedSection);
    if (!section || !section.price) return 0;
    return Number(section.price) * selectedSeats.size;
  };

  const formatDate = (dateStr: string): string => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-US', {
      weekday: 'long',
      month: 'long',
      day: 'numeric',
      year: 'numeric'
    });
  };

  const formatTime = (timeStr: string): string => {
    if (!timeStr) return '';
    return timeStr.substring(0, 5);
  };

  const groupedSeats = seats.reduce((acc, seat) => {
    const rowLabel = seat.rowLabel || '';
    if (!acc[rowLabel]) {
      acc[rowLabel] = [];
    }
    acc[rowLabel].push(seat);
    return acc;
  }, {} as Record<string, SeatResponse[]>);

  const handleBooking = () => {
    if (!eventId || selectedSeats.size === 0) return;
    
    const token = localStorage.getItem('token');
    if (!token) {
      alert('Please login to book tickets');
      navigate('/');
      return;
    }
    // TODO: payment flow
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="text-xl text-gray-500 mt-4">Loading event details...</p>
        </div>
      </div>
    );
  }

  if (error || !event) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-xl text-red-600 mb-4">{error || 'Event not found'}</p>
          <button onClick={() => navigate('/')} className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
            Back to Events
          </button>
        </div>
      </div>
    );
  }

return (
  <div className="min-h-screen bg-gray-50">
    <div className="bg-white/80 backdrop-blur-md border-b sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 py-3">
        <button
          onClick={() => navigate('/')}
          className="flex items-center gap-2 text-gray-600 hover:text-blue-600 transition-colors font-medium"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back to Events
        </button>
      </div>
    </div>

    <div className="bg-white shadow-sm">
      <div className="max-w-7xl mx-auto px-4 py-10">
        <div className="grid md:grid-cols-2 gap-10 items-start">
          <div className="relative group">
            <img
              src={event.image || 'https://placehold.co/600x400?text=Event'}
              alt={event.title}
              className="w-full h-[400px] object-cover rounded-2xl shadow-lg group-hover:shadow-xl transition-shadow"
              onError={(e) => {
                e.currentTarget.src = 'https://placehold.co/600x400?text=Event';
              }}
            />
            <div className="absolute top-4 left-4">
              <span className="px-4 py-1.5 bg-blue-600 text-white rounded-full text-sm font-semibold shadow-md">
                {event.category}
              </span>
            </div>
          </div>

          <div className="flex flex-col h-full">
            <h1 className="text-4xl lg:text-5xl font-extrabold text-gray-900 mb-6 tracking-tight">
              {event.title}
            </h1>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-8">
              <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-xl">
                <div className="p-2 bg-white rounded-lg shadow-sm text-blue-600">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 font-bold uppercase tracking-wider">Date</p>
                  <p className="text-gray-700 font-medium">{formatDate(event.date)}</p>
                </div>
              </div>

              <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-xl">
                <div className="p-2 bg-white rounded-lg shadow-sm text-blue-600">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <p className="text-xs text-gray-400 font-bold uppercase tracking-wider">Time</p>
                  <p className="text-gray-700 font-medium">{formatTime(event.startTime || '')} - {formatTime(event.endTime || '')}</p>
                </div>
              </div>
            </div>

            <div className="space-y-4 mb-8">
              <div className="flex items-center gap-3 text-gray-600">
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                </svg>
                <span className="font-medium">{event.location}</span>
              </div>
              {event.organizer && (
                <div className="flex items-center gap-3 text-gray-600">
                  <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span>Hosted by <span className="font-semibold text-gray-900">{event.organizer}</span></span>
                </div>
              )}
            </div>

            <div className="mt-auto pt-6 border-t border-gray-100 flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-400 font-bold uppercase tracking-wider">Tickets From</p>
                <p className="text-3xl font-black text-blue-600">${event.minPrice}</p>
              </div>
              <div className="text-right">
                <p className="text-sm text-gray-400 font-bold uppercase tracking-wider">Availability</p>
                <p className="text-lg font-bold text-gray-900">{event.availableSeats} seats left</p>
              </div>
            </div>
          </div>
        </div>

        {event.longDescription && (
          <div className="mt-12 p-8 bg-gray-50 rounded-2xl">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">About the Event</h2>
            <p className="text-gray-600 leading-relaxed whitespace-pre-line">{event.longDescription}</p>
          </div>
        )}
      </div>
    </div>

    <div className="max-w-7xl mx-auto px-4 py-16">
      <div className="text-center mb-10">
        <h2 className="text-3xl font-extrabold text-gray-900 mb-2">Choose Your Seats</h2>
        <p className="text-gray-500">Select a section and click on the seats you'd like to reserve</p>
      </div>

      <div className="flex flex-wrap justify-center gap-3 mb-10">
        {sections.map(section => (
          <button
            key={section.id}
            onClick={() => {
              if (section.id) {
                setSelectedSection(section.id);
                setSelectedSeats(new Set());
              }
            }}
            className={`group flex flex-col items-center px-6 py-4 rounded-xl border-2 transition-all ${
              selectedSection === section.id
                ? 'border-blue-600 bg-blue-50'
                : 'border-transparent bg-white hover:border-gray-200'
            }`}
          >
            <span className={`font-bold ${selectedSection === section.id ? 'text-blue-700' : 'text-gray-900'}`}>
              {section.name}
            </span>
            <span className={`text-sm ${selectedSection === section.id ? 'text-blue-600' : 'text-gray-500'}`}>
              ${section.price} • {section.availableSeats} left
            </span>
          </button>
        ))}
      </div>

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
                        onClick={() => seat.id && toggleSeat(seat.id, seat.isAvailable)}
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

      {selectedSeats.size > 0 && (
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
                  ${calculateTotal().toFixed(2)}
                  <span className="ml-3 text-sm font-medium text-gray-400">
                    for {selectedSeats.size} ticket{selectedSeats.size > 1 ? 's' : ''}
                  </span>
                </h3>
              </div>
            </div>
            <button
              onClick={handleBooking}
              className="w-full md:w-auto px-10 py-4 bg-white text-gray-900 rounded-xl font-bold hover:bg-blue-50 transition-all active:scale-95 shadow-xl"
            >
              Checkout Now
            </button>
          </div>
        </div>
      )}
    </div>
  </div>
);
};
