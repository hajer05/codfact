package com.example.learning_service.repository;

import com.example.learning_service.entity.ConsultingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultingRequestRepository extends JpaRepository<ConsultingRequest, Long> {
    
    List<ConsultingRequest> findByRequestedByIdOrderByCreatedAtDesc(Long userId);
    
    List<ConsultingRequest> findByStatusOrderByCreatedAtDesc(ConsultingRequest.ConsultingStatus status);
    
    List<ConsultingRequest> findByAssignedToIdOrderByCreatedAtDesc(Long consultantId);
    
    @Query("SELECT cr FROM ConsultingRequest cr WHERE cr.assignedTo.id = :consultantId AND cr.status = :status ORDER BY cr.createdAt DESC")
    List<ConsultingRequest> findByAssignedToIdAndStatusOrderByCreatedAtDesc(
        @Param("consultantId") Long consultantId,
        @Param("status") ConsultingRequest.ConsultingStatus status
    );
    
    Optional<ConsultingRequest> findById(Long id);
    
    long countByStatus(ConsultingRequest.ConsultingStatus status);
}

