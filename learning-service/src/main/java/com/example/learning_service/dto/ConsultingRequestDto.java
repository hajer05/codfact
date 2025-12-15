package com.example.learning_service.dto;

import com.example.learning_service.entity.ConsultingRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultingRequestDto {
    private Long id;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private String serviceType;
    private String projectDescription;
    private String needs;
    private String budget;
    private String timeline;
    private ConsultingRequest.ConsultingStatus status;
    private String adminNotes;
    private Long requestedById;
    private String requestedByName;
    private Long assignedToId;
    private String assignedToName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime answeredAt;
}

