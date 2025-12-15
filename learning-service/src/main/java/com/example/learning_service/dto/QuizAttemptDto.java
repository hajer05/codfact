package com.example.learning_service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class QuizAttemptDto {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private Long userId;
    private String userName;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
    private Boolean isExpired;
    private Long remainingTimeSeconds; // Temps restant en secondes
    private Integer score;
    private Integer totalPoints;
    private Integer earnedPoints;
    private Boolean passed;
    private Map<Long, String> answers = new HashMap<>();
    private Boolean isCompleted;
    private List<QuestionResultDto> questionResults; // Résultats détaillés par question
}

