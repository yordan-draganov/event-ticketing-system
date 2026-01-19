import React from 'react';
import { UserMenu } from '../UserMenu/UserMenu';

interface HeaderProps {
  isAuthenticated: boolean;
  userName?: string;
  userEmail?: string;
  onLoginClick: () => void;
  onSignupClick: () => void;
  onProfileClick: () => void;
  onChangeNameClick: () => void;
  onChangePasswordClick: () => void;
  onLogoutClick: () => void;
  onHomeClick: () => void;
  onMyTicketsClick: () => void;
}

export const Header: React.FC<HeaderProps> = ({ 
  isAuthenticated, 
  userName,
  userEmail,
  onLoginClick, 
  onSignupClick,
  onProfileClick,
  onChangeNameClick,
  onChangePasswordClick,
  onLogoutClick,
  onHomeClick,
  onMyTicketsClick,
}) => {
  return (
    <header className="bg-white shadow-sm sticky top-0 z-40">
      <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center gap-8">
            <h1 
              onClick={onHomeClick}
              className="text-2xl font-bold text-blue-600 cursor-pointer hover:text-blue-700 transition"
            >
              Eventsly
            </h1>
            
            {isAuthenticated && (
              <nav className="hidden md:flex gap-6">
                <button
                  onClick={onHomeClick}
                  className="text-gray-700 hover:text-blue-600 transition font-medium"
                >
                  Events
                </button>
                <button
                  onClick={onMyTicketsClick}
                  className="text-gray-700 hover:text-blue-600 transition font-medium"
                >
                  My Tickets
                </button>
              </nav>
            )}
          </div>
          
          <div className="flex gap-3 items-center">
            {isAuthenticated ? (
              <UserMenu
                userName={userName || 'User'}
                userEmail={userEmail}
                onProfileClick={onProfileClick}
                onChangeNameClick={onChangeNameClick}
                onChangePasswordClick={onChangePasswordClick}
                onLogoutClick={onLogoutClick}
              />
            ) : (
              <>
                <button
                  onClick={onLoginClick}
                  className="px-4 py-2 text-blue-600 hover:bg-blue-50 rounded-lg transition"
                >
                  Login
                </button>
                <button
                  onClick={onSignupClick}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                >
                  Sign Up
                </button>
              </>
            )}
          </div>
        </div>
      </nav>
    </header>
  );
};