package com.example.learning_service.repository;

import com.example.learning_service.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    
    Optional<Evaluation> findBySubjectId(Long subjectId);
    
    List<Evaluation> findByEvaluatedByIdOrderByEvaluatedAtDesc(Long evaluatedById);
    
    List<Evaluation> findAllByOrderByEvaluatedAtDesc();
    
    boolean existsBySubjectId(Long subjectId);
}
