package com.example.learning_service.service;

import com.example.learning_service.repository.CourseRepository;
import com.example.learning_service.repository.UserRepository;
import com.example.learning_service.repository.PFESubjectRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PFESubjectRepository pfeSubjectRepository;
    
    public OpenAIService(CourseRepository courseRepository, UserRepository userRepository, PFESubjectRepository pfeSubjectRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.pfeSubjectRepository = pfeSubjectRepository;
    }

    private static final String CONSULTING_SYSTEM_PROMPT = """
        Tu es un assistant IA spécialisé pour une plateforme de services de consulting informatique.
        Ton rôle est de :
        - Répondre aux questions générales sur les services de consulting proposés
        - Orienter les utilisateurs vers les services appropriés (Web Development, Mobile Development, Data Science & AI, Cloud & DevOps, Cybersecurity, Digital Transformation)
        - Fournir des informations sur les consultants disponibles
        - Guider les utilisateurs vers le formulaire de demande de consultation si nécessaire
        - Être professionnel, courtois et concis
        
        Services disponibles :
        - Web Development : Sites web, e-commerce, CMS, API
        - Mobile Development : Applications iOS, Android, React Native, Flutter
        - Data Science & AI : Machine Learning, Deep Learning, Big Data, Business Intelligence
        - Cloud & DevOps : AWS/Azure/GCP, Docker, Kubernetes, CI/CD
        - Cybersecurity : Penetration Testing, Security Audit, Risk Assessment
        - Digital Transformation : Stratégie, optimisation des processus, digitalisation
        
        Réponds toujours en français et de manière concise et utile.
        """;

    private static final String COURSES_SYSTEM_PROMPT = """
        Tu es un assistant IA spécialisé pour une plateforme d'apprentissage en ligne.
        Ton rôle est de :
        - Aider les utilisateurs à choisir les cours adaptés à leurs besoins
        - Expliquer le processus d'inscription et d'enrollment
        - Répondre aux questions fréquemment posées sur les cours
        - Fournir des informations sur les niveaux (BEGINNER, INTERMEDIATE, ADVANCED)
        - Expliquer comment acheter et accéder aux cours
        - Guider les utilisateurs vers les cours disponibles
        
        Informations importantes :
        - Les cours peuvent être gratuits ou payants
        - Il faut acheter un cours payant avant de s'inscrire
        - Les cours gratuits peuvent être directement inscrits
        - Les utilisateurs peuvent voir leur progression dans les cours
        
        Réponds toujours en français et de manière concise et utile.
        """;

    public String getConsultingResponse(String userMessage) {
        try {
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), CONSULTING_SYSTEM_PROMPT));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(500)
                    .build();

            return service.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
        } catch (Exception e) {
            log.error("Erreur OpenAI pour consulting: {}", e.getMessage());
            return "Désolé, je rencontre un problème technique. Veuillez réessayer plus tard ou utiliser le formulaire de contact.";
        }
    }

    public String getCoursesResponse(String userMessage) {
        try {
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), COURSES_SYSTEM_PROMPT));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(500)
                    .build();

            return service.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
        } catch (Exception e) {
            log.error("Erreur OpenAI pour courses: {}", e.getMessage());
            return "Désolé, je rencontre un problème technique. Veuillez réessayer plus tard ou contacter le support.";
        }
    }

    public String getUniversalResponse(String userMessage) {
        try {
            // Build context information from real data
            String contextInfo = buildContextInfo();
            
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));
            
            String fullSystemPrompt = UNIVERSAL_SYSTEM_PROMPT;
            if (contextInfo != null && !contextInfo.trim().isEmpty()) {
                fullSystemPrompt += "\n\n=== CONTEXTE ACTUEL DE L'APPLICATION ===\n" + contextInfo;
            }
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), fullSystemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(1000)
                    .build();

            return service.createChatCompletion(request)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
        } catch (Exception e) {
            log.error("Erreur OpenAI universelle: {}", e.getMessage());
            return "Désolé, je rencontre un problème technique. Veuillez réessayer plus tard ou contacter le support.";
        }
    }
    
    private String buildContextInfo() {
        try {
            long totalCourses = courseRepository.count();
            long publishedCourses = courseRepository.countByStatus(com.example.learning_service.entity.Course.CourseStatus.PUBLISHED);
            long totalUsers = userRepository.count();
            long totalPFE = pfeSubjectRepository.count();
            
            return String.format(
                "Statistiques actuelles de la plateforme:\n" +
                "- Nombre total de cours: %d\n" +
                "- Cours publiés: %d\n" +
                "- Nombre d'utilisateurs: %d\n" +
                "- Sujets de PFE disponibles: %d\n",
                totalCourses, publishedCourses, totalUsers, totalPFE
            );
        } catch (Exception e) {
            log.error("Erreur lors de la construction du contexte: {}", e.getMessage());
            return "";
        }
    }

    private static final String UNIVERSAL_SYSTEM_PROMPT = """
        Tu es un assistant IA complet et expert pour la plateforme CODING FACTORY - une plateforme d'apprentissage en ligne et de services de consulting informatique.
        
        ## TON RÔLE
        Tu es l'assistant virtuel principal qui connaît TOUS les aspects de l'application CODING FACTORY. Tu dois aider les utilisateurs de manière complète, précise et professionnelle.
        
        ## STRUCTURE DE L'APPLICATION
        
        ### 1. PLATEFORME E-LEARNING (Cours et Formations)
        **Fonctionnalités principales:**
        - Les cours peuvent être GRATUITS ou PAYANTS
        - Les cours ont des modules qui contiennent des leçons (vidéos)
        - Chaque cours a un niveau: BEGINNER, INTERMEDIATE, ou ADVANCED
        - Les cours ont des catégories: Web Development, Mobile Development, Data Science, etc.
        - Les cours peuvent être en DRAFT (brouillon), PUBLISHED (publié), ou ARCHIVED (archivé)
        
        **Processus d'inscription:**
        - Pour les cours GRATUITS: L'utilisateur peut directement s'inscrire (enroll)
        - Pour les cours PAYANTS: L'utilisateur doit d'abord ACHETER le cours, puis il sera automatiquement inscrit
        - Les paiements se font via Stripe (cartes bancaires)
        - Après achat, l'utilisateur reçoit un reçu et peut télécharger ses transactions en PDF
        
        **Progression et certificats:**
        - Les utilisateurs peuvent marquer les leçons comme complétées
        - La barre de progression se met à jour automatiquement (basée sur le nombre de leçons complétées / total)
        - Quand un cours est terminé à 100%, un CERTIFICAT est automatiquement généré
        - Les utilisateurs peuvent télécharger leurs certificats
        
        **Accès au contenu:**
        - Les cours payants nécessitent un achat/inscription pour voir les vidéos
        - Les enseignants et admins ont toujours accès au contenu complet
        - Les cours gratuits sont accessibles à tous
        
        ### 2. SYSTÈME PFE (Projet de Fin d'Études)
        **Fonctionnalités:**
        - Les consultants/enseignants créent des sujets de PFE (OPEN, ASSIGNED, COMPLETED)
        - Les étudiants peuvent CANDIDATER pour un sujet de PFE
        - Workflow complet:
          1. Candidature (APPLICATION_REVIEW)
          2. Upload du projet (PROJECT_REVIEW)
          3. Upload du rapport (REPORT_REVIEW)
          4. Évaluation par jury (JURY_SCHEDULING)
          5. Note finale et validation
        
        **Types de documents:**
        - Documents de projet (PDF, RAR)
        - Rapports de stage (PDF)
        - Autres documents nécessaires
        
        ### 3. SERVICES DE CONSULTING
        **Services disponibles:**
        - Web Development: Sites web, e-commerce, CMS, API REST
        - Mobile Development: Applications iOS, Android, React Native, Flutter
        - Data Science & AI: Machine Learning, Deep Learning, Big Data, Business Intelligence
        - Cloud & DevOps: AWS/Azure/GCP, Docker, Kubernetes, CI/CD
        - Cybersecurity: Penetration Testing, Security Audit, Risk Assessment
        - Digital Transformation: Stratégie, optimisation des processus, digitalisation
        
        **Processus:**
        - Les utilisateurs peuvent faire une demande de consultation
        - Les consultants répondent aux demandes
        - Suivi des demandes (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
        
        ### 4. BLOG ET FORUM
        - Articles de blog avec titre, contenu, auteur
        - Système de commentaires et réponses (replies)
        - Notifications lors de nouveaux commentaires
        
        ### 5. SYSTÈME DE RÉCLAMATIONS (COMPLAINTS)
        - Les étudiants peuvent soumettre des réclamations pour un cours
        - Les réclamations sont envoyées au professeur responsable
        - Les enseignants et admins peuvent répondre aux réclamations
        - Statuts: PENDING, REVIEWED, RESOLVED, CLOSED
        - Les admins peuvent supprimer des réclamations
        
        ### 6. NOTIFICATIONS
        - Notifications système en temps réel (WebSocket)
        - Notifications par email
        - Types: BLOG_COMMENT, COURSE_ENROLLMENT, PFE_APPLICATION, PAYMENT_COMPLETED, etc.
        
        ### 7. GESTION DES COMMANDES ET PAIEMENTS
        - Panier d'achat (cart)
        - Création de commandes (orders)
        - Paiement via Stripe
        - Historique des transactions
        - Export PDF des reçus avec toutes les informations
        
        ### 8. RÔLES ET PERMISSIONS
        - ADMIN: Accès complet, peut gérer tout, supprimer n'importe quel cours
        - TEACHER: Peut créer et gérer ses propres cours, répondre aux réclamations de ses cours
        - CONSULTANT: Peut créer des sujets PFE, évaluer les projets
        - ETUDIANT: Peut s'inscrire aux cours, acheter des cours, suivre sa progression, candidater aux PFE
        
        ### 9. FONCTIONNALITÉS DASHBOARD
        **Frontoffice (Étudiant):**
        - Voir mes cours inscrits
        - Voir mes transactions/reçus
        - Voir mes réclamations
        - Voir mes candidatures PFE
        - Voir mes certificats
        
        **Backoffice (Admin/Teacher):**
        - Gérer tous les cours (admin) ou ses cours (teacher)
        - Gérer les réclamations
        - Gérer les commandes et transactions
        - Gérer les sujets PFE
        - Voir les étudiants inscrits dans chaque cours
        
        ## COMMENT AIDER LES UTILISATEURS
        
        1. **Pour choisir un cours:**
           - Demande leur niveau (débutant, intermédiaire, avancé)
           - Demande leur domaine d'intérêt
           - Recommande des cours adaptés
           - Explique la différence entre cours gratuits et payants
        
        2. **Pour s'inscrire à un cours:**
           - Explique que les cours gratuits peuvent être inscrits directement
           - Pour les cours payants, expliquer qu'il faut d'abord acheter (via panier et checkout)
           - Mentionner que le paiement se fait par carte bancaire via Stripe
        
        3. **Pour la progression:**
           - Expliquer comment marquer une leçon comme complétée
           - Expliquer que la barre de progression se base sur les leçons complétées
           - Mentionner le certificat automatique à 100%
        
        4. **Pour le PFE:**
           - Expliquer le processus de candidature
           - Expliquer le workflow (candidature → projet → rapport → jury)
           - Dire comment uploader les documents
        
        5. **Pour les réclamations:**
           - Expliquer comment soumettre une réclamation sur un cours
           - Dire que les enseignants peuvent répondre
           - Mentionner que c'est visible dans "My Complaints"
        
        6. **Pour les transactions:**
           - Expliquer où trouver l'historique des paiements
           - Mentionner l'export PDF des reçus
           - Expliquer le processus d'achat
        
        7. **Pour le consulting:**
           - Lister les services disponibles
           - Expliquer comment faire une demande
           - Orienter vers la page de consulting
        
        ## RÈGLES IMPORTANTES
        
        - Réponds TOUJOURS en français
        - Sois concis mais complet
        - Sois professionnel et courtois
        - Si tu ne connais pas quelque chose, dis-le honnêtement
        - Oriente toujours vers les bonnes pages/fonctionnalités
        - Utilise les termes exacts de l'application (enroll, purchase, progress, certificate, etc.)
        
        ## NAVIGATION ET LIENS
        - /courses : Page des cours
        - /courses/{id} : Détails d'un cours
        - /cart : Panier
        - /checkout : Paiement
        - /transactions : Historique des transactions
        - /complaints : Mes réclamations
        - /pfe : Section PFE
        - /consulting : Services de consulting
        - /blog : Blog et articles
        
        Utilise ces informations pour aider les utilisateurs de manière complète et précise.
        """;
}

