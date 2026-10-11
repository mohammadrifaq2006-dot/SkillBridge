package com.skillbridge.lms.repository;

import com.skillbridge.lms.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCategoryIgnoreCaseOrderByIdAsc(String category);

    @Query("SELECT c FROM Course c WHERE " +
           "(:category IS NULL OR :category = '' OR LOWER(c.category) = LOWER(:category)) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.instructor) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.category) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY c.id ASC")
    List<Course> searchCourses(@Param("keyword") String keyword, @Param("category") String category);

    @Query("SELECT DISTINCT c.category FROM Course c ORDER BY c.category ASC")
    List<String> findDistinctCategories();
}
