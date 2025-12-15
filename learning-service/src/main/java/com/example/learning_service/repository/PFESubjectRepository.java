package com.example.learning_service.repository;

import com.example.learning_service.entity.PFESubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PFESubjectRepository extends JpaRepository<PFESubject, Long> {
    
    List<PFESubject> findAllByOrderByCreatedAtDesc();
    
    List<PFESubject> findByStatusOrderByCreatedAtDesc(PFESubject.PFEStatus status);
    
    List<PFESubject> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);
    
    @Query("SELECT p FROM PFESubject p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.requirements) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY p.createdAt DESC")
    List<PFESubject> searchSubjects(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.subject.id = :subjectId AND a.status = 'PENDING'")
    Long countPendingApplicationsBySubject(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.subject.id = :subjectId AND a.status = 'ACCEPTED'")
    Long countAcceptedApplicationsBySubject(@Param("subjectId") Long subjectId);
}
