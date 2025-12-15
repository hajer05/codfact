package com.example.learning_service.repository;

import com.example.learning_service.entity.Cart;
import com.example.learning_service.entity.CartItem;
import com.example.learning_service.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndCourse(Cart cart, Course course);
    void deleteByCart(Cart cart);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.course.id = :courseId")
    List<CartItem> findByCourseId(@Param("courseId") Long courseId);
}

