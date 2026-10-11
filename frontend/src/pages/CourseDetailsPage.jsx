import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
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

  useEffect(() => {
    let isMounted = true;
    async function loadCourseDetails() {
      try {
        setLoading(true);
        const courseRes = await axiosClient.get(`/courses/${id}`);
        if (isMounted) {
          setCourse(courseRes.data);
          setError('');
        }
        if (isAuthenticated) {
          const myEnrollmentsRes = await axiosClient.get('/enrollments/my');
          if (isMounted) {
            const match = myEnrollmentsRes.data.find(
              (enr) => String(enr.courseId) === String(id)
            );
            setExistingEnrollment(match || null);
          }
        }
      } catch (err) {
        if (isMounted) {
          setError(err.response?.data?.message || 'Course could not be loaded.');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }
    loadCourseDetails();
    return () => {
      isMounted = false;
    };
  }, [id, isAuthenticated]);

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
      setSuccessMsg('You have been enrolled in this course! Your enrollment is saved in MySQL.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to enroll in this course.');
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
        <div className="alert alert-error">{error || 'Course not found.'}</div>
        <Link to="/courses" className="btn btn-secondary">
          &larr; Back to Catalog
        </Link>
      </div>
    );
  }

  const isFree = course.free || Number(course.price) === 0;
  const syllabusLines = (course.syllabus || '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Link to="/courses" className="btn btn-secondary btn-sm">
          &larr; Back to Course Catalog
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {successMsg && <div className="alert alert-success">{successMsg}</div>}

      <div className="card section">
        {course.imageUrl && (
          <img
            src={course.imageUrl}
            alt={course.title}
            loading="lazy"
            style={{
              width: '100%',
              maxHeight: 240,
              objectFit: 'cover',
              borderRadius: 6,
              marginBottom: 16,
              border: '1px solid var(--border-light)',
            }}
          />
        )}

        <div className="course-meta-row" style={{ marginBottom: 12 }}>
          <span className="badge badge-accent">{course.category}</span>
          <span className={`badge ${isFree ? 'badge-free' : 'badge-paid'}`}>
            {isFree ? 'Free Course' : `Paid Course (₹${Number(course.price).toFixed(2)})`}
          </span>
        </div>

        <h1 className="page-title">{course.title}</h1>
        <p style={{ color: 'var(--text-grey)', marginBottom: 16, fontSize: 14 }}>
          <strong>Instructor:</strong> {course.instructor} &nbsp;|&nbsp;{' '}
          <strong>Duration:</strong> {course.duration}
        </p>

        <h3 style={{ fontSize: 16, marginBottom: 6 }}>Course Description</h3>
        <p style={{ color: 'var(--text-dark-grey)', marginBottom: 20 }}>{course.description}</p>

        <div className="grid-2" style={{ marginBottom: 20 }}>
          <div className="card" style={{ backgroundColor: 'var(--bg-subtle)' }}>
            <h3 style={{ fontSize: 16, marginBottom: 8 }}>Syllabus</h3>
            {syllabusLines.length > 0 ? (
              <ul style={{ paddingLeft: 18, color: 'var(--text-dark-grey)' }}>
                {syllabusLines.map((item, idx) => (
                  <li key={idx} style={{ marginBottom: 4 }}>
                    {item}
                  </li>
                ))}
              </ul>
            ) : (
              <p>{course.syllabus}</p>
            )}
          </div>

          <div className="card" style={{ backgroundColor: 'var(--bg-subtle)' }}>
            <h3 style={{ fontSize: 16, marginBottom: 8 }}>Prerequisites & Instructor Info</h3>
            <p style={{ marginBottom: 12, color: 'var(--text-dark-grey)' }}>
              <strong>Prerequisites:</strong> {course.prerequisites}
            </p>
            <p style={{ color: 'var(--text-dark-grey)' }}>
              <strong>Lead Instructor:</strong> {course.instructor}
            </p>
            <p style={{ color: 'var(--text-grey)', fontSize: 13, marginTop: 4 }}>
              Estimated Completion Time: {course.duration}
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
            paddingTop: 16,
            borderTop: '1px solid var(--border-light)',
          }}
        >
          {existingEnrollment ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
              <span className="badge badge-free">
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
