package com.example.learning_service.service;

import com.example.learning_service.dto.CertificateDto;
import com.example.learning_service.entity.Certificate;
import com.example.learning_service.entity.Course;
import com.example.learning_service.entity.Enrollment;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.CertificateRepository;
import com.example.learning_service.repository.CourseRepository;
import com.example.learning_service.repository.EnrollmentRepository;
import com.example.learning_service.repository.QuizAttemptRepository;
import com.example.learning_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificateService {
    
    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressService lessonProgressService;
    private final QuizAttemptRepository quizAttemptRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    public CertificateDto generateCertificate(Long userId, Long courseId) {
        System.out.println("Generating certificate for userId: " + userId + ", courseId: " + courseId);
        
        // Check if certificate already exists
        Optional<Certificate> existingCertificate = certificateRepository.findByUserIdAndCourseId(userId, courseId);
        if (existingCertificate.isPresent()) {
            System.out.println("Certificate already exists, returning existing one");
            return convertToDto(existingCertificate.get());
        }
        
        // Check if user is enrolled first
        System.out.println("Looking for enrollment with studentId: " + userId + " and courseId: " + courseId);
        Optional<Enrollment> enrollment = enrollmentRepository.findByStudentIdAndCourseId(userId, courseId);
        System.out.println("Enrollment found: " + enrollment.isPresent());
        if (enrollment.isEmpty()) {
            // Let's also check all enrollments for this user to debug
            List<Enrollment> userEnrollments = enrollmentRepository.findByStudentIdWithCourse(userId);
            System.out.println("User " + userId + " has " + userEnrollments.size() + " total enrollments:");
            for (Enrollment e : userEnrollments) {
                System.out.println("  - Course ID: " + e.getCourse().getId() + ", Status: " + e.getStatus());
            }
            throw new RuntimeException("User is not enrolled in this course. Please enroll first before generating certificate.");
        }
        
        // Check if user has completed the course (100% progress)
        double progress = lessonProgressService.getCourseProgressPercentage(courseId, userId);
        System.out.println("Course progress: " + progress + "%");
        
        // Vérifier la progression du cours
        if (progress < 100.0 && progress > 0.0) {
            throw new RuntimeException("Le cours doit être complété à 100% avant de générer le certificat. Progression actuelle: " + progress + "%");
        }
        
        // NOUVELLE RÈGLE: Vérifier que l'utilisateur a réussi le quiz avec au moins 70/100
        System.out.println("Checking quiz completion for user " + userId + " and course " + courseId);
        
        // Vérifier s'il existe une tentative de quiz réussie
        boolean hasPassedQuiz = quizAttemptRepository.hasPassedQuiz(userId, courseId);
        
        if (!hasPassedQuiz) {
            throw new RuntimeException("Vous devez réussir le quiz du cours avec une note d'au moins 70/100 avant de pouvoir obtenir le certificat. Le quiz doit être approuvé par l'enseignant ou l'administrateur.");
        }
        
        System.out.println("Quiz requirement satisfied for user " + userId);
        
        // Pour les cours sans leçons (progress = 0%), permettre la génération en mode test
        if (progress == 0.0) {
            System.out.println("WARNING: Generating certificate for course with no lessons (testing mode)");
        }
        
        // Get user and course details
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Generate certificate file
        String fileName = generateCertificateFile(user, course);
        
        // Create certificate record
        Certificate certificate = new Certificate();
        certificate.setUserId(userId);
        certificate.setCourseId(courseId);
        certificate.setCertificateFileName(fileName);
        certificate.setCertificateUrl("/uploads/certificates/" + fileName);
        certificate.setIssuedAt(LocalDateTime.now());
        certificate.setStudentName(user.getFirstName() + " " + user.getLastName());
        certificate.setCourseName(course.getTitle());
        certificate.setInstructorName(course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName());
        
        Certificate savedCertificate = certificateRepository.save(certificate);
        return convertToDto(savedCertificate);
    }
    
    private String generateCertificateFile(User user, Course course) {
        try {
            // Create certificate image - larger size for better quality
            int width = 1600;
            int height = 1200;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // Set rendering hints for better quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // CodingFactory brand colors
            Color codingFactoryRed = new Color(220, 38, 38); // Red from logo
            Color codingFactoryBlack = new Color(17, 24, 39); // Dark black
            Color lightGray = new Color(249, 250, 251);
            Color darkGray = new Color(75, 85, 99);
            
            // Background with subtle gradient
            GradientPaint gradient = new GradientPaint(0, 0, Color.WHITE, width, height, lightGray);
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
            
            // Decorative border with CodingFactory colors
            g2d.setStroke(new BasicStroke(12));
            g2d.setColor(codingFactoryRed);
            g2d.drawRect(40, 40, width - 80, height - 80);
            
            // Inner border
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(codingFactoryBlack);
            g2d.drawRect(60, 60, width - 120, height - 120);
            
            // Load and draw CodingFactory logo (if available)
            try {
                BufferedImage logo = ImageIO.read(new File("frontoffice/public/logo/logo-codingfactory.png"));
                if (logo != null) {
                    // Scale logo to appropriate size
                    int logoWidth = 300;
                    int logoHeight = (int) (logo.getHeight() * ((double) logoWidth / logo.getWidth()));
                    g2d.drawImage(logo, (width - logoWidth) / 2, 120, logoWidth, logoHeight, null);
                }
            } catch (Exception e) {
                // If logo can't be loaded, draw text logo
                g2d.setColor(codingFactoryBlack);
                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                FontMetrics fm = g2d.getFontMetrics();
                String logoText = "Coding";
                int logoTextWidth = fm.stringWidth(logoText);
                g2d.drawString(logoText, (width - logoTextWidth - 100) / 2, 180);
                
                g2d.setColor(codingFactoryRed);
                String factoryText = "Factory";
                g2d.drawString(factoryText, (width - logoTextWidth - 100) / 2 + logoTextWidth, 180);
                
                g2d.setColor(darkGray);
                g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                fm = g2d.getFontMetrics();
                String tagline = "BOOST YOUR CAREER";
                int taglineX = (width - fm.stringWidth(tagline)) / 2;
                g2d.drawString(tagline, taglineX, 200);
            }
            
            // Certificate title
            g2d.setColor(codingFactoryBlack);
            g2d.setFont(new Font("Serif", Font.BOLD, 64));
            FontMetrics fm = g2d.getFontMetrics();
            String title = "Certificate of Achievement";
            int titleX = (width - fm.stringWidth(title)) / 2;
            g2d.drawString(title, titleX, 320);
            
            // Decorative line under title
            g2d.setStroke(new BasicStroke(4));
            g2d.setColor(codingFactoryRed);
            g2d.drawLine(titleX, 340, titleX + fm.stringWidth(title), 340);
            
            // Certification text
            g2d.setColor(codingFactoryBlack);
            g2d.setFont(new Font("Arial", Font.PLAIN, 32));
            fm = g2d.getFontMetrics();
            String certifyText = "This is to certify that";
            int certifyX = (width - fm.stringWidth(certifyText)) / 2;
            g2d.drawString(certifyText, certifyX, 420);
            
            // Student name - prominent and styled
            String studentName = user.getFirstName() + " " + user.getLastName();
            g2d.setColor(codingFactoryRed);
            g2d.setFont(new Font("Serif", Font.BOLD, 52));
            fm = g2d.getFontMetrics();
            int nameX = (width - fm.stringWidth(studentName)) / 2;
            g2d.drawString(studentName, nameX, 500);
            
            // Underline for name
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(nameX - 50, 520, nameX + fm.stringWidth(studentName) + 50, 520);
            
            // Course completion text
            g2d.setColor(codingFactoryBlack);
            g2d.setFont(new Font("Arial", Font.PLAIN, 28));
            fm = g2d.getFontMetrics();
            String completionText = "has successfully completed the course";
            int completionX = (width - fm.stringWidth(completionText)) / 2;
            g2d.drawString(completionText, completionX, 580);
            
            // Course name - highlighted
            g2d.setColor(codingFactoryRed);
            g2d.setFont(new Font("Arial", Font.BOLD, 38));
            fm = g2d.getFontMetrics();
            String courseName = "\"" + course.getTitle() + "\"";
            int courseX = (width - fm.stringWidth(courseName)) / 2;
            g2d.drawString(courseName, courseX, 640);
            
            // Date and completion info
            g2d.setColor(codingFactoryBlack);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            fm = g2d.getFontMetrics();
            String dateText = "Completed on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
            int dateX = (width - fm.stringWidth(dateText)) / 2;
            g2d.drawString(dateText, dateX, 720);
            
            // Instructor signature area
            g2d.setColor(codingFactoryBlack);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            fm = g2d.getFontMetrics();
            String instructorName = course.getTeacher().getFirstName() + " " + course.getTeacher().getLastName();
            
            // Signature line (bottom right)
            int signatureX = width - 350;
            int signatureY = height - 200;
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(signatureX, signatureY, signatureX + 200, signatureY);
            
            // Instructor name under signature
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            fm = g2d.getFontMetrics();
            g2d.drawString(instructorName, signatureX + (200 - fm.stringWidth(instructorName)) / 2, signatureY + 25);
            
            // Title under instructor name
            g2d.setFont(new Font("Arial", Font.ITALIC, 16));
            fm = g2d.getFontMetrics();
            String instructorTitle = "Course Instructor";
            g2d.drawString(instructorTitle, signatureX + (200 - fm.stringWidth(instructorTitle)) / 2, signatureY + 45);
            
            // Add a stylized signature (handwriting-style)
            g2d.setColor(codingFactoryRed);
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Simple signature-like curves
            int sigStartX = signatureX + 20;
            int sigStartY = signatureY - 20;
            g2d.drawLine(sigStartX, sigStartY, sigStartX + 30, sigStartY - 10);
            g2d.drawLine(sigStartX + 30, sigStartY - 10, sigStartX + 60, sigStartY + 5);
            g2d.drawLine(sigStartX + 60, sigStartY + 5, sigStartX + 90, sigStartY - 15);
            g2d.drawLine(sigStartX + 90, sigStartY - 15, sigStartX + 120, sigStartY);
            g2d.drawLine(sigStartX + 120, sigStartY, sigStartX + 160, sigStartY - 10);
            
            // Certificate ID and validation
            g2d.setColor(darkGray);
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            fm = g2d.getFontMetrics();
            String certId = "Certificate ID: CF-" + course.getId() + "-" + user.getId() + "-" + 
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            g2d.drawString(certId, 100, height - 100);
            
            // CodingFactory footer
            g2d.setColor(codingFactoryBlack);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            fm = g2d.getFontMetrics();
            String footer = "CodingFactory - Boost Your Career";
            int footerX = (width - fm.stringWidth(footer)) / 2;
            g2d.drawString(footer, footerX, height - 60);
            
            g2d.dispose();
            
            // Save certificate
            String fileName = "certificate_" + user.getId() + "_" + course.getId() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".png";
            File certificatesDir = new File(uploadDir + "/certificates");
            if (!certificatesDir.exists()) {
                certificatesDir.mkdirs();
            }
            
            File certificateFile = new File(certificatesDir, fileName);
            ImageIO.write(image, "PNG", certificateFile);
            
            return fileName;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate certificate file", e);
        }
    }
    
    public List<CertificateDto> getUserCertificates(Long userId) {
        return certificateRepository.findByUserId(userId)
            .stream()
            .map(this::convertToDto)
            .toList();
    }
    
    public Optional<CertificateDto> getCertificate(Long userId, Long courseId) {
        return certificateRepository.findByUserIdAndCourseId(userId, courseId)
            .map(this::convertToDto);
    }
    
    public boolean hasCertificate(Long userId, Long courseId) {
        return certificateRepository.existsByUserIdAndCourseId(userId, courseId);
    }
    
    private CertificateDto convertToDto(Certificate certificate) {
        CertificateDto dto = new CertificateDto();
        dto.setId(certificate.getId());
        dto.setUserId(certificate.getUserId());
        dto.setCourseId(certificate.getCourseId());
        dto.setCertificateFileName(certificate.getCertificateFileName());
        dto.setCertificateUrl(certificate.getCertificateUrl());
        dto.setIssuedAt(certificate.getIssuedAt());
        dto.setStudentName(certificate.getStudentName());
        dto.setCourseName(certificate.getCourseName());
        dto.setInstructorName(certificate.getInstructorName());
        return dto;
    }
}
