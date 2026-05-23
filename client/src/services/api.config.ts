import { Configuration, UsersApi } from '../generated/api';

const API_BASE_PATH = import.meta.env.VITE_API_BASE_URL || '';

const shouldRefresh = (url: string): boolean => {
  return !url.includes('/api/users/login')
    && !url.includes('/api/users/signup')
    && !url.includes('/api/users/logout')
    && !url.includes('/api/users/refresh');
};

export const getApiConfig = (): Configuration => {
  return new Configuration({
    basePath: API_BASE_PATH,
    credentials: 'include', 
    headers: {
      'Content-Type': 'application/json',
    },
    middleware: [
      {
        post: async ({ url, init, response }) => {
          if (response.status !== 401 || !shouldRefresh(url)) {
            return response;
          }

          try {
            const refreshApi = new UsersApi(getRefreshApiConfig());
            await refreshApi.refresh();
          } catch {
            return response;
          }

          return fetch(url, {
            ...init,
            credentials: 'include',
          });
        },
      },
    ],
  });
};

const getRefreshApiConfig = (): Configuration => {
  return new Configuration({
    basePath: API_BASE_PATH,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
  });
};