import { useState, useEffect } from 'react';
import { ApiClient } from '../services/api.client';
import type { EventResponse } from '../generated/api';
import { getErrorMessage } from '../utils/errorUtils';

export const useEvents = () => {
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await ApiClient.getAllEvents();
      setEvents(data);
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Failed to fetch events'));
      console.error('Error fetching events:', err);
    } finally {
      setLoading(false);
    }
  };

  const refetch = () => {
    fetchEvents();
  };

  return { events, loading, error, refetch };
};
