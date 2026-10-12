import React, { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Award, PlayCircle, RefreshCw } from 'lucide-react';
import axiosClient, { extractApiError } from '../api/axiosClient';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/LoadingSpinner';

export default function LearnerDashboardPage() {
  const { user } = useAuth();
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusMessage, setStatusMessage] = useState('');

  // Active enrollment for "Continue Learning" modal
  const [activeEnrollment, setActiveEnrollment] = useState(null);
  const [progressInput, setProgressInput] = useState(0);
  const [savingProgress, setSavingProgress] = useState(false);

  const fetchMyEnrollments = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const res = await axiosClient.get('/enrollments/my');
      setEnrollments(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(extractApiError(err, 'Unable to load your enrolled courses.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchMyEnrollments();
  }, [fetchMyEnrollments]);

  const openLearningModal = (enrollment) => {
    setActiveEnrollment(enrollment);
    setProgressInput(enrollment.completionPercentage || 0);
    setStatusMessage('');
  };

  const handleSaveProgress = async (e) => {
    e.preventDefault();
    if (!activeEnrollment || savingProgress) return;

    const numericProgress = Math.min(100, Math.max(0, Number(progressInput)));
    try {
      setSavingProgress(true);
      const res = await axiosClient.put(`/enrollments/${activeEnrollment.id}/progress`, {
        completionPercentage: numericProgress,
      });

      setEnrollments((prev) =>
        prev.map((item) => (item.id === res.data.id ? res.data : item))
      );
      setActiveEnrollment(null);
      setStatusMessage(
        numericProgress === 100
          ? `Congratulations! You have completed "${res.data.courseTitle}". Your certificate is now available for download.`
          : `Progress for "${res.data.courseTitle}" updated to ${numericProgress}%.`
      );
    } catch (err) {
      setError(extractApiError(err, 'Failed to update course progress.'));
    } finally {
      setSavingProgress(false);
    }
  };

  const handleDownloadCertificate = (enrollment) => {
    const canvas = document.createElement('canvas');
    canvas.width = 1000;
    canvas.height = 700;
    const ctx = canvas.getContext('2d');

    // White background
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Outer Deep Navy border & inner Teal border
    ctx.strokeStyle = '#14213D';
    ctx.lineWidth = 8;
    ctx.strokeRect(28, 28, 944, 644);

    ctx.strokeStyle = '#14B8A6';
    ctx.lineWidth = 2.5;
    ctx.strokeRect(42, 42, 916, 616);

    // Header
    ctx.fillStyle = '#14213D';
    ctx.textAlign = 'center';
    ctx.font = 'bold 28px Inter, Arial, sans-serif';
    ctx.fillText('SKILLBRIDGE – ONLINE LEARNING PLATFORM', 500, 125);

    ctx.fillStyle = '#2563EB';
    ctx.font = 'bold 38px Georgia, serif';
    ctx.fillText('CERTIFICATE OF COMPLETION', 500, 195);

    ctx.fillStyle = '#334155';
    ctx.font = '18px Inter, Arial, sans-serif';
    ctx.fillText('This is to certify that', 500, 260);

    // Student Name
    ctx.fillStyle = '#14213D';
    ctx.font = 'bold 34px Georgia, serif';
    ctx.fillText(user?.name || enrollment.userName || 'Learner', 500, 315);

    // Divider line under name
    ctx.beginPath();
    ctx.moveTo(280, 330);
    ctx.lineTo(720, 330);
    ctx.strokeStyle = '#14B8A6';
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.fillStyle = '#334155';
    ctx.font = '18px Inter, Arial, sans-serif';
    ctx.fillText('has successfully completed the online course', 500, 380);

    // Course Title
    ctx.fillStyle = '#2563EB';
    ctx.font = 'bold 28px Inter, Arial, sans-serif';
    ctx.fillText(enrollment.courseTitle, 500, 430);

    // Metadata
    const completionDate = enrollment.lastUpdated
      ? new Date(enrollment.lastUpdated).toLocaleDateString()
      : new Date().toLocaleDateString();

    ctx.fillStyle = '#14213D';
    ctx.font = '16px Inter, Arial, sans-serif';
    ctx.fillText(
      `Category: ${enrollment.courseCategory}  |  Duration: ${enrollment.courseDuration}`,
      500,
      480
    );

    // Footer signatures
    ctx.textAlign = 'left';
    ctx.fillStyle = '#14213D';
    ctx.font = 'bold 16px Inter, Arial, sans-serif';
    ctx.fillText(`Instructor: ${enrollment.courseInstructor}`, 100, 580);
    ctx.font = '14px Inter, Arial, sans-serif';
    ctx.fillStyle = '#64748B';
    ctx.fillText(`Certificate ID: SB-CERT-${enrollment.id}-${enrollment.courseId}`, 100, 605);

    ctx.textAlign = 'right';
    ctx.fillStyle = '#14213D';
    ctx.font = 'bold 16px Inter, Arial, sans-serif';
    ctx.fillText(`Completion Date: ${completionDate}`, 900, 580);
    ctx.font = '14px Inter, Arial, sans-serif';
    ctx.fillStyle = '#64748B';
    ctx.fillText('Verified by SkillBridge LMS', 900, 605);

    const dataUrl = canvas.toDataURL('image/png');
    const link = document.createElement('a');
    const safeTitle = (enrollment.courseTitle || 'Course')
      .replace(/[^a-zA-Z0-9]+/g, '_')
      .replace(/^_|_$/g, '');
    link.download = `SkillBridge_Certificate_${safeTitle}.png`;
    link.href = dataUrl;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h1 className="page-title">Learner Dashboard</h1>
          <p className="page-subtitle" style={{ marginBottom: 0 }}>
            Signed in as <strong>{user?.name}</strong> ({user?.email})
          </p>
        </div>
        <Link to="/courses" className="btn btn-primary">
          Browse More Courses
        </Link>
      </div>

      {error && (
        <div className="alert alert-error">
          <span>{error}</span>
          <button type="button" className="btn btn-secondary btn-sm" onClick={fetchMyEnrollments}>
            <RefreshCw size={14} />
            Retry
          </button>
        </div>
      )}
      {statusMessage && <div className="alert alert-success">{statusMessage}</div>}

      {loading && <LoadingSpinner message="Loading your enrolled courses..." />}

      {!loading && !error && enrollments.length === 0 && (
        <div className="empty-state">
          <h3 style={{ color: 'var(--navy)', marginBottom: 8 }}>No Course Enrollments Yet</h3>
          <p style={{ marginBottom: 18 }}>
            Your learner dashboard is currently empty. Browse the course catalog and enroll in a
            course to start tracking your progress.
          </p>
          <Link to="/courses" className="btn btn-primary">
            Explore Course Catalog
          </Link>
        </div>
      )}

      {!loading && enrollments.length > 0 && (
        <div className="grid-2">
          {enrollments.map((enrollment) => {
            const pct = enrollment.completionPercentage || 0;
            const isCompleted = pct >= 100 || enrollment.status === 'COMPLETED';

            return (
              <div
                key={enrollment.id}
                className="card"
                style={{
                  borderTop: `4px solid ${isCompleted ? 'var(--teal)' : 'var(--royal-blue)'}`,
                }}
              >
                <div className="course-meta-row">
                  <span className="badge badge-accent">{enrollment.courseCategory}</span>
                  <span className={`badge ${isCompleted ? 'badge-teal' : ''}`}>
                    {isCompleted ? 'COMPLETED' : 'IN PROGRESS'}
                  </span>
                </div>

                <h3 className="course-title" style={{ marginTop: 6 }}>
                  <Link to={`/courses/${enrollment.courseId}`}>{enrollment.courseTitle}</Link>
                </h3>
                <p className="course-instructor">
                  Instructor: {enrollment.courseInstructor} &nbsp;|&nbsp; Duration:{' '}
                  {enrollment.courseDuration}
                </p>

                <div style={{ margin: '14px 0' }}>
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      fontSize: 13,
                      fontWeight: 600,
                      color: 'var(--navy)',
                    }}
                  >
                    <span>Completion Progress</span>
                    <span>{pct}%</span>
                  </div>
                  <div className="progress-track">
                    <div
                      className={`progress-fill ${isCompleted ? 'completed' : ''}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                  <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                    Enrolled on:{' '}
                    {enrollment.enrollmentDate
                      ? new Date(enrollment.enrollmentDate).toLocaleDateString()
                      : 'N/A'}
                  </div>
                </div>

                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginTop: 14 }}>
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    onClick={() => openLearningModal(enrollment)}
                  >
                    <PlayCircle size={15} />
                    {isCompleted ? 'Review Course / Progress' : 'Continue Learning'}
                  </button>

                  {isCompleted && (
                    <button
                      type="button"
                      className="btn btn-teal btn-sm"
                      onClick={() => handleDownloadCertificate(enrollment)}
                    >
                      <Award size={15} />
                      Download Certificate
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Continue Learning & Progress Update Modal */}
      {activeEnrollment && (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-box">
            <div className="modal-header">
              <h3>Continue Learning: {activeEnrollment.courseTitle}</h3>
            </div>

            <div style={{ marginBottom: 16 }}>
              <h4 style={{ fontSize: 14, color: 'var(--navy)', marginBottom: 6 }}>
                Course Syllabus Modules
              </h4>
              <div
                style={{
                  backgroundColor: 'var(--bg-light)',
                  border: '1px solid var(--border-light)',
                  borderRadius: 8,
                  padding: 14,
                  fontSize: 13.5,
                  color: 'var(--text-secondary)',
                  whiteSpace: 'pre-line',
                }}
              >
                {activeEnrollment.courseSyllabus || 'Complete all modules to earn your certificate.'}
              </div>
            </div>

            <form onSubmit={handleSaveProgress}>
              <div className="form-group">
                <label htmlFor="progressRange" className="form-label">
                  Update Completion Percentage: <strong>{progressInput}%</strong>
                </label>
                <input
                  id="progressRange"
                  type="range"
                  min="0"
                  max="100"
                  step="5"
                  value={progressInput}
                  onChange={(e) => setProgressInput(Number(e.target.value))}
                  style={{ width: '100%', margin: '8px 0' }}
                />
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 8 }}>
                  {[25, 50, 75, 100].map((preset) => (
                    <button
                      key={preset}
                      type="button"
                      className="btn btn-secondary btn-sm"
                      onClick={() => setProgressInput(preset)}
                    >
                      Mark {preset}%
                    </button>
                  ))}
                </div>
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setActiveEnrollment(null)}
                  disabled={savingProgress}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={savingProgress}>
                  {savingProgress ? 'Saving...' : 'Save Progress'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
