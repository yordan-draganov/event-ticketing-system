import React from 'react';
import { useNavigate } from 'react-router-dom';
import { EventCard } from '../../components/EventCard/EventCard';
import { useEvents } from '../../hooks/useEvents';
import { useFilters } from '../../hooks/useFilters';

export const Home: React.FC = () => {
  const navigate = useNavigate();
  const { events, loading: eventsLoading } = useEvents();
  const { 
    searchQuery, 
    setSearchQuery, 
    categoryFilter, 
    setCategoryFilter, 
    filteredEvents 
  } = useFilters(events);

  const categories = ['All', 'Music', 'Technology', 'Food', 'Entertainment', 'Sports', 'Art', 'Business', 'Education'];

  return (
    <div className="min-h-screen bg-white">
      <section className="relative bg-[#0F172A] overflow-hidden">
        <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-blue-600/10 rounded-full blur-[120px]" />
        <div className="relative max-w-6xl mx-auto px-4 py-24 text-center">
          <h1 className="text-5xl md:text-7xl font-black text-white mb-6 tracking-tight">
            Find your next <span className="text-blue-500">experience.</span>
          </h1>
          <p className="text-blue-100/60 text-lg md:text-xl mb-12 max-w-2xl mx-auto">
            The simplest way to book tickets for the world's most exciting events.
          </p>

          <div className="max-w-2xl mx-auto">
            <div className="relative group">
              <input
                type="search"
                placeholder="Search by event, artist or city..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full px-6 py-5 bg-white rounded-2xl text-gray-900 shadow-2xl focus:outline-none text-lg"
              />
              <div className="absolute right-4 top-1/2 -translate-y-1/2">
                <div className="p-2 bg-blue-600 rounded-xl text-white">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="sticky top-0 z-30 bg-white/80 backdrop-blur-xl border-b border-gray-100">
        <div className="max-w-6xl mx-auto px-4 py-6 flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex gap-2 overflow-x-auto no-scrollbar w-full md:w-auto">
            {categories.map(cat => (
              <button
                key={cat}
                onClick={() => setCategoryFilter(cat)}
                className={`px-6 py-2.5 rounded-xl text-sm font-bold transition-all whitespace-nowrap ${
                  categoryFilter === cat
                    ? 'bg-gray-900 text-white shadow-lg'
                    : 'bg-gray-50 text-gray-500 hover:bg-gray-100'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
          <p className="text-xs font-black text-gray-400 uppercase tracking-[0.2em]">
            {filteredEvents.length} Events Available
          </p>
        </div>
      </div>

      <main className="max-w-6xl mx-auto px-4 py-16">
        {eventsLoading ? (
          <div className="flex justify-center py-20">
            <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : filteredEvents.length === 0 ? (
          <div className="text-center py-20 bg-gray-50 rounded-3xl border border-gray-100">
            <h3 className="text-xl font-bold text-gray-900">No results found</h3>
            <p className="text-gray-500 mt-2">Try a different search or category.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10">
            {filteredEvents.map(event => (
              <EventCard 
                key={event.id} 
                event={event}
                onViewDetails={() => navigate(`/events/${event.id}`)}
              />
            ))}
          </div>
        )}
      </main>
    </div>
  );
};