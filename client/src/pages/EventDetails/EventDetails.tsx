import React, { useCallback, useState, useEffect } from 'react';
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
  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchEventDetails = useCallback(async () => {
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
  }, [eventId]);

  const fetchSeats = useCallback(async (sectionId: string) => {
    try {
      const seatsData = await ApiClient.getSeatsBySection(sectionId);
      setSeats(seatsData);
    } catch {
      setError('Failed to load seats. Please refresh the page.');
    }
  }, []);

  useEffect(() => {
    if (eventId) {
      fetchEventDetails();
    }
  }, [eventId, fetchEventDetails]);

  useEffect(() => {
    if (selectedSection) {
      fetchSeats(selectedSection);
    }
  }, [selectedSection, fetchSeats]);

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

  const handleBooking = async () => {
    if (!eventId || selectedSeats.size === 0 || !event) return;

    try {
      setCheckoutLoading(true);
      await ApiClient.getCurrentUser();
    } catch {
      alert('Please login to book tickets');
      navigate('/');
      setCheckoutLoading(false);
      return;
    }

    try {
      const payment = await ApiClient.createPaymentIntent({
        eventId,
        seatIds: Array.from(selectedSeats),
      });

      if (!payment.reservationId) {
        throw new Error('Failed to create checkout reservation.');
      }

      navigate(`/checkout/${payment.reservationId}`);
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Some of the selected seats are no longer available. Please choose different seats.');
      if (selectedSection) {
        fetchSeats(selectedSection);
      }
    } finally {
      setCheckoutLoading(false);
    }
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
          onCheckout={checkoutLoading ? () => undefined : handleBooking}
        />
      </div>
    </div>
  );
};
