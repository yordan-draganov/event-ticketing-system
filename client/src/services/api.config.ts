import { Configuration } from '../generated/api';

export const getApiConfig = (): Configuration => {
  return new Configuration({
    basePath: import.meta.env.VITE_API_BASE_URL || '',
    credentials: 'include', 
    headers: {
      'Content-Type': 'application/json',
    },
  });
};