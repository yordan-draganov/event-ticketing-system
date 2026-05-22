import { useState } from 'react';
import { ApiClient } from '../services/api.client';
import type { LoginRequest, SignupRequest } from '../generated/api';
import { getErrorMessage } from '../utils/errorUtils';

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
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Login failed'));
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
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Signup failed'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setLoading(true);
    try {
      await ApiClient.logout();
      setSuccess('Logged out successfully');
      return true;
    } catch {
      setSuccess('Logged out successfully');
      return true;
    } finally {
      setLoading(false);
    }
  };

  const changeName = async (newName: string) => {
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      const response = await ApiClient.changeName(newName);
      setSuccess('Username changed successfully!');
      return response;
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Failed to change name'));
      return null;
    } finally {
      setLoading(false);
    }
  };

  const changePassword = async (oldPassword: string, newPassword: string) => {
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      await ApiClient.changePassword(oldPassword, newPassword);
      setSuccess('Password changed successfully! Please log in again.');
      return true;
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Failed to change password'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { 
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
  };
};
