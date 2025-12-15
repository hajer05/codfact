package com.example.learning_service.dto;

import com.example.learning_service.entity.ConsultingRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConsultingRequest {
    private ConsultingRequest.ConsultingStatus status;
    private String adminNotes;
    private Long assignedToId;
}

