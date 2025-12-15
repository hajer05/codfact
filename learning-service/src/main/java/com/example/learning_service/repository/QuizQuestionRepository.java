package com.example.learning_service.repository;

import com.example.learning_service.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdOrderByQuestionOrderAsc(Long quizId);
    void deleteByQuizId(Long quizId);
}

