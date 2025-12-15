package com.example.learning_service.controller;

import com.example.learning_service.dto.EnrollmentResponseDto;
import com.example.learning_service.entity.Enrollment;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.UserRepository;
import com.example.learning_service.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollInCourse(@PathVariable Long courseId, @RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserIdFromAuth(userId);
            
            Enrollment enrollment = enrollmentService.enrollInCourse(actualUserId, courseId);
            
            // Create a simple response DTO to avoid serialization issues
            EnrollmentResponseDto response = new EnrollmentResponseDto();
            response.setId(enrollment.getId());
            response.setCourseId(enrollment.getCourse().getId());
            response.setCourseTitle(enrollment.getCourse().getTitle());
            response.setStudentId(enrollment.getStudent().getId());
            response.setStudentName(enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName());
            response.setEnrolledAt(enrollment.getEnrolledAt());
            response.setProgress(enrollment.getProgress());
            response.setStatus(enrollment.getStatus().name());
            response.setSuccess(true);
            response.setMessage("Successfully enrolled in course");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            EnrollmentResponseDto errorResponse = new EnrollmentResponseDto();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/unenroll/{courseId}")
    public ResponseEntity<?> unenrollFromCourse(@PathVariable Long courseId, @RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserIdFromAuth(userId);
            
            enrollmentService.unenrollFromCourse(actualUserId, courseId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-enrollments")
    public ResponseEntity<List<EnrollmentResponseDto>> getMyEnrollments(@RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserIdFromAuth(userId);
            
            List<Enrollment> enrollments = enrollmentService.getUserEnrollments(actualUserId);
            
            // Convert to DTOs to avoid serialization issues
            List<EnrollmentResponseDto> enrollmentDtos = enrollments.stream()
                .map(enrollment -> {
                    EnrollmentResponseDto dto = new EnrollmentResponseDto();
                    dto.setId(enrollment.getId());
                    dto.setCourseId(enrollment.getCourse().getId());
                    dto.setCourseTitle(enrollment.getCourse().getTitle());
                    dto.setStudentId(enrollment.getStudent().getId());
                    dto.setStudentName(enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName());
                    dto.setEnrolledAt(enrollment.getEnrolledAt());
                    dto.setProgress(enrollment.getProgress());
                    dto.setStatus(enrollment.getStatus().name());
                    dto.setSuccess(true);
                    return dto;
                })
                .toList();
                
            return ResponseEntity.ok(enrollmentDtos);
        } catch (Exception e) {
            System.err.println("Error getting enrollments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/check/{courseId}")
    public ResponseEntity<Map<String, Boolean>> checkEnrollment(@PathVariable Long courseId, @RequestParam(required = false) Long userId) {
        try {
            Long actualUserId = getUserIdFromAuth(userId);
            System.out.println("Enrollment check - User ID: " + actualUserId + ", Course ID: " + courseId);
            
            boolean isEnrolled = enrollmentService.isUserEnrolled(actualUserId, courseId);
            System.out.println("Enrollment check result: " + isEnrolled);
            return ResponseEntity.ok(Map.of("enrolled", isEnrolled));
        } catch (Exception e) {
            System.err.println("Error in enrollment check: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("enrolled", false));
        }
    }

    @PutMapping("/{enrollmentId}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long enrollmentId, 
            @RequestBody Map<String, Double> progressData) {
        try {
            Double progress = progressData.get("progress");
            Enrollment enrollment = enrollmentService.updateProgress(enrollmentId, progress);
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Enrollment>> getCourseEnrollments(@PathVariable Long courseId) {
        try {
            List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(courseId);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Public endpoint for enrollment count
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Map<String, Long>> getCourseEnrollmentCount(@PathVariable Long courseId) {
        try {
            List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(courseId);
            long count = enrollments.size();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0L));
        }
    }

    private Long getUserIdFromAuth(Long providedUserId) {
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
                    throw new RuntimeException("User not found for email: " + email);
                }
            }
        } else {
            // For testing purposes, use a default user ID
            return 1L;
        }
    }
}
