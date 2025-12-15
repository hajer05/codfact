package com.example.learning_service.dto;

import com.example.learning_service.entity.QuizQuestion;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuizQuestionDto {
    private Long id;
    private Long quizId;
    private String question;
    private QuizQuestion.QuestionType type;
    private List<String> options = new ArrayList<>();
    private String correctAnswer; // Ne sera pas retourné aux étudiants pendant le quiz
    private Integer points;
    private String explanation;
    private Integer questionOrder;
}

