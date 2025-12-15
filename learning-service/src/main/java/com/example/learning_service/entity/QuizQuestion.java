package com.example.learning_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class QuizQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long quizId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type = QuestionType.MULTIPLE_CHOICE;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "quiz_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option", columnDefinition = "TEXT")
    @OrderColumn(name = "option_order")
    private List<String> options = new ArrayList<>();
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;
    
    @Column
    private Integer points = 1; // Points pour cette question
    
    @Column(columnDefinition = "TEXT")
    private String explanation; // Explication de la réponse correcte
    
    @Column(nullable = false)
    private Integer questionOrder = 0; // Ordre de la question dans le quiz
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quizId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"questions", "course"})
    private Quiz quiz;
    
    public enum QuestionType {
        MULTIPLE_CHOICE,  // Choix multiple (une seule réponse)
        TRUE_FALSE,       // Vrai ou Faux
        SHORT_ANSWER      // Réponse courte
    }
}

