package com.example.learning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyDto {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private Long commentId;
    private LocalDateTime createdAt;
}
