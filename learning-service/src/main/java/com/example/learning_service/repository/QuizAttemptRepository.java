package com.example.learning_service.repository;

import com.example.learning_service.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserId(Long userId);
    List<QuizAttempt> findByUserIdAndCourseId(Long userId, Long courseId);
    List<QuizAttempt> findByUserIdAndCourseIdOrderByCompletedAtDesc(Long userId, Long courseId);
    List<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId);
    Optional<QuizAttempt> findByUserIdAndCourseIdAndIsCompletedTrue(Long userId, Long courseId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.userId = :userId AND qa.courseId = :courseId AND qa.passed = true ORDER BY qa.score DESC")
    Optional<QuizAttempt> findBestPassingAttempt(Long userId, Long courseId);
    
    @Query("SELECT CASE WHEN COUNT(qa) > 0 THEN true ELSE false END FROM QuizAttempt qa WHERE qa.userId = :userId AND qa.courseId = :courseId AND qa.passed = true")
    boolean hasPassedQuiz(Long userId, Long courseId);
    
    // Pour l'admin - voir tous les résultats
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.courseId = :courseId ORDER BY qa.completedAt DESC")
    List<QuizAttempt> findByCourseIdOrderByCompletedAtDesc(@Param("courseId") Long courseId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quizId = :quizId ORDER BY qa.completedAt DESC")
    List<QuizAttempt> findByQuizIdOrderByCompletedAtDesc(@Param("quizId") Long quizId);
}

