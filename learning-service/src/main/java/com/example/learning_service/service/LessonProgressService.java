package com.example.learning_service.service;

import com.example.learning_service.entity.*;
import com.example.learning_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentService enrollmentService;

    public LessonProgress markLessonAsCompleted(Long userId, Long lessonId) {
        System.out.println("📝 Marking lesson " + lessonId + " as completed for user " + userId);
        
        // Get the lesson
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found with ID: " + lessonId));

        Long courseId = lesson.getModule().getCourse().getId();
        System.out.println("Found lesson in course ID: " + courseId);

        // Get the enrollment for this user and course
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("No enrollment found for user " + userId + " in course " + courseId));
        
        System.out.println("Found enrollment: ID=" + enrollment.getId());

        // Check if progress already exists
        Optional<LessonProgress> existingProgress = lessonProgressRepository.findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId);

        LessonProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            System.out.println("Updating existing lesson progress record");
        } else {
            progress = new LessonProgress();
            progress.setEnrollment(enrollment);
            progress.setLesson(lesson);
            System.out.println("Creating new lesson progress record");
        }

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progress.setLastAccessedAt(LocalDateTime.now());

        LessonProgress savedProgress = lessonProgressRepository.save(progress);
        System.out.println("Saved lesson progress with ID: " + savedProgress.getId() + ", completed: " + savedProgress.isCompleted());

        // Update course progress
        updateCourseProgress(enrollment);

        return savedProgress;
    }

    public LessonProgress updateLessonProgress(Long userId, Long lessonId, Long watchedDuration) {
        // Find the user's enrollment for this lesson's course
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        
        Long courseId = lesson.getModule().getCourse().getId();
        
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new RuntimeException("User is not enrolled in this course"));

        // Find or create lesson progress
        LessonProgress progress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId)
                .orElse(new LessonProgress());

        if (progress.getId() == null) {
            progress.setEnrollment(enrollment);
            progress.setLesson(lesson);
        }

        progress.setWatchedDuration(watchedDuration);
        progress.setLastAccessedAt(LocalDateTime.now());

        // Auto-complete if watched duration reaches lesson duration
        if (lesson.getVideoDuration() != null && watchedDuration >= lesson.getVideoDuration()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        LessonProgress savedProgress = lessonProgressRepository.save(progress);

        // Update overall course progress if lesson was completed
        if (progress.isCompleted()) {
            updateCourseProgress(enrollment);
        }

        return savedProgress;
    }

    public List<LessonProgress> getUserLessonProgress(Long userId, Long courseId) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId);
        
        if (enrollmentOpt.isPresent()) {
            return lessonProgressRepository.findByEnrollmentId(enrollmentOpt.get().getId());
        } else {
            // User is not enrolled, return empty list (similar to getCourseProgressPercentage)
            System.out.println("No enrollment found for user " + userId + " in course " + courseId + ", returning empty progress list");
            return Collections.emptyList();
        }
    }

    public Optional<LessonProgress> getLessonProgress(Long userId, Long lessonId) {
        return lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId);
    }

    private void updateCourseProgress(Enrollment enrollment) {
        long completedLessons = lessonProgressRepository.countCompletedLessonsByEnrollment(enrollment.getId());
        long totalLessons = lessonProgressRepository.countTotalLessonsByCourse(enrollment.getCourse().getId());

        System.out.println("Updating progress for enrollment " + enrollment.getId() + 
                          ": completed=" + completedLessons + ", total=" + totalLessons);
        
        // Debug: Check all lesson progress records for this enrollment
        List<LessonProgress> allProgress = lessonProgressRepository.findByEnrollmentId(enrollment.getId());
        System.out.println("Total lesson progress records for enrollment " + enrollment.getId() + ": " + allProgress.size());
        for (LessonProgress lp : allProgress) {
            System.out.println("  Lesson " + lp.getLesson().getId() + " - completed: " + lp.isCompleted());
        }

        if (totalLessons > 0) {
            // Calculate progress based ONLY on completed lessons / total lessons (not duration)
            double progressPercentage = (double) completedLessons / totalLessons * 100.0;
            
            // Ensure exactly 100% when all lessons are completed
            if (completedLessons == totalLessons) {
                progressPercentage = 100.0;
            }
            
            // Round to 2 decimal places for precision
            progressPercentage = Math.round(progressPercentage * 100.0) / 100.0;
            
            System.out.println("Calculated progress: " + progressPercentage + "% (" + completedLessons + "/" + totalLessons + " lessons)");
            enrollmentService.updateProgress(enrollment.getId(), progressPercentage);
        } else {
            System.out.println("No lessons found for course " + enrollment.getCourse().getId());
            enrollmentService.updateProgress(enrollment.getId(), 0.0);
        }
    }

    public double getCourseProgressPercentage(Long courseId, Long userId) {
        try {
            System.out.println("Getting progress for courseId: " + courseId + ", userId: " + userId);
            Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId)
                    .orElse(null);
            
            if (enrollment == null) {
                System.out.println("No enrollment found for user " + userId + " in course " + courseId);
                return 0.0;
            }
            
            // Recalculate progress to ensure it's up to date
            updateCourseProgress(enrollment);
            
            // Refresh enrollment to get updated progress
            enrollment = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId).orElse(enrollment);
            
            double progress = enrollment.getProgress();
            System.out.println("Found enrollment with progress: " + progress);
            return progress;
        } catch (Exception e) {
            System.err.println("Error getting course progress: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    
    public void recalculateCourseProgress(Long courseId, Long userId) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId);
        if (enrollmentOpt.isPresent()) {
            updateCourseProgress(enrollmentOpt.get());
        } else {
            System.out.println("Warning: No enrollment found for user " + userId + " and course " + courseId);
        }
    }
    
    public void markAllLessonsCompleted(Long courseId, Long userId) {
        // Get all lessons for this course
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByModuleAndLesson(courseId);
        System.out.println("Found " + lessons.size() + " lessons for course " + courseId);
        
        if (lessons.isEmpty()) {
            System.out.println("No lessons found for course " + courseId + ". Checking if course exists...");
            // Let's also check what lessons exist in the database
            List<Lesson> allLessons = lessonRepository.findAll();
            System.out.println("Total lessons in database: " + allLessons.size());
            for (Lesson lesson : allLessons) {
                System.out.println("  Lesson ID: " + lesson.getId() + 
                                 ", Title: " + lesson.getTitle() + 
                                 ", Module ID: " + lesson.getModule().getId() + 
                                 ", Course ID: " + lesson.getModule().getCourse().getId());
            }
        }
        
        for (Lesson lesson : lessons) {
            markLessonAsCompleted(userId, lesson.getId());
        }
    }
    
    public Map<String, Object> debugLessonsAndCourses() {
        Map<String, Object> debug = new HashMap<>();
        
        // Get all lessons
        List<Lesson> allLessons = lessonRepository.findAll();
        debug.put("totalLessons", allLessons.size());
        
        List<Map<String, Object>> lessonDetails = new ArrayList<>();
        for (Lesson lesson : allLessons) {
            Map<String, Object> lessonInfo = new HashMap<>();
            lessonInfo.put("lessonId", lesson.getId());
            lessonInfo.put("lessonTitle", lesson.getTitle());
            lessonInfo.put("moduleId", lesson.getModule().getId());
            lessonInfo.put("moduleTitle", lesson.getModule().getTitle());
            lessonInfo.put("courseId", lesson.getModule().getCourse().getId());
            lessonInfo.put("courseTitle", lesson.getModule().getCourse().getTitle());
            lessonDetails.add(lessonInfo);
        }
        debug.put("lessons", lessonDetails);
        
        // Test the specific query for course 1
        List<Lesson> course1Lessons = lessonRepository.findByCourseIdOrderByModuleAndLesson(1L);
        debug.put("course1LessonsCount", course1Lessons.size());
        
        return debug;
    }
}
