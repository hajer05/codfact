package com.example.learning_service.dto;

import com.example.learning_service.entity.PFESubject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PFESubjectDto {
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private Long createdById;
    private String createdByName;
    private String createdByRole;
    private PFESubject.PFEStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int applicationCount;
    private int pendingApplications;
    private int acceptedApplications;
    private boolean hasEvaluation;
    private Double evaluationGrade;
}
