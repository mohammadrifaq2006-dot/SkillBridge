import React from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { BookOpen, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          <span className="brand-icon-box">
            <BookOpen size={19} />
          </span>
          <span>SkillBridge</span>
        </Link>

        <nav className="navbar-links" aria-label="Primary Navigation">
          <NavLink to="/" end className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Home
          </NavLink>
          <NavLink to="/courses" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Course Catalog
          </NavLink>
          <NavLink to="/chatbot" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            AI Assistant
          </NavLink>
          <NavLink to="/about" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            About & Contact
          </NavLink>

          {isAuthenticated && (
            <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              Learner Dashboard
            </NavLink>
          )}

          {isAdmin && (
            <NavLink to="/admin" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              Admin Console
            </NavLink>
          )}
        </nav>

        <div className="navbar-user">
          {isAuthenticated ? (
            <>
              <span className="user-pill">
                <strong>{user.name}</strong>
                <span className="badge badge-teal" style={{ fontSize: 11, padding: '1px 7px' }}>
                  {user.role}
                </span>
              </span>
              <button
                type="button"
                onClick={handleLogout}
                className="btn btn-outline-light btn-sm"
                title="Log out of your account"
              >
                <LogOut size={14} />
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline-light btn-sm">
                Log In
              </Link>
              <Link to="/register" className="btn btn-teal btn-sm">
                Get Started
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
