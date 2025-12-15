package com.example.learning_service.repository;

import com.example.learning_service.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourseId(Long courseId);
    List<Quiz> findByCourseIdAndStatus(Long courseId, Quiz.QuizStatus status);
    
    @Query("SELECT q FROM Quiz q WHERE q.courseId = :courseId AND q.status = :status")
    Optional<Quiz> findFirstByCourseIdAndStatus(@Param("courseId") Long courseId, @Param("status") Quiz.QuizStatus status);
    
    List<Quiz> findByStatus(Quiz.QuizStatus status);
    boolean existsByCourseIdAndStatus(Long courseId, Quiz.QuizStatus status);
}

