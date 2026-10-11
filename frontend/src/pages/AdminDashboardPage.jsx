import React, { useEffect, useState } from 'react';
import axiosClient from '../api/axiosClient';
import ConfirmModal from '../components/ConfirmModal';
import LoadingSpinner from '../components/LoadingSpinner';
import Pagination from '../components/Pagination';

const INITIAL_COURSE_FORM = {
  title: '',
  category: '',
  instructor: '',
  duration: '',
  price: '0',
  description: '',
  syllabus: '',
  prerequisites: 'None',
  imageUrl: '',
};

const ITEMS_PER_PAGE = 8;

export default function AdminDashboardPage() {
  const [activeTab, setActiveTab] = useState('courses');

  const [courses, setCourses] = useState([]);
  const [users, setUsers] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [inquiries, setInquiries] = useState([]);
  const [loading, setLoading] = useState(true);

  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Course Form State
  const [showForm, setShowForm] = useState(false);
  const [editingCourseId, setEditingCourseId] = useState(null);
  const [formData, setFormData] = useState(INITIAL_COURSE_FORM);
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  // Confirmation Modals
  const [courseToDelete, setCourseToDelete] = useState(null);
  const [pendingEditConfirm, setPendingEditConfirm] = useState(false);

  // Pagination
  const [coursePage, setCoursePage] = useState(1);

  const loadAdminData = async () => {
    try {
      setLoading(true);
      const [coursesRes, usersRes, enrollmentsRes, inquiriesRes] = await Promise.all([
        axiosClient.get('/courses'),
        axiosClient.get('/admin/users'),
        axiosClient.get('/admin/enrollments'),
        axiosClient.get('/admin/inquiries'),
      ]);
      setCourses(coursesRes.data);
      setUsers(usersRes.data);
      setEnrollments(enrollmentsRes.data);
      setInquiries(inquiriesRes.data);
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load administrative records.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAdminData();
  }, []);

  const validateCourseForm = () => {
    const errs = {};
    if (!formData.title.trim() || formData.title.trim().length < 3) {
      errs.title = 'Course title must be at least 3 characters.';
    }
    if (!formData.category.trim()) {
      errs.category = 'Category is required.';
    }
    if (!formData.instructor.trim()) {
      errs.instructor = 'Instructor name is required.';
    }
    if (!formData.duration.trim()) {
      errs.duration = 'Duration is required (e.g., 6 Weeks).';
    }
    if (formData.price === '' || isNaN(Number(formData.price)) || Number(formData.price) < 0) {
      errs.price = 'Price must be 0 or a positive number.';
    }
    if (!formData.description.trim() || formData.description.trim().length < 10) {
      errs.description = 'Description must be at least 10 characters.';
    }
    if (!formData.syllabus.trim() || formData.syllabus.trim().length < 10) {
      errs.syllabus = 'Syllabus must be at least 10 characters.';
    }
    if (!formData.prerequisites.trim()) {
      errs.prerequisites = 'Prerequisites are required (enter None if none).';
    }
    setFormErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleOpenCreateForm = () => {
    setEditingCourseId(null);
    setFormData(INITIAL_COURSE_FORM);
    setFormErrors({});
    setShowForm(true);
    setError('');
    setSuccessMsg('');
  };

  const handleOpenEditForm = (course) => {
    setEditingCourseId(course.id);
    setFormData({
      title: course.title || '',
      category: course.category || '',
      instructor: course.instructor || '',
      duration: course.duration || '',
      price: String(course.price ?? 0),
      description: course.description || '',
      syllabus: course.syllabus || '',
      prerequisites: course.prerequisites || 'None',
      imageUrl: course.imageUrl || '',
    });
    setFormErrors({});
    setShowForm(true);
    setError('');
    setSuccessMsg('');
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
    if (!validateCourseForm()) {
      return;
    }

    if (editingCourseId) {
      // Open edit confirmation modal before updating
      setPendingEditConfirm(true);
    } else {
      await executeSaveCourse();
    }
  };

  const executeSaveCourse = async () => {
    setPendingEditConfirm(false);
    const payload = {
      title: formData.title.trim(),
      category: formData.category.trim(),
      instructor: formData.instructor.trim(),
      duration: formData.duration.trim(),
      price: Number(formData.price),
      description: formData.description.trim(),
      syllabus: formData.syllabus.trim(),
      prerequisites: formData.prerequisites.trim(),
      imageUrl: formData.imageUrl.trim() || null,
    };

    try {
      setSubmitting(true);
      if (editingCourseId) {
        await axiosClient.put(`/courses/${editingCourseId}`, payload);
        setSuccessMsg(`Course "${payload.title}" updated in MySQL.`);
      } else {
        await axiosClient.post('/courses', payload);
        setSuccessMsg(`Course "${payload.title}" added to MySQL.`);
      }
      setShowForm(false);
      setEditingCourseId(null);
      setFormData(INITIAL_COURSE_FORM);
      await loadAdminData();
    } catch (err) {
      if (err.response?.data?.errors) {
        setFormErrors(err.response.data.errors);
      }
      setError(err.response?.data?.message || 'Failed to save course.');
    } finally {
      setSubmitting(false);
    }
  };

  const executeDeleteCourse = async () => {
    if (!courseToDelete) return;
    try {
      setError('');
      setSuccessMsg('');
      await axiosClient.delete(`/courses/${courseToDelete.id}`);
      setSuccessMsg(`Course "${courseToDelete.title}" has been deleted.`);
      setCourseToDelete(null);
      await loadAdminData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete course.');
      setCourseToDelete(null);
    }
  };

  const totalCoursePages = Math.ceil(courses.length / ITEMS_PER_PAGE);
  const paginatedCourses = courses.slice(
    (coursePage - 1) * ITEMS_PER_PAGE,
    coursePage * ITEMS_PER_PAGE
  );

  return (
    <div>
      <div className="section-header">
        <div>
          <h1 className="page-title">Course & Platform Management</h1>
          <p className="page-subtitle" style={{ marginBottom: 0 }}>
            Administrator dashboard for managing courses, viewing registered users, and monitoring enrollments.
          </p>
        </div>
        {activeTab === 'courses' && !showForm && (
          <button type="button" className="btn btn-primary" onClick={handleOpenCreateForm}>
            + Add New Course
          </button>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {successMsg && <div className="alert alert-success">{successMsg}</div>}

      <div className="tabs">
        <button
          type="button"
          className={`tab-btn ${activeTab === 'courses' ? 'active' : ''}`}
          onClick={() => setActiveTab('courses')}
        >
          Courses ({courses.length})
        </button>
        <button
          type="button"
          className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          Registered Users ({users.length})
        </button>
        <button
          type="button"
          className={`tab-btn ${activeTab === 'enrollments' ? 'active' : ''}`}
          onClick={() => setActiveTab('enrollments')}
        >
          Enrollments ({enrollments.length})
        </button>
        <button
          type="button"
          className={`tab-btn ${activeTab === 'inquiries' ? 'active' : ''}`}
          onClick={() => setActiveTab('inquiries')}
        >
          Contact Inquiries ({inquiries.length})
        </button>
      </div>

      {loading && <LoadingSpinner message="Loading records..." />}

      {/* Tab 1: Courses Management */}
      {!loading && activeTab === 'courses' && (
        <div>
          {showForm && (
            <div className="card section">
              <div className="section-header">
                <h3>{editingCourseId ? 'Edit Course' : 'Add New Course'}</h3>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => setShowForm(false)}
                >
                  Close Form
                </button>
              </div>

              <form onSubmit={handleFormSubmit} noValidate>
                <div className="grid-2">
                  <div className="form-group">
                    <label htmlFor="course-title" className="form-label">
                      Course Title *
                    </label>
                    <input
                      id="course-title"
                      type="text"
                      className="form-control"
                      value={formData.title}
                      onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    />
                    {formErrors.title && <div className="form-error">{formErrors.title}</div>}
                  </div>

                  <div className="form-group">
                    <label htmlFor="course-category" className="form-label">
                      Category *
                    </label>
                    <input
                      id="course-category"
                      type="text"
                      className="form-control"
                      placeholder="e.g., Web Development, Database Systems"
                      value={formData.category}
                      onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                    />
                    {formErrors.category && <div className="form-error">{formErrors.category}</div>}
                  </div>

                  <div className="form-group">
                    <label htmlFor="course-instructor" className="form-label">
                      Instructor *
                    </label>
                    <input
                      id="course-instructor"
                      type="text"
                      className="form-control"
                      value={formData.instructor}
                      onChange={(e) => setFormData({ ...formData, instructor: e.target.value })}
                    />
                    {formErrors.instructor && (
                      <div className="form-error">{formErrors.instructor}</div>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="course-duration" className="form-label">
                      Duration *
                    </label>
                    <input
                      id="course-duration"
                      type="text"
                      className="form-control"
                      placeholder="e.g., 6 Weeks"
                      value={formData.duration}
                      onChange={(e) => setFormData({ ...formData, duration: e.target.value })}
                    />
                    {formErrors.duration && <div className="form-error">{formErrors.duration}</div>}
                  </div>

                  <div className="form-group">
                    <label htmlFor="course-price" className="form-label">
                      Price in ₹ (Enter 0 for Free) *
                    </label>
                    <input
                      id="course-price"
                      type="number"
                      min="0"
                      step="0.01"
                      className="form-control"
                      value={formData.price}
                      onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                    />
                    {formErrors.price && <div className="form-error">{formErrors.price}</div>}
                  </div>

                  <div className="form-group">
                    <label htmlFor="course-prerequisites" className="form-label">
                      Prerequisites *
                    </label>
                    <input
                      id="course-prerequisites"
                      type="text"
                      className="form-control"
                      value={formData.prerequisites}
                      onChange={(e) => setFormData({ ...formData, prerequisites: e.target.value })}
                    />
                    {formErrors.prerequisites && (
                      <div className="form-error">{formErrors.prerequisites}</div>
                    )}
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="course-description" className="form-label">
                    Description *
                  </label>
                  <textarea
                    id="course-description"
                    rows="3"
                    className="form-control"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  />
                  {formErrors.description && (
                    <div className="form-error">{formErrors.description}</div>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="course-syllabus" className="form-label">
                    Syllabus (One module per line) *
                  </label>
                  <textarea
                    id="course-syllabus"
                    rows="4"
                    className="form-control"
                    placeholder="Module 1: Introduction&#10;Module 2: Core Concepts"
                    value={formData.syllabus}
                    onChange={(e) => setFormData({ ...formData, syllabus: e.target.value })}
                  />
                  {formErrors.syllabus && <div className="form-error">{formErrors.syllabus}</div>}
                </div>

                <div style={{ display: 'flex', gap: 10 }}>
                  <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting
                      ? 'Saving...'
                      : editingCourseId
                        ? 'Update Course'
                        : 'Create Course'}
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setShowForm(false)}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          {courses.length === 0 ? (
            <div className="empty-state">No courses found in the database.</div>
          ) : (
            <>
              <div className="table-wrapper">
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Title</th>
                      <th>Category</th>
                      <th>Instructor</th>
                      <th>Duration</th>
                      <th>Price</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginatedCourses.map((course) => (
                      <tr key={course.id}>
                        <td>{course.id}</td>
                        <td>
                          <strong>{course.title}</strong>
                        </td>
                        <td>{course.category}</td>
                        <td>{course.instructor}</td>
                        <td>{course.duration}</td>
                        <td>
                          {course.free || Number(course.price) === 0
                            ? 'Free'
                            : `₹${Number(course.price).toFixed(2)}`}
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: 8 }}>
                            <button
                              type="button"
                              className="btn btn-secondary btn-sm"
                              onClick={() => handleOpenEditForm(course)}
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              className="btn btn-danger btn-sm"
                              onClick={() => setCourseToDelete(course)}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <Pagination
                currentPage={coursePage}
                totalPages={totalCoursePages}
                onPageChange={(page) => setCoursePage(page)}
              />
            </>
          )}
        </div>
      )}

      {/* Tab 2: Registered Users */}
      {!loading && activeTab === 'users' && (
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>User ID</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Registered On</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.name}</td>
                  <td>{u.email}</td>
                  <td>
                    <span className="badge">{u.role}</span>
                  </td>
                  <td>
                    {u.creationDate ? new Date(u.creationDate).toLocaleString() : 'N/A'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Tab 3: Enrollments */}
      {!loading && activeTab === 'enrollments' && (
        <div>
          {enrollments.length === 0 ? (
            <div className="empty-state">No student enrollments have been recorded yet.</div>
          ) : (
            <div className="table-wrapper">
              <table className="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Student Name</th>
                    <th>Student Email</th>
                    <th>Course Title</th>
                    <th>Progress</th>
                    <th>Status</th>
                    <th>Enrolled Date</th>
                  </tr>
                </thead>
                <tbody>
                  {enrollments.map((enr) => (
                    <tr key={enr.id}>
                      <td>{enr.id}</td>
                      <td>{enr.userName}</td>
                      <td>{enr.userEmail}</td>
                      <td>{enr.courseTitle}</td>
                      <td>{enr.completionPercentage}%</td>
                      <td>
                        <span
                          className={`badge ${
                            enr.status === 'COMPLETED' ? 'badge-free' : ''
                          }`}
                        >
                          {enr.status}
                        </span>
                      </td>
                      <td>
                        {enr.enrollmentDate
                          ? new Date(enr.enrollmentDate).toLocaleDateString()
                          : 'N/A'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Tab 4: Contact Inquiries */}
      {!loading && activeTab === 'inquiries' && (
        <div>
          {inquiries.length === 0 ? (
            <div className="empty-state">No contact inquiries have been submitted yet.</div>
          ) : (
            <div className="table-wrapper">
              <table className="table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Sender Name</th>
                    <th>Email</th>
                    <th>Subject</th>
                    <th>Message</th>
                    <th>Submitted On</th>
                  </tr>
                </thead>
                <tbody>
                  {inquiries.map((inq) => (
                    <tr key={inq.id}>
                      <td>{inq.id}</td>
                      <td>{inq.name}</td>
                      <td>{inq.email}</td>
                      <td>
                        <strong>{inq.subject}</strong>
                      </td>
                      <td>{inq.message}</td>
                      <td>
                        {inq.creationDate
                          ? new Date(inq.creationDate).toLocaleString()
                          : 'N/A'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Edit Confirmation Modal */}
      <ConfirmModal
        isOpen={pendingEditConfirm}
        title="Confirm Course Update"
        message={`Are you sure you want to save the changes to "${formData.title}"?`}
        confirmLabel="Save Changes"
        cancelLabel="Cancel"
        variant="primary"
        onConfirm={executeSaveCourse}
        onCancel={() => setPendingEditConfirm(false)}
      />

      {/* Delete Confirmation Modal */}
      <ConfirmModal
        isOpen={Boolean(courseToDelete)}
        title="Confirm Course Deletion"
        message={
          courseToDelete
            ? `Are you sure you want to permanently delete "${courseToDelete.title}"? Any enrollments associated with this course will also be removed.`
            : ''
        }
        confirmLabel="Delete Course"
        cancelLabel="Cancel"
        variant="danger"
        onConfirm={executeDeleteCourse}
        onCancel={() => setCourseToDelete(null)}
      />
    </div>
  );
}
