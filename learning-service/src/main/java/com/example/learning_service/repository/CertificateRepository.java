package com.example.learning_service.repository;

import com.example.learning_service.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    Optional<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);
    
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    
    List<Certificate> findByUserId(Long userId);
    
    List<Certificate> findByCourseId(Long courseId);
}
