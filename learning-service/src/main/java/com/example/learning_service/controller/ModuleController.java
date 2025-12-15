package com.example.learning_service.controller;

import com.example.learning_service.dto.ModuleRequest;
import com.example.learning_service.dto.ModuleResponse;
import com.example.learning_service.entity.Module;
import com.example.learning_service.service.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModuleController {
    
    private final ModuleService moduleService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ModuleResponse> createModule(@Valid @RequestBody ModuleRequest request) {
        try {
            Module module = moduleService.createModule(request);
            ModuleResponse response = ModuleResponse.fromEntity(module);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ModuleResponse> updateModule(@PathVariable Long id, @Valid @RequestBody ModuleRequest request) {
        try {
            Module module = moduleService.updateModule(id, request);
            ModuleResponse response = ModuleResponse.fromEntity(module);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        try {
            moduleService.deleteModule(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ModuleResponse>> getModulesByCourse(@PathVariable Long courseId) {
        try {
            List<Module> modules = moduleService.getModulesByCourse(courseId);
            List<ModuleResponse> responses = modules.stream()
                .map(ModuleResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponse> getModuleById(@PathVariable Long id) {
        try {
            Module module = moduleService.getModuleById(id);
            ModuleResponse response = ModuleResponse.fromEntity(module);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
