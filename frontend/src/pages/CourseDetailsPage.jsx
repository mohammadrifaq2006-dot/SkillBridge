import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { CheckCircle2, Clock, Layers, RefreshCw, User } from 'lucide-react';
import axiosClient, { extractApiError } from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';

export default function CourseDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [course, setCourse] = useState(null);
  const [existingEnrollment, setExistingEnrollment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const loadCourseDetails = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const courseRes = await axiosClient.get(`/courses/${id}`);
      setCourse(courseRes.data);

      if (isAuthenticated) {
        const myEnrollmentsRes = await axiosClient.get('/enrollments/my');
        const list = Array.isArray(myEnrollmentsRes.data) ? myEnrollmentsRes.data : [];
        const match = list.find((enr) => String(enr.courseId) === String(id));
        setExistingEnrollment(match || null);
      }
    } catch (err) {
      setError(extractApiError(err, 'Course details could not be loaded.'));
    } finally {
      setLoading(false);
    }
  }, [id, isAuthenticated]);

  useEffect(() => {
    loadCourseDetails();
  }, [loadCourseDetails]);

  const handleEnroll = async () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/courses/${id}` } } });
      return;
    }
    try {
      setEnrolling(true);
      setError('');
      setSuccessMsg('');
      const res = await axiosClient.post(`/enrollments/course/${id}`);
      setExistingEnrollment(res.data);
      setSuccessMsg('You are now enrolled in this course! Your progress record has been created.');
    } catch (err) {
      setError(extractApiError(err, 'Failed to enroll in this course.'));
    } finally {
      setEnrolling(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading course details..." />;
  }

  if (!course) {
    return (
      <div className="card">
        <div className="alert alert-error">
          <span>{error || 'Course not found.'}</span>
          <button type="button" className="btn btn-secondary btn-sm" onClick={loadCourseDetails}>
            <RefreshCw size={14} />
            Retry
          </button>
        </div>
        <Link to="/courses" className="btn btn-secondary">
          &larr; Back to Course Catalog
        </Link>
      </div>
    );
  }

  const isFree = course.free || Number(course.price) === 0;
  const difficulty = course.difficulty || 'Intermediate';
  const syllabusLines = (course.syllabus || '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);

  return (
    <div>
      <div style={{ marginBottom: 18 }}>
        <Link to="/courses" className="btn btn-secondary btn-sm">
          &larr; Back to Course Catalog
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {successMsg && <div className="alert alert-success">{successMsg}</div>}

      <div className="card section" style={{ borderTop: '4px solid var(--royal-blue)' }}>
        {course.imageUrl && (
          <img
            src={course.imageUrl}
            alt={course.title}
            loading="lazy"
            style={{
              width: '100%',
              maxHeight: 260,
              objectFit: 'cover',
              borderRadius: 8,
              marginBottom: 18,
              border: '1px solid var(--border-light)',
            }}
          />
        )}

        <div className="course-meta-row" style={{ marginBottom: 14 }}>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <span className="badge badge-accent">{course.category}</span>
            <span className="badge">
              <Layers size={12} />
              {difficulty}
            </span>
          </div>
          <span className={`badge ${isFree ? 'badge-free' : 'badge-paid'}`}>
            {isFree ? 'Free Course' : `Paid Course (₹${Number(course.price).toFixed(2)})`}
          </span>
        </div>

        <h1 className="page-title">{course.title}</h1>
        <div
          style={{
            display: 'flex',
            gap: 20,
            flexWrap: 'wrap',
            color: 'var(--text-muted)',
            marginBottom: 20,
            fontSize: 14,
          }}
        >
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
            <User size={15} />
            <strong>Instructor:</strong> {course.instructor}
          </span>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
            <Clock size={15} />
            <strong>Duration:</strong> {course.duration}
          </span>
        </div>

        <h3 style={{ fontSize: 17, color: 'var(--navy)', marginBottom: 8 }}>Course Overview</h3>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 24 }}>{course.description}</p>

        <div className="grid-2" style={{ marginBottom: 24 }}>
          <div className="card" style={{ backgroundColor: 'var(--bg-light)' }}>
            <h3 style={{ fontSize: 17, color: 'var(--navy)', marginBottom: 12 }}>
              Course Syllabus
            </h3>
            {syllabusLines.length > 0 ? (
              <ul style={{ listStyle: 'none', padding: 0 }}>
                {syllabusLines.map((item, idx) => (
                  <li
                    key={idx}
                    style={{
                      display: 'flex',
                      alignItems: 'flex-start',
                      gap: 8,
                      marginBottom: 10,
                      color: 'var(--text-secondary)',
                      fontSize: 14,
                    }}
                  >
                    <CheckCircle2
                      size={16}
                      style={{ color: 'var(--teal-dark)', flexShrink: 0, marginTop: 3 }}
                    />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p style={{ color: 'var(--text-secondary)' }}>{course.syllabus}</p>
            )}
          </div>

          <div className="card" style={{ backgroundColor: 'var(--bg-light)' }}>
            <h3 style={{ fontSize: 17, color: 'var(--navy)', marginBottom: 12 }}>
              Prerequisites & Academic Details
            </h3>
            <p style={{ marginBottom: 14, color: 'var(--text-secondary)', fontSize: 14 }}>
              <strong>Prerequisites:</strong> {course.prerequisites}
            </p>
            <p style={{ marginBottom: 10, color: 'var(--text-secondary)', fontSize: 14 }}>
              <strong>Difficulty Level:</strong> {difficulty}
            </p>
            <p style={{ marginBottom: 10, color: 'var(--text-secondary)', fontSize: 14 }}>
              <strong>Lead Instructor:</strong> {course.instructor}
            </p>
            <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>
              Estimated Study Duration: {course.duration}
            </p>
          </div>
        </div>

        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: 12,
            paddingTop: 18,
            borderTop: '1px solid var(--border-light)',
          }}
        >
          {existingEnrollment ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: 14, flexWrap: 'wrap' }}>
              <span className="badge badge-teal">
                Enrolled ({existingEnrollment.completionPercentage}% Completed)
              </span>
              <Link to="/dashboard" className="btn btn-primary">
                Go to Learner Dashboard
              </Link>
            </div>
          ) : (
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleEnroll}
              disabled={enrolling}
            >
              {enrolling
                ? 'Enrolling...'
                : isAuthenticated
                  ? 'Enroll in Course'
                  : 'Log In to Enroll'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
