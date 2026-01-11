import { useState } from 'react';
import { ApiClient } from '../services/api.client';
import type { LoginRequest, SignupRequest } from '../generated/api';

export const useAuth = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const login = async (data: LoginRequest) => {
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      const response = await ApiClient.login(data);
      setSuccess(response.message || 'Login successful!');
      return true;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Login failed';
      setError(errorMessage);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const signup = async (data: SignupRequest) => {
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      const response = await ApiClient.signup(data);
      setSuccess(response.message || 'Account created successfully!');
      return true;
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Signup failed';
      setError(errorMessage);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    try {
      await ApiClient.logout();
      setSuccess('Logged out successfully');
      return true;
    } catch (err: any) {
      console.error('Logout error:', err);
      localStorage.removeItem('token');
      localStorage.removeItem('userName');
      localStorage.removeItem('userId');
      return true;
    }
  };

  return { 
    login, 
    signup, 
    logout, 
    loading, 
    error, 
    success, 
    setSuccess,
    setError 
  };
};