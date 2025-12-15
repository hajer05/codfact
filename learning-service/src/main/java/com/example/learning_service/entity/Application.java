package com.example.learning_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pfe_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "applications", "documents", "evaluation"})
    private PFESubject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String motivation;

    @Column(name = "cv_file_name")
    private String cvFileName;

    @Column(name = "cv_file_url")
    private String cvFileUrl;

    @Column(name = "cv_original_file_name")
    private String cvOriginalFileName;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User reviewedBy;

    @Column(name = "review_comment")
    private String reviewComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "workflow_step")
    private WorkflowStep workflowStep = WorkflowStep.APPLICATION_REVIEW;

    @Column(name = "project_review_comment")
    private String projectReviewComment;

    @Column(name = "project_reviewed_at")
    private LocalDateTime projectReviewedAt;

    @Column(name = "report_review_comment")
    private String reportReviewComment;

    @Column(name = "report_reviewed_at")
    private LocalDateTime reportReviewedAt;

    @Column(name = "jury_date")
    private LocalDateTime juryDate;

    @Column(name = "jury_location")
    private String juryLocation;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
    }

    public enum ApplicationStatus {
        PENDING, ACCEPTED, REJECTED
    }

    public enum WorkflowStep {
        APPLICATION_REVIEW,     // Initial application review
        PROJECT_UPLOAD,         // Student needs to upload project (RAR)
        PROJECT_REVIEW,         // Consultant reviewing project
        REPORT_UPLOAD,          // Student needs to upload report (PDF/DOCX)
        REPORT_REVIEW,          // Consultant reviewing report
        JURY_SCHEDULING,        // Consultant scheduling jury
        COMPLETED               // Process completed, jury scheduled
    }
}
