package com.example.learning_service.dto;

import com.example.learning_service.entity.Lesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonRequest {
    @NotBlank(message = "Lesson title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Module ID is required")
    private Long moduleId;
    
    private Integer orderIndex;
    
    private Lesson.LessonType type = Lesson.LessonType.VIDEO;
    
    private String videoUrl;
    
    private String videoFileName;
    
    private Long videoDuration; // in seconds
    
    private String content;
    
    private String attachmentUrl;
    
    private String attachmentFileName;
    
    private boolean isFree = false;
}
