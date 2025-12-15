package com.example.learning_service.controller;

import com.example.learning_service.dto.LessonRequest;
import com.example.learning_service.dto.LessonResponse;
import com.example.learning_service.entity.Lesson;
import com.example.learning_service.service.LessonService;
import com.example.learning_service.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class LessonController {
    
    private final LessonService lessonService;
    private final FileStorageService fileStorageService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<LessonResponse> createLesson(@Valid @RequestBody LessonRequest request) {
        try {
            Lesson lesson = lessonService.createLesson(request);
            LessonResponse response = LessonResponse.fromEntity(lesson);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping(value = "/with-video", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<LessonResponse> createLessonWithVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("moduleId") Long moduleId,
            @RequestParam(value = "videoDuration", defaultValue = "0") Long videoDuration,
            @RequestParam(value = "type", defaultValue = "VIDEO") String type,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            log.info("=== CREATING LESSON WITH VIDEO ===");
            log.info("Title: {}, ModuleId: {}, Duration: {}, Type: {}", title, moduleId, videoDuration, type);
            log.info("Video file provided: {}", videoFile != null && !videoFile.isEmpty());
            
            LessonRequest request = new LessonRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setModuleId(moduleId);
            request.setVideoDuration(videoDuration);
            request.setType(Lesson.LessonType.valueOf(type));
            
            // Handle video file upload
            if (videoFile != null && !videoFile.isEmpty()) {
                log.info("Video file details - Name: {}, Size: {}, ContentType: {}", 
                    videoFile.getOriginalFilename(), videoFile.getSize(), videoFile.getContentType());
                
                if (fileStorageService.isVideoFile(videoFile)) {
                    String videoPath = fileStorageService.storeFile(videoFile, "lessons/videos");
                    log.info("Video stored at path: {}", videoPath);
                    
                    // videoPath is like "lessons/videos/uuid.mp4", extract just the filename
                    String storedFileName = videoPath.substring(videoPath.lastIndexOf("/") + 1);
                    log.info("Extracted filename: {}", storedFileName);
                    
                    request.setVideoUrl(videoPath);
                    request.setVideoFileName(storedFileName); // Store the actual stored filename (UUID + extension)
                    
                    log.info("Request videoUrl: {}, videoFileName: {}", request.getVideoUrl(), request.getVideoFileName());
                } else {
                    log.warn("File is not a valid video file: {}", videoFile.getContentType());
                }
            } else {
                log.info("No video file provided for lesson creation");
            }
            
            Lesson lesson = lessonService.createLesson(request);
            log.info("Lesson created successfully - ID: {}, videoUrl: {}, videoFileName: {}", 
                lesson.getId(), lesson.getVideoUrl(), lesson.getVideoFileName());
            
            LessonResponse response = LessonResponse.fromEntity(lesson);
            log.info("Response - videoUrl: {}, videoFileName: {}", response.getVideoUrl(), response.getVideoFileName());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("ERROR creating lesson with video: ", e);
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable Long id, @Valid @RequestBody LessonRequest request) {
        try {
            Lesson lesson = lessonService.updateLesson(id, request);
            LessonResponse response = LessonResponse.fromEntity(lesson);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping(value = "/{id}/with-video", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<LessonResponse> updateLessonWithVideo(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("moduleId") Long moduleId,
            @RequestParam(value = "videoDuration", defaultValue = "0") Long videoDuration,
            @RequestParam(value = "type", defaultValue = "VIDEO") String type,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            log.info("=== UPDATING LESSON WITH VIDEO ===");
            log.info("Lesson ID: {}, Title: {}, ModuleId: {}, Duration: {}, Type: {}", 
                id, title, moduleId, videoDuration, type);
            log.info("Video file provided: {}", videoFile != null && !videoFile.isEmpty());
            
            // Get existing lesson first to check current state
            Lesson existingLesson = lessonService.getLessonById(id);
            log.info("Existing lesson - videoUrl: {}, videoFileName: {}, videoDuration: {}", 
                existingLesson.getVideoUrl(), existingLesson.getVideoFileName(), existingLesson.getVideoDuration());
            
            LessonRequest request = new LessonRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setModuleId(moduleId);
            request.setVideoDuration(videoDuration);
            request.setType(Lesson.LessonType.valueOf(type));
            
            // Handle video file upload
            if (videoFile != null && !videoFile.isEmpty()) {
                log.info("New video file details - Name: {}, Size: {}, ContentType: {}", 
                    videoFile.getOriginalFilename(), videoFile.getSize(), videoFile.getContentType());
                
                if (fileStorageService.isVideoFile(videoFile)) {
                    // Delete old video file if it exists
                    if (existingLesson.getVideoFileName() != null) {
                        try {
                            String oldVideoPath = "lessons/videos/" + existingLesson.getVideoFileName();
                            log.info("Deleting old video file: {}", oldVideoPath);
                            fileStorageService.deleteFile(oldVideoPath);
                            log.info("Old video file deleted successfully");
                        } catch (Exception e) {
                            log.error("Error deleting old video file: {}", e.getMessage());
                            // Continue anyway - old file might not exist
                        }
                    }
                    
                    String videoPath = fileStorageService.storeFile(videoFile, "lessons/videos");
                    log.info("New video stored at path: {}", videoPath);
                    
                    // videoPath is like "lessons/videos/uuid.mp4", extract just the filename
                    String storedFileName = videoPath.substring(videoPath.lastIndexOf("/") + 1);
                    log.info("Extracted filename: {}", storedFileName);
                    
                    request.setVideoUrl(videoPath);
                    request.setVideoFileName(storedFileName);
                    
                    log.info("Request videoUrl: {}, videoFileName: {}", request.getVideoUrl(), request.getVideoFileName());
                } else {
                    log.warn("File is not a valid video file: {}", videoFile.getContentType());
                }
            } else {
                // No new video file provided - preserve existing video information
                log.info("No new video file - preserving existing video information");
                
                // Only preserve video info if it exists (not null)
                if (existingLesson.getVideoUrl() != null) {
                    request.setVideoUrl(existingLesson.getVideoUrl());
                    log.info("Preserved videoUrl: {}", existingLesson.getVideoUrl());
                } else {
                    log.warn("Existing lesson has no videoUrl - this might be a problem");
                }
                
                if (existingLesson.getVideoFileName() != null) {
                    request.setVideoFileName(existingLesson.getVideoFileName());
                    log.info("Preserved videoFileName: {}", existingLesson.getVideoFileName());
                } else {
                    log.warn("Existing lesson has no videoFileName - this might be a problem");
                }
                
                // Also preserve the existing video duration if not provided or is 0
                if ((videoDuration == null || videoDuration == 0) && existingLesson.getVideoDuration() != null) {
                    request.setVideoDuration(existingLesson.getVideoDuration());
                    log.info("Preserved videoDuration: {}", existingLesson.getVideoDuration());
                }
            }
            
            log.info("About to update lesson with request - videoUrl: {}, videoFileName: {}, videoDuration: {}", 
                request.getVideoUrl(), request.getVideoFileName(), request.getVideoDuration());
            
            Lesson lesson = lessonService.updateLesson(id, request);
            log.info("Lesson updated successfully - ID: {}, videoUrl: {}, videoFileName: {}", 
                lesson.getId(), lesson.getVideoUrl(), lesson.getVideoFileName());
            
            LessonResponse response = LessonResponse.fromEntity(lesson);
            log.info("Response - videoUrl: {}, videoFileName: {}", response.getVideoUrl(), response.getVideoFileName());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument error updating lesson {}: {}", id, e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            log.error("Runtime error updating lesson {}: {}", id, e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Unexpected error updating lesson {} with video: ", id, e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        try {
            lessonService.deleteLesson(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<LessonResponse>> getLessonsByModule(@PathVariable Long moduleId) {
        try {
            List<Lesson> lessons = lessonService.getLessonsByModule(moduleId);
            List<LessonResponse> responses = lessons.stream()
                .map(LessonResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonResponse>> getLessonsByCourse(@PathVariable Long courseId) {
        try {
            List<Lesson> lessons = lessonService.getLessonsByCourse(courseId);
            List<LessonResponse> responses = lessons.stream()
                .map(LessonResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable Long id) {
        try {
            Lesson lesson = lessonService.getLessonById(id);
            LessonResponse response = LessonResponse.fromEntity(lesson);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
