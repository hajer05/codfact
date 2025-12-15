package com.example.learning_service.dto;

import com.example.learning_service.entity.Course;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCourseRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private String shortDescription;
    
    @NotNull(message = "Price is required")
    private BigDecimal price;
    
    @NotNull(message = "Level is required")
    private Course.CourseLevel level;
    
    private String category;
    private String language = "French";
}
