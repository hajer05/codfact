package com.example.learning_service.service;

import com.example.learning_service.dto.CourseDTO;
import com.example.learning_service.dto.CourseDetailsResponse;
import com.example.learning_service.dto.CreateCourseRequest;
import com.example.learning_service.entity.Course;
import com.example.learning_service.entity.Enrollment;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.CourseRepository;
import com.example.learning_service.repository.EnrollmentRepository;
import com.example.learning_service.repository.OrderRepository;
import com.example.learning_service.repository.OrderItemRepository;
import com.example.learning_service.repository.CartItemRepository;
import com.example.learning_service.repository.UserRepository;
import com.example.learning_service.repository.ComplaintRepository;
import com.example.learning_service.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ComplaintRepository complaintRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CourseDTO> getPublishedCourses() {
        return courseRepository.findByStatus(Course.CourseStatus.PUBLISHED).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CourseDTO> getTeacherCourses() {
        User currentUser = getCurrentUser();
        return courseRepository.findByTeacher(currentUser).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return convertToDTO(course);
    }

    public CourseDTO createCourse(CreateCourseRequest request) {
        User currentUser = getCurrentUser();
        
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setShortDescription(request.getShortDescription());
        course.setPrice(request.getPrice());
        course.setLevel(request.getLevel());
        course.setCategory(request.getCategory());
        course.setLanguage(request.getLanguage());
        course.setTeacher(currentUser);
        course.setStatus(Course.CourseStatus.DRAFT);

        Course savedCourse = courseRepository.save(course);

        // Notification email à l'enseignant
        emailService.sendCourseCreated(savedCourse, currentUser);
        return convertToDTO(savedCourse);
    }

    public CourseDTO updateCourse(Long id, CreateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        User currentUser = getCurrentUser();
        if (!course.getTeacher().getId().equals(currentUser.getId()) && 
            !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Not authorized to update this course");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setShortDescription(request.getShortDescription());
        course.setPrice(request.getPrice());
        course.setLevel(request.getLevel());
        course.setCategory(request.getCategory());
        course.setLanguage(request.getLanguage());

        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }

    public CourseDTO uploadCourseThumbnail(Long courseId, MultipartFile file) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!fileStorageService.isImageFile(file)) {
            throw new RuntimeException("Only image files are allowed for thumbnails");
        }

        // Delete old thumbnail if exists
        if (course.getThumbnailImage() != null) {
            try {
                fileStorageService.deleteFile(course.getThumbnailImage());
            } catch (IOException e) {
                // Log error but continue
            }
        }

        String filePath = fileStorageService.storeFile(file, "courses/thumbnails");
        course.setThumbnailImage(filePath);
        
        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }

    public CourseDTO uploadPreviewVideo(Long courseId, MultipartFile file) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!fileStorageService.isVideoFile(file)) {
            throw new RuntimeException("Only video files are allowed for preview videos");
        }

        // Delete old preview video if exists
        if (course.getPreviewVideo() != null) {
            try {
                fileStorageService.deleteFile(course.getPreviewVideo());
            } catch (IOException e) {
                // Log error but continue
            }
        }

        String filePath = fileStorageService.storeFile(file, "courses/previews");
        course.setPreviewVideo(filePath);
        
        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }

    public CourseDTO publishCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setStatus(Course.CourseStatus.PUBLISHED);
        Course savedCourse = courseRepository.save(course);
        return convertToDTO(savedCourse);
    }

    public CourseDetailsResponse getCourseDetails(Long id) {
        return getCourseDetails(id, null);
    }

    public CourseDetailsResponse getCourseDetails(Long id, Long userId) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Determine if user has access to content
        boolean includeContent = false;
        
        // Check if user is admin or teacher of the course - they always have access
        boolean isAdminOrTeacher = false;
        if (userId != null) {
            // Check if user is teacher of this course
            isAdminOrTeacher = course.getTeacher().getId().equals(userId);
            
            // Check if user is admin
            if (!isAdminOrTeacher) {
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.getAuthorities() != null) {
                        isAdminOrTeacher = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    }
                } catch (Exception e) {
                    // Ignore if can't check roles
                }
            }
        }
        
        if (isAdminOrTeacher) {
            // Admins and teachers always have full access
            includeContent = true;
        } else {
            // Free courses: everyone can see content
            if (course.getPrice() == null || course.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0) {
                includeContent = true;
            } else {
                // Paid courses: only enrolled users can see content
                if (userId != null) {
                    includeContent = enrollmentRepository.existsByStudentIdAndCourseId(userId, id);
                }
            }
        }
        
        return CourseDetailsResponse.fromEntity(course, includeContent);
    }

    public Map<String, Object> getCourseDeleteInfo(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Count enrollments
        long enrollmentCount = enrollmentRepository.countByCourseId(id);
        long activeEnrollmentCount = enrollmentRepository.countByCourseIdAndStatus(id, com.example.learning_service.entity.Enrollment.EnrollmentStatus.ACTIVE);
        
        // Count paid orders (only for paid courses)
        long paidOrdersCount = 0;
        if (course.getPrice() != null && course.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
            paidOrdersCount = orderRepository.countByCourseIdAndStatus(id, Order.OrderStatus.COMPLETED);
        }
        
        // Count complaints
        long complaintsCount = complaintRepository.findByCourseId(id).size();
        
        Map<String, Object> info = new HashMap<>();
        info.put("courseId", id);
        info.put("courseTitle", course.getTitle());
        info.put("totalEnrollments", enrollmentCount);
        info.put("activeEnrollments", activeEnrollmentCount);
        info.put("paidOrdersCount", paidOrdersCount);
        info.put("complaintsCount", complaintsCount);
        info.put("hasEnrollments", enrollmentCount > 0);
        info.put("hasPaidOrders", paidOrdersCount > 0);
        info.put("hasComplaints", complaintsCount > 0);
        info.put("canDeleteSafely", enrollmentCount == 0 && paidOrdersCount == 0 && complaintsCount == 0);
        
        return info;
    }

    public Map<String, Object> getCourseStudents(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Get enrolled students
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        
        // Get purchased students (from completed orders)
        List<com.example.learning_service.entity.OrderItem> orderItems = 
            orderItemRepository.findByCourseId(courseId);
        
        Set<Long> enrolledUserIds = enrollments.stream()
            .map(e -> e.getStudent().getId())
            .collect(Collectors.toSet());
        
        Set<Long> purchasedUserIds = orderItems.stream()
            .filter(oi -> oi.getOrder().getStatus() == com.example.learning_service.entity.Order.OrderStatus.COMPLETED)
            .map(oi -> oi.getOrder().getUser().getId())
            .collect(Collectors.toSet());
        
        // Combine all user IDs
        Set<Long> allUserIds = new HashSet<>(enrolledUserIds);
        allUserIds.addAll(purchasedUserIds);
        
        // Get user details
        List<Map<String, Object>> students = new java.util.ArrayList<>();
        for (Long userId : allUserIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("id", user.getId());
                studentInfo.put("firstName", user.getFirstName());
                studentInfo.put("lastName", user.getLastName());
                studentInfo.put("email", user.getEmail());
                studentInfo.put("isEnrolled", enrolledUserIds.contains(userId));
                studentInfo.put("isPurchased", purchasedUserIds.contains(userId));
                
                // Get enrollment details if enrolled
                if (enrolledUserIds.contains(userId)) {
                    Enrollment enrollment = enrollments.stream()
                        .filter(e -> e.getStudent().getId().equals(userId))
                        .findFirst()
                        .orElse(null);
                    if (enrollment != null) {
                        studentInfo.put("enrolledAt", enrollment.getEnrolledAt());
                        studentInfo.put("progress", enrollment.getProgress());
                        studentInfo.put("status", enrollment.getStatus().name());
                    }
                }
                
                students.add(studentInfo);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("courseId", courseId);
        result.put("courseTitle", course.getTitle());
        result.put("totalStudents", students.size());
        result.put("enrolledCount", enrolledUserIds.size());
        result.put("purchasedCount", purchasedUserIds.size());
        result.put("students", students);
        
        return result;
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        User currentUser = getCurrentUser();
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        // Check if user is the teacher or admin
        // Teachers can only delete their own courses
        if (!course.getTeacher().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new RuntimeException("Not authorized to delete this course");
        }
        
        // If admin, delete complaints associated with the course first
        // If teacher, prevent deletion if complaints exist
        long complaintsCount = complaintRepository.findByCourseId(id).size();
        if (complaintsCount > 0) {
            if (isAdmin) {
                // Admin can delete course - complaints will be deleted automatically via cascade
                // But we delete them explicitly to avoid any issues
                List<com.example.learning_service.entity.Complaint> complaints = complaintRepository.findByCourseId(id);
                complaintRepository.deleteAll(complaints);
            } else {
                // Teacher cannot delete course with complaints
                throw new RuntimeException("Cannot delete course with " + complaintsCount + " complaint(s). Please resolve or delete all complaints first.");
            }
        }
        
        // Delete order items referencing this course first
        // This is necessary because of foreign key constraints
        try {
            orderItemRepository.deleteByCourseId(id);
        } catch (Exception e) {
            // Log but continue - some items might not exist
            System.err.println("Error deleting order items: " + e.getMessage());
        }
        
        // Delete cart items referencing this course
        try {
            List<com.example.learning_service.entity.CartItem> cartItems = cartItemRepository.findByCourseId(id);
            if (!cartItems.isEmpty()) {
                cartItemRepository.deleteAll(cartItems);
            }
        } catch (Exception e) {
            // Log but continue
            System.err.println("Error deleting cart items: " + e.getMessage());
        }
        
        // Delete associated files
        if (course.getThumbnailImage() != null) {
            try {
                fileStorageService.deleteFile(course.getThumbnailImage());
            } catch (IOException e) {
                // Log error but continue
            }
        }
        if (course.getPreviewVideo() != null) {
            try {
                fileStorageService.deleteFile(course.getPreviewVideo());
            } catch (IOException e) {
                // Log error but continue
            }
        }
        
        courseRepository.delete(course);
    }
    
    public void deleteAllCourses() {
        User currentUser = getCurrentUser();
        // Only admins can delete all courses
        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Only administrators can delete all courses");
        }
        
        // Delete courses one by one to handle cascade properly
        // Skip courses with complaints and collect errors
        List<Course> allCourses = courseRepository.findAll();
        List<String> errors = new java.util.ArrayList<>();
        int deletedCount = 0;
        
        for (Course course : allCourses) {
            try {
                // Admin can delete courses even with complaints - complaints will be deleted automatically
                deleteCourse(course.getId());
                deletedCount++;
            } catch (Exception e) {
                errors.add("Failed to delete course \"" + course.getTitle() + "\": " + e.getMessage());
            }
        }
        
        // If there were errors, throw an exception with details
        if (!errors.isEmpty()) {
            String errorMessage = "Deleted " + deletedCount + " course(s). " + errors.size() + " course(s) could not be deleted:\n" + 
                                  String.join("\n", errors);
            throw new RuntimeException(errorMessage);
        }
    }

    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setShortDescription(course.getShortDescription());
        dto.setThumbnailImage(course.getThumbnailImage());
        dto.setPreviewVideo(course.getPreviewVideo());
        dto.setPrice(course.getPrice());
        dto.setLevel(course.getLevel());
        dto.setStatus(course.getStatus());
        dto.setCategory(course.getCategory());
        dto.setLanguage(course.getLanguage());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setPublishedAt(course.getPublishedAt());
        dto.setTeacherName(course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName());
        dto.setTeacherId(course.getTeacher().getId());
        
        if (course.getModules() != null) {
            dto.setModuleCount(course.getModules().size());
            dto.setLessonCount(course.getModules().stream()
                    .mapToInt(module -> module.getLessons() != null ? module.getLessons().size() : 0)
                    .sum());
        }
        
        return dto;
    }

    public List<String> getCategories() {
        return courseRepository.findAll().stream()
                .map(Course::getCategory)
                .filter(category -> category != null && !category.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getTags() {
        // For now, return a static list of common programming tags
        // This can be enhanced later to extract from course descriptions or add a tags field
        return List.of(
            "JavaScript", "React", "Angular", "Vue.js", "Node.js",
            "Python", "Java", "Spring Boot", "Django", "Flask",
            "HTML", "CSS", "Tailwind CSS", "Bootstrap",
            "TypeScript", "PHP", "Laravel", "Symfony",
            "C#", ".NET", "ASP.NET",
            "Go", "Rust", "Kotlin", "Swift",
            "Docker", "Kubernetes", "AWS", "Azure",
            "MongoDB", "PostgreSQL", "MySQL",
            "Git", "DevOps", "CI/CD",
            "Machine Learning", "Data Science", "AI",
            "Mobile Development", "Web Development", "Backend", "Frontend"
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}
