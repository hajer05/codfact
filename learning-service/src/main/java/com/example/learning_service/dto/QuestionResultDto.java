package com.example.learning_service.dto;

import lombok.Data;

@Data
public class QuestionResultDto {
    private Long questionId;
    private String question;
    private String studentAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private Integer points;
    private Integer earnedPoints;
    private String explanation;
}

