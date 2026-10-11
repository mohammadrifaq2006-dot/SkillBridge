package com.skillbridge.lms.repository;

import com.skillbridge.lms.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByUserIdOrderByEnrollmentDateDesc(Long userId);
    List<Enrollment> findByCourseId(Long courseId);
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    void deleteByCourseId(Long courseId);
}
