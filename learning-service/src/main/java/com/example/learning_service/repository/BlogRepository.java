package com.example.learning_service.repository;

import com.example.learning_service.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    
    // Find blogs by author
    List<Blog> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    // Find blogs by tag
    @Query("SELECT b FROM Blog b JOIN b.tags t WHERE t = :tag ORDER BY b.createdAt DESC")
    List<Blog> findByTagOrderByCreatedAtDesc(@Param("tag") String tag);
    
    // Find blogs containing search term in title or content
    @Query("SELECT b FROM Blog b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(b.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY b.createdAt DESC")
    List<Blog> findByTitleOrContentContainingIgnoreCaseOrderByCreatedAtDesc(@Param("searchTerm") String searchTerm);
    
    // Find all blogs ordered by creation date (most recent first)
    List<Blog> findAllByOrderByCreatedAtDesc();
    
    // Find all blogs with pagination ordered by creation date
    Page<Blog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Find most liked blogs
    List<Blog> findTop10ByOrderByLikesDescCreatedAtDesc();
    
    // Count blogs by author
    long countByAuthorId(Long authorId);
}
