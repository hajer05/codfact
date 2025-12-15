package com.example.learning_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "lessons")
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    private LessonType type;

    private String videoUrl;
    
    private String videoFileName;
    
    private Long videoDuration; // in seconds

    @Column(length = 5000)
    private String content; // For text-based lessons

    private String attachmentUrl;
    
    private String attachmentFileName;

    @Column(nullable = false)
    private boolean isFree = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonBackReference
    private Module module;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "lesson"})
    private Set<LessonProgress> lessonProgress;

    public enum LessonType {
        VIDEO, TEXT, QUIZ, ASSIGNMENT
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    
    public LessonType getType() { return type; }
    public void setType(LessonType type) { this.type = type; }
    
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    
    public String getVideoFileName() { return videoFileName; }
    public void setVideoFileName(String videoFileName) { this.videoFileName = videoFileName; }
    
    public Long getVideoDuration() { return videoDuration; }
    public void setVideoDuration(Long videoDuration) { this.videoDuration = videoDuration; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    
    public String getAttachmentFileName() { return attachmentFileName; }
    public void setAttachmentFileName(String attachmentFileName) { this.attachmentFileName = attachmentFileName; }
    
    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }
    
    public Set<LessonProgress> getLessonProgress() { return lessonProgress; }
    public void setLessonProgress(Set<LessonProgress> lessonProgress) { this.lessonProgress = lessonProgress; }

    // Proper hashCode and equals using only ID to avoid circular references
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lesson)) return false;
        Lesson lesson = (Lesson) o;
        return id != null && id.equals(lesson.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
