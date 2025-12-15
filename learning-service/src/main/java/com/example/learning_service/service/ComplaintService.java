package com.example.learning_service.service;

import com.example.learning_service.entity.Complaint;
import com.example.learning_service.entity.Course;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.ComplaintRepository;
import com.example.learning_service.repository.CourseRepository;
import com.example.learning_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintService {
    
    private final ComplaintRepository complaintRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public Complaint createComplaint(Long studentId, Long courseId, String subject, String message) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        User teacher = course.getTeacher();
        if (teacher == null) {
            throw new RuntimeException("Course teacher not found");
        }
        
        Complaint complaint = new Complaint();
        complaint.setStudent(student);
        complaint.setCourse(course);
        complaint.setTeacher(teacher);
        complaint.setSubject(subject);
        complaint.setMessage(message);
        complaint.setStatus(Complaint.ComplaintStatus.PENDING);
        
        Complaint saved = complaintRepository.save(complaint);
        
        // Send notification email to teacher (errors ignored)
        try {
            emailService.sendComplaintNotification(saved);
        } catch (Exception ignored) {}
        
        return saved;
    }

    public List<Complaint> getComplaintsByTeacher(Long teacherId) {
        return complaintRepository.findByTeacherId(teacherId);
    }

    public List<Complaint> getComplaintsByStudent(Long studentId) {
        return complaintRepository.findByStudentId(studentId);
    }

    public List<Complaint> getComplaintsByCourse(Long courseId) {
        return complaintRepository.findByCourseId(courseId);
    }

    public Complaint respondToComplaint(Long complaintId, String response, Long userId, boolean isAdmin) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        // Allow response if user is the teacher OR if user is admin
        if (!isAdmin && !complaint.getTeacher().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You are not the teacher of this course");
        }
        
        complaint.setResponse(response);
        complaint.setStatus(Complaint.ComplaintStatus.REVIEWED);
        
        Complaint saved = complaintRepository.save(complaint);
        
        // Send notification email to student (errors ignored)
        try {
            emailService.sendComplaintResponse(saved);
        } catch (Exception ignored) {}
        
        return saved;
    }

    public Complaint updateComplaintStatus(Long complaintId, Complaint.ComplaintStatus status, Long userId, boolean isAdmin) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        // Allow status update if user is the teacher OR if user is admin
        if (!isAdmin && !complaint.getTeacher().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You are not the teacher of this course");
        }
        
        complaint.setStatus(status);
        return complaintRepository.save(complaint);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    public void deleteComplaint(Long complaintId, Long userId, boolean isAdmin) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        // Allow deletion if user is the teacher OR if user is admin
        if (!isAdmin && !complaint.getTeacher().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You are not the teacher of this course");
        }
        
        complaintRepository.delete(complaint);
    }
}

