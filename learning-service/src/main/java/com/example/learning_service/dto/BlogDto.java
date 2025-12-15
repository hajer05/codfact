package com.example.learning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogDto {
    private Long id;
    private String title;
    private String content;
    private String photo;
    private List<String> tags;
    private Long likes;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int commentCount;
}
