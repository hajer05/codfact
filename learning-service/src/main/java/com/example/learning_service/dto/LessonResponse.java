package com.example.learning_service.dto;

import com.example.learning_service.entity.Lesson;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonResponse {
    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    private Lesson.LessonType type;
    private String videoUrl;
    private String videoFileName;
    private Long videoDuration;
    private String content;
    private String attachmentUrl;
    private String attachmentFileName;
    private boolean isFree;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LessonResponse fromEntity(Lesson lesson) {
        return fromEntity(lesson, true);
    }

    public static LessonResponse fromEntity(Lesson lesson, boolean includeContent) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setTitle(lesson.getTitle());
        response.setDescription(lesson.getDescription());
        response.setOrderIndex(lesson.getOrderIndex());
        response.setType(lesson.getType());
        response.setVideoDuration(lesson.getVideoDuration());
        response.setFree(lesson.isFree());
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
        
        // Only include content (video, text, attachments) if user has access
        if (includeContent) {
            response.setVideoUrl(lesson.getVideoUrl());
            response.setVideoFileName(lesson.getVideoFileName());
            response.setContent(lesson.getContent());
            response.setAttachmentUrl(lesson.getAttachmentUrl());
            response.setAttachmentFileName(lesson.getAttachmentFileName());
            
            // Log for debugging
            if (lesson.getVideoFileName() != null || lesson.getVideoUrl() != null) {
                System.out.println("LessonResponse.fromEntity - Lesson ID: " + lesson.getId() + 
                    ", videoUrl: " + lesson.getVideoUrl() + 
                    ", videoFileName: " + lesson.getVideoFileName());
            }
        } else {
            // Hide content but keep metadata visible
            response.setVideoUrl(null);
            response.setVideoFileName(null);
            response.setContent(null);
            response.setAttachmentUrl(null);
            response.setAttachmentFileName(null);
        }
        
        return response;
    }
}
