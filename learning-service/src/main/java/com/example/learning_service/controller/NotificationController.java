package com.example.learning_service.controller;

import com.example.learning_service.dto.NotificationDto;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.UserRepository;
import com.example.learning_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
// import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.messaging.handler.annotation.Payload;
// import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        Page<NotificationDto> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    // WebSocket functionality will be added after dependencies are resolved
    
    // Test endpoint to send a notification (for development)
    @PostMapping("/test")
    public ResponseEntity<NotificationDto> sendTestNotification(@RequestBody Map<String, Object> payload) {
        Long recipientId = Long.valueOf(payload.get("recipientId").toString());
        String message = (String) payload.get("message");
        
        NotificationDto notification = NotificationDto.builder()
                .recipientId(recipientId)
                .senderId(1L) // Test sender
                .senderName("Test User")
                .type("TEST")
                .title("Test Notification")
                .message(message != null ? message : "This is a test notification")
                .isRead(false)
                .build();
        
        NotificationDto created = notificationService.createNotification(notification);
        return ResponseEntity.ok(created);
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("NotificationController.getCurrentUserId() called");
        
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            try {
                String authName = authentication.getName();
                System.out.println("Authenticated user name: " + authName);
                
                // Try to parse as user ID first
                try {
                    Long userId = Long.parseLong(authName);
                    System.out.println("Parsed user ID from auth name: " + userId);
                    return userId;
                } catch (NumberFormatException e) {
                    // It's an email, look up the user in the database
                    System.out.println("Auth name is email, looking up user in database: " + authName);
                    
                    try {
                        User user = userRepository.findByEmail(authName)
                                .orElseThrow(() -> new RuntimeException("User not found with email: " + authName));
                        
                        System.out.println("Found user in database: ID=" + user.getId() + ", Name=" + 
                                         user.getFirstName() + " " + user.getLastName());
                        return user.getId();
                    } catch (Exception ex) {
                        System.err.println("Error looking up user by email: " + ex.getMessage());
                        // Fallback to predefined mappings for known emails
                        if ("student@codingfactory.com".equals(authName)) {
                            System.out.println("Fallback: Mapped to student user ID: 4");
                            return 4L;
                        }
                        if ("teacher@codingfactory.com".equals(authName)) {
                            System.out.println("Fallback: Mapped to teacher user ID: 2");
                            return 2L;
                        }
                        if ("admin@codingfactory.com".equals(authName)) {
                            System.out.println("Fallback: Mapped to admin user ID: 1");
                            return 1L;
                        }
                        if ("consultant@codingfactory.com".equals(authName)) {
                            System.out.println("Fallback: Mapped to consultant user ID: 3");
                            return 3L;
                        }
                        
                        System.err.println("No fallback mapping found for email: " + authName);
                        return null;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error getting current user ID: " + e.getMessage());
                return null;
            }
        }
        
        System.out.println("No valid authentication found");
        return null;
    }
}
