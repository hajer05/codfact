package com.example.learning_service.repository;

import com.example.learning_service.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    
    // Find replies by comment ID ordered by creation date
    List<Reply> findByCommentIdOrderByCreatedAtAsc(Long commentId);
    
    // Find replies by author
    List<Reply> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    // Count replies by comment
    long countByCommentId(Long commentId);
}
