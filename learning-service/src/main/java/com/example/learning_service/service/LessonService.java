package com.example.learning_service.service;

import com.example.learning_service.dto.LessonRequest;
import com.example.learning_service.entity.Lesson;
import com.example.learning_service.entity.Module;
import com.example.learning_service.repository.LessonRepository;
import com.example.learning_service.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LessonService {
    
    private final LessonRepository lessonRepository;
    private final ModuleRepository moduleRepository;
    
    public Lesson createLesson(LessonRequest request) {
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found"));
        
        Lesson lesson = new Lesson();
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setModule(module);
        lesson.setType(request.getType());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setVideoFileName(request.getVideoFileName());
        lesson.setVideoDuration(request.getVideoDuration());
        lesson.setContent(request.getContent());
        lesson.setAttachmentUrl(request.getAttachmentUrl());
        lesson.setAttachmentFileName(request.getAttachmentFileName());
        lesson.setFree(request.isFree());
        
        // Set order index
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        } else {
            Long lessonCount = lessonRepository.countByModuleId(request.getModuleId());
            lesson.setOrderIndex(lessonCount.intValue());
        }
        
        return lessonRepository.save(lesson);
    }
    
    public Lesson updateLesson(Long id, LessonRequest request) {
        log.info("=== LessonService.updateLesson ===");
        log.info("Lesson ID: {}", id);
        log.info("Request - videoUrl: {}, videoFileName: {}, videoDuration: {}", 
            request.getVideoUrl(), request.getVideoFileName(), request.getVideoDuration());
        
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Lesson not found with ID: {}", id);
                    return new RuntimeException("Lesson not found");
                });
        
        log.info("Existing lesson before update - videoUrl: {}, videoFileName: {}, videoDuration: {}", 
            lesson.getVideoUrl(), lesson.getVideoFileName(), lesson.getVideoDuration());
        
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setType(request.getType());
        
        // Only update video fields if they are provided (not null)
        // This preserves existing video information when editing other fields
        if (request.getVideoUrl() != null) {
            log.info("Updating videoUrl from '{}' to '{}'", lesson.getVideoUrl(), request.getVideoUrl());
            lesson.setVideoUrl(request.getVideoUrl());
        } else {
            log.info("videoUrl is null in request - keeping existing: {}", lesson.getVideoUrl());
        }
        
        if (request.getVideoFileName() != null) {
            log.info("Updating videoFileName from '{}' to '{}'", lesson.getVideoFileName(), request.getVideoFileName());
            lesson.setVideoFileName(request.getVideoFileName());
        } else {
            log.info("videoFileName is null in request - keeping existing: {}", lesson.getVideoFileName());
        }
        
        if (request.getVideoDuration() != null) {
            log.info("Updating videoDuration from '{}' to '{}'", lesson.getVideoDuration(), request.getVideoDuration());
            lesson.setVideoDuration(request.getVideoDuration());
        } else {
            log.info("videoDuration is null in request - keeping existing: {}", lesson.getVideoDuration());
        }
        
        // Only update content if provided
        if (request.getContent() != null) {
            lesson.setContent(request.getContent());
        }
        
        // Only update attachment fields if provided
        if (request.getAttachmentUrl() != null) {
            lesson.setAttachmentUrl(request.getAttachmentUrl());
        }
        if (request.getAttachmentFileName() != null) {
            lesson.setAttachmentFileName(request.getAttachmentFileName());
        }
        
        lesson.setFree(request.isFree());
        
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }
        
        Lesson savedLesson = lessonRepository.save(lesson);
        log.info("Lesson saved - ID: {}, videoUrl: {}, videoFileName: {}, videoDuration: {}", 
            savedLesson.getId(), savedLesson.getVideoUrl(), savedLesson.getVideoFileName(), savedLesson.getVideoDuration());
        
        return savedLesson;
    }
    
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }
    
    public List<Lesson> getLessonsByModule(Long moduleId) {
        return lessonRepository.findByModuleIdOrderByOrderIndex(moduleId);
    }
    
    public List<Lesson> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByModuleAndLesson(courseId);
    }
    
    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }
}
