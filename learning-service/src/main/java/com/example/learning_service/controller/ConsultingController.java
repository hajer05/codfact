package com.example.learning_service.controller;

import com.example.learning_service.dto.ConsultingRequestDto;
import com.example.learning_service.dto.CreateConsultingRequest;
import com.example.learning_service.dto.UpdateConsultingRequest;
import com.example.learning_service.entity.ConsultingRequest;
import com.example.learning_service.service.ConsultingService;
import com.example.learning_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consulting")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConsultingController {
    
    private final ConsultingService consultingService;
    private final AuthService authService;
    
    @PostMapping("/request")
    public ResponseEntity<ConsultingRequestDto> createRequest(@Valid @RequestBody CreateConsultingRequest request) {
        try {
            Long userId = authService.getCurrentUserId();
            ConsultingRequestDto response = consultingService.createRequest(request, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/requests")
    public ResponseEntity<List<ConsultingRequestDto>> getAllRequests() {
        try {
            Long userId = authService.getCurrentUserId();
            List<ConsultingRequestDto> requests = consultingService.getRequestsByUser(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/request/{id}")
    public ResponseEntity<ConsultingRequestDto> getRequestById(@PathVariable Long id) {
        try {
            ConsultingRequestDto request = consultingService.getRequestById(id);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Admin/Consultant endpoints - ADMIN and CONSULTANT can access all requests
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT')")
    public ResponseEntity<List<ConsultingRequestDto>> getAllRequestsAdmin() {
        try {
            List<ConsultingRequestDto> requests = consultingService.getAllRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT')")
    public ResponseEntity<List<ConsultingRequestDto>> getRequestsByStatus(
            @PathVariable ConsultingRequest.ConsultingStatus status) {
        try {
            List<ConsultingRequestDto> requests = consultingService.getRequestsByStatus(status);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Consultant endpoint - Get requests assigned to current consultant
    @GetMapping("/consultant/assigned")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<List<ConsultingRequestDto>> getAssignedRequests() {
        try {
            Long userId = authService.getCurrentUserId();
            List<ConsultingRequestDto> requests = consultingService.getRequestsAssignedToConsultant(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/admin/request/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONSULTANT')")
    public ResponseEntity<ConsultingRequestDto> updateRequest(
            @PathVariable Long id,
            @RequestBody UpdateConsultingRequest request) {
        try {
            ConsultingRequestDto updated = consultingService.updateRequest(id, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}

