import { Configuration } from '../generated/api';

export const getApiConfig = (): Configuration => {
  return new Configuration({
    basePath: 'http://localhost:8081',
    credentials: 'include', 
    headers: {
      'Content-Type': 'application/json',
    },
  });
};