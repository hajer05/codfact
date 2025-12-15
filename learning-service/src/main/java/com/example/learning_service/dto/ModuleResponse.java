package com.example.learning_service.dto;

import com.example.learning_service.entity.Module;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ModuleResponse {
    private Long id;
    private String title;
    private String description;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LessonResponse> lessons;

    public static ModuleResponse fromEntity(Module module) {
        ModuleResponse response = new ModuleResponse();
        response.setId(module.getId());
        response.setTitle(module.getTitle());
        response.setDescription(module.getDescription());
        response.setOrderIndex(module.getOrderIndex());
        response.setCreatedAt(module.getCreatedAt());
        response.setUpdatedAt(module.getUpdatedAt());
        
        if (module.getLessons() != null) {
            response.setLessons(module.getLessons().stream()
                .map(LessonResponse::fromEntity)
                .collect(Collectors.toList()));
        }
        
        return response;
    }
}
