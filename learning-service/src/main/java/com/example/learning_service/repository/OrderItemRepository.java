package com.example.learning_service.repository;

import com.example.learning_service.entity.Order;
import com.example.learning_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.course.id = :courseId")
    List<OrderItem> findByCourseId(@Param("courseId") Long courseId);
    
    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);
}

