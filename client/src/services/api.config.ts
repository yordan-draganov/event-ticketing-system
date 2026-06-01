import { Configuration, UsersApi } from '../generated/api';

const API_BASE_PATH = import.meta.env.VITE_API_BASE_URL || '';

const shouldRefresh = (url: string): boolean => {
  return !url.includes('/api/users/login')
    && !url.includes('/api/users/signup')
    && !url.includes('/api/users/logout')
    && !url.includes('/api/users/refresh');
};

const isCurrentUserLookup = (url: string): boolean => {
  return url.includes('/api/users/me');
};

const shouldAttemptRefresh = (url: string, response: Response): boolean => {
  if (!shouldRefresh(url)) {
    return false;
  }

  return response.status === 401
    || response.status === 403
    || (response.status === 204 && isCurrentUserLookup(url));
};

const errorFromResponse = async (response: Response): Promise<Error> => {
  try {
    const body = await response.clone().json();
    if (typeof body?.message === 'string') {
      return new Error(body.message);
    }
  } catch {
    return new Error(response.statusText || 'Request failed');
  }

  return new Error(response.statusText || 'Request failed');
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
          let finalResponse = response;

          if (shouldAttemptRefresh(url, response)) {
            try {
              const refreshApi = new UsersApi(getRefreshApiConfig());
              await refreshApi.refresh();
              finalResponse = await fetch(url, {
                ...init,
                credentials: 'include',
              });
            } catch {
              finalResponse = response;
            }
          }

          if (!finalResponse.ok) {
            throw await errorFromResponse(finalResponse);
          }

          return finalResponse;
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
