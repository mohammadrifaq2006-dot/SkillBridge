import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowRight, BookOpen, RefreshCw, Sparkles } from 'lucide-react';
import axiosClient, { extractApiError } from '../api/axiosClient';
import CourseCard from '../components/CourseCard';
import LoadingSpinner from '../components/LoadingSpinner';

export default function HomePage() {
  const [courses, setCourses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const fetchHomeData = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const [coursesRes, categoriesRes] = await Promise.all([
        axiosClient.get('/courses'),
        axiosClient.get('/courses/categories'),
      ]);
      setCourses(Array.isArray(coursesRes.data) ? coursesRes.data : []);
      setCategories(Array.isArray(categoriesRes.data) ? categoriesRes.data : []);
    } catch (err) {
      setError(
        extractApiError(
          err,
          'Unable to load courses from the server. Please check your connection and try again.'
        )
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHomeData();
  }, [fetchHomeData]);

  const featuredCourses = courses.slice(0, 6);

  return (
    <div>
      {/* Hero Banner */}
      <section className="hero-banner">
        <div className="hero-eyebrow">
          <BookOpen size={14} />
          <span>Structured Technical Education</span>
        </div>
        <h1 className="hero-title">
          Build Practical Software Engineering Skills with SkillBridge
        </h1>
        <p className="hero-subtitle">
          Explore structured computer science and software development courses with transparent
          syllabi, track module-by-module completion on your personal dashboard, and earn
          downloadable completion certificates.
        </p>
        <div className="hero-actions">
          <Link to="/courses" className="btn btn-teal">
            Browse Course Catalog
            <ArrowRight size={16} />
          </Link>
          <Link to="/chatbot" className="btn btn-outline-light">
            <Sparkles size={16} />
            Ask AI Learning Assistant
          </Link>
        </div>
      </section>

      {/* Learning Categories Section */}
      <section className="section">
        <div className="section-header">
          <div>
            <h2>Subject Categories</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>
              Select a domain to filter courses in the catalog
            </p>
          </div>
        </div>
        {categories.length === 0 && !loading && !error ? (
          <p style={{ color: 'var(--text-muted)' }}>
            No published categories are currently available.
          </p>
        ) : (
          <div className="category-list">
            {categories.map((cat) => (
              <button
                key={cat}
                type="button"
                className="category-chip"
                onClick={() => navigate(`/courses?category=${encodeURIComponent(cat)}`)}
              >
                {cat}
              </button>
            ))}
          </div>
        )}
      </section>

      {/* Available Courses Section */}
      <section className="section">
        <div className="section-header">
          <div>
            <h2>Published Courses</h2>
            <p style={{ color: 'var(--text-muted)', fontSize: 14 }}>
              Loaded directly from the SkillBridge MySQL catalog
            </p>
          </div>
          <Link to="/courses" className="btn btn-secondary btn-sm">
            View Full Catalog &rarr;
          </Link>
        </div>

        {loading && <LoadingSpinner message="Loading published courses from the database..." />}

        {error && (
          <div className="alert alert-error">
            <span>{error}</span>
            <button type="button" className="btn btn-secondary btn-sm" onClick={fetchHomeData}>
              <RefreshCw size={14} />
              Retry
            </button>
          </div>
        )}

        {!loading && !error && featuredCourses.length === 0 && (
          <div className="empty-state">
            <h3 style={{ color: 'var(--navy)', marginBottom: 6 }}>No Courses Currently Published</h3>
            <p>There are currently no published courses in the database.</p>
          </div>
        )}

        {!loading && !error && featuredCourses.length > 0 && (
          <div className="grid-3">
            {featuredCourses.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
        )}
      </section>

      {/* About Section */}
      <section className="section card" style={{ borderLeft: '4px solid var(--teal)' }}>
        <h2 style={{ marginBottom: 10, color: 'var(--navy)' }}>How SkillBridge Works</h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: 16 }}>
          SkillBridge connects foundational engineering coursework with hands-on software practice.
          Browse any course syllabus publicly, create a learner account to enroll, update your module
          completion percentage as you study, and download a verified completion certificate when you
          reach 100%.
        </p>
        <Link to="/about" className="btn btn-secondary btn-sm">
          Learn More & Contact Support &rarr;
        </Link>
      </section>
    </div>
  );
}
