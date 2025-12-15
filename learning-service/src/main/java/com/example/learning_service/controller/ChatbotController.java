package com.example.learning_service.controller;

import com.example.learning_service.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final OpenAIService openAIService;

    @PostMapping("/consulting")
    public ResponseEntity<Map<String, String>> consultingChat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le message est requis"));
            }

            String response = openAIService.getConsultingResponse(message);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la génération de la réponse"));
        }
    }

    @PostMapping("/courses")
    public ResponseEntity<Map<String, String>> coursesChat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le message est requis"));
            }

            String response = openAIService.getCoursesResponse(message);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la génération de la réponse"));
        }
    }

    @PostMapping("/universal")
    public ResponseEntity<Map<String, String>> universalChat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le message est requis"));
            }

            String response = openAIService.getUniversalResponse(message);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la génération de la réponse"));
        }
    }
}

