package com.example.learning_service.controller;

import com.example.learning_service.entity.LessonProgress;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.UserRepository;
import com.example.learning_service.service.LessonProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lesson-progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;
    private final UserRepository userRepository;

    @PostMapping("/mark-completed/{lessonId}")
    public ResponseEntity<?> markLessonAsCompleted(@PathVariable Long lessonId, @AuthenticationPrincipal User user) {
        try {
            Long userId = getUserId(null);
            System.out.println("========================================");
            System.out.println("LessonProgressController - mark-completed endpoint called");
            System.out.println("Lesson ID: " + lessonId);
            System.out.println("User ID: " + userId);
            // Don't print the whole Authentication object as it causes LazyInitializationException
            System.out.println("Authenticated User Email: " + (user != null ? user.getEmail() : "null"));
            System.out.println("========================================");
            
            LessonProgress progress = lessonProgressService.markLessonAsCompleted(userId, lessonId);
            System.out.println("✅ Lesson " + lessonId + " marked as completed successfully");
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            System.err.println("========================================");
            System.err.println("❌ ERROR marking lesson as completed: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update/{lessonId}")
    public ResponseEntity<?> updateLessonProgress(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Long> progressData,
            @RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserId(userId);
            Long watchedDuration = progressData.get("watchedDuration");
            
            LessonProgress progress = lessonProgressService.updateLessonProgress(actualUserId, lessonId, watchedDuration);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getUserLessonProgress(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserId(userId);
            List<LessonProgress> progress = lessonProgressService.getUserLessonProgress(actualUserId, courseId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            System.err.println("Error getting user lesson progress: " + e.getMessage());
            e.printStackTrace();
            // Return empty list instead of error to allow viewing courses without enrollment
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<?> getLessonProgress(
            @PathVariable Long lessonId,
            @RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserId(userId);
            Optional<LessonProgress> progress = lessonProgressService.getLessonProgress(actualUserId, lessonId);
            
            if (progress.isPresent()) {
                return ResponseEntity.ok(progress.get());
            } else {
                return ResponseEntity.ok(Map.of("completed", false, "watchedDuration", 0));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("completed", false, "watchedDuration", 0));
        }
    }

    @GetMapping("/course/{courseId}/percentage")
    public ResponseEntity<Map<String, Double>> getCourseProgressPercentage(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserId(userId);
            double percentage = lessonProgressService.getCourseProgressPercentage(courseId, actualUserId);
            return ResponseEntity.ok(Map.of("percentage", percentage));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("percentage", 0.0));
        }
    }

    @PostMapping("/course/{courseId}/recalculate")
    public ResponseEntity<Map<String, String>> recalculateProgress(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserId(userId);
            lessonProgressService.recalculateCourseProgress(courseId, actualUserId);
            return ResponseEntity.ok(Map.of("message", "Progress recalculated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/course/{courseId}/complete-all")
    public ResponseEntity<Map<String, String>> completeAllLessons(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserId(userId);
            lessonProgressService.markAllLessonsCompleted(courseId, actualUserId);
            return ResponseEntity.ok(Map.of("message", "All lessons marked as completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/debug/lessons")
    public ResponseEntity<Map<String, Object>> debugLessons() {
        try {
            Map<String, Object> debug = lessonProgressService.debugLessonsAndCourses();
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long getUserId(Long providedUserId) {
        if (providedUserId != null) {
            return providedUserId;
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                // Try to parse as Long first (if it's a user ID)
                return Long.parseLong(auth.getName());
            } catch (NumberFormatException e) {
                // If it's an email, find user by email
                String email = auth.getName();
                System.out.println("Authentication name is email: " + email);
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    System.out.println("Found user ID: " + user.getId() + " for email: " + email);
                    return user.getId();
                } else {
                    System.err.println("User not found for email: " + email);
                    throw new RuntimeException("User not found for email: " + email);
                }
            }
        }
        
        System.err.println("No authentication found, throwing error");
        throw new RuntimeException("Authentication required");
    }
}
