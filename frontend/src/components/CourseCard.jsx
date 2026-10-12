import React from 'react';
import { Link } from 'react-router-dom';
import { Clock, Layers, User } from 'lucide-react';

export default function CourseCard({ course }) {
  const isFree = course.free || Number(course.price) === 0;
  const difficulty = course.difficulty || 'Intermediate';
  const shortDescription =
    course.description && course.description.length > 135
      ? `${course.description.slice(0, 135)}...`
      : course.description;

  return (
    <article className="card course-card">
      <div>
        {course.imageUrl && (
          <img
            src={course.imageUrl}
            alt={course.title}
            loading="lazy"
            className="course-card-thumb"
          />
        )}

        <div className="course-meta-row">
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            <span className="badge badge-accent">{course.category}</span>
            <span className="badge">
              <Layers size={11} />
              {difficulty}
            </span>
          </div>
          <span className={`badge ${isFree ? 'badge-free' : 'badge-paid'}`}>
            {isFree ? 'Free' : `Paid (₹${Number(course.price).toFixed(2)})`}
          </span>
        </div>

        <h3 className="course-title">{course.title}</h3>
        <p className="course-instructor">
          <User size={14} />
          <span>{course.instructor}</span>
        </p>
        <p className="course-desc">{shortDescription}</p>
      </div>

      <div className="course-footer">
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
          <Clock size={14} />
          {course.duration}
        </span>
        <Link to={`/courses/${course.id}`} className="btn btn-primary btn-sm">
          Explore Course
        </Link>
      </div>
    </article>
  );
}
