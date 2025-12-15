package com.example.learning_service.repository;

import com.example.learning_service.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    
    List<Enrollment> findByStudentId(Long studentId);
    
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentIdWithCourse(@Param("studentId") Long studentId);
    
    List<Enrollment> findByStudentIdAndStatus(Long studentId, Enrollment.EnrollmentStatus status);
    
    List<Enrollment> findByCourseId(Long courseId);
    
    List<Enrollment> findByCourseIdAndStatus(Long courseId, Enrollment.EnrollmentStatus status);
    
    long countByCourseId(Long courseId);
    
    long countByCourseIdAndStatus(Long courseId, Enrollment.EnrollmentStatus status);
    
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
