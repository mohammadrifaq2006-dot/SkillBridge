import React, { createContext, useContext, useEffect, useState } from 'react';
import axiosClient from '../api/axiosClient';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('skillbridge_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [token, setToken] = useState(() => localStorage.getItem('skillbridge_token') || null);
  const [loading, setLoading] = useState(Boolean(localStorage.getItem('skillbridge_token')));
  const [sessionMessage, setSessionMessage] = useState('');

  useEffect(() => {
    const handleUnauthorized = () => {
      setToken(null);
      setUser(null);
      setSessionMessage('Your session has expired. Please log in again.');
    };
    window.addEventListener('skillbridge:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('skillbridge:unauthorized', handleUnauthorized);
  }, []);

  useEffect(() => {
    async function verifySession() {
      if (!token) {
        setLoading(false);
        return;
      }
      try {
        const res = await axiosClient.get('/auth/me');
        setUser(res.data);
        localStorage.setItem('skillbridge_user', JSON.stringify(res.data));
      } catch {
        localStorage.removeItem('skillbridge_token');
        localStorage.removeItem('skillbridge_user');
        setToken(null);
        setUser(null);
      } finally {
        setLoading(false);
      }
    }
    verifySession();
  }, [token]);

  const login = async (email, password) => {
    setSessionMessage('');
    const response = await axiosClient.post('/auth/login', { email, password });
    const { token: jwtToken, user: userData } = response.data;
    localStorage.setItem('skillbridge_token', jwtToken);
    localStorage.setItem('skillbridge_user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return userData;
  };

  const register = async (name, email, password, confirmPassword) => {
    setSessionMessage('');
    const response = await axiosClient.post('/auth/register', {
      name,
      email,
      password,
      confirmPassword,
    });
    const { token: jwtToken, user: userData } = response.data;
    localStorage.setItem('skillbridge_token', jwtToken);
    localStorage.setItem('skillbridge_user', JSON.stringify(userData));
    setToken(jwtToken);
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem('skillbridge_token');
    localStorage.removeItem('skillbridge_user');
    setToken(null);
    setUser(null);
  };

  const value = {
    user,
    token,
    loading,
    sessionMessage,
    clearSessionMessage: () => setSessionMessage(''),
    isAuthenticated: Boolean(token && user),
    isAdmin: user?.role === 'ADMIN',
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
