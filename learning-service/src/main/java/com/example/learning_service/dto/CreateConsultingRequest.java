package com.example.learning_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsultingRequest {
    
    @NotBlank(message = "Company name is required")
    @Size(max = 200)
    private String companyName;
    
    @NotBlank(message = "Contact name is required")
    @Size(max = 100)
    private String contactName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100)
    private String email;
    
    @Size(max = 20)
    private String phone;
    
    @NotBlank(message = "Service type is required")
    @Size(max = 100)
    private String serviceType;
    
    @NotBlank(message = "Project description is required")
    private String projectDescription;
    
    @NotBlank(message = "Needs description is required")
    private String needs;
    
    private String budget;
    
    private String timeline;
}

