package com.example.learning_service.repository;

import com.example.learning_service.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find comments by blog ID ordered by creation date
    List<Comment> findByBlogIdOrderByCreatedAtAsc(Long blogId);
    
    // Find comments by author
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    // Count comments by blog
    long countByBlogId(Long blogId);
    
    // Find comments with replies for a specific blog
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.blog.id = :blogId ORDER BY c.createdAt ASC")
    List<Comment> findByBlogIdWithReplies(@Param("blogId") Long blogId);
}
