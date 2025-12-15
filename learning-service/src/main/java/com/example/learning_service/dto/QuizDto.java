package com.example.learning_service.dto;

import com.example.learning_service.entity.Quiz;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class QuizDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private Quiz.QuizStatus status;
    private Integer passingScore;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private String approvedByName;
    private Boolean generatedByAI;
    private List<QuizQuestionDto> questions = new ArrayList<>();
    private Integer totalQuestions;
    private Integer totalPoints;
}

