import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
import CourseCard from '../components/CourseCard';
import LoadingSpinner from '../components/LoadingSpinner';

export default function HomePage() {
  const [courses, setCourses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    let isMounted = true;
    async function fetchHomeData() {
      try {
        setLoading(true);
        const [coursesRes, categoriesRes] = await Promise.all([
          axiosClient.get('/courses'),
          axiosClient.get('/courses/categories'),
        ]);
        if (isMounted) {
          setCourses(coursesRes.data);
          setCategories(categoriesRes.data);
          setError('');
        }
      } catch (err) {
        if (isMounted) {
          setError(err.response?.data?.message || 'Unable to load courses from the server.');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }
    fetchHomeData();
    return () => {
      isMounted = false;
    };
  }, []);

  const featuredCourses = courses.slice(0, 6);

  return (
    <div>
      {/* Hero / Introduction Section */}
      <section className="section card" style={{ padding: '28px 24px', backgroundColor: 'var(--bg-subtle)' }}>
        <h1 className="page-title" style={{ fontSize: 28 }}>
          SkillBridge – Online Learning Platform
        </h1>
        <p style={{ color: 'var(--text-dark-grey)', maxWidth: 740, marginBottom: 20, fontSize: 16 }}>
          SkillBridge is a structured online learning platform designed for students to explore technical courses,
          enroll in subjects of interest, track module-by-module progress, and download completion certificates.
        </p>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <Link to="/courses" className="btn btn-primary">
            Browse Courses
          </Link>
          <Link to="/chatbot" className="btn btn-secondary">
            Ask AI Learning Assistant
          </Link>
        </div>
      </section>

      {/* Learning Categories Section */}
      <section className="section">
        <div className="section-header">
          <h2>Learning Categories</h2>
        </div>
        {categories.length === 0 && !loading ? (
          <p style={{ color: 'var(--text-grey)' }}>No categories available yet.</p>
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
          <h2>Available Courses</h2>
          <Link to="/courses">View Full Catalog &rarr;</Link>
        </div>

        {loading && <LoadingSpinner message="Loading available courses..." />}
        {error && <div className="alert alert-error">{error}</div>}

        {!loading && !error && featuredCourses.length === 0 && (
          <div className="empty-state">
            <p>No courses are currently available in the catalog.</p>
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
      <section className="section card">
        <h2 style={{ marginBottom: 10 }}>About SkillBridge</h2>
        <p style={{ color: 'var(--text-dark-grey)', marginBottom: 12 }}>
          SkillBridge connects foundational computer science concepts with practical engineering skills. Students can
          register for an account, browse categorized courses with clear syllabi and prerequisites, track their own
          completion percentage on the Learner Dashboard, and download a verified completion certificate upon finishing a course.
        </p>
        <Link to="/about">Read More & Contact Us &rarr;</Link>
      </section>
    </div>
  );
}
