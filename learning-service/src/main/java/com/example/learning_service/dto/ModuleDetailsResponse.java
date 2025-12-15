package com.example.learning_service.dto;

import com.example.learning_service.entity.Module;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ModuleDetailsResponse {
    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LessonResponse> lessons;

    public static ModuleDetailsResponse fromEntity(Module module) {
        return fromEntity(module, true);
    }

    public static ModuleDetailsResponse fromEntity(Module module, boolean includeContent) {
        ModuleDetailsResponse response = new ModuleDetailsResponse();
        response.setId(module.getId());
        response.setTitle(module.getTitle());
        response.setDescription(module.getDescription());
        response.setOrderIndex(module.getOrderIndex());
        response.setCreatedAt(module.getCreatedAt());
        response.setUpdatedAt(module.getUpdatedAt());
        
        if (module.getLessons() != null) {
            response.setLessons(module.getLessons().stream()
                    .map(lesson -> LessonResponse.fromEntity(lesson, includeContent))
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
}
