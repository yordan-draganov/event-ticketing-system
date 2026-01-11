import { Configuration } from '../generated/api';

export const getApiConfig = (): Configuration => {
  const token = localStorage.getItem('token');
  
  return new Configuration({
    basePath: 'http://localhost:8081',
    accessToken: token || undefined, 
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    },
  });
};