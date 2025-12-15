package com.example.learning_service.dto;

import com.example.learning_service.entity.Application;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long subjectId;
    private String subjectTitle;
    private String subjectCreatedBy;
    private Application.ApplicationStatus status;
    private String motivation;
    private String cvFileName;
    private String cvFileUrl;
    private String cvOriginalFileName;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedById;
    private String reviewedByName;
    private String reviewComment;
    private String workflowStep;
    private String projectReviewComment;
    private LocalDateTime projectReviewedAt;
    private String reportReviewComment;
    private LocalDateTime reportReviewedAt;
    private LocalDateTime juryDate;
    private String juryLocation;
}
