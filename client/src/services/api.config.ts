import { Configuration } from '../generated/api';

export const apiConfig = new Configuration({
  basePath: 'http://localhost:8081',
  accessToken: () => {
    return localStorage.getItem('token') || '';
  },
  headers: {
    'Content-Type': 'application/json',
  },
});