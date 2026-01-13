import React, { useState, useEffect } from 'react';
import { ApiClient } from '../../services/api.client';
import type { TicketResponse } from '../../generated/api';
import { MyTicketsHeader } from '../../components/MyTickets/MyTicketsHeader';
import { TicketCard } from '../../components/MyTickets/TicketCard';
import { EmptyState } from '../../components/MyTickets/EmptyState';
import { LoadingState } from '../../components/MyTickets/LoadingState';

export const MyTickets: React.FC = () => {
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchTickets();
  }, []);

  const fetchTickets = async () => {
    try {
      setLoading(true);
      const data = await ApiClient.getMyTickets();
      setTickets(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load tickets');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingState />;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <MyTicketsHeader />

      <div className="max-w-4xl mx-auto px-4 py-8">
        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg">
            {error}
          </div>
        )}

        {tickets.length === 0 ? (
          <EmptyState />
        ) : (
          <div className="space-y-4">
            {tickets.map(ticket => (
              <TicketCard key={ticket.id} ticket={ticket} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

