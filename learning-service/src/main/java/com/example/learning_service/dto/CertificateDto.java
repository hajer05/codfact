package com.example.learning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDto {
    private Long id;
    private Long userId;
    private Long courseId;
    private String certificateFileName;
    private String certificateUrl;
    private LocalDateTime issuedAt;
    private String studentName;
    private String courseName;
    private String instructorName;
}
