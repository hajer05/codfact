package com.example.learning_service.service;

import com.example.learning_service.entity.Course;
import com.example.learning_service.entity.Enrollment;
import com.example.learning_service.entity.Order;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.CourseRepository;
import com.example.learning_service.repository.EnrollmentRepository;
import com.example.learning_service.repository.OrderRepository;
import com.example.learning_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public Enrollment enrollInCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if user is already enrolled
        Optional<Enrollment> existingEnrollment = enrollmentRepository
                .findByStudentIdAndCourseId(userId, courseId);
        
        if (existingEnrollment.isPresent()) {
            throw new RuntimeException("User is already enrolled in this course");
        }

        // For paid courses, check if user has a completed order
        if (course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            // Check if user has a completed order with this course
            boolean hasPaidOrder = orderRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, Order.OrderStatus.COMPLETED);
            
            if (!hasPaidOrder) {
                throw new RuntimeException("You must purchase this course before enrolling");
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(user);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgress(0.0);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);

        Enrollment saved = enrollmentRepository.save(enrollment);
        // Envoyer un email si inscription directe (cours gratuit)
        try { emailService.sendEnrollmentConfirmed(saved); } catch (Exception ignored) {}
        return saved;
    }

    public void unenrollFromCourse(Long userId, Long courseId) {
        // Check for active enrollment first
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId);
        
        if (enrollmentOpt.isEmpty()) {
            throw new RuntimeException("Enrollment not found");
        }
        
        Enrollment enrollment = enrollmentOpt.get();
        
        // Only allow unenrolling from ACTIVE enrollments
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ACTIVE) {
            throw new RuntimeException("Cannot unenroll from a course that is " + enrollment.getStatus().name().toLowerCase());
        }
        
        // Update status instead of deleting to preserve history
        enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getUserEnrollments(Long userId) {
        // Return both ACTIVE and COMPLETED enrollments so users can access completed courses
        return enrollmentRepository.findByStudentId(userId).stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE || 
                            e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .toList();
    }

    public boolean isUserEnrolled(Long userId, Long courseId) {
        // Check for ACTIVE or COMPLETED enrollment (users should still have access to completed courses)
        Optional<Enrollment> enrollment = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId);
        return enrollment.isPresent() && 
               (enrollment.get().getStatus() == Enrollment.EnrollmentStatus.ACTIVE ||
                enrollment.get().getStatus() == Enrollment.EnrollmentStatus.COMPLETED);
    }

    public Optional<Enrollment> getEnrollment(Long userId, Long courseId) {
        return enrollmentRepository.findByStudentIdAndCourseId(userId, courseId);
    }

    public Enrollment updateProgress(Long enrollmentId, Double progress) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        
        enrollment.setProgress(progress);
        
        // Mark as completed if progress is 100%
        if (progress >= 100.0) {
            enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
        }
        
        return enrollmentRepository.save(enrollment);
    }

    public List<Enrollment> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }
}
