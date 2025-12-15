package com.example.learning_service.repository;

import com.example.learning_service.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    
    Optional<LessonProgress> findByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);
    
    List<LessonProgress> findByEnrollmentId(Long enrollmentId);
    
    List<LessonProgress> findByEnrollmentIdAndCompleted(Long enrollmentId, boolean completed);
    
    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.enrollment.id = :enrollmentId AND lp.completed = true")
    long countCompletedLessonsByEnrollment(@Param("enrollmentId") Long enrollmentId);
    
    @Query("SELECT COUNT(l) FROM Lesson l JOIN Module m ON l.module.id = m.id WHERE m.course.id = :courseId")
    long countTotalLessonsByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT lp FROM LessonProgress lp WHERE lp.enrollment.student.id = :userId AND lp.lesson.id = :lessonId")
    Optional<LessonProgress> findByUserIdAndLessonId(@Param("userId") Long userId, @Param("lessonId") Long lessonId);
}
