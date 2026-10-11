import React from 'react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-container">
        <div>
          <strong>SkillBridge – Online Learning Platform</strong>
          <p>BTech Technical Training Mini-Project | React, Spring Boot, and MySQL</p>
        </div>
        <div className="footer-links">
          <Link to="/courses">Course Catalog</Link>
          <Link to="/chatbot">AI Assistant</Link>
          <Link to="/about">About & Contact</Link>
        </div>
      </div>
    </footer>
  );
}
