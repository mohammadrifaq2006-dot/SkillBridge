import axios from 'axios';

function resolveApiBaseUrl() {
  const configured = import.meta.env.VITE_API_BASE_URL;
  if (configured && String(configured).trim()) {
    const trimmed = String(configured).trim().replace(/\/+$/, '');
    if (trimmed === '/api' || trimmed.endsWith('/api')) {
      return trimmed;
    }
    return `${trimmed}/api`;
  }

  if (typeof window !== 'undefined') {
    const host = window.location.hostname;
    if (host === 'localhost' || host === '127.0.0.1') {
      return 'http://localhost:8080/api';
    }
  }

  return 'https://skillbridge-backend-api.onrender.com/api';
}

export const API_BASE_URL = resolveApiBaseUrl();

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  timeout: 45000,
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

export function extractApiError(error, fallbackMessage = 'An unexpected error occurred.') {
  if (!error) {
    return fallbackMessage;
  }
  if (error.response) {
    const data = error.response.data;
    if (data && typeof data.message === 'string' && data.message.trim()) {
      return data.message;
    }
    if (error.response.status === 401) {
      return 'Invalid credentials or expired session. Please log in again.';
    }
    if (error.response.status === 403) {
      return 'Access denied: you do not have permission for this action.';
    }
    if (error.response.status === 404) {
      return 'The requested resource was not found on the server.';
    }
    if (error.response.status === 429) {
      return 'Too many requests. Please wait a moment and try again.';
    }
  }
  if (error.code === 'ECONNABORTED') {
    return 'The server took too long to respond (it may be waking up from idle). Please click Retry.';
  }
  return 'Could not connect to the SkillBridge backend server. Please check your connection and try again.';
}

export default axiosClient;
