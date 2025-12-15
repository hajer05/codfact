package com.example.learning_service.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SubmitQuizRequest {
    private Long attemptId;
    private Map<Long, String> answers = new HashMap<>(); // questionId -> réponse
}

