package com.example.learning_service.controller;

import com.example.learning_service.entity.User;
import com.example.learning_service.repository.EnrollmentRepository;
import com.example.learning_service.repository.LessonRepository;
import com.example.learning_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final String uploadDir = "uploads/";
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @GetMapping("/lessons/videos/{filename:.+}")
    public ResponseEntity<Resource> serveVideo(@PathVariable String filename) {
        try {
            log.debug("Serving video: {}", filename);
            
            // Simple approach: Serve the file if it exists (like course images)
            // Access control is handled at the API level (lessons are not returned to unauthorized users)
            Path filePath = Paths.get(uploadDir + "lessons/videos/").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes") // Enable byte-range requests for video streaming
                        .body(resource);
            } else {
                log.warn("Video file not found: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("Malformed URL exception: ", e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("IO exception: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected exception: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error getting current user ID: ", e);
            // If authentication fails, return null (anonymous user)
        }
        return null;
    }

    @GetMapping("/courses/images/{filename:.+}")
    public ResponseEntity<Resource> serveCourseImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir + "courses/").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/lessons/attachments/{filename:.+}")
    public ResponseEntity<Resource> serveLessonAttachment(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir + "lessons/").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
