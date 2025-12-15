package com.example.learning_service.controller;

import com.example.learning_service.entity.Complaint;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.UserRepository;
import com.example.learning_service.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ComplaintController {
    
    private final ComplaintService complaintService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Complaint> createComplaint(
            @RequestBody Map<String, Object> request) {
        try {
            Long studentId = Long.valueOf(request.get("studentId").toString());
            Long courseId = Long.valueOf(request.get("courseId").toString());
            String subject = request.get("subject").toString();
            String message = request.get("message").toString();
            
            Complaint complaint = complaintService.createComplaint(studentId, courseId, subject, message);
            return ResponseEntity.ok(complaint);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<Complaint>> getTeacherComplaints(@PathVariable Long teacherId) {
        try {
            Long currentUserId = getCurrentUserId();
            boolean isAdmin = isCurrentUserAdmin();
            
            // Teachers can only see their own complaints, admins can see any teacher's complaints
            if (!isAdmin && !currentUserId.equals(teacherId)) {
                log.error("Teacher {} attempted to access complaints for teacher {}", currentUserId, teacherId);
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
            }
            
            List<Complaint> complaints = complaintService.getComplaintsByTeacher(teacherId);
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            log.error("Error getting teacher complaints: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/my-complaints")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<Complaint>> getMyComplaints() {
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                return ResponseEntity.badRequest().build();
            }
            List<Complaint> complaints = complaintService.getComplaintsByTeacher(currentUserId);
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            log.error("Error getting my complaints: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Complaint>> getStudentComplaints(@PathVariable Long studentId) {
        List<Complaint> complaints = complaintService.getComplaintsByStudent(studentId);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<Complaint>> getCourseComplaints(@PathVariable Long courseId) {
        try {
            Long currentUserId = getCurrentUserId();
            boolean isAdmin = isCurrentUserAdmin();
            
            // Get complaints and verify ownership for teachers
            List<Complaint> complaints = complaintService.getComplaintsByCourse(courseId);
            
            // Teachers can only see complaints for their own courses
            if (!isAdmin && !complaints.isEmpty()) {
                Long courseTeacherId = complaints.get(0).getTeacher().getId();
                if (!currentUserId.equals(courseTeacherId)) {
                    log.error("Teacher {} attempted to access complaints for course {} owned by teacher {}", 
                             currentUserId, courseId, courseTeacherId);
                    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
                }
            }
            
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            log.error("Error getting course complaints: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        try {
            List<Complaint> complaints = complaintService.getAllComplaints();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{complaintId}/respond")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Complaint> respondToComplaint(
            @PathVariable Long complaintId,
            @RequestBody Map<String, String> request) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                log.error("Unable to get current user ID for responding to complaint");
                return ResponseEntity.badRequest().build();
            }
            
            String response = request.get("response");
            if (response == null || response.trim().isEmpty()) {
                log.error("Response text is empty");
                return ResponseEntity.badRequest().build();
            }
            
            // Check if user is admin - admins can respond to any complaint
            boolean isAdmin = isCurrentUserAdmin();
            
            Complaint complaint = complaintService.respondToComplaint(complaintId, response.trim(), userId, isAdmin);
            return ResponseEntity.ok(complaint);
        } catch (RuntimeException e) {
            log.error("Error responding to complaint {}: {}", complaintId, e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Unexpected error responding to complaint {}: ", complaintId, e);
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{complaintId}/status")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Complaint> updateComplaintStatus(
            @PathVariable Long complaintId,
            @RequestBody Map<String, String> request) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                log.error("Unable to get current user ID for updating complaint status");
                return ResponseEntity.badRequest().build();
            }
            
            String statusStr = request.get("status");
            if (statusStr == null) {
                log.error("Status is null");
                return ResponseEntity.badRequest().build();
            }
            
            Complaint.ComplaintStatus status = Complaint.ComplaintStatus.valueOf(statusStr);
            boolean isAdmin = isCurrentUserAdmin();
            
            Complaint complaint = complaintService.updateComplaintStatus(complaintId, status, userId, isAdmin);
            return ResponseEntity.ok(complaint);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", request.get("status"));
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("Error updating complaint status: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error updating complaint status: ", e);
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                String authName = auth.getName();
                log.debug("Authentication name: {}", authName);
                try {
                    // Try to parse as Long first (if it's a user ID)
                    return Long.parseLong(authName);
                } catch (NumberFormatException e) {
                    // If it's an email, find user by email
                    User user = userRepository.findByEmail(authName).orElse(null);
                    if (user != null) {
                        log.debug("Found user by email: {}", user.getId());
                        return user.getId();
                    } else {
                        log.error("User not found for email: {}", authName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting current user ID: ", e);
        }
        return null;
    }

    @DeleteMapping("/{complaintId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long complaintId) {
        try {
            Long userId = getCurrentUserId();
            if (userId == null) {
                log.error("Unable to get current user ID for deleting complaint");
                return ResponseEntity.badRequest().build();
            }
            
            boolean isAdmin = isCurrentUserAdmin();
            complaintService.deleteComplaint(complaintId, userId, isAdmin);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error deleting complaint {}: {}", complaintId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error deleting complaint {}: ", complaintId, e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isCurrentUserAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities() != null) {
                return auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }
        } catch (Exception e) {
            log.error("Error checking admin role: ", e);
        }
        return false;
    }
}

