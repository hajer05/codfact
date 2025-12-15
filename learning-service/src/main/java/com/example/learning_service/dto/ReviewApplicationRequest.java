package com.example.learning_service.dto;

import com.example.learning_service.entity.Application;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewApplicationRequest {
    private Application.ApplicationStatus status;
    private String reviewComment;
}
