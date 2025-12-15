package com.example.learning_service.repository;

import com.example.learning_service.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    @Query("SELECT m FROM Module m WHERE m.course.id = :courseId ORDER BY m.orderIndex ASC")
    List<Module> findByCourseIdOrderByOrderIndex(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(m) FROM Module m WHERE m.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
}
