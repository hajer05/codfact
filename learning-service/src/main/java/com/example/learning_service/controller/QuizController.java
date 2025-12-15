package com.example.learning_service.controller;

import com.example.learning_service.dto.*;
import com.example.learning_service.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizController {
    
    private final QuizService quizService;
    
    /**
     * Générer un quiz avec IA (Admin ou Enseignant)
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<QuizDto> generateQuiz(@RequestBody GenerateQuizRequest request) {
        try {
            log.info("Generating quiz for course: {}", request.getCourseId());
            QuizDto quiz = quizService.generateQuizWithAI(request);
            log.info("Quiz generated successfully with {} questions", quiz.getTotalQuestions());
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            log.error("Error generating quiz: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Approuver un quiz (Admin ou Enseignant propriétaire du cours)
     */
    @PostMapping("/{quizId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<QuizDto> approveQuiz(
            @PathVariable Long quizId,
            @RequestParam Long approvedBy) {
        try {
            log.info("Approving quiz: {} by user: {}", quizId, approvedBy);
            QuizDto quiz = quizService.approveQuiz(quizId, approvedBy);
            log.info("Quiz approved successfully");
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            log.error("Error approving quiz: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Rejeter un quiz
     */
    @PostMapping("/{quizId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> rejectQuiz(
            @PathVariable Long quizId,
            @RequestParam Long rejectedBy) {
        try {
            log.info("Rejecting quiz: {} by user: {}", quizId, rejectedBy);
            quizService.rejectQuiz(quizId, rejectedBy);
            log.info("Quiz rejected successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error rejecting quiz: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir tous les quiz d'un cours (Admin/Enseignant)
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<QuizDto>> getCourseQuizzes(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "true") boolean includeQuestions) {
        try {
            log.info("Getting quizzes for course: {}, includeQuestions: {}", courseId, includeQuestions);
            List<QuizDto> quizzes = quizService.getCourseQuizzes(courseId, includeQuestions);
            log.info("Found {} quizzes for course {}", quizzes.size(), courseId);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            log.error("Error getting course quizzes: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir les quiz approuvés d'un cours (Pour les étudiants)
     */
    @GetMapping("/course/{courseId}/approved")
    public ResponseEntity<List<QuizDto>> getApprovedCourseQuizzes(@PathVariable Long courseId) {
        try {
            List<QuizDto> quizzes = quizService.getApprovedCourseQuizzes(courseId);
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir un quiz par ID (Admin/Enseignant - avec réponses)
     */
    @GetMapping("/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<QuizDto> getQuiz(@PathVariable Long quizId) {
        try {
            log.info("Getting quiz: {}", quizId);
            QuizDto quiz = quizService.getQuizById(quizId, true);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            log.error("Error getting quiz {}: ", quizId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtenir un quiz pour un étudiant (sans les réponses)
     */
    @GetMapping("/{quizId}/student")
    public ResponseEntity<QuizDto> getQuizForStudent(@PathVariable Long quizId) {
        try {
            QuizDto quiz = quizService.getQuizForStudent(quizId);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Commencer une tentative de quiz
     */
    @PostMapping("/{quizId}/start")
    public ResponseEntity<QuizAttemptDto> startQuizAttempt(
            @PathVariable Long quizId,
            @RequestParam Long userId) {
        try {
            QuizAttemptDto attempt = quizService.startQuizAttempt(quizId, userId);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Soumettre les réponses du quiz
     */
    @PostMapping("/attempts/submit")
    public ResponseEntity<QuizAttemptDto> submitQuizAttempt(@RequestBody SubmitQuizRequest request) {
        try {
            QuizAttemptDto result = quizService.submitQuizAttempt(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir toutes les tentatives de quiz d'un utilisateur
     */
    @GetMapping("/attempts/user/{userId}")
    public ResponseEntity<List<QuizAttemptDto>> getUserQuizAttempts(@PathVariable Long userId) {
        try {
            List<QuizAttemptDto> attempts = quizService.getUserAllAttempts(userId);
            return ResponseEntity.ok(attempts);
        } catch (Exception e) {
            log.error("Error getting user quiz attempts: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir les tentatives d'un utilisateur pour un cours
     */
    @GetMapping("/attempts/user/{userId}/course/{courseId}")
    public ResponseEntity<List<QuizAttemptDto>> getUserCourseAttempts(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        try {
            List<QuizAttemptDto> attempts = quizService.getUserCourseAttempts(userId, courseId);
            return ResponseEntity.ok(attempts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Vérifier si l'utilisateur a réussi le quiz du cours
     */
    @GetMapping("/check-passed/user/{userId}/course/{courseId}")
    public ResponseEntity<Boolean> hasPassedCourseQuiz(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        try {
            boolean passed = quizService.hasPassedCourseQuiz(userId, courseId);
            return ResponseEntity.ok(passed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Vérifier si un cours a un quiz approuvé
     */
    @GetMapping("/course/{courseId}/has-quiz")
    public ResponseEntity<Boolean> courseHasApprovedQuiz(@PathVariable Long courseId) {
        try {
            boolean hasQuiz = quizService.courseHasApprovedQuiz(courseId);
            return ResponseEntity.ok(hasQuiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir le quiz approuvé d'un cours (pour redirection)
     */
    @GetMapping("/course/{courseId}/approved-quiz")
    public ResponseEntity<QuizDto> getApprovedQuizForCourse(@PathVariable Long courseId) {
        try {
            return quizService.getApprovedQuizForCourse(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir les résultats détaillés d'une tentative de quiz
     */
    @GetMapping("/attempts/{attemptId}/results")
    public ResponseEntity<QuizAttemptDto> getAttemptResults(@PathVariable Long attemptId) {
        log.info("📊 GET /attempts/{}/results - Récupération des résultats de la tentative", attemptId);
        try {
            QuizAttemptDto result = quizService.getAttemptResults(attemptId);
            log.info("✅ Résultats de la tentative {} récupérés avec succès", attemptId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des résultats de la tentative {}: {}", attemptId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir la dernière tentative réussie d'un utilisateur pour un cours
     */
    @GetMapping("/attempts/user/{userId}/course/{courseId}/last-passed")
    public ResponseEntity<QuizAttemptDto> getLastPassedAttempt(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        log.info("🏆 GET /attempts/user/{}/course/{}/last-passed", userId, courseId);
        try {
            QuizAttemptDto lastPassed = quizService.getLastPassedAttempt(userId, courseId);
            if (lastPassed != null) {
                log.info("✅ Dernière tentative réussie trouvée pour l'utilisateur {} sur le cours {}", userId, courseId);
                return ResponseEntity.ok(lastPassed);
            } else {
                log.info("ℹ️ Aucune tentative réussie trouvée pour l'utilisateur {} sur le cours {}", userId, courseId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération de la dernière tentative réussie: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir tous les résultats de quiz pour un cours (Admin/Enseignant)
     */
    @GetMapping("/results/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<QuizAttemptDto>> getAllCourseQuizResults(@PathVariable Long courseId) {
        try {
            log.info("Getting quiz results for course: {}", courseId);
            List<QuizAttemptDto> results = quizService.getAllCourseQuizResults(courseId);
            log.info("Found {} results for course {}", results.size(), courseId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error getting course quiz results: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Obtenir tous les résultats d'un quiz spécifique (Admin/Enseignant)
     */
    @GetMapping("/results/quiz/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<QuizAttemptDto>> getAllQuizResults(@PathVariable Long quizId) {
        try {
            log.info("Getting results for quiz: {}", quizId);
            List<QuizAttemptDto> results = quizService.getAllQuizResults(quizId);
            log.info("Found {} results for quiz {}", results.size(), quizId);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error getting quiz results: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}

