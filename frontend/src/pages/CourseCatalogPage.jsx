import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import axiosClient from '../api/axiosClient';
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
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    axiosClient
      .get('/courses/categories')
      .then((res) => setCategories(res.data))
      .catch(() => {});
  }, []);

  useEffect(() => {
    let isMounted = true;
    async function loadCourses() {
      try {
        setLoading(true);
        const params = {};
        if (keyword.trim()) {
          params.keyword = keyword.trim();
        }
        if (selectedCategory && selectedCategory !== 'ALL') {
          params.category = selectedCategory;
        }
        const res = await axiosClient.get('/courses', { params });
        if (isMounted) {
          setCourses(res.data);
          setCurrentPage(1);
          setError('');
        }
      } catch (err) {
        if (isMounted) {
          setError(err.response?.data?.message || 'Failed to load courses.');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    }
    loadCourses();
    return () => {
      isMounted = false;
    };
  }, [keyword, selectedCategory]);

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
    setSearchParams({});
  };

  const totalPages = Math.ceil(courses.length / COURSES_PER_PAGE);
  const paginatedCourses = courses.slice(
    (currentPage - 1) * COURSES_PER_PAGE,
    currentPage * COURSES_PER_PAGE
  );

  return (
    <div>
      <h1 className="page-title">Course Catalog</h1>
      <p className="page-subtitle">
        Search by keyword or filter by subject category to find a course.
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

        {(keyword || selectedCategory !== 'ALL') && (
          <button type="button" className="btn btn-secondary" onClick={handleClearFilters}>
            Reset Filters
          </button>
        )}
      </div>

      {loading && <LoadingSpinner message="Loading courses..." />}
      {error && <div className="alert alert-error">{error}</div>}

      {!loading && !error && courses.length === 0 && (
        <div className="empty-state">
          <p>No courses match your current search or category filter.</p>
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
