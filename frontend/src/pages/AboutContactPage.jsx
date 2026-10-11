import React, { useState } from 'react';
import axiosClient from '../api/axiosClient';

const FAQ_ITEMS = [
  {
    question: 'How do I enroll in a course on SkillBridge?',
    answer:
      'Register for a learner account or log in, open the Course Catalog, select a course to view its syllabus and prerequisites, and click "Enroll in Course".',
  },
  {
    question: 'How is my learning progress tracked?',
    answer:
      'Every enrollment creates a progress record in MySQL. From your Learner Dashboard, click "Continue Learning" on any enrolled course to review modules and update your completion percentage.',
  },
  {
    question: 'When can I download a course certificate?',
    answer:
      'Once your completion percentage for an enrolled course reaches 100%, the "Download Certificate" button becomes available on your Learner Dashboard.',
  },
  {
    question: 'Are passwords stored securely?',
    answer:
      'Yes. Passwords are hashed using BCrypt before being saved in MySQL, and protected endpoints require a valid signed JSON Web Token (JWT).',
  },
];

export default function AboutContactPage() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: '',
  });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [savedInquiry, setSavedInquiry] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const validate = () => {
    const newErrors = {};
    if (!formData.name.trim() || formData.name.trim().length < 2) {
      newErrors.name = 'Name must be at least 2 characters.';
    }
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.email.trim() || !emailPattern.test(formData.email.trim())) {
      newErrors.email = 'Please enter a valid email address.';
    }
    if (!formData.subject.trim() || formData.subject.trim().length < 3) {
      newErrors.subject = 'Subject must be at least 3 characters.';
    }
    if (!formData.message.trim() || formData.message.trim().length < 10) {
      newErrors.message = 'Message must be at least 10 characters.';
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
    setServerError('');
    setSavedInquiry(null);
    if (!validate()) {
      return;
    }

    try {
      setSubmitting(true);
      const res = await axiosClient.post('/contact', {
        name: formData.name.trim(),
        email: formData.email.trim(),
        subject: formData.subject.trim(),
        message: formData.message.trim(),
      });
      // Only show success message after inquiry has actually been saved in MySQL
      if (res.status === 201 && res.data?.id) {
        setSavedInquiry(res.data);
        setFormData({ name: '', email: '', subject: '', message: '' });
      }
    } catch (err) {
      if (err.response?.data?.errors) {
        setErrors(err.response.data.errors);
      }
      setServerError(
        err.response?.data?.message || 'Failed to save your inquiry. Please try again.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      {/* Mission Statement */}
      <section className="section card">
        <h1 className="page-title">About SkillBridge</h1>
        <h3 style={{ fontSize: 16, marginBottom: 8, color: 'var(--text-dark-grey)' }}>
          Our Mission Statement
        </h3>
        <p style={{ color: 'var(--text-dark-grey)' }}>
          SkillBridge is an academic online learning platform built to make structured technical
          education accessible, transparent, and practical. Our mission is to provide engineering
          students with clear course syllabi, milestone-based progress tracking, and verifiable
          completion certificates within a clean, distraction-free learning environment.
        </p>
      </section>

      <div className="grid-2">
        {/* Basic FAQ Section */}
        <section className="section card">
          <h2 style={{ fontSize: 20, marginBottom: 16 }}>Frequently Asked Questions</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {FAQ_ITEMS.map((item, idx) => (
              <div
                key={idx}
                style={{
                  paddingBottom: 12,
                  borderBottom:
                    idx < FAQ_ITEMS.length - 1 ? '1px solid var(--border-light)' : 'none',
                }}
              >
                <h4 style={{ fontSize: 15, marginBottom: 4 }}>{item.question}</h4>
                <p style={{ fontSize: 14, color: 'var(--text-dark-grey)' }}>{item.answer}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Contact Form Section */}
        <section className="section card">
          <h2 style={{ fontSize: 20, marginBottom: 8 }}>Contact Us</h2>
          <p className="page-subtitle">
            Have a question about a course or need support? Submit an inquiry below.
          </p>

          {serverError && <div className="alert alert-error">{serverError}</div>}
          {savedInquiry && (
            <div className="alert alert-success">
              Thank you, <strong>{savedInquiry.name}</strong>! Your inquiry (Reference ID: #
              {savedInquiry.id}) has been saved in the database.
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            <div className="form-group">
              <label htmlFor="contact-name" className="form-label">
                Your Name *
              </label>
              <input
                id="contact-name"
                name="name"
                type="text"
                className="form-control"
                value={formData.name}
                onChange={handleChange}
                placeholder="Enter your name"
              />
              {errors.name && <div className="form-error">{errors.name}</div>}
            </div>

            <div className="form-group">
              <label htmlFor="contact-email" className="form-label">
                Email Address *
              </label>
              <input
                id="contact-email"
                name="email"
                type="email"
                className="form-control"
                value={formData.email}
                onChange={handleChange}
                placeholder="you@example.edu"
              />
              {errors.email && <div className="form-error">{errors.email}</div>}
            </div>

            <div className="form-group">
              <label htmlFor="contact-subject" className="form-label">
                Subject *
              </label>
              <input
                id="contact-subject"
                name="subject"
                type="text"
                className="form-control"
                value={formData.subject}
                onChange={handleChange}
                placeholder="Course inquiry or technical support"
              />
              {errors.subject && <div className="form-error">{errors.subject}</div>}
            </div>

            <div className="form-group">
              <label htmlFor="contact-message" className="form-label">
                Message *
              </label>
              <textarea
                id="contact-message"
                name="message"
                rows="4"
                className="form-control"
                value={formData.message}
                onChange={handleChange}
                placeholder="Write your question or message (minimum 10 characters)..."
              />
              {errors.message && <div className="form-error">{errors.message}</div>}
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              style={{ width: '100%' }}
              disabled={submitting}
            >
              {submitting ? 'Submitting Inquiry...' : 'Submit Inquiry'}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}
