package com.example.learning_service.repository;

import com.example.learning_service.entity.Order;
import com.example.learning_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserId(Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o " +
           "JOIN o.items oi " +
           "WHERE o.user.id = :userId AND oi.course.id = :courseId AND o.status = :status")
    boolean existsByUserIdAndCourseIdAndStatus(@Param("userId") Long userId, 
                                                 @Param("courseId") Long courseId, 
                                                 @Param("status") Order.OrderStatus status);
    
    @Query("SELECT COUNT(DISTINCT o) FROM Order o " +
           "JOIN o.items oi " +
           "WHERE oi.course.id = :courseId AND o.status = :status")
    long countByCourseIdAndStatus(@Param("courseId") Long courseId, 
                                   @Param("status") Order.OrderStatus status);
}

