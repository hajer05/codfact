package com.example.learning_service.service;

import com.example.learning_service.dto.*;
import com.example.learning_service.entity.*;
import com.example.learning_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PFEService {

    private final PFESubjectRepository pfeSubjectRepository;
    private final ApplicationRepository applicationRepository;
    private final EvaluationRepository evaluationRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    // PFE Subject operations
    public PFESubjectDto createSubject(CreatePFESubjectRequest request, Long createdById) {
        if (request == null) {
            throw new RuntimeException("Request cannot be null");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Title is required");
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Description is required");
        }
        
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + createdById));

        PFESubject subject = new PFESubject();
        subject.setTitle(request.getTitle().trim());
        subject.setDescription(request.getDescription().trim());
        subject.setRequirements(request.getRequirements() != null ? request.getRequirements().trim() : null);
        subject.setCreatedBy(creator);
        subject.setStatus(PFESubject.PFEStatus.OPEN);

        PFESubject savedSubject = pfeSubjectRepository.save(subject);
        return convertToPFESubjectDto(savedSubject);
    }

    public List<PFESubjectDto> getAllSubjects() {
        return pfeSubjectRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToPFESubjectDto)
                .collect(Collectors.toList());
    }

    public List<PFESubjectDto> getOpenSubjects() {
        return pfeSubjectRepository.findAll()
                .stream()
                .filter(subject -> subject.getStatus() != PFESubject.PFEStatus.COMPLETED)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::convertToPFESubjectDto)
                .collect(Collectors.toList());
    }

    public List<PFESubjectDto> getSubjectsByCreator(Long creatorId) {
        return pfeSubjectRepository.findByCreatedByIdOrderByCreatedAtDesc(creatorId)
                .stream()
                .map(this::convertToPFESubjectDto)
                .collect(Collectors.toList());
    }

    public PFESubjectDto getSubjectById(Long id) {
        PFESubject subject = pfeSubjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PFE Subject not found"));
        return convertToPFESubjectDto(subject);
    }

    public PFESubjectDto updateSubject(Long id, CreatePFESubjectRequest request, Long userId) {
        PFESubject subject = pfeSubjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PFE Subject not found"));

        // Check if user is admin
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Teachers can only update their own subjects, admins can update any
        if (!isAdmin && !subject.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("You can only update your own subjects");
        }

        subject.setTitle(request.getTitle());
        subject.setDescription(request.getDescription());
        subject.setRequirements(request.getRequirements());

        PFESubject updatedSubject = pfeSubjectRepository.save(subject);
        return convertToPFESubjectDto(updatedSubject);
    }

    public void deleteSubject(Long id, Long userId) {
        PFESubject subject = pfeSubjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PFE Subject not found"));

        // Check if user is admin
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Teachers can only delete their own subjects, admins can delete any
        if (!isAdmin && !subject.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own subjects");
        }

        pfeSubjectRepository.delete(subject);
    }

    // Application operations
    public ApplicationDto applyToSubject(Long subjectId, CreateApplicationRequest request, MultipartFile cvFile, Long studentId) throws IOException {
        PFESubject subject = pfeSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("PFE Subject not found"));

        if (subject.getStatus() == PFESubject.PFEStatus.COMPLETED) {
            throw new RuntimeException("This subject is no longer open for applications");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if student already applied
        if (applicationRepository.existsByStudentIdAndSubjectId(studentId, subjectId)) {
            throw new RuntimeException("You have already applied to this subject");
        }

        Application application = new Application();
        application.setStudent(student);
        application.setSubject(subject);
        application.setMotivation(request.getMotivation());
        application.setStatus(Application.ApplicationStatus.PENDING);

        // Handle CV file upload if provided
        if (cvFile != null && !cvFile.isEmpty()) {
            String cvInfo = saveCVFile(cvFile, subjectId, studentId);
            String[] cvParts = cvInfo.split("\\|");
            application.setCvFileName(cvParts[0]);
            application.setCvFileUrl(cvParts[1]);
            application.setCvOriginalFileName(cvParts[2]);
        }

        Application savedApplication = applicationRepository.save(application);
        return convertToApplicationDto(savedApplication);
    }

    private String saveCVFile(MultipartFile file, Long subjectId, Long studentId) throws IOException {
        // Validate file type (PDF only for CV)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("CV must be a PDF file");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("CV file size must be less than 5MB");
        }

        // Create upload directory
        Path uploadPath = Paths.get(uploadDir, "pfe", "cv", subjectId.toString());
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = "cv_" + studentId + "_" + UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create file URL
        String fileUrl = "/uploads/pfe/cv/" + subjectId + "/" + uniqueFilename;

        return uniqueFilename + "|" + fileUrl + "|" + originalFilename;
    }

    public List<ApplicationDto> getApplicationsByStudent(Long studentId) {
        System.out.println("Service: Getting applications for student ID: " + studentId);
        List<Application> applications = applicationRepository.findByStudentIdOrderByAppliedAtDesc(studentId);
        System.out.println("Service: Found " + applications.size() + " applications in database");
        return applications.stream()
                .map(this::convertToApplicationDto)
                .collect(Collectors.toList());
    }

    public List<ApplicationDto> getApplicationsBySubject(Long subjectId) {
        return applicationRepository.findBySubjectIdOrderByAppliedAtDesc(subjectId)
                .stream()
                .map(this::convertToApplicationDto)
                .collect(Collectors.toList());
    }

    public List<ApplicationDto> getApplicationsByConsultant(Long consultantId) {
        return applicationRepository.findByConsultantIdOrderByAppliedAtDesc(consultantId)
                .stream()
                .map(this::convertToApplicationDto)
                .collect(Collectors.toList());
    }

    private final EmailService emailService;

    public ApplicationDto reviewApplication(Long applicationId, ReviewApplicationRequest request, Long reviewerId) {
        System.out.println("Service: Reviewing application " + applicationId + " by user " + reviewerId);
        // Utiliser fetch join pour charger student et subject (éviter lazy loading)
        Application application = applicationRepository.findByIdWithStudentAndSubject(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        System.out.println("Application found: " + application.getId());

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        System.out.println("Reviewer found: " + reviewer.getEmail());

        // Check if reviewer is the subject creator
        System.out.println("Subject creator ID: " + application.getSubject().getCreatedBy().getId());
        System.out.println("Reviewer ID: " + reviewerId);
        if (!application.getSubject().getCreatedBy().getId().equals(reviewerId)) {
            throw new RuntimeException("You can only review applications for your own subjects");
        }

        application.setStatus(request.getStatus());
        application.setReviewComment(request.getReviewComment());
        application.setReviewedBy(reviewer);
        application.setReviewedAt(LocalDateTime.now());
        
        // Update workflow step based on status
        if (request.getStatus() == Application.ApplicationStatus.ACCEPTED) {
            application.setWorkflowStep(Application.WorkflowStep.PROJECT_UPLOAD);
        } else if (request.getStatus() == Application.ApplicationStatus.REJECTED) {
            // Keep current workflow step for rejected applications
        }

        // Update subject status if accepted
        if (request.getStatus() == Application.ApplicationStatus.ACCEPTED) {
            PFESubject subject = application.getSubject();
            subject.setStatus(PFESubject.PFEStatus.ASSIGNED);
            pfeSubjectRepository.save(subject);
        }

        applicationRepository.save(application);
        
        // Recharger avec fetch join pour être sûr que student et subject sont disponibles
        Application updatedApplication = applicationRepository.findByIdWithStudentAndSubject(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found after save"));

        // Envoyer email selon le statut (même logique que confirmOrder pour achat cours)
        try {
            // Le student devrait être chargé maintenant grâce au fetch join
            User student = updatedApplication.getStudent();
            if (student == null || student.getEmail() == null || student.getEmail().isEmpty()) {
                System.err.println("Cannot send PFE email: student or email is null for application " + updatedApplication.getId());
            } else {
                System.out.println("Attempting to send PFE email to: " + student.getEmail());
                if (request.getStatus() == Application.ApplicationStatus.ACCEPTED) {
                    emailService.sendPFEAcceptance(updatedApplication);
                    System.out.println("PFE acceptance email sent successfully to: " + student.getEmail());
                } else if (request.getStatus() == Application.ApplicationStatus.REJECTED) {
                    emailService.sendPFERejection(updatedApplication);
                    System.out.println("PFE rejection email sent successfully to: " + student.getEmail());
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending PFE email: " + e.getMessage());
            e.printStackTrace();
        }

        return convertToApplicationDto(updatedApplication);
    }

    // Evaluation operations
    public void evaluateProject(Long subjectId, CreateEvaluationRequest request, Long evaluatorId) {
        PFESubject subject = pfeSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("PFE Subject not found"));

        User evaluator = userRepository.findById(evaluatorId)
                .orElseThrow(() -> new RuntimeException("Evaluator not found"));

        // Check if evaluator is the subject creator
        if (!subject.getCreatedBy().getId().equals(evaluatorId)) {
            throw new RuntimeException("You can only evaluate your own subjects");
        }

        // Check if subject is assigned
        if (subject.getStatus() != PFESubject.PFEStatus.ASSIGNED) {
            throw new RuntimeException("Subject must be assigned before evaluation");
        }

        // Check if already evaluated
        if (evaluationRepository.existsBySubjectId(subjectId)) {
            throw new RuntimeException("This subject has already been evaluated");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setGrade(request.getGrade());
        evaluation.setComment(request.getComment());
        evaluation.setSubject(subject);
        evaluation.setEvaluatedBy(evaluator);

        evaluationRepository.save(evaluation);

        // Update subject status to completed
        subject.setStatus(PFESubject.PFEStatus.COMPLETED);
        pfeSubjectRepository.save(subject);

        // Notifier le ou les candidats acceptés que le PFE est complété avec succès (même logique que confirmOrder)
        try {
            String gradeStr = evaluation.getGrade() != null ? evaluation.getGrade().toString() : null;
            // Utiliser fetch join pour charger student et subject (éviter lazy loading)
            applicationRepository.findBySubjectIdWithStudentAndSubject(subjectId)
                .stream()
                .findFirst()
                .ifPresent(app -> {
                    User student = app.getStudent();
                    if (student == null || student.getEmail() == null || student.getEmail().isEmpty()) {
                        System.err.println("Cannot send PFE completion email: student or email is null for application " + app.getId());
                    } else {
                        System.out.println("Attempting to send PFE completion email to: " + student.getEmail());
                        emailService.sendPFECompletion(app, gradeStr);
                        System.out.println("PFE completion email sent successfully to: " + student.getEmail());
                    }
                });
        } catch (Exception e) {
            System.err.println("Error sending PFE completion email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Search operations
    public List<PFESubjectDto> searchSubjects(String searchTerm) {
        return pfeSubjectRepository.searchSubjects(searchTerm)
                .stream()
                .map(this::convertToPFESubjectDto)
                .collect(Collectors.toList());
    }

    // Conversion methods
    private PFESubjectDto convertToPFESubjectDto(PFESubject subject) {
        PFESubjectDto dto = new PFESubjectDto();
        dto.setId(subject.getId());
        dto.setTitle(subject.getTitle());
        dto.setDescription(subject.getDescription());
        dto.setRequirements(subject.getRequirements());
        dto.setCreatedById(subject.getCreatedBy().getId());
        dto.setCreatedByName(subject.getCreatedBy().getFirstName() + " " + subject.getCreatedBy().getLastName());
        dto.setCreatedByRole(subject.getCreatedBy().getRoles().isEmpty() ? "USER" : 
                            subject.getCreatedBy().getRoles().iterator().next().getName().toString());
        dto.setStatus(subject.getStatus());
        dto.setCreatedAt(subject.getCreatedAt());
        dto.setUpdatedAt(subject.getUpdatedAt());
        
        // Count applications
        dto.setApplicationCount(subject.getApplications() != null ? subject.getApplications().size() : 0);
        dto.setPendingApplications(pfeSubjectRepository.countPendingApplicationsBySubject(subject.getId()).intValue());
        dto.setAcceptedApplications(pfeSubjectRepository.countAcceptedApplicationsBySubject(subject.getId()).intValue());
        
        // Check evaluation
        dto.setHasEvaluation(subject.getEvaluation() != null);
        if (subject.getEvaluation() != null) {
            dto.setEvaluationGrade(subject.getEvaluation().getGrade());
        }
        
        return dto;
    }

    private ApplicationDto convertToApplicationDto(Application application) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(application.getId());
        dto.setStudentId(application.getStudent().getId());
        dto.setStudentName(application.getStudent().getFirstName() + " " + application.getStudent().getLastName());
        dto.setStudentEmail(application.getStudent().getEmail());
        dto.setSubjectId(application.getSubject().getId());
        dto.setSubjectTitle(application.getSubject().getTitle());
        dto.setSubjectCreatedBy(application.getSubject().getCreatedBy().getFirstName() + " " + 
                               application.getSubject().getCreatedBy().getLastName());
        dto.setStatus(application.getStatus());
        dto.setMotivation(application.getMotivation());
        dto.setCvFileName(application.getCvFileName());
        dto.setCvFileUrl(application.getCvFileUrl());
        dto.setCvOriginalFileName(application.getCvOriginalFileName());
        dto.setAppliedAt(application.getAppliedAt());
        dto.setReviewedAt(application.getReviewedAt());
        if (application.getReviewedBy() != null) {
            dto.setReviewedById(application.getReviewedBy().getId());
            dto.setReviewedByName(application.getReviewedBy().getFirstName() + " " + 
                                 application.getReviewedBy().getLastName());
        }
        dto.setReviewComment(application.getReviewComment());
        // Handle null workflowStep for existing applications
        dto.setWorkflowStep(application.getWorkflowStep() != null ? 
                           application.getWorkflowStep().toString() : 
                           Application.WorkflowStep.APPLICATION_REVIEW.toString());
        dto.setProjectReviewComment(application.getProjectReviewComment());
        dto.setProjectReviewedAt(application.getProjectReviewedAt());
        dto.setReportReviewComment(application.getReportReviewComment());
        dto.setReportReviewedAt(application.getReportReviewedAt());
        dto.setJuryDate(application.getJuryDate());
        dto.setJuryLocation(application.getJuryLocation());
        return dto;
    }

    // Document management methods
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public DocumentDto uploadDocument(Long subjectId, MultipartFile file, String documentType, String description, Long userId) throws IOException {
        // Verify user has access to this subject (either student with accepted application or subject creator)
        PFESubject subject = pfeSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get the application for workflow validation
        Application application = applicationRepository.findByStudentIdAndSubjectId(userId, subjectId)
                .orElse(null);

        // Check permissions and workflow step
        boolean isConsultant = subject.getCreatedBy().getId().equals(userId);
        boolean isAcceptedStudent = application != null && 
                                   application.getStatus() == Application.ApplicationStatus.ACCEPTED;
        
        if (!isConsultant && !isAcceptedStudent) {
            throw new RuntimeException("You don't have permission to upload documents for this subject");
        }

        // Validate workflow step and file type for students
        if (!isConsultant && application != null) {
            validateWorkflowUpload(application, documentType, file);
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, "pfe", subjectId.toString());
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create file URL (relative path for serving files)
        String fileUrl = "/uploads/pfe/" + subjectId + "/" + uniqueFilename;

        // Create document entity
        Document document = new Document();
        document.setFileName(uniqueFilename);
        document.setOriginalFileName(originalFilename);
        document.setFileUrl(fileUrl);
        document.setDocumentType(Document.DocumentType.valueOf(documentType.toUpperCase()));
        document.setDescription(description);
        document.setFileSize(file.getSize());
        document.setUploadedBy(user);
        document.setSubject(subject);
        
        // Set workflow step for students
        if (!isConsultant && application != null) {
            document.setWorkflowStep(application.getWorkflowStep());
        }
        
        document = documentRepository.save(document);
        
        // Update application workflow step after successful upload
        if (!isConsultant && application != null) {
            updateWorkflowAfterUpload(application, documentType);
        }
        
        return convertToDocumentDto(document);
    }

    public List<DocumentDto> getSubjectDocuments(Long subjectId, Long userId) {
        // Verify user has access to this subject
        PFESubject subject = pfeSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        
        boolean hasPermission = subject.getCreatedBy().getId().equals(userId) || 
                               applicationRepository.existsBySubjectIdAndStudentIdAndStatus(subjectId, userId, Application.ApplicationStatus.ACCEPTED);
        
        if (!hasPermission) {
            throw new RuntimeException("You don't have permission to view documents for this subject");
        }

        return documentRepository.findBySubjectIdOrderByUploadDateDesc(subjectId)
                .stream()
                .map(this::convertToDocumentDto)
                .collect(Collectors.toList());
    }

    public void deleteDocument(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Only the uploader or subject creator can delete
        if (!document.getUploadedBy().getId().equals(userId) && 
            !document.getSubject().getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this document");
        }

        // Delete physical file
        try {
            Path filePath = Paths.get(uploadDir, "pfe", document.getSubject().getId().toString(), document.getFileName());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
        }

        documentRepository.delete(document);
    }

    public Resource downloadDocument(Long documentId, Long userId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Verify user has access
        boolean hasPermission = document.getUploadedBy().getId().equals(userId) ||
                               document.getSubject().getCreatedBy().getId().equals(userId) ||
                               applicationRepository.existsBySubjectIdAndStudentIdAndStatus(
                                   document.getSubject().getId(), userId, Application.ApplicationStatus.ACCEPTED);
        
        if (!hasPermission) {
            throw new RuntimeException("You don't have permission to download this document");
        }

        Path filePath = Paths.get(uploadDir, "pfe", document.getSubject().getId().toString(), document.getFileName());
        Resource resource = new UrlResource(filePath.toUri());
        
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found or not readable");
        }
        
        return resource;
    }

    private DocumentDto convertToDocumentDto(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setFileName(document.getFileName());
        dto.setOriginalFileName(document.getOriginalFileName());
        dto.setFileUrl(document.getFileUrl());
        dto.setDocumentType(document.getDocumentType().toString());
        dto.setDescription(document.getDescription());
        dto.setFileSize(document.getFileSize());
        dto.setUploadedByName(document.getUploadedBy().getFirstName() + " " + document.getUploadedBy().getLastName());
        dto.setUploadedByEmail(document.getUploadedBy().getEmail());
        dto.setUploadedAt(document.getUploadDate());
        dto.setSubjectId(document.getSubject().getId());
        dto.setSubjectTitle(document.getSubject().getTitle());
        return dto;
    }

    // Workflow validation methods
    private void validateWorkflowUpload(Application application, String documentType, MultipartFile file) {
        Document.DocumentType docType = Document.DocumentType.valueOf(documentType.toUpperCase());
        Application.WorkflowStep currentStep = application.getWorkflowStep();
        
        switch (currentStep) {
            case PROJECT_UPLOAD:
                if (docType != Document.DocumentType.PROJECT) {
                    throw new RuntimeException("You must upload a PROJECT file at this step");
                }
                validateProjectFile(file);
                break;
                
            case REPORT_UPLOAD:
                if (docType != Document.DocumentType.REPORT) {
                    throw new RuntimeException("You must upload a REPORT file at this step");
                }
                validateReportFile(file);
                break;
                
            default:
                throw new RuntimeException("Document upload not allowed at current workflow step: " + currentStep);
        }
    }
    
    private void validateProjectFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".rar")) {
            throw new RuntimeException("Project must be uploaded as a RAR file");
        }
        
        // Check file size (max 50MB for project files)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new RuntimeException("Project file size must be less than 50MB");
        }
    }
    
    private void validateReportFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("Invalid file");
        }
        
        String lowerFilename = filename.toLowerCase();
        if (!lowerFilename.endsWith(".pdf") && 
            !lowerFilename.endsWith(".doc") && 
            !lowerFilename.endsWith(".docx")) {
            throw new RuntimeException("Report must be uploaded as PDF, DOC, or DOCX file");
        }
        
        // Check file size (max 10MB for report files)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("Report file size must be less than 10MB");
        }
    }
    
    private void updateWorkflowAfterUpload(Application application, String documentType) {
        Document.DocumentType docType = Document.DocumentType.valueOf(documentType.toUpperCase());
        
        switch (docType) {
            case PROJECT:
                application.setWorkflowStep(Application.WorkflowStep.PROJECT_REVIEW);
                break;
            case REPORT:
                application.setWorkflowStep(Application.WorkflowStep.REPORT_REVIEW);
                break;
            case PRESENTATION:
            case OTHER:
                // These don't change workflow step
                break;
        }
        
        applicationRepository.save(application);
    }

    // Workflow review methods
    public ApplicationDto reviewProject(Long applicationId, ReviewProjectRequest request, Long reviewerId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        // Check if reviewer is the subject creator
        if (!application.getSubject().getCreatedBy().getId().equals(reviewerId)) {
            throw new RuntimeException("You can only review applications for your own subjects");
        }

        // Validate workflow step
        if (application.getWorkflowStep() != Application.WorkflowStep.PROJECT_REVIEW) {
            throw new RuntimeException("Project is not ready for review at this step");
        }

        application.setProjectReviewComment(request.getComment());
        application.setProjectReviewedAt(LocalDateTime.now());

        if (request.isApproved()) {
            application.setWorkflowStep(Application.WorkflowStep.REPORT_UPLOAD);
        } else {
            // If rejected, go back to project upload
            application.setWorkflowStep(Application.WorkflowStep.PROJECT_UPLOAD);
        }

        Application updatedApplication = applicationRepository.save(application);
        return convertToApplicationDto(updatedApplication);
    }

    public ApplicationDto reviewReport(Long applicationId, ReviewReportRequest request, Long reviewerId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        // Check if reviewer is the subject creator
        if (!application.getSubject().getCreatedBy().getId().equals(reviewerId)) {
            throw new RuntimeException("You can only review applications for your own subjects");
        }

        // Validate workflow step
        if (application.getWorkflowStep() != Application.WorkflowStep.REPORT_REVIEW) {
            throw new RuntimeException("Report is not ready for review at this step");
        }

        application.setReportReviewComment(request.getComment());
        application.setReportReviewedAt(LocalDateTime.now());

        if (request.isApproved()) {
            application.setWorkflowStep(Application.WorkflowStep.JURY_SCHEDULING);
        } else {
            // If rejected, go back to report upload
            application.setWorkflowStep(Application.WorkflowStep.REPORT_UPLOAD);
        }

        Application updatedApplication = applicationRepository.save(application);
        return convertToApplicationDto(updatedApplication);
    }

    public ApplicationDto scheduleJury(Long applicationId, ScheduleJuryRequest request, Long reviewerId) {
        // Utiliser fetch join pour charger student et subject (éviter lazy loading)
        Application application = applicationRepository.findByIdWithStudentAndSubject(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        // Check if reviewer is the subject creator
        if (!application.getSubject().getCreatedBy().getId().equals(reviewerId)) {
            throw new RuntimeException("You can only schedule jury for your own subjects");
        }

        // Validate workflow step
        if (application.getWorkflowStep() != Application.WorkflowStep.JURY_SCHEDULING) {
            throw new RuntimeException("Application is not ready for jury scheduling");
        }

        application.setJuryDate(request.getJuryDate());
        application.setJuryLocation(request.getJuryLocation());
        application.setWorkflowStep(Application.WorkflowStep.COMPLETED);

        applicationRepository.save(application);
        
        // Recharger avec fetch join pour être sûr que student et subject sont disponibles
        Application updatedApplication = applicationRepository.findByIdWithStudentAndSubject(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found after save"));
        
        // Envoyer un email de succès au candidat que toutes les étapes sont complétées (même logique que confirmOrder)
        try {
            User student = updatedApplication.getStudent();
            if (student == null || student.getEmail() == null || student.getEmail().isEmpty()) {
                System.err.println("Cannot send PFE completion email: student or email is null for application " + updatedApplication.getId());
            } else {
                System.out.println("Attempting to send PFE completion email to: " + student.getEmail());
                // Récupérer la note si l'évaluation existe
                String[] grade = {null};
                evaluationRepository.findBySubjectId(application.getSubject().getId())
                    .ifPresent(eval -> grade[0] = eval.getGrade() != null ? eval.getGrade().toString() : null);
                
                emailService.sendPFECompletion(updatedApplication, grade[0]);
                System.out.println("PFE completion email sent successfully to: " + student.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Error sending PFE completion email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return convertToApplicationDto(updatedApplication);
    }

    public ApplicationDto getApplicationById(Long applicationId, Long userId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Check if user has access to this application
        boolean hasAccess = application.getStudent().getId().equals(userId) ||
                           application.getSubject().getCreatedBy().getId().equals(userId);

        if (!hasAccess) {
            throw new RuntimeException("You don't have permission to view this application");
        }

        // Update workflow step for existing applications if null
        if (application.getWorkflowStep() == null) {
            if (application.getStatus() == Application.ApplicationStatus.ACCEPTED) {
                application.setWorkflowStep(Application.WorkflowStep.PROJECT_UPLOAD);
            } else {
                application.setWorkflowStep(Application.WorkflowStep.APPLICATION_REVIEW);
            }
            applicationRepository.save(application);
        }

        return convertToApplicationDto(application);
    }

    public Resource downloadCV(Long applicationId, Long userId) throws IOException {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        // Verify user has access (student or subject creator)
        boolean hasAccess = application.getStudent().getId().equals(userId) ||
                           application.getSubject().getCreatedBy().getId().equals(userId);
        
        if (!hasAccess) {
            throw new RuntimeException("You don't have permission to download this CV");
        }

        if (application.getCvFileName() == null || application.getCvFileUrl() == null) {
            throw new RuntimeException("No CV uploaded for this application");
        }

        Path filePath = Paths.get(uploadDir, "pfe", "cv", application.getSubject().getId().toString(), application.getCvFileName());
        Resource resource = new UrlResource(filePath.toUri());
        
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("CV file not found or not readable");
        }
        
        return resource;
    }
}
