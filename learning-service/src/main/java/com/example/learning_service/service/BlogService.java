package com.example.learning_service.service;

import com.example.learning_service.dto.*;
import com.example.learning_service.entity.Blog;
import com.example.learning_service.entity.Comment;
import com.example.learning_service.entity.Reply;
import com.example.learning_service.entity.User;
import com.example.learning_service.repository.BlogRepository;
import com.example.learning_service.repository.CommentRepository;
import com.example.learning_service.repository.ReplyRepository;
import com.example.learning_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogService {

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public BlogDto createBlog(CreateBlogRequest request, Long authorId) {
        System.out.println("BlogService.createBlog called with authorId: " + authorId);
        System.out.println("Request details - Title: " + request.getTitle() + ", Content length: " + 
                          (request.getContent() != null ? request.getContent().length() : "null"));
        
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> {
                    System.err.println("Author not found with ID: " + authorId);
                    return new RuntimeException("Author not found with ID: " + authorId);
                });

        System.out.println("Found author: " + author.getEmail() + " (" + author.getFirstName() + " " + author.getLastName() + ")");

        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setPhoto(request.getPhoto());
        blog.setTags(request.getTags());
        blog.setAuthor(author);

        System.out.println("Saving blog to database...");
        Blog savedBlog = blogRepository.save(blog);
        System.out.println("Blog saved with ID: " + savedBlog.getId());
        
        return convertToBlogDto(savedBlog);
    }

    public List<BlogDto> getAllBlogs() {
        return blogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToBlogDto)
                .collect(Collectors.toList());
    }

    public BlogDto getBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        return convertToBlogDto(blog);
    }

    public BlogDto updateBlog(Long id, CreateBlogRequest request, Long authorId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("You can only edit your own blogs");
        }

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setPhoto(request.getPhoto());
        blog.setTags(request.getTags());

        Blog updatedBlog = blogRepository.save(blog);
        return convertToBlogDto(updatedBlog);
    }

    public void deleteBlog(Long id, Long authorId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("You can only delete your own blogs");
        }

        blogRepository.delete(blog);
    }

    public BlogDto likeBlog(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        blog.incrementLikes();
        Blog updatedBlog = blogRepository.save(blog);
        return convertToBlogDto(updatedBlog);
    }

    public CommentDto addComment(Long blogId, CreateCommentRequest request, Long authorId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setBlog(blog);

        Comment savedComment = commentRepository.save(comment);
        
        // Send notification to blog author if someone else commented
        if (!blog.getAuthor().getId().equals(authorId)) {
            System.out.println("Creating notification for blog comment:");
            System.out.println("Blog Author ID: " + blog.getAuthor().getId());
            System.out.println("Commenter ID: " + authorId);
            System.out.println("Blog Title: " + blog.getTitle());
            System.out.println("Comment: " + request.getContent());
            
            notificationService.createBlogCommentNotification(
                blog.getAuthor().getId(), // Blog author (recipient)
                authorId, // Commenter (sender)
                author.getFirstName() + " " + author.getLastName(), // Commenter name
                blogId, // Blog ID
                blog.getTitle(), // Blog title
                request.getContent() // Comment text
            );
        } else {
            System.out.println("Not creating notification - user commented on their own blog");
        }
        
        return convertToCommentDto(savedComment);
    }

    public List<CommentDto> getBlogComments(Long blogId) {
        return commentRepository.findByBlogIdWithReplies(blogId)
                .stream()
                .map(this::convertToCommentDto)
                .collect(Collectors.toList());
    }

    public ReplyDto addReply(Long commentId, CreateReplyRequest request, Long authorId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Reply reply = new Reply();
        reply.setContent(request.getContent());
        reply.setAuthor(author);
        reply.setComment(comment);

        Reply savedReply = replyRepository.save(reply);
        return convertToReplyDto(savedReply);
    }

    private BlogDto convertToBlogDto(Blog blog) {
        BlogDto dto = new BlogDto();
        dto.setId(blog.getId());
        dto.setTitle(blog.getTitle());
        dto.setContent(blog.getContent());
        dto.setPhoto(blog.getPhoto());
        dto.setTags(blog.getTags());
        dto.setLikes(blog.getLikes());
        dto.setAuthorId(blog.getAuthor().getId());
        dto.setAuthorName(blog.getAuthor().getFirstName() + " " + blog.getAuthor().getLastName());
        
        // Get author's first role name
        String authorRole = blog.getAuthor().getRoles().stream()
                .findFirst()
                .map(role -> role.getName().name())
                .orElse("USER");
        dto.setAuthorRole(authorRole);
        
        dto.setCreatedAt(blog.getCreatedAt());
        dto.setUpdatedAt(blog.getUpdatedAt());
        dto.setCommentCount(blog.getComments().size());
        return dto;
    }

    private CommentDto convertToCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName());
        
        // Get author's first role name
        String authorRole = comment.getAuthor().getRoles().stream()
                .findFirst()
                .map(role -> role.getName().name())
                .orElse("USER");
        dto.setAuthorRole(authorRole);
        
        dto.setBlogId(comment.getBlog().getId());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setReplies(comment.getReplies().stream().map(this::convertToReplyDto).collect(Collectors.toList()));
        dto.setReplyCount(comment.getReplies().size());
        return dto;
    }

    private ReplyDto convertToReplyDto(Reply reply) {
        ReplyDto dto = new ReplyDto();
        dto.setId(reply.getId());
        dto.setContent(reply.getContent());
        dto.setAuthorId(reply.getAuthor().getId());
        dto.setAuthorName(reply.getAuthor().getFirstName() + " " + reply.getAuthor().getLastName());
        
        // Get author's first role name
        String authorRole = reply.getAuthor().getRoles().stream()
                .findFirst()
                .map(role -> role.getName().name())
                .orElse("USER");
        dto.setAuthorRole(authorRole);
        
        dto.setCommentId(reply.getComment().getId());
        dto.setCreatedAt(reply.getCreatedAt());
        return dto;
    }
}
