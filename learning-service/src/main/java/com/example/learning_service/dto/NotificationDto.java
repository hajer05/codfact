package com.example.learning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Long recipientId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String type;
    private String title;
    private String message;
    private Long referenceId;
    private String referenceType;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    // Helper method to create notification for blog comment
    public static NotificationDto createBlogCommentNotification(
            Long recipientId, 
            Long senderId, 
            String senderName,
            Long blogId, 
            String blogTitle,
            String commentText) {
        
        return NotificationDto.builder()
                .recipientId(recipientId)
                .senderId(senderId)
                .senderName(senderName)
                .type("BLOG_COMMENT")
                .title("New comment on your blog")
                .message(senderName + " commented on your blog \"" + blogTitle + "\": " + 
                        (commentText.length() > 50 ? commentText.substring(0, 50) + "..." : commentText))
                .referenceId(blogId)
                .referenceType("BLOG")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
