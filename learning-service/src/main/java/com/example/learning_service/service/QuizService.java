package com.example.learning_service.service;

import com.example.learning_service.dto.*;
import com.example.learning_service.entity.*;
import com.example.learning_service.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {
    
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    
    @Value("${openai.api.key}")
    private String openAIKey;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Génère un quiz avec OpenAI basé sur le contenu du cours
     */
    @Transactional
    public QuizDto generateQuizWithAI(GenerateQuizRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Récupérer le contenu du cours pour contexte
        String courseContext = buildCourseContext(course);
        
        // Générer les questions avec OpenAI
        String aiResponse = generateQuestionsWithOpenAI(course, courseContext, request);
        
        // Parser la réponse JSON d'OpenAI
        Quiz quiz = parseAndCreateQuiz(aiResponse, course, request);
        
        return convertToDto(quiz, true);
    }
    
    private String buildCourseContext(Course course) {
        StringBuilder context = new StringBuilder();
        context.append("Cours: ").append(course.getTitle()).append("\n");
        context.append("Description: ").append(course.getDescription()).append("\n");
        context.append("Niveau: ").append(course.getLevel()).append("\n");
        context.append("Catégorie: ").append(course.getCategory()).append("\n\n");
        
        // Ajouter les modules et leçons
        List<com.example.learning_service.entity.Module> modules = moduleRepository.findByCourseIdOrderByOrderIndex(course.getId());
        for (com.example.learning_service.entity.Module module : modules) {
            context.append("Module: ").append(module.getTitle()).append("\n");
            context.append("Description: ").append(module.getDescription()).append("\n");
            
            List<Lesson> lessons = lessonRepository.findByModuleIdOrderByOrderIndex(module.getId());
            for (Lesson lesson : lessons) {
                context.append("  - Leçon: ").append(lesson.getTitle()).append("\n");
            }
            context.append("\n");
        }
        
        return context.toString();
    }
    
    private String generateQuestionsWithOpenAI(Course course, String courseContext, GenerateQuizRequest request) {
        try {
            OpenAiService service = new OpenAiService(openAIKey, Duration.ofSeconds(60));
            
            String systemPrompt = """
                Tu es un expert en création de quiz éducatifs. Tu dois générer des questions de quiz de haute qualité
                basées sur le contenu d'un cours fourni.
                
                IMPORTANT: Tu dois OBLIGATOIREMENT répondre avec un JSON valide dans ce format exact:
                {
                  "title": "Titre du quiz",
                  "description": "Description du quiz",
                  "questions": [
                    {
                      "question": "Question posée",
                      "type": "MULTIPLE_CHOICE",
                      "options": ["Option A", "Option B", "Option C", "Option D"],
                      "correctAnswer": "Option correcte",
                      "explanation": "Explication de la réponse",
                      "points": 1
                    }
                  ]
                }
                
                Types de questions disponibles: MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
                
                Pour TRUE_FALSE, options doit être: ["Vrai", "Faux"]
                Pour SHORT_ANSWER, options doit être un tableau vide: []
                
                Règles:
                - Crée des questions pertinentes et variées
                - Les questions doivent couvrir différents aspects du cours
                - Les explications doivent être claires et éducatives
                - Pour les choix multiples, 4 options dont 1 seule correcte
                - Varie les types de questions
                - Adapte la difficulté au niveau demandé
                
                Réponds UNIQUEMENT avec le JSON, sans texte avant ou après.
                """;
            
            String userPrompt = String.format("""
                Génère un quiz de %d questions pour le cours suivant:
                
                %s
                
                Difficulté: %s
                %s
                
                Génère le JSON maintenant:
                """,
                request.getNumberOfQuestions(),
                courseContext,
                request.getDifficulty(),
                request.getFocusTopics() != null ? "Focus sur: " + request.getFocusTopics() : ""
            );
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt));
            
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("gpt-4o-mini")
                .messages(messages)
                .temperature(0.7)
                .maxTokens(3000)
                .build();
            
            String response = service.createChatCompletion(completionRequest)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();
            
            log.info("OpenAI Response: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération du quiz avec OpenAI", e);
            throw new RuntimeException("Erreur lors de la génération du quiz: " + e.getMessage());
        }
    }
    
    @Transactional
    private Quiz parseAndCreateQuiz(String aiResponse, Course course, GenerateQuizRequest request) {
        try {
            // Nettoyer la réponse (enlever les balises markdown si présentes)
            String cleanedResponse = aiResponse.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();
            
            JsonNode root = objectMapper.readTree(cleanedResponse);
            
            // Créer le quiz
            Quiz quiz = new Quiz();
            quiz.setCourseId(course.getId());
            quiz.setTitle(root.get("title").asText());
            quiz.setDescription(root.get("description").asText());
            quiz.setStatus(Quiz.QuizStatus.PENDING_APPROVAL);
            quiz.setPassingScore(70);
            quiz.setGeneratedByAI(true);
            quiz.setCreatedAt(LocalDateTime.now());
            
            Quiz savedQuiz = quizRepository.save(quiz);
            
            // Créer les questions
            JsonNode questionsNode = root.get("questions");
            int order = 0;
            for (JsonNode questionNode : questionsNode) {
                QuizQuestion question = new QuizQuestion();
                question.setQuizId(savedQuiz.getId());
                question.setQuestion(questionNode.get("question").asText());
                question.setType(QuizQuestion.QuestionType.valueOf(questionNode.get("type").asText()));
                
                // Options
                List<String> options = new ArrayList<>();
                if (questionNode.has("options")) {
                    JsonNode optionsNode = questionNode.get("options");
                    for (JsonNode option : optionsNode) {
                        options.add(option.asText());
                    }
                }
                question.setOptions(options);
                
                question.setCorrectAnswer(questionNode.get("correctAnswer").asText());
                question.setExplanation(questionNode.has("explanation") ? 
                    questionNode.get("explanation").asText() : "");
                question.setPoints(questionNode.has("points") ? 
                    questionNode.get("points").asInt() : 1);
                question.setQuestionOrder(order++);
                
                quizQuestionRepository.save(question);
            }
            
            return savedQuiz;
            
        } catch (Exception e) {
            log.error("Erreur lors du parsing de la réponse OpenAI", e);
            throw new RuntimeException("Erreur lors du parsing du quiz: " + e.getMessage());
        }
    }
    
    /**
     * Approuver un quiz (enseignant ou admin)
     */
    @Transactional
    public QuizDto approveQuiz(Long quizId, Long approvedBy) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        // Vérifier que l'utilisateur a le droit d'approuver
        User approver = userRepository.findById(approvedBy)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Course course = courseRepository.findById(quiz.getCourseId())
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Vérifier que c'est l'enseignant du cours ou un admin
        boolean isAdmin = approver.getRoles().stream()
            .anyMatch(role -> role.getName().equals(Role.RoleName.ADMIN));
        boolean isTeacher = course.getTeacher().getId().equals(approvedBy);
        
        if (!isAdmin && !isTeacher) {
            throw new RuntimeException("Vous n'avez pas le droit d'approuver ce quiz");
        }
        
        quiz.setStatus(Quiz.QuizStatus.APPROVED);
        quiz.setApprovedBy(approvedBy);
        quiz.setApprovedAt(LocalDateTime.now());
        
        Quiz savedQuiz = quizRepository.save(quiz);
        return convertToDto(savedQuiz, true);
    }
    
    /**
     * Rejeter un quiz
     */
    @Transactional
    public void rejectQuiz(Long quizId, Long rejectedBy) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        // Vérifications similaires à approveQuiz
        User rejector = userRepository.findById(rejectedBy)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Course course = courseRepository.findById(quiz.getCourseId())
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        boolean isAdmin = rejector.getRoles().stream()
            .anyMatch(role -> role.getName().equals(Role.RoleName.ADMIN));
        boolean isTeacher = course.getTeacher().getId().equals(rejectedBy);
        
        if (!isAdmin && !isTeacher) {
            throw new RuntimeException("Vous n'avez pas le droit de rejeter ce quiz");
        }
        
        quiz.setStatus(Quiz.QuizStatus.REJECTED);
        quizRepository.save(quiz);
    }
    
    /**
     * Commencer une tentative de quiz
     */
    @Transactional
    public QuizAttemptDto startQuizAttempt(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        if (!quiz.getStatus().equals(Quiz.QuizStatus.APPROVED)) {
            throw new RuntimeException("Ce quiz n'est pas encore approuvé");
        }
        
        // Vérifier que l'utilisateur est inscrit au cours
        Course course = courseRepository.findById(quiz.getCourseId())
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(quiz.getDurationMinutes());
        
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quizId);
        attempt.setUserId(userId);
        attempt.setCourseId(quiz.getCourseId());
        attempt.setStartedAt(now);
        attempt.setExpiresAt(expiresAt);
        attempt.setIsExpired(false);
        attempt.setIsCompleted(false);
        
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        return convertAttemptToDto(savedAttempt, false);
    }
    
    /**
     * Soumettre les réponses et calculer le score
     */
    @Transactional
    public QuizAttemptDto submitQuizAttempt(SubmitQuizRequest request) {
        QuizAttempt attempt = quizAttemptRepository.findById(request.getAttemptId())
            .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));
        
        if (attempt.getIsCompleted()) {
            throw new RuntimeException("Ce quiz a déjà été soumis");
        }
        
        // Vérifier si le temps est écoulé
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(attempt.getExpiresAt())) {
            attempt.setIsExpired(true);
            attempt.setIsCompleted(true);
            attempt.setCompletedAt(now);
            attempt.setPassed(false);
            attempt.setScore(0);
            quizAttemptRepository.save(attempt);
            throw new RuntimeException("Le temps alloué pour ce quiz est écoulé. Vous devez recommencer.");
        }
        
        Quiz quiz = quizRepository.findById(attempt.getQuizId())
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByQuestionOrderAsc(quiz.getId());
        
        // Calculer le score
        int totalPoints = questions.stream().mapToInt(QuizQuestion::getPoints).sum();
        int earnedPoints = 0;
        
        List<QuestionResultDto> results = new ArrayList<>();
        
        for (QuizQuestion question : questions) {
            String studentAnswer = request.getAnswers().get(question.getId());
            boolean isCorrect = checkAnswer(question, studentAnswer);
            
            QuestionResultDto result = new QuestionResultDto();
            result.setQuestionId(question.getId());
            result.setQuestion(question.getQuestion());
            result.setStudentAnswer(studentAnswer);
            result.setCorrectAnswer(question.getCorrectAnswer());
            result.setIsCorrect(isCorrect);
            result.setPoints(question.getPoints());
            result.setEarnedPoints(isCorrect ? question.getPoints() : 0);
            result.setExplanation(question.getExplanation());
            
            results.add(result);
            
            if (isCorrect) {
                earnedPoints += question.getPoints();
            }
        }
        
        // Calculer le score sur 100
        int score = (int) Math.round((double) earnedPoints / totalPoints * 100);
        boolean passed = score >= quiz.getPassingScore();
        
        // Mettre à jour la tentative
        attempt.setAnswers(request.getAnswers());
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setTotalPoints(totalPoints);
        attempt.setEarnedPoints(earnedPoints);
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setIsCompleted(true);
        
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        
        QuizAttemptDto dto = convertAttemptToDto(savedAttempt, true);
        dto.setQuestionResults(results);
        
        return dto;
    }
    
    private boolean checkAnswer(QuizQuestion question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return false;
        }
        
        String correctAnswer = question.getCorrectAnswer().trim();
        studentAnswer = studentAnswer.trim();
        
        // Pour les questions à choix multiples et vrai/faux, comparaison exacte
        if (question.getType() == QuizQuestion.QuestionType.MULTIPLE_CHOICE ||
            question.getType() == QuizQuestion.QuestionType.TRUE_FALSE) {
            return correctAnswer.equalsIgnoreCase(studentAnswer);
        }
        
        // Pour les réponses courtes, comparaison plus flexible
        if (question.getType() == QuizQuestion.QuestionType.SHORT_ANSWER) {
            return correctAnswer.equalsIgnoreCase(studentAnswer) ||
                   studentAnswer.toLowerCase().contains(correctAnswer.toLowerCase());
        }
        
        return false;
    }
    
    /**
     * Obtenir tous les quiz d'un cours
     */
    public List<QuizDto> getCourseQuizzes(Long courseId, boolean includeQuestions) {
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        return quizzes.stream()
            .map(quiz -> convertToDto(quiz, includeQuestions))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtenir les quiz approuvés d'un cours
     */
    public List<QuizDto> getApprovedCourseQuizzes(Long courseId) {
        List<Quiz> quizzes = quizRepository.findByCourseIdAndStatus(courseId, Quiz.QuizStatus.APPROVED);
        return quizzes.stream()
            .map(quiz -> convertToDto(quiz, false))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtenir toutes les tentatives de quiz d'un utilisateur
     */
    public List<QuizAttemptDto> getUserAllAttempts(Long userId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(userId);
        return attempts.stream()
            .map(attempt -> convertAttemptToDto(attempt, true))
            .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtenir les tentatives d'un utilisateur pour un cours
     */
    public List<QuizAttemptDto> getUserCourseAttempts(Long userId, Long courseId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdAndCourseId(userId, courseId);
        return attempts.stream()
            .map(attempt -> convertAttemptToDto(attempt, true))
            .collect(Collectors.toList());
    }
    
    /**
     * Vérifier si l'utilisateur a réussi le quiz du cours
     */
    public boolean hasPassedCourseQuiz(Long userId, Long courseId) {
        return quizAttemptRepository.hasPassedQuiz(userId, courseId);
    }
    
    /**
     * Obtenir les résultats détaillés d'une tentative de quiz
     */
    public QuizAttemptDto getAttemptResults(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Tentative de quiz introuvable"));
        
        if (!attempt.getIsCompleted()) {
            throw new RuntimeException("Cette tentative n'est pas encore complétée");
        }
        
        // Récupérer les questions du quiz
        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByQuestionOrderAsc(attempt.getQuiz().getId());
        
        // Reconstruire les résultats des questions
        List<QuestionResultDto> results = new ArrayList<>();
        
        for (QuizQuestion question : questions) {
            String studentAnswer = attempt.getAnswers().get(question.getId());
            boolean isCorrect = checkAnswer(question, studentAnswer);
            
            QuestionResultDto result = new QuestionResultDto();
            result.setQuestionId(question.getId());
            result.setQuestion(question.getQuestion());
            result.setStudentAnswer(studentAnswer);
            result.setCorrectAnswer(question.getCorrectAnswer());
            result.setIsCorrect(isCorrect);
            result.setPoints(question.getPoints());
            result.setEarnedPoints(isCorrect ? question.getPoints() : 0);
            result.setExplanation(question.getExplanation());
            
            results.add(result);
        }
        
        QuizAttemptDto dto = convertAttemptToDto(attempt, true);
        dto.setQuestionResults(results);
        
        return dto;
    }
    
    /**
     * Obtenir la dernière tentative réussie d'un utilisateur pour un cours
     */
    public QuizAttemptDto getLastPassedAttempt(Long userId, Long courseId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdAndCourseIdOrderByCompletedAtDesc(userId, courseId);
        
        QuizAttempt lastPassed = attempts.stream()
            .filter(a -> a.getIsCompleted() && a.getPassed())
            .findFirst()
            .orElse(null);
        
        if (lastPassed == null) {
            return null;
        }
        
        return convertAttemptToDto(lastPassed, false);
    }
    
    /**
     * Obtenir un quiz avec ses questions (pour les enseignants)
     */
    public QuizDto getQuizById(Long quizId, boolean includeAnswers) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return convertToDto(quiz, includeAnswers);
    }
    
    /**
     * Obtenir un quiz pour un étudiant (sans les réponses correctes)
     */
    public QuizDto getQuizForStudent(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        if (!quiz.getStatus().equals(Quiz.QuizStatus.APPROVED)) {
            throw new RuntimeException("Ce quiz n'est pas disponible");
        }
        
        // IMPORTANT: inclure les questions (true) pour que l'étudiant puisse voir le quiz
        QuizDto dto = convertToDto(quiz, true);
        
        // Retirer les réponses correctes pour les étudiants
        if (dto.getQuestions() != null) {
            dto.getQuestions().forEach(q -> {
                q.setCorrectAnswer(null);
                q.setExplanation(null);
            });
        }
        
        return dto;
    }
    
    /**
     * Obtenir tous les résultats de quiz pour un cours (Admin/Enseignant)
     */
    public List<QuizAttemptDto> getAllCourseQuizResults(Long courseId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByCourseIdOrderByCompletedAtDesc(courseId);
        return attempts.stream()
            .map(attempt -> convertAttemptToDto(attempt, true))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtenir tous les résultats d'un quiz spécifique (Admin/Enseignant)
     */
    public List<QuizAttemptDto> getAllQuizResults(Long quizId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizIdOrderByCompletedAtDesc(quizId);
        return attempts.stream()
            .map(attempt -> convertAttemptToDto(attempt, true))
            .collect(Collectors.toList());
    }
    
    /**
     * Vérifier si un cours a un quiz approuvé
     */
    public boolean courseHasApprovedQuiz(Long courseId) {
        return quizRepository.existsByCourseIdAndStatus(courseId, Quiz.QuizStatus.APPROVED);
    }
    
    /**
     * Obtenir le quiz approuvé d'un cours
     */
    public Optional<QuizDto> getApprovedQuizForCourse(Long courseId) {
        return quizRepository.findFirstByCourseIdAndStatus(courseId, Quiz.QuizStatus.APPROVED)
            .map(quiz -> convertToDto(quiz, false));
    }
    
    private QuizDto convertToDto(Quiz quiz, boolean includeQuestions) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setCourseId(quiz.getCourseId());
        
        if (quiz.getCourse() != null) {
            dto.setCourseTitle(quiz.getCourse().getTitle());
        }
        
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setStatus(quiz.getStatus());
        dto.setPassingScore(quiz.getPassingScore());
        dto.setDurationMinutes(quiz.getDurationMinutes());
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setApprovedAt(quiz.getApprovedAt());
        dto.setApprovedBy(quiz.getApprovedBy());
        dto.setGeneratedByAI(quiz.getGeneratedByAI());
        
        if (quiz.getApprovedBy() != null) {
            userRepository.findById(quiz.getApprovedBy()).ifPresent(user -> 
                dto.setApprovedByName(user.getFirstName() + " " + user.getLastName())
            );
        }
        
        if (includeQuestions) {
            List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByQuestionOrderAsc(quiz.getId());
            dto.setQuestions(questions.stream()
                .map(this::convertQuestionToDto)
                .collect(Collectors.toList()));
            dto.setTotalQuestions(questions.size());
            dto.setTotalPoints(questions.stream().mapToInt(QuizQuestion::getPoints).sum());
        }
        
        return dto;
    }
    
    private QuizQuestionDto convertQuestionToDto(QuizQuestion question) {
        QuizQuestionDto dto = new QuizQuestionDto();
        dto.setId(question.getId());
        dto.setQuizId(question.getQuizId());
        dto.setQuestion(question.getQuestion());
        dto.setType(question.getType());
        dto.setOptions(new ArrayList<>(question.getOptions()));
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setPoints(question.getPoints());
        dto.setExplanation(question.getExplanation());
        dto.setQuestionOrder(question.getQuestionOrder());
        return dto;
    }
    
    private QuizAttemptDto convertAttemptToDto(QuizAttempt attempt, boolean includeDetails) {
        QuizAttemptDto dto = new QuizAttemptDto();
        dto.setId(attempt.getId());
        dto.setQuizId(attempt.getQuizId());
        dto.setUserId(attempt.getUserId());
        dto.setCourseId(attempt.getCourseId());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());
        dto.setExpiresAt(attempt.getExpiresAt());
        dto.setIsExpired(attempt.getIsExpired());
        
        // Calculer le temps restant en secondes
        if (attempt.getExpiresAt() != null && !attempt.getIsCompleted()) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(attempt.getExpiresAt())) {
                long secondsRemaining = java.time.Duration.between(now, attempt.getExpiresAt()).getSeconds();
                dto.setRemainingTimeSeconds(secondsRemaining);
            } else {
                dto.setRemainingTimeSeconds(0L);
            }
        }
        
        dto.setScore(attempt.getScore());
        dto.setTotalPoints(attempt.getTotalPoints());
        dto.setEarnedPoints(attempt.getEarnedPoints());
        dto.setPassed(attempt.getPassed());
        dto.setIsCompleted(attempt.getIsCompleted());
        
        if (includeDetails) {
            dto.setAnswers(new HashMap<>(attempt.getAnswers()));
        }
        
        if (attempt.getQuiz() != null) {
            dto.setQuizTitle(attempt.getQuiz().getTitle());
        }
        
        if (attempt.getUser() != null) {
            dto.setUserName(attempt.getUser().getFirstName() + " " + attempt.getUser().getLastName());
        }
        
        if (attempt.getCourse() != null) {
            dto.setCourseTitle(attempt.getCourse().getTitle());
        }
        
        return dto;
    }
}

