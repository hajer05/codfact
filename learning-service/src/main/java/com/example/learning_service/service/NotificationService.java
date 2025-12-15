package com.example.learning_service.service;

import com.example.learning_service.dto.NotificationDto;
import com.example.learning_service.entity.Notification;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.NotificationRepository;
import com.example.learning_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailService emailService;
    
    @Transactional
    public NotificationDto createNotification(NotificationDto notificationDto) {
        System.out.println("NotificationService.createNotification called with:");
        System.out.println("Recipient ID: " + notificationDto.getRecipientId());
        System.out.println("Sender ID: " + notificationDto.getSenderId());
        System.out.println("Type: " + notificationDto.getType());
        System.out.println("Title: " + notificationDto.getTitle());
        System.out.println("Message: " + notificationDto.getMessage());
        
        try {
            // Create notification entity
            Notification notification = Notification.builder()
                    .recipientId(notificationDto.getRecipientId())
                    .senderId(notificationDto.getSenderId())
                    .type(notificationDto.getType())
                    .title(notificationDto.getTitle())
                    .message(notificationDto.getMessage())
                    .referenceId(notificationDto.getReferenceId())
                    .referenceType(notificationDto.getReferenceType())
                    .isRead(false)
                    .build();
            
            System.out.println("Saving notification to database...");
            // Save to database
            notification = notificationRepository.save(notification);
            System.out.println("Notification saved with ID: " + notification.getId());
            
            // Convert to DTO with sender information
            NotificationDto savedNotification = convertToDto(notification);
            
            // Send real-time notification via WebSocket
            try {
                sendRealTimeNotification(savedNotification);
            } catch (Exception e) {
                System.out.println("WebSocket notification failed (this is expected if WebSocket is not fully configured): " + e.getMessage());
            }
            
            // Send email notification
            try {
                sendEmailNotification(savedNotification);
            } catch (Exception e) {
                log.error("Failed to send email notification: {}", e.getMessage());
            }
            
            log.info("Created notification: {} for user: {}", notification.getType(), notification.getRecipientId());
            
            return savedNotification;
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public void sendRealTimeNotification(NotificationDto notification) {
        try {
            // Send to specific user's notification queue
            messagingTemplate.convertAndSendToUser(
                notification.getRecipientId().toString(),
                "/queue/notifications",
                notification
            );
            
            // Also send unread count update
            long unreadCount = getUnreadCount(notification.getRecipientId());
            messagingTemplate.convertAndSendToUser(
                notification.getRecipientId().toString(),
                "/queue/unread-count",
                unreadCount
            );
            
            log.info("Sent real-time notification to user: {}", notification.getRecipientId());
        } catch (Exception e) {
            log.error("Failed to send real-time notification: {}", e.getMessage());
        }
    }
    
    public Page<NotificationDto> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.map(this::convertToDto);
    }
    
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }
    
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.markAsReadByIdAndRecipientId(notificationId, userId);
        
        // Send updated unread count
        long unreadCount = getUnreadCount(userId);
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/unread-count",
            unreadCount
        );
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByRecipientId(userId);
        
        // Send updated unread count (should be 0)
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/unread-count",
            0L
        );
    }
    
    public void sendEmailNotification(NotificationDto notification) {
        try {
            // Get recipient user information
            Optional<User> recipientUser = userRepository.findById(notification.getRecipientId());
            if (recipientUser.isEmpty()) {
                log.warn("Cannot send email notification - recipient user not found: {}", notification.getRecipientId());
                return;
            }

            User recipient = recipientUser.get();
            String recipientEmail = recipient.getEmail();
            String recipientName = recipient.getFirstName() + " " + recipient.getLastName();

            // Send email based on notification type
            switch (notification.getType()) {
                case "BLOG_COMMENT":
                    if (notification.getReferenceId() != null) {
                        emailService.sendBlogCommentNotification(
                            recipientEmail,
                            recipientName,
                            notification.getSenderName(),
                            extractBlogTitleFromMessage(notification.getMessage()),
                            extractCommentFromMessage(notification.getMessage()),
                            notification.getReferenceId()
                        );
                    }
                    break;
                case "COURSE_ENROLLMENT":
                    if (notification.getReferenceId() != null) {
                        emailService.sendCourseEnrollmentNotification(
                            recipientEmail,
                            recipientName,
                            notification.getTitle(),
                            notification.getReferenceId()
                        );
                    }
                    break;
                case "PFE_APPLICATION":
                    if (notification.getReferenceId() != null) {
                        emailService.sendPFEApplicationNotification(
                            recipientEmail,
                            recipientName,
                            notification.getSenderName(),
                            notification.getTitle(),
                            notification.getReferenceId()
                        );
                    }
                    break;
                default:
                    log.info("No email template configured for notification type: {}", notification.getType());
                    break;
            }

            log.info("Email notification sent for type: {} to: {}", notification.getType(), recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage(), e);
        }
    }

    private String extractBlogTitleFromMessage(String message) {
        // Extract blog title from message like: "John Doe commented on your blog "My Blog Title": Comment text"
        try {
            int startIndex = message.indexOf("\"") + 1;
            int endIndex = message.indexOf("\"", startIndex);
            if (startIndex > 0 && endIndex > startIndex) {
                return message.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            log.warn("Could not extract blog title from message: {}", message);
        }
        return "Your Blog Post";
    }

    private String extractCommentFromMessage(String message) {
        // Extract comment text from message like: "John Doe commented on your blog "My Blog Title": Comment text"
        try {
            int colonIndex = message.lastIndexOf(": ");
            if (colonIndex > 0 && colonIndex + 2 < message.length()) {
                return message.substring(colonIndex + 2);
            }
        } catch (Exception e) {
            log.warn("Could not extract comment from message: {}", message);
        }
        return "New comment on your blog";
    }

    // Helper method for blog comment notifications
    public void createBlogCommentNotification(Long blogAuthorId, Long commenterId, String commenterName, 
                                            Long blogId, String blogTitle, String commentText) {
        System.out.println("NotificationService.createBlogCommentNotification called");
        System.out.println("Blog Author ID: " + blogAuthorId + ", Commenter ID: " + commenterId);
        
        // Don't notify if user comments on their own blog
        if (blogAuthorId.equals(commenterId)) {
            System.out.println("Skipping notification - same user");
            return;
        }
        
        System.out.println("Creating notification DTO...");
        NotificationDto notification = NotificationDto.createBlogCommentNotification(
            blogAuthorId, commenterId, commenterName, blogId, blogTitle, commentText
        );
        
        System.out.println("Calling createNotification...");
        createNotification(notification);
        System.out.println("Notification creation completed");
    }
    
    // Cleanup old notifications (can be scheduled)
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Keep notifications for 30 days
        notificationRepository.deleteOldReadNotifications(cutoffDate);
        log.info("Cleaned up old notifications before: {}", cutoffDate);
    }
    
    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = NotificationDto.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .senderId(notification.getSenderId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
        
        // Add sender information if available
        if (notification.getSenderId() != null) {
            Optional<User> sender = userRepository.findById(notification.getSenderId());
            if (sender.isPresent()) {
                User senderUser = sender.get();
                dto.setSenderName(senderUser.getFirstName() + " " + senderUser.getLastName());
                // Add avatar if you have it in User entity
            }
        }
        
        return dto;
    }
}
