import React from 'react';

interface HeroProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
}

export const Hero: React.FC<HeroProps> = ({ searchQuery, onSearchChange }) => {
  return (
    <section className="bg-gradient-to-r from-blue-600 to-blue-800 text-white py-16">
      <div className="max-w-6xl mx-auto px-4">
        <h2 className="text-4xl md:text-5xl font-bold mb-4 text-center">
          Discover Amazing Events
        </h2>
        <p className="text-xl mb-8 text-center opacity-90">
          Find and book tickets for concerts, conferences, sports, and more
        </p>

        <div className="max-w-2xl mx-auto relative">
          <input
            type="search"
            placeholder="Search events, locations..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className="w-full px-5 py-4 pr-12 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
          <svg
            className="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 pointer-events-none"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </div>
      </div>
    </section>
  );
};