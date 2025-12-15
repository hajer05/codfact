package com.example.learning_service.repository;

import com.example.learning_service.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByStudentIdOrderByAppliedAtDesc(Long studentId);
    
    List<Application> findBySubjectIdOrderByAppliedAtDesc(Long subjectId);
    
    List<Application> findByStatusOrderByAppliedAtDesc(Application.ApplicationStatus status);
    
    Optional<Application> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
    
    @Query("SELECT a FROM Application a WHERE a.subject.createdBy.id = :consultantId ORDER BY a.appliedAt DESC")
    List<Application> findByConsultantIdOrderByAppliedAtDesc(@Param("consultantId") Long consultantId);

    boolean existsBySubjectIdAndStudentIdAndStatus(Long subjectId, Long studentId, Application.ApplicationStatus status);
    
    @Query("SELECT a FROM Application a WHERE a.subject.createdBy.id = :consultantId AND a.status = :status ORDER BY a.appliedAt DESC")
    List<Application> findByConsultantIdAndStatusOrderByAppliedAtDesc(@Param("consultantId") Long consultantId, 
                                                                      @Param("status") Application.ApplicationStatus status);
    
    boolean existsByStudentIdAndSubjectId(Long studentId, Long subjectId);
    
    @Query("SELECT a FROM Application a JOIN FETCH a.student JOIN FETCH a.subject WHERE a.id = :id")
    Optional<Application> findByIdWithStudentAndSubject(@Param("id") Long id);
    
    @Query("SELECT a FROM Application a JOIN FETCH a.student JOIN FETCH a.subject WHERE a.subject.id = :subjectId AND a.status = 'ACCEPTED' ORDER BY a.appliedAt DESC")
    List<Application> findBySubjectIdWithStudentAndSubject(@Param("subjectId") Long subjectId);
}
