package com.example.learning_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long recipientId;
    
    @Column(nullable = false)
    private Long senderId;
    
    @Column(nullable = false)
    private String type; // BLOG_COMMENT, BLOG_LIKE, COURSE_ENROLLMENT, etc.
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 500)
    private String message;
    
    @Column(name = "reference_id")
    private Long referenceId; // ID of the blog, comment, etc.
    
    @Column(name = "reference_type")
    private String referenceType; // BLOG, COMMENT, COURSE, etc.
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    // Helper method to mark as read
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
