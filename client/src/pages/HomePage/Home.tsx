import React, { useState, useEffect } from 'react';
import { Header } from '../../components/Header/Header';
import { EventCard } from '../../components/EventCard/EventCard';
import { LoginModal } from '../../components/LoginModal/LoginModal';
import { SignupModal } from '../../components/SignupModal/SignupModal';
import { useEvents } from '../../hooks/useEvents';
import { useAuth } from '../../hooks/useAuth';
import { useFilters } from '../../hooks/useFilters';
import type { LoginRequest, SignupRequest } from '../../generated/api';

const HomePage: React.FC = () => {
  const [loginOpen, setLoginOpen] = useState(false);
  const [signupOpen, setSignupOpen] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userName, setUserName] = useState('');

  const { events, loading: eventsLoading } = useEvents();
  const { login, signup, logout, loading, error, success, setSuccess } = useAuth();
  const { 
    searchQuery, 
    setSearchQuery, 
    categoryFilter, 
    setCategoryFilter, 
    filteredEvents 
  } = useFilters(events);

  const categories = [
    'All', 
    'Music', 
    'Technology', 
    'Food', 
    'Entertainment', 
    'Sports', 
    'Art', 
    'Business', 
    'Education'
  ];

  useEffect(() => {
    const token = localStorage.getItem('token');
    const name = localStorage.getItem('userName');
    if (token && name) {
      setIsAuthenticated(true);
      setUserName(name);
    }
  }, []);

  const handleLogin = async (data: LoginRequest) => {
    const result = await login(data);
    if (result) {
      setLoginOpen(false);
      setIsAuthenticated(true);
      setUserName(localStorage.getItem('userName') || '');
    }
  };

  const handleSignup = async (data: SignupRequest) => {
    const result = await signup(data);
    if (result) {
      setSignupOpen(false);
      setIsAuthenticated(true);
      setUserName(localStorage.getItem('userName') || '');
    }
  };

  const handleLogout = async () => {
    await logout();
    setIsAuthenticated(false);
    setUserName('');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header 
        isAuthenticated={isAuthenticated}
        userName={userName}
        onLoginClick={() => setLoginOpen(true)} 
        onSignupClick={() => setSignupOpen(true)}
        onLogoutClick={handleLogout}
      />

      {success && (
        <div className="max-w-7xl mx-auto px-4 pt-4">
          <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-lg flex justify-between items-center">
            <span>{success}</span>
            <button onClick={() => setSuccess('')} className="text-green-600 hover:text-green-800">
              ✕
            </button>
          </div>
        </div>
      )}

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
              onChange={(e) => setSearchQuery(e.target.value)}
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

      <div className="max-w-6xl mx-auto px-4 py-6">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex-shrink-0">
            <label htmlFor="category-filter" className="sr-only">Filter by category</label>
            <select
              id="category-filter"
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {categories.map(cat => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>
          </div>
          <p className="text-sm text-gray-600">
            {filteredEvents.length} events found
          </p>
        </div>
      </div>

      <main className="max-w-6xl mx-auto px-4 pb-12">
        {eventsLoading ? (
          <div className="text-center py-12">
            <p className="text-xl text-gray-500">Loading events...</p>
          </div>
        ) : filteredEvents.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-xl text-gray-500">No events found</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredEvents.map(event => (
              <EventCard key={event.id} event={event} />
            ))}
          </div>
        )}
      </main>

      <LoginModal
        isOpen={loginOpen}
        onClose={() => setLoginOpen(false)}
        onLogin={handleLogin}
        loading={loading}
        error={error}
      />

      <SignupModal
        isOpen={signupOpen}
        onClose={() => setSignupOpen(false)}
        onSignup={handleSignup}
        loading={loading}
        error={error}
      />
    </div>
  );
};

export default HomePage;