package com.example.learning_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePFESubjectRequest {
    private String title;
    private String description;
    private String requirements;
}
