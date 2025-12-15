package com.example.learning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private Long blogId;
    private LocalDateTime createdAt;
    private List<ReplyDto> replies;
    private int replyCount;
}
