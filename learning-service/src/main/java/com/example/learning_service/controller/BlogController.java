package com.example.learning_service.controller;

import com.example.learning_service.dto.*;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.UserRepository;
import com.example.learning_service.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BlogController {

    private final BlogService blogService;
    private final UserRepository userRepository;

    // Blog CRUD endpoints
    @PostMapping
    public ResponseEntity<BlogDto> createBlog(@RequestBody CreateBlogRequest request) {
        try {
            System.out.println("BlogController.createBlog called");
            System.out.println("Request: " + request);
            
            Long userId = getUserId();
            System.out.println("Got user ID: " + userId);
            
            BlogDto blog = blogService.createBlog(request, userId);
            System.out.println("Blog created successfully: " + blog.getId());
            
            return ResponseEntity.ok(blog);
        } catch (Exception e) {
            System.err.println("Error creating blog: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<BlogDto>> getAllBlogs() {
        List<BlogDto> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogDto> getBlogById(@PathVariable Long id) {
        try {
            BlogDto blog = blogService.getBlogById(id);
            return ResponseEntity.ok(blog);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogDto> updateBlog(@PathVariable Long id, @RequestBody CreateBlogRequest request) {
        try {
            Long userId = getUserId();
            BlogDto blog = blogService.updateBlog(id, request, userId);
            return ResponseEntity.ok(blog);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        try {
            Long userId = getUserId();
            blogService.deleteBlog(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<BlogDto> likeBlog(@PathVariable Long id) {
        try {
            BlogDto blog = blogService.likeBlog(id);
            return ResponseEntity.ok(blog);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Comment endpoints
    @PostMapping("/{blogId}/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long blogId, @RequestBody CreateCommentRequest request) {
        try {
            Long userId = getUserId();
            CommentDto comment = blogService.addComment(blogId, request, userId);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{blogId}/comments")
    public ResponseEntity<List<CommentDto>> getBlogComments(@PathVariable Long blogId) {
        List<CommentDto> comments = blogService.getBlogComments(blogId);
        return ResponseEntity.ok(comments);
    }

    // Reply endpoints
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<ReplyDto> addReply(@PathVariable Long commentId, @RequestBody CreateReplyRequest request) {
        try {
            Long userId = getUserId();
            ReplyDto reply = blogService.addReply(commentId, request, userId);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("BlogController.getUserId() called");
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String authName = auth.getName();
            System.out.println("Authenticated user name: " + authName);
            
            try {
                // First try to parse as user ID
                Long userId = Long.parseLong(authName);
                System.out.println("Parsed user ID from auth name: " + userId);
                return userId;
            } catch (NumberFormatException e) {
                // It's an email, look up the user in the database
                System.out.println("Auth name is email, looking up user in database: " + authName);
                
                try {
                    User user = userRepository.findByEmail(authName)
                            .orElseThrow(() -> new RuntimeException("User not found with email: " + authName));
                    
                    System.out.println("Found user in database: ID=" + user.getId() + ", Name=" + 
                                     user.getFirstName() + " " + user.getLastName());
                    return user.getId();
                } catch (Exception ex) {
                    System.err.println("Error looking up user by email: " + ex.getMessage());
                    // Fallback to predefined mappings for known emails
                    if ("student@codingfactory.com".equals(authName)) {
                        System.out.println("Fallback: Mapped to student user ID: 4");
                        return 4L;
                    }
                    if ("teacher@codingfactory.com".equals(authName)) {
                        System.out.println("Fallback: Mapped to teacher user ID: 2");
                        return 2L;
                    }
                    if ("admin@codingfactory.com".equals(authName)) {
                        System.out.println("Fallback: Mapped to admin user ID: 1");
                        return 1L;
                    }
                    if ("consultant@codingfactory.com".equals(authName)) {
                        System.out.println("Fallback: Mapped to consultant user ID: 3");
                        return 3L;
                    }
                    
                    System.err.println("No fallback mapping found for email: " + authName);
                    throw new RuntimeException("User not found and no fallback mapping available for: " + authName);
                }
            }
        }
        
        System.out.println("No authentication found");
        throw new RuntimeException("No authentication found");
    }
}
