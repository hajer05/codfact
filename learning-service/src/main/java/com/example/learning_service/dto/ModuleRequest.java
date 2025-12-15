package com.example.learning_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModuleRequest {
    @NotBlank(message = "Module title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    private Integer orderIndex;
}
