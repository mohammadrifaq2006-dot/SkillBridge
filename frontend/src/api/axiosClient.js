import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Request interceptor to attach JWT Authorization header
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('skillbridge_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle expired or invalid JWT tokens
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const requestUrl = error.config?.url || '';
      const isAuthAttempt =
        requestUrl.includes('/auth/login') ||
        requestUrl.includes('/auth/register') ||
        requestUrl.includes('/auth/reset-password');

      if (!isAuthAttempt && localStorage.getItem('skillbridge_token')) {
        localStorage.removeItem('skillbridge_token');
        localStorage.removeItem('skillbridge_user');
        window.dispatchEvent(new CustomEvent('skillbridge:unauthorized'));
      }
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
