package com.example.learning_service.controller;

import com.example.learning_service.dto.CertificateDto;
import com.example.learning_service.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CertificateController {
    
    private final CertificateService certificateService;
    
    @PostMapping("/generate")
    public ResponseEntity<CertificateDto> generateCertificate(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        try {
            CertificateDto certificate = certificateService.generateCertificate(userId, courseId);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            System.err.println("Certificate generation error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CertificateDto>> getUserCertificates(@PathVariable Long userId) {
        List<CertificateDto> certificates = certificateService.getUserCertificates(userId);
        return ResponseEntity.ok(certificates);
    }
    
    @GetMapping("/check")
    public ResponseEntity<Boolean> hasCertificate(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        boolean hasCertificate = certificateService.hasCertificate(userId, courseId);
        return ResponseEntity.ok(hasCertificate);
    }
    
    @GetMapping("/get")
    public ResponseEntity<CertificateDto> getCertificate(
            @RequestParam Long userId,
            @RequestParam Long courseId) {
        Optional<CertificateDto> certificate = certificateService.getCertificate(userId, courseId);
        return certificate.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("uploads/certificates").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
