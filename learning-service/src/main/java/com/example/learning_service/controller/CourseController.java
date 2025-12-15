package com.example.learning_service.controller;

import com.example.learning_service.dto.CourseDTO;
import com.example.learning_service.dto.CourseDetailsResponse;
import com.example.learning_service.dto.CreateCourseRequest;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.EnrollmentRepository;
import com.example.learning_service.repository.UserRepository;
import com.example.learning_service.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/public")
    public ResponseEntity<List<CourseDTO>> getPublishedCourses() {
        List<CourseDTO> courses = courseService.getPublishedCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = courseService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getTags() {
        List<String> tags = courseService.getTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<CourseDTO>> getTeacherCourses() {
        List<CourseDTO> courses = courseService.getTeacherCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<CourseDetailsResponse> getCourseDetails(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        CourseDetailsResponse courseDetails = courseService.getCourseDetails(id, userId);
        return ResponseEntity.ok(courseDetails);
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCourseStudents(@PathVariable Long id) {
        try {
            Map<String, Object> students = courseService.getCourseStudents(id);
            return ResponseEntity.ok(students);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                String authName = auth.getName();
                try {
                    // Try to parse as Long first (if it's a user ID)
                    return Long.parseLong(authName);
                } catch (NumberFormatException e) {
                    // If it's an email, find user by email
                    User user = userRepository.findByEmail(authName).orElse(null);
                    if (user != null) {
                        return user.getId();
                    }
                }
            }
        } catch (Exception e) {
            // If authentication fails, return null (anonymous user)
        }
        return null;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseDTO course = courseService.createCourse(request);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CreateCourseRequest request) {
        CourseDTO course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(course);
    }

    @PostMapping("/{id}/thumbnail")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseDTO> uploadThumbnail(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            CourseDTO course = courseService.uploadCourseThumbnail(id, file);
            return ResponseEntity.ok(course);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/preview-video")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseDTO> uploadPreviewVideo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            CourseDTO course = courseService.uploadPreviewVideo(id, file);
            return ResponseEntity.ok(course);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<CourseDTO> publishCourse(@PathVariable Long id) {
        CourseDTO course = courseService.publishCourse(id);
        return ResponseEntity.ok(course);
    }

    @GetMapping("/{id}/delete-info")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCourseDeleteInfo(@PathVariable Long id) {
        Map<String, Object> info = courseService.getCourseDeleteInfo(id);
        return ResponseEntity.ok(info);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/admin/delete-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllCourses() {
        courseService.deleteAllCourses();
        return ResponseEntity.ok().build();
    }
}
