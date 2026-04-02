import React, { useState, useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { Header } from '../Header/Header';
import { LoginModal } from '../LoginModal/LoginModal';
import { SignupModal } from '../SignupModal/SignupModal';
import { ProfileModal } from '../ProfileModal/ProfileModal';
import { ChangeNameModal } from '../ChangeNameModal/ChangeNameModal';
import { ChangePasswordModal } from '../ChangePasswordModal/ChangePasswordModal';
import { useAuth } from '../../hooks/useAuth';
import type { LoginRequest, SignupRequest } from '../../generated/api';

export const Layout: React.FC = () => {
  const navigate = useNavigate();

  const getStoredValue = (key: string) => {
    try {
      return localStorage.getItem(key);
    } catch (storageError) {
      console.error(`Failed to read ${key} from local storage:`, storageError);
      return null;
    }
  };

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

  useEffect(() => {
    try {
      const name = getStoredValue('userName');
      const email = getStoredValue('userEmail');
      const id = getStoredValue('userId');
      const role = getStoredValue('userRole');

      if (name) {
        setIsAuthenticated(true);
        setUserName(name);
        setUserEmail(email || '');
        setUserId(id || '');
        setUserRole(role || 'user');
      }
    } catch (error) {
      console.error('Failed to initialize authentication state:', error);
    }
  }, []);

  const handleLogin = async (data: LoginRequest) => {
    const result = await login(data);
    if (result) {
      setLoginOpen(false);
      setIsAuthenticated(true);
      setUserName(getStoredValue('userName') || '');
      setUserEmail(getStoredValue('userEmail') || '');
      setUserId(getStoredValue('userId') || '');
      setUserRole(getStoredValue('userRole') || 'user');
    }
  };

  const handleSignup = async (data: SignupRequest) => {
    const result = await signup(data);
    if (result) {
      setSignupOpen(false);
      setIsAuthenticated(true);
      setUserName(getStoredValue('userName') || '');
      setUserEmail(getStoredValue('userEmail') || '');
      setUserId(getStoredValue('userId') || '');
      setUserRole(getStoredValue('userRole') || 'user');
    }
  };

  const handleLogout = async () => {
    await logout();
    setIsAuthenticated(false);
    setUserName('');
    setUserEmail('');
    setUserId('');
    setUserRole('');
    navigate('/');
  };

  const handleChangeName = async (newName: string) => {
    const result = await changeName(newName);
    if (result) {
      setChangeNameOpen(false);
      setUserName(newName);
      const updatedName = getStoredValue('userName');
      if (updatedName) {
        setUserName(updatedName);
      }
    }
  };

  const handleChangePassword = async (oldPassword: string, newPassword: string) => {
    const result = await changePassword(oldPassword, newPassword);
    if (result) {
      setChangePasswordOpen(false);
      setIsAuthenticated(false);
      setUserName('');
      setUserEmail('');
      setUserId('');
      setUserRole('');
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
            <button onClick={() => setSuccess('')} className="text-green-600 hover:text-green-800">
              âœ•
            </button>
          </div>
        </div>
      )}

      {error && (
        <div className="max-w-7xl mx-auto px-4 pt-4">
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg flex justify-between items-center">
            <span>{error}</span>
            <button onClick={() => setError('')} className="text-red-600 hover:text-red-800">
              âœ•
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
