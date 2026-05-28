import React from 'react';
import { UserMenu } from '../UserMenu/UserMenu';

interface HeaderProps {
  isAuthenticated: boolean;
  userName?: string;
  userEmail?: string;
  userRole?: string;
  onLoginClick: () => void;
  onSignupClick: () => void;
  onProfileClick: () => void;
  onChangeNameClick: () => void;
  onChangePasswordClick: () => void;
  onLogoutClick: () => void;
  onHomeClick: () => void;
  onMyTicketsClick: () => void;
  onAdminDashboardClick: () => void;
}

export const Header: React.FC<HeaderProps> = ({ 
  isAuthenticated, 
  userName,
  userEmail,
  userRole,
  onLoginClick, 
  onSignupClick,
  onProfileClick,
  onChangeNameClick,
  onChangePasswordClick,
  onLogoutClick,
  onHomeClick,
  onMyTicketsClick,
  onAdminDashboardClick,
}) => {
  const isAdmin = userRole === 'admin';

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
                {isAdmin && (
                  <button
                    onClick={onAdminDashboardClick}
                    className="text-gray-700 hover:text-blue-600 transition font-medium flex items-center gap-1"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                    Admin
                  </button>
                )}
              </nav>
            )}
          </div>
          
          <div className="flex gap-3 items-center">
            {isAuthenticated ? (
              <UserMenu
                userName={userName || 'User'}
                userEmail={userEmail}
                userRole={userRole}
                onProfileClick={onProfileClick}
                onChangeNameClick={onChangeNameClick}
                onChangePasswordClick={onChangePasswordClick}
                onLogoutClick={onLogoutClick}
                onAdminDashboardClick={isAdmin ? onAdminDashboardClick : undefined}
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

        {isAuthenticated && (
          <div className="md:hidden border-t border-gray-100 py-2">
            <div className="flex gap-2 overflow-x-auto">
              <button
                onClick={onHomeClick}
                className="whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition"
              >
                Events
              </button>
              <button
                onClick={onMyTicketsClick}
                className="whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition"
              >
                My Tickets
              </button>
              {isAdmin && (
                <button
                  onClick={onAdminDashboardClick}
                  className="whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium text-gray-700 hover:bg-blue-50 hover:text-blue-600 transition"
                >
                  Admin
                </button>
              )}
            </div>
          </div>
        )}
      </nav>
    </header>
  );
};
