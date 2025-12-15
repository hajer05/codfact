package com.example.learning_service.repository;

import com.example.learning_service.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByCourseId(Long courseId);
    List<Complaint> findByTeacherId(Long teacherId);
    List<Complaint> findByStudentId(Long studentId);
    List<Complaint> findByCourseIdAndStudentId(Long courseId, Long studentId);
    List<Complaint> findByStatus(Complaint.ComplaintStatus status);
}

