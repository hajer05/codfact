package com.example.learning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String fileUrl;
    private String documentType;
    private String description;
    private Long fileSize;
    private String uploadedByName;
    private String uploadedByEmail;
    private LocalDateTime uploadedAt;
    private Long subjectId;
    private String subjectTitle;
}
