import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ApiClient } from '../../services/api.client';
import type { EventResponse, SectionResponse, SeatResponse } from '../../generated/api';
import { EventInfo } from '../../components/EventDetails/EventInfo';
import { SectionSelector } from '../../components/EventDetails/SectionSelector';
import { SeatMap } from '../../components/EventDetails/SeatMap';
import { CheckoutSummary } from '../../components/EventDetails/CheckoutSummary';
import { calculateTotal } from '../../utils/eventUtils';

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

  const handleSectionChange = (sectionId: string) => {
    setSelectedSection(sectionId);
    setSelectedSeats(new Set());
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

  const handleBooking = () => {
    if (!eventId || selectedSeats.size === 0 || !event) return;
    
    let userName: string | null = null;
    try {
      userName = localStorage.getItem('userName');
    } catch (storageError) {
      console.error('Failed to read authentication state during booking:', storageError);
      alert('Unable to access your session. Please try again.');
      return;
    }

    if (!userName) {
      alert('Please login to book tickets');
      navigate('/');
      return;
    }

    const selectedSeatObjects = seats.filter(seat => seat.id && selectedSeats.has(seat.id));
    
    const selectedSectionObj = sections.find(s => s.id === selectedSection);
    
    if (!selectedSectionObj) {
      alert('Please select a section');
      return;
    }

    navigate('/checkout', {
      state: {
        event,
        sections,
        selectedSeats: selectedSeatObjects,
        selectedSection: selectedSectionObj,
        totalPrice: calculateTotal(sections, selectedSection, selectedSeats.size),
      }
    });
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

  const total = calculateTotal(sections, selectedSection, selectedSeats.size);

  return (
    <div className="min-h-screen bg-gray-50">
      <EventInfo event={event} />

      <div className="max-w-7xl mx-auto px-4 py-16">
        <div className="text-center mb-10">
          <h2 className="text-3xl font-extrabold text-gray-900 mb-2">Choose Your Seats</h2>
          <p className="text-gray-500">Select a section and click on the seats you'd like to reserve</p>
        </div>

        <SectionSelector
          sections={sections}
          selectedSection={selectedSection}
          onSectionChange={handleSectionChange}
        />

        <SeatMap
          seats={seats}
          selectedSeats={selectedSeats}
          onSeatToggle={toggleSeat}
        />

        <CheckoutSummary
          total={total}
          ticketCount={selectedSeats.size}
          onCheckout={handleBooking}
        />
      </div>
    </div>
  );
};
