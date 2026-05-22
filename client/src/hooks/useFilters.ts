import { useMemo, useState } from 'react';
import type { EventResponse } from '../generated/api';

export const useFilters = (events: EventResponse[]) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('All');

  const filteredEvents = useMemo(() => {
    let filtered = events;

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(event =>
        event.title?.toLowerCase().includes(query) ||
        event.location?.toLowerCase().includes(query) ||
        event.description?.toLowerCase().includes(query)
      );
    }

    if (categoryFilter !== 'All') {
      filtered = filtered.filter(event => event.category === categoryFilter);
    }

    return filtered;
  }, [searchQuery, categoryFilter, events]);

  return {
    searchQuery,
    setSearchQuery,
    categoryFilter,
    setCategoryFilter,
    filteredEvents
  };
};
