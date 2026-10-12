import React, { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { RefreshCw } from 'lucide-react';
import axiosClient, { extractApiError } from '../api/axiosClient';
import CourseCard from '../components/CourseCard';
import LoadingSpinner from '../components/LoadingSpinner';
import Pagination from '../components/Pagination';

const COURSES_PER_PAGE = 6;

export default function CourseCatalogPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialCategory = searchParams.get('category') || 'ALL';

  const [courses, setCourses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [selectedCategory, setSelectedCategory] = useState(initialCategory);
  const [selectedDifficulty, setSelectedDifficulty] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadCategories = useCallback(async () => {
    try {
      const res = await axiosClient.get('/courses/categories');
      setCategories(Array.isArray(res.data) ? res.data : []);
    } catch {
      // Categories will be retried when courses reload
    }
  }, []);

  const loadCourses = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const params = {};
      if (keyword.trim()) {
        params.keyword = keyword.trim();
      }
      if (selectedCategory && selectedCategory !== 'ALL') {
        params.category = selectedCategory;
      }
      const res = await axiosClient.get('/courses', { params });
      setCourses(Array.isArray(res.data) ? res.data : []);
      setCurrentPage(1);
    } catch (err) {
      setError(extractApiError(err, 'Failed to load courses from the server.'));
    } finally {
      setLoading(false);
    }
  }, [keyword, selectedCategory]);

  useEffect(() => {
    loadCategories();
  }, [loadCategories]);

  useEffect(() => {
    loadCourses();
  }, [loadCourses]);

  const handleCategoryChange = (categoryValue) => {
    setSelectedCategory(categoryValue);
    if (categoryValue === 'ALL') {
      setSearchParams({});
    } else {
      setSearchParams({ category: categoryValue });
    }
  };

  const handleClearFilters = () => {
    setKeyword('');
    setSelectedCategory('ALL');
    setSelectedDifficulty('ALL');
    setSearchParams({});
  };

  const filteredCourses = courses.filter((c) => {
    if (selectedDifficulty === 'ALL') return true;
    return (c.difficulty || 'Intermediate').toLowerCase() === selectedDifficulty.toLowerCase();
  });

  const totalPages = Math.ceil(filteredCourses.length / COURSES_PER_PAGE);
  const paginatedCourses = filteredCourses.slice(
    (currentPage - 1) * COURSES_PER_PAGE,
    currentPage * COURSES_PER_PAGE
  );

  const hasFilters =
    Boolean(keyword.trim()) || selectedCategory !== 'ALL' || selectedDifficulty !== 'ALL';

  return (
    <div>
      <h1 className="page-title">Course Catalog</h1>
      <p className="page-subtitle">
        Search by keyword or filter by subject category and difficulty level.
      </p>

      <div className="filter-bar">
        <input
          type="text"
          className="form-control search-input"
          placeholder="Search by course title, instructor, or topic..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          aria-label="Search courses"
        />

        <select
          className="form-control category-select"
          value={selectedCategory}
          onChange={(e) => handleCategoryChange(e.target.value)}
          aria-label="Filter by category"
        >
          <option value="ALL">All Categories</option>
          {categories.map((cat) => (
            <option key={cat} value={cat}>
              {cat}
            </option>
          ))}
        </select>

        <select
          className="form-control"
          style={{ minWidth: 170 }}
          value={selectedDifficulty}
          onChange={(e) => {
            setSelectedDifficulty(e.target.value);
            setCurrentPage(1);
          }}
          aria-label="Filter by difficulty"
        >
          <option value="ALL">All Difficulties</option>
          <option value="Beginner">Beginner</option>
          <option value="Intermediate">Intermediate</option>
          <option value="Advanced">Advanced</option>
        </select>

        {hasFilters && (
          <button type="button" className="btn btn-secondary" onClick={handleClearFilters}>
            Reset Filters
          </button>
        )}
      </div>

      {loading && <LoadingSpinner message="Loading published courses..." />}

      {error && (
        <div className="alert alert-error">
          <span>{error}</span>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => {
              loadCategories();
              loadCourses();
            }}
          >
            <RefreshCw size={14} />
            Retry
          </button>
        </div>
      )}

      {!loading && !error && filteredCourses.length === 0 && (
        <div className="empty-state">
          <h3 style={{ color: 'var(--navy)', marginBottom: 6 }}>
            {hasFilters ? 'No Matching Courses Found' : 'No Courses Currently Published'}
          </h3>
          <p style={{ marginBottom: hasFilters ? 16 : 0 }}>
            {hasFilters
              ? 'No courses match your current search keyword or filter selection.'
              : 'There are currently no published courses in the database.'}
          </p>
          {hasFilters && (
            <button type="button" className="btn btn-secondary btn-sm" onClick={handleClearFilters}>
              Clear All Filters
            </button>
          )}
        </div>
      )}

      {!loading && !error && paginatedCourses.length > 0 && (
        <>
          <div className="grid-3">
            {paginatedCourses.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={(page) => setCurrentPage(page)}
          />
        </>
      )}
    </div>
  );
}
