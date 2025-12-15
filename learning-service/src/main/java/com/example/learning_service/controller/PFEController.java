package com.example.learning_service.controller;

import com.example.learning_service.dto.*;
import com.example.learning_service.repository.UserRepository;
import com.example.learning_service.service.PFEService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pfe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PFEController {

    private final PFEService pfeService;
    private final UserRepository userRepository;

    // PFE Subject endpoints
    @PostMapping("/subjects")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<?> createSubject(@RequestBody CreatePFESubjectRequest request) {
        try {
            System.out.println("Creating PFE subject with request: " + request);
            
            // Validation des champs requis
            if (request == null) {
                System.err.println("Request is null");
                return ResponseEntity.badRequest().body("Request body is required");
            }
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                System.err.println("Title is null or empty");
                return ResponseEntity.badRequest().body("Title is required");
            }
            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                System.err.println("Description is null or empty");
                return ResponseEntity.badRequest().body("Description is required");
            }
            
            Long userId = getUserId();
            System.out.println("User ID: " + userId);
            if (userId == null) {
                System.err.println("User ID is null - cannot create PFE subject");
                return ResponseEntity.status(401).body("User not authenticated");
            }
            
            PFESubjectDto subject = pfeService.createSubject(request, userId);
            System.out.println("PFE subject created successfully: " + subject.getId());
            return ResponseEntity.ok(subject);
        } catch (Exception e) {
            System.err.println("Error creating PFE subject: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating PFE subject: " + e.getMessage());
        }
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<PFESubjectDto>> getAllSubjects() {
        List<PFESubjectDto> subjects = pfeService.getAllSubjects();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/subjects/open")
    public ResponseEntity<List<PFESubjectDto>> getOpenSubjects() {
        List<PFESubjectDto> subjects = pfeService.getOpenSubjects();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/subjects/my")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<PFESubjectDto>> getMySubjects() {
        try {
            Long userId = getUserId();
            List<PFESubjectDto> subjects = pfeService.getSubjectsByCreator(userId);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/subjects/{id}")
    public ResponseEntity<PFESubjectDto> getSubjectById(@PathVariable Long id) {
        try {
            PFESubjectDto subject = pfeService.getSubjectById(id);
            return ResponseEntity.ok(subject);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/subjects/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<PFESubjectDto> updateSubject(@PathVariable Long id, @RequestBody CreatePFESubjectRequest request) {
        try {
            Long userId = getUserId();
            PFESubjectDto subject = pfeService.updateSubject(id, request, userId);
            return ResponseEntity.ok(subject);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/subjects/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        try {
            Long userId = getUserId();
            pfeService.deleteSubject(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/subjects/search")
    public ResponseEntity<List<PFESubjectDto>> searchSubjects(@RequestParam String q) {
        List<PFESubjectDto> subjects = pfeService.searchSubjects(q);
        return ResponseEntity.ok(subjects);
    }

    // Application endpoints
    @PostMapping("/subjects/{subjectId}/apply")
    public ResponseEntity<ApplicationDto> applyToSubject(@PathVariable Long subjectId, 
                                                        @RequestParam("motivation") String motivation,
                                                        @RequestParam(value = "cvFile", required = false) MultipartFile cvFile) {
        try {
            Long userId = getUserId();
            CreateApplicationRequest request = new CreateApplicationRequest();
            request.setMotivation(motivation);
            ApplicationDto application = pfeService.applyToSubject(subjectId, request, cvFile, userId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            System.err.println("Error applying to subject: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/applications/my")
    public ResponseEntity<List<ApplicationDto>> getMyApplications() {
        try {
            System.out.println("Getting my applications...");
            Long userId = getUserId();
            System.out.println("User ID: " + userId);
            List<ApplicationDto> applications = pfeService.getApplicationsByStudent(userId);
            System.out.println("Found " + applications.size() + " applications");
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            System.err.println("Error getting my applications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApplicationDto> getApplicationById(@PathVariable Long applicationId) {
        try {
            Long userId = getUserId();
            ApplicationDto application = pfeService.getApplicationById(applicationId, userId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/subjects/{subjectId}/applications")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<ApplicationDto>> getSubjectApplications(@PathVariable Long subjectId) {
        List<ApplicationDto> applications = pfeService.getApplicationsBySubject(subjectId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/applications/consultant")
    public ResponseEntity<List<ApplicationDto>> getConsultantApplications() {
        try {
            Long userId = getUserId();
            List<ApplicationDto> applications = pfeService.getApplicationsByConsultant(userId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/applications/{applicationId}/review")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApplicationDto> reviewApplication(@PathVariable Long applicationId, @RequestBody ReviewApplicationRequest request) {
        try {
            System.out.println("Reviewing application ID: " + applicationId);
            System.out.println("Request: " + request);
            Long userId = getUserId();
            System.out.println("User ID: " + userId);
            ApplicationDto application = pfeService.reviewApplication(applicationId, request, userId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            System.err.println("Error reviewing application: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Document endpoints
    @PostMapping("/subjects/{subjectId}/documents")
    public ResponseEntity<DocumentDto> uploadDocument(@PathVariable Long subjectId, 
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam("documentType") String documentType,
                                                     @RequestParam(value = "description", required = false) String description) {
        try {
            Long userId = getUserId();
            DocumentDto document = pfeService.uploadDocument(subjectId, file, documentType, description, userId);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            System.err.println("Error uploading document: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/subjects/{subjectId}/documents")
    public ResponseEntity<List<DocumentDto>> getSubjectDocuments(@PathVariable Long subjectId) {
        try {
            Long userId = getUserId();
            List<DocumentDto> documents = pfeService.getSubjectDocuments(subjectId, userId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        try {
            Long userId = getUserId();
            pfeService.deleteDocument(documentId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        try {
            Long userId = getUserId();
            Resource resource = pfeService.downloadDocument(documentId, userId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/applications/{applicationId}/cv/download")
    public ResponseEntity<Resource> downloadCV(@PathVariable Long applicationId) {
        try {
            Long userId = getUserId();
            Resource resource = pfeService.downloadCV(applicationId, userId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Error downloading CV: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Workflow review endpoints
    @PutMapping("/applications/{applicationId}/review-project")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApplicationDto> reviewProject(@PathVariable Long applicationId, @RequestBody ReviewProjectRequest request) {
        try {
            Long userId = getUserId();
            ApplicationDto application = pfeService.reviewProject(applicationId, request, userId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            System.err.println("Error reviewing project: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/applications/{applicationId}/review-report")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApplicationDto> reviewReport(@PathVariable Long applicationId, @RequestBody ReviewReportRequest request) {
        try {
            Long userId = getUserId();
            ApplicationDto application = pfeService.reviewReport(applicationId, request, userId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            System.err.println("Error reviewing report: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/applications/{applicationId}/schedule-jury")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApplicationDto> scheduleJury(@PathVariable Long applicationId, @RequestBody ScheduleJuryRequest request) {
        try {
            Long userId = getUserId();
            ApplicationDto application = pfeService.scheduleJury(applicationId, request, userId);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            System.err.println("Error scheduling jury: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Evaluation endpoints
    @PostMapping("/subjects/{subjectId}/evaluate")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> evaluateProject(@PathVariable Long subjectId, @RequestBody CreateEvaluationRequest request) {
        try {
            Long userId = getUserId();
            pfeService.evaluateProject(subjectId, request, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String authName = auth.getName();
            try {
                // Si authName est un nombre, c'est un ID utilisateur
                return Long.parseLong(authName);
            } catch (NumberFormatException e) {
                // Si authName est un email, chercher l'utilisateur dans la base de données
                return userRepository.findByEmail(authName)
                        .map(user -> user.getId())
                        .orElseThrow(() -> new RuntimeException("User not found with email: " + authName));
            }
        }
        throw new RuntimeException("User not authenticated");
    }
}
