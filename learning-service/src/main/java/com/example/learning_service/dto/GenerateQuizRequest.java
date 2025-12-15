package com.example.learning_service.dto;

import lombok.Data;

@Data
public class GenerateQuizRequest {
    private Long courseId;
    private Integer numberOfQuestions = 10;
    private String difficulty = "INTERMEDIATE"; // BEGINNER, INTERMEDIATE, ADVANCED
    private String focusTopics; // Topics spécifiques (optionnel)
}

