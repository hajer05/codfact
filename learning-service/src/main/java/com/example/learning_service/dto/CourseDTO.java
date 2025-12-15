package com.example.learning_service.dto;

import com.example.learning_service.entity.Course;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private String thumbnailImage;
    private String previewVideo;
    private BigDecimal price;
    private Course.CourseLevel level;
    private Course.CourseStatus status;
    private String category;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private String teacherName;
    private Long teacherId;
    private int moduleCount;
    private int lessonCount;
}
