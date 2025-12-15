package com.example.learning_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QuizAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long quizId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long courseId;
    
    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime completedAt;
    
    @Column
    private LocalDateTime expiresAt; // Date/heure d'expiration du quiz
    
    @Column
    private Boolean isExpired = false; // true si le temps est écoulé
    
    @Column
    private Integer score; // Score obtenu (sur 100)
    
    @Column
    private Integer totalPoints; // Total des points du quiz
    
    @Column
    private Integer earnedPoints; // Points obtenus
    
    @Column(nullable = false)
    private Boolean passed = false; // true si score >= passing score
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "quiz_attempt_answers", joinColumns = @JoinColumn(name = "attempt_id"))
    @MapKeyColumn(name = "question_id")
    @Column(name = "answer", columnDefinition = "TEXT")
    private Map<Long, String> answers = new HashMap<>(); // questionId -> réponse de l'étudiant
    
    @Column(nullable = false)
    private Boolean isCompleted = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quizId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"questions"})
    private Quiz quiz;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "enrollments", "lessonProgress"})
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "modules", "enrollments"})
    private Course course;
}

