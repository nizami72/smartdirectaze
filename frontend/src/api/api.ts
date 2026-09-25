import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Not logged in or the session expired: go to the login page.
// Login/registration requests handle their own 401 (wrong password etc.).
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthRequest = error.config?.url?.includes('/api/v1/auth/');
    const onAuthPage = ['/login', '/register'].includes(window.location.pathname);
    if (error.response?.status === 401 && !isAuthRequest && !onAuthPage) {
      window.location.assign('/login');
    }
    return Promise.reject(error);
  },
);

export default api;
