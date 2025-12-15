package com.example.learning_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private Double progress = 0.0; // Percentage (0-100)

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "enrollments", "password"})
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "modules", "enrollments"})
    private Course course;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<LessonProgress> lessonProgress;

    public enum EnrollmentStatus {
        ACTIVE, COMPLETED, DROPPED
    }

    // Override equals and hashCode to avoid circular reference issues
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Enrollment)) return false;
        Enrollment enrollment = (Enrollment) o;
        return id != null && id.equals(enrollment.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
