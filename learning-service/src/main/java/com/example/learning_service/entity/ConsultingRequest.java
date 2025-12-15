package com.example.learning_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consulting_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConsultingRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String companyName;
    
    @Column(nullable = false, length = 100)
    private String contactName;
    
    @Column(nullable = false, length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(nullable = false, length = 100)
    private String serviceType; // Ex: "Web Development", "Data Science", "AI/ML", etc.
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String projectDescription;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String needs;
    
    @Column(columnDefinition = "TEXT")
    private String budget;
    
    @Column(columnDefinition = "TEXT")
    private String timeline;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultingStatus status = ConsultingStatus.PENDING;
    
    @Column(columnDefinition = "TEXT")
    private String adminNotes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User requestedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User assignedTo;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
    
    public enum ConsultingStatus {
        PENDING,
        IN_PROGRESS,
        ANSWERED,
        CLOSED,
        REJECTED
    }
}

