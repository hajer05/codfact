package com.example.learning_service.repository;

import com.example.learning_service.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    @Query("SELECT l FROM Lesson l WHERE l.module.id = :moduleId ORDER BY l.orderIndex ASC")
    List<Lesson> findByModuleIdOrderByOrderIndex(@Param("moduleId") Long moduleId);
    
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.id = :moduleId")
    Long countByModuleId(@Param("moduleId") Long moduleId);
    
    @Query("SELECT l FROM Lesson l JOIN l.module m WHERE m.course.id = :courseId ORDER BY m.orderIndex ASC, l.orderIndex ASC")
    List<Lesson> findByCourseIdOrderByModuleAndLesson(@Param("courseId") Long courseId);
    
    @Query("SELECT l FROM Lesson l WHERE l.videoFileName = :fileName")
    java.util.Optional<Lesson> findByVideoFileName(@Param("fileName") String fileName);
}
