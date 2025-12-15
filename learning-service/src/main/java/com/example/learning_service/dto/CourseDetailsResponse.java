package com.example.learning_service.dto;

import com.example.learning_service.entity.Course;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CourseDetailsResponse {
    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private String thumbnailImage;
    private String previewVideo;
    private java.math.BigDecimal price;
    private String level;
    private String status;
    private String category;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private String teacherName;
    private Long teacherId;
    private List<ModuleDetailsResponse> modules;

    public static CourseDetailsResponse fromEntity(Course course) {
        return fromEntity(course, true);
    }

    public static CourseDetailsResponse fromEntity(Course course, boolean includeContent) {
        CourseDetailsResponse response = new CourseDetailsResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setShortDescription(course.getShortDescription());
        response.setThumbnailImage(course.getThumbnailImage());
        response.setPreviewVideo(course.getPreviewVideo());
        response.setPrice(course.getPrice());
        response.setLevel(course.getLevel().toString());
        response.setStatus(course.getStatus().toString());
        response.setCategory(course.getCategory());
        response.setLanguage(course.getLanguage());
        response.setCreatedAt(course.getCreatedAt());
        response.setPublishedAt(course.getPublishedAt());
        response.setTeacherName(course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName());
        response.setTeacherId(course.getTeacher().getId());
        
        if (course.getModules() != null) {
            response.setModules(course.getModules().stream()
                    .map(module -> ModuleDetailsResponse.fromEntity(module, includeContent))
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
}
