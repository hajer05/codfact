package com.example.learning_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Quiz {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long courseId;
    
    @Column(nullable = false, length = 1000)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status = QuizStatus.PENDING_APPROVAL;
    
    @Column(nullable = false)
    private Integer passingScore = 70; // Score minimum pour passer (sur 100)
    
    @Column(nullable = false)
    private Integer durationMinutes = 10; // Durée du quiz en minutes
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime approvedAt;
    
    @Column
    private Long approvedBy; // ID de l'admin ou enseignant qui a approuvé
    
    @Column(nullable = false)
    private Boolean generatedByAI = true;
    
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"quiz"})
    private List<QuizQuestion> questions = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "modules", "enrollments"})
    private Course course;
    
    public enum QuizStatus {
        PENDING_APPROVAL,  // En attente d'approbation par l'enseignant/admin
        APPROVED,          // Approuvé et disponible pour les étudiants
        REJECTED,          // Rejeté par l'enseignant/admin
        ARCHIVED           // Archivé
    }
}

