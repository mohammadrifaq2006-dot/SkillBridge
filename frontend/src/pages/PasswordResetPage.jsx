import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import axiosClient, { extractApiError } from '../api/axiosClient';

export default function PasswordResetPage() {
  const [formData, setFormData] = useState({
    email: '',
    name: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const validate = () => {
    const newErrors = {};
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email.trim() || !emailPattern.test(formData.email.trim())) {
      newErrors.email = 'Please enter your registered email address.';
    }
    if (!formData.name.trim()) {
      newErrors.name = 'Please enter your registered full name for account verification.';
    }
    if (!formData.newPassword || formData.newPassword.length < 6) {
      newErrors.newPassword = 'New password must be at least 6 characters long.';
    }
    if (formData.newPassword !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match.';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (submitting) return;
    setServerError('');
    setSuccessMsg('');
    if (!validate()) {
      return;
    }

    try {
      setSubmitting(true);
      const res = await axiosClient.post('/auth/reset-password', {
        email: formData.email.trim(),
        name: formData.name.trim(),
        newPassword: formData.newPassword,
        confirmPassword: formData.confirmPassword,
      });
      setSuccessMsg(res.data.message);
      setFormData({ email: '', name: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setServerError(
        extractApiError(err, 'Unable to reset password. Please check your account details.')
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="form-container">
      <div className="card auth-card">
        <h1 className="page-title">Reset Password</h1>
        <p className="page-subtitle">
          Verify your registered email and full name to set a new BCrypt-hashed password.
        </p>

        {serverError && <div className="alert alert-error">{serverError}</div>}
        {successMsg && (
          <div className="alert alert-success">
            <span>{successMsg}</span>
            <Link to="/login" className="btn btn-primary btn-sm">
              Proceed to Log In &rarr;
            </Link>
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="email" className="form-label">
              Registered Email Address
            </label>
            <input
              id="email"
              name="email"
              type="email"
              className="form-control"
              value={formData.email}
              onChange={handleChange}
              placeholder="student@example.edu"
            />
            {errors.email && <div className="form-error">{errors.email}</div>}
          </div>

          <div className="form-group">
            <label htmlFor="name" className="form-label">
              Registered Full Name (Verification)
            </label>
            <input
              id="name"
              name="name"
              type="text"
              className="form-control"
              value={formData.name}
              onChange={handleChange}
              placeholder="Enter your exact registered name"
            />
            {errors.name && <div className="form-error">{errors.name}</div>}
          </div>

          <div className="form-group">
            <label htmlFor="newPassword" className="form-label">
              New Password
            </label>
            <input
              id="newPassword"
              name="newPassword"
              type="password"
              className="form-control"
              value={formData.newPassword}
              onChange={handleChange}
              placeholder="At least 6 characters"
            />
            {errors.newPassword && <div className="form-error">{errors.newPassword}</div>}
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword" className="form-label">
              Confirm New Password
            </label>
            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              className="form-control"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Re-enter new password"
            />
            {errors.confirmPassword && <div className="form-error">{errors.confirmPassword}</div>}
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%' }}
            disabled={submitting}
          >
            {submitting ? 'Updating Password...' : 'Update Password'}
          </button>
        </form>

        <p style={{ marginTop: 18, fontSize: 14, textAlign: 'center', color: 'var(--text-muted)' }}>
          Remember your password? <Link to="/login">Back to Log In</Link>
        </p>
      </div>
    </div>
  );
}
