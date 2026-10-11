import React from 'react';
import { Link } from 'react-router-dom';
import { Clock, User } from 'lucide-react';

export default function CourseCard({ course }) {
  const isFree = course.free || Number(course.price) === 0;
  const shortDescription =
    course.description && course.description.length > 130
      ? `${course.description.slice(0, 130)}...`
      : course.description;

  return (
    <div className="card course-card">
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
          <span className="badge badge-accent">{course.category}</span>
          <span className={`badge ${isFree ? 'badge-free' : 'badge-paid'}`}>
            {isFree ? 'Free' : `Paid (₹${Number(course.price).toFixed(2)})`}
          </span>
        </div>

        <h3 className="course-title">{course.title}</h3>
        <p className="course-instructor">
          <User size={13} style={{ verticalAlign: 'middle', marginRight: 4 }} />
          Instructor: {course.instructor}
        </p>
        <p className="course-desc">{shortDescription}</p>
      </div>

      <div className="course-footer">
        <span>
          <Clock size={13} style={{ verticalAlign: 'middle', marginRight: 4 }} />
          {course.duration}
        </span>
        <Link to={`/courses/${course.id}`} className="btn btn-primary btn-sm">
          View Details
        </Link>
      </div>
    </div>
  );
}
