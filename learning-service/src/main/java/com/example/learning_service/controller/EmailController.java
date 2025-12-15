package com.example.learning_service.controller;

import com.example.learning_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;

    /**
     * Test endpoint to send a test email
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email address is required"));
            }

            emailService.sendTestEmail(email);
            
            return ResponseEntity.ok(Map.of(
                "message", "Test email sent successfully to " + email,
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to send test email: " + e.getMessage(),
                    "status", "error"
                ));
        }
    }

    /**
     * Send a simple text email
     */
    @PostMapping("/send-simple")
    public ResponseEntity<Map<String, String>> sendSimpleEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String subject = request.get("subject");
            String text = request.get("text");

            if (to == null || subject == null || text == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: to, subject, text"));
            }

            emailService.sendSimpleEmail(to, subject, text);
            
            return ResponseEntity.ok(Map.of(
                "message", "Email sent successfully to " + to,
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to send email: " + e.getMessage(),
                    "status", "error"
                ));
        }
    }

    /**
     * Send an HTML email
     */
    @PostMapping("/send-html")
    public ResponseEntity<Map<String, String>> sendHtmlEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String subject = request.get("subject");
            String htmlContent = request.get("htmlContent");

            if (to == null || subject == null || htmlContent == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: to, subject, htmlContent"));
            }

            emailService.sendHtmlEmail(to, subject, htmlContent);
            
            return ResponseEntity.ok(Map.of(
                "message", "HTML email sent successfully to " + to,
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Failed to send HTML email: " + e.getMessage(),
                    "status", "error"
                ));
        }
    }
}
