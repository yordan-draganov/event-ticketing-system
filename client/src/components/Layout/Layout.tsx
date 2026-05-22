import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { Header } from '../Header/Header';
import { LoginModal } from '../LoginModal/LoginModal';
import { SignupModal } from '../SignupModal/SignupModal';
import { ProfileModal } from '../ProfileModal/ProfileModal';
import { ChangeNameModal } from '../ChangeNameModal/ChangeNameModal';
import { ChangePasswordModal } from '../ChangePasswordModal/ChangePasswordModal';
import { useAuth } from '../../hooks/useAuth';
import { ApiClient } from '../../services/api.client';
import type { LoginRequest, SignupRequest, UserDTO } from '../../generated/api';

export const Layout: React.FC = () => {
  const navigate = useNavigate();

  const [loginOpen, setLoginOpen] = useState(false);
  const [signupOpen, setSignupOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [changeNameOpen, setChangeNameOpen] = useState(false);
  const [changePasswordOpen, setChangePasswordOpen] = useState(false);

  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userName, setUserName] = useState('');
  const [userEmail, setUserEmail] = useState('');
  const [userId, setUserId] = useState('');
  const [userRole, setUserRole] = useState('');

  const { 
    login, 
    signup, 
    logout, 
    changeName, 
    changePassword, 
    loading, 
    error, 
    success, 
    setSuccess, 
    setError 
  } = useAuth();

  const clearUser = () => {
    setIsAuthenticated(false);
    setUserName('');
    setUserEmail('');
    setUserId('');
    setUserRole('');
  };

  const applyUser = (user: UserDTO) => {
    setIsAuthenticated(true);
    setUserName(user.name || '');
    setUserEmail(user.email || '');
    setUserId(user.id || '');
    setUserRole(user.role || 'user');
  };

  const refreshCurrentUser = async () => {
    const user = await ApiClient.getCurrentUser();
    applyUser(user);
  };

  useEffect(() => {
    let cancelled = false;

    const loadCurrentUser = async () => {
      try {
        const user = await ApiClient.getCurrentUser();
        if (!cancelled) {
          applyUser(user);
        }
      } catch {
        if (!cancelled) {
          clearUser();
        }
      }
    };

    loadCurrentUser();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleLogin = async (data: LoginRequest) => {
    const result = await login(data);
    if (result) {
      setLoginOpen(false);
      await refreshCurrentUser();
    }
  };

  const handleSignup = async (data: SignupRequest) => {
    const result = await signup(data);
    if (result) {
      setSignupOpen(false);
      await refreshCurrentUser();
    }
  };

  const handleLogout = async () => {
    await logout();
    clearUser();
    navigate('/');
  };

  const handleChangeName = async (newName: string) => {
    const result = await changeName(newName);
    if (result) {
      setChangeNameOpen(false);
      await refreshCurrentUser();
    }
  };

  const handleChangePassword = async (oldPassword: string, newPassword: string) => {
    const result = await changePassword(oldPassword, newPassword);
    if (result) {
      setChangePasswordOpen(false);
      clearUser();
      setTimeout(() => {
        setLoginOpen(true);
      }, 1000);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header 
        isAuthenticated={isAuthenticated}
        userName={userName}
        userEmail={userEmail}
        userRole={userRole}
        onLoginClick={() => setLoginOpen(true)} 
        onSignupClick={() => setSignupOpen(true)}
        onProfileClick={() => setProfileOpen(true)}
        onChangeNameClick={() => {
          setChangeNameOpen(true);
          setError('');
        }}
        onChangePasswordClick={() => {
          setChangePasswordOpen(true);
          setError('');
        }}
        onLogoutClick={handleLogout}
        onHomeClick={() => navigate('/')}
        onMyTicketsClick={() => navigate('/my-tickets')}
        onAdminDashboardClick={() => navigate('/admin/dashboard')}
      />

      {success && (
        <div className="max-w-7xl mx-auto px-4 pt-4">
          <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-lg flex justify-between items-center">
            <span>{success}</span>
            <button
              type="button"
              aria-label="Dismiss success message"
              onClick={() => setSuccess('')}
              className="text-xl leading-none text-green-600 hover:text-green-800"
            >
              x
            </button>
          </div>
        </div>
      )}

      {error && (
        <div className="max-w-7xl mx-auto px-4 pt-4">
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg flex justify-between items-center">
            <span>{error}</span>
            <button
              type="button"
              aria-label="Dismiss error message"
              onClick={() => setError('')}
              className="text-xl leading-none text-red-600 hover:text-red-800"
            >
              x
            </button>
          </div>
        </div>
      )}

      <Outlet />

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

      <ProfileModal
        isOpen={profileOpen}
        onClose={() => setProfileOpen(false)}
        userName={userName}
        userEmail={userEmail}
        userId={userId}
        userRole={userRole}
      />

      <ChangeNameModal
        isOpen={changeNameOpen}
        currentName={userName}
        onClose={() => setChangeNameOpen(false)}
        onChangeName={handleChangeName}
        loading={loading}
        error={error}
      />

      <ChangePasswordModal
        isOpen={changePasswordOpen}
        onClose={() => setChangePasswordOpen(false)}
        onChangePassword={handleChangePassword}
        loading={loading}
        error={error}
      />
    </div>
  );
};
