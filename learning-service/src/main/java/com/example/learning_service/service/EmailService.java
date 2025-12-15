package com.example.learning_service.service;

import com.example.learning_service.entity.Course;
import com.example.learning_service.entity.Enrollment;
import com.example.learning_service.entity.Order;
import com.example.learning_service.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.from-name}")
    private String fromName;
    
    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Send a simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            if (to == null || to.isEmpty()) {
                log.error("Cannot send email: recipient email is null or empty");
                return;
            }
            if (fromEmail == null || fromEmail.isEmpty()) {
                log.error("Cannot send email: sender email is null or empty (check app.email.from in application.properties)");
                return;
            }
            
            log.info("Sending email from: {} to: {} with subject: {}", fromEmail, to, subject);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent successfully from: {} to: {}", fromEmail, to);
        } catch (Exception e) {
            log.error("Failed to send simple email from: {} to: {} - Error: {}", fromEmail, to, e.getMessage(), e);
            // Ne pas lancer d'exception pour ne pas bloquer le flux métier
            // L'exception sera loggée pour debug
        }
    }

    /**
     * Send an HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // ---- Domain convenience methods ----
    public void sendCourseCreated(Course course, User teacher) {
        String subject = "Cours créé: " + course.getTitle();
        String body = "Bonjour " + teacher.getFirstName() + ",\n\n" +
                "Votre cours '" + course.getTitle() + "' a été créé en statut " + course.getStatus() + ".\n" +
                "Vous pouvez continuer l'édition dans votre espace enseignant.\n\n" +
                "— Plateforme Coding Factory";
        sendSimpleEmail(teacher.getEmail(), subject, body);
    }

    public void sendOrderCompleted(Order order) {
        try {
            User user = order.getUser();
            if (user == null) {
                log.error("Cannot send order completion email: user is null for order {}", order.getId());
                return;
            }
            log.info("Sending order completion email to: {} for order: {}", user.getEmail(), order.getOrderNumber());
            String subject = "Commande confirmée: " + order.getOrderNumber();
            String body = "Bonjour " + user.getFirstName() + ",\n\n" +
                    "Votre paiement a été confirmé. Montant: " + order.getTotalAmount() + ".\n" +
                    "Les accès à vos cours ont été activés.\n\n" +
                    "— Plateforme Coding Factory";
            sendSimpleEmail(user.getEmail(), subject, body);
        } catch (Exception e) {
            log.error("Error in sendOrderCompleted for order {}: {}", order.getId(), e.getMessage(), e);
        }
    }

    public void sendEnrollmentConfirmed(Enrollment enrollment) {
        User user = enrollment.getStudent();
        Course course = enrollment.getCourse();
        String subject = "Inscription confirmée: " + course.getTitle();
        String body = "Bonjour " + user.getFirstName() + ",\n\n" +
                "Vous êtes inscrit au cours '" + course.getTitle() + "'.\n" +
                "Bon apprentissage !\n\n" +
                "— Plateforme Coding Factory";
        sendSimpleEmail(user.getEmail(), subject, body);
    }

    /**
     * Send PFE acceptance notification (exact same logic as sendOrderCompleted)
     */
    public void sendPFEAcceptance(com.example.learning_service.entity.Application application) {
        try {
            com.example.learning_service.entity.User user = application.getStudent();
            if (user == null) {
                log.error("Cannot send PFE acceptance email: student is null for application {}", application.getId());
                return;
            }
            com.example.learning_service.entity.PFESubject subject = application.getSubject();
            if (subject == null) {
                log.error("Cannot send PFE acceptance email: subject is null for application {}", application.getId());
                return;
            }
            String subjectStr = "Candidature PFE acceptée: " + subject.getTitle();
            String body = "Bonjour " + user.getFirstName() + ",\n\n" +
                    "Votre candidature au PFE '" + subject.getTitle() + "' a été acceptée.\n" +
                    "Vous pouvez maintenant commencer votre projet de fin d'études.\n\n" +
                    "— Plateforme Coding Factory";
            sendSimpleEmail(user.getEmail(), subjectStr, body);
        } catch (Exception e) {
            log.error("Error in sendPFEAcceptance for application {}: {}", application.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send PFE rejection notification (same format)
     */
    public void sendPFERejection(com.example.learning_service.entity.Application application) {
        try {
            com.example.learning_service.entity.User user = application.getStudent();
            if (user == null) {
                log.error("Cannot send PFE rejection email: student is null for application {}", application.getId());
                return;
            }
            com.example.learning_service.entity.PFESubject subject = application.getSubject();
            if (subject == null) {
                log.error("Cannot send PFE rejection email: subject is null for application {}", application.getId());
                return;
            }
            String subjectStr = "Candidature PFE: " + subject.getTitle();
            String body = "Bonjour " + user.getFirstName() + ",\n\n" +
                    "Votre candidature au PFE '" + subject.getTitle() + "' n'a pas été retenue.\n" +
                    "Merci pour votre intérêt.\n\n" +
                    "— Plateforme Coding Factory";
            sendSimpleEmail(user.getEmail(), subjectStr, body);
        } catch (Exception e) {
            log.error("Error in sendPFERejection for application {}: {}", application.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send PFE completion success notification (exact same logic as sendOrderCompleted)
     */
    public void sendPFECompletion(com.example.learning_service.entity.Application application, String grade) {
        try {
            com.example.learning_service.entity.User user = application.getStudent();
            if (user == null) {
                log.error("Cannot send PFE completion email: student is null for application {}", application.getId());
                return;
            }
            com.example.learning_service.entity.PFESubject subject = application.getSubject();
            if (subject == null) {
                log.error("Cannot send PFE completion email: subject is null for application {}", application.getId());
                return;
            }
            String subjectStr = "PFE complété avec succès: " + subject.getTitle();
            String body = "Bonjour " + user.getFirstName() + ",\n\n" +
                    "Votre PFE '" + subject.getTitle() + "' a été complété avec succès." +
                    (grade != null ? "\nNote obtenue: " + grade + "." : "") +
                    "\nVous pouvez consulter tous les détails dans la plateforme.\n\n" +
                    "— Plateforme Coding Factory";
            sendSimpleEmail(user.getEmail(), subjectStr, body);
        } catch (Exception e) {
            log.error("Error in sendPFECompletion for application {}: {}", application.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send blog comment notification email
     */
    public void sendBlogCommentNotification(String recipientEmail, String recipientName, 
                                          String commenterName, String blogTitle, 
                                          String commentText, Long blogId) {
        try {
            String subject = "New comment on your blog: " + blogTitle;
            String htmlContent = createBlogCommentEmailTemplate(
                recipientName, commenterName, blogTitle, commentText, blogId
            );
            
            sendHtmlEmail(recipientEmail, subject, htmlContent);
            log.info("Blog comment notification sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send blog comment notification to: {}", recipientEmail, e);
        }
    }

    /**
     * Send course enrollment notification email
     */
    public void sendCourseEnrollmentNotification(String recipientEmail, String recipientName, 
                                               String courseName, Long courseId) {
        try {
            String subject = "Welcome to " + courseName + "!";
            String htmlContent = createCourseEnrollmentEmailTemplate(
                recipientName, courseName, courseId
            );
            
            sendHtmlEmail(recipientEmail, subject, htmlContent);
            log.info("Course enrollment notification sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send course enrollment notification to: {}", recipientEmail, e);
        }
    }

    /**
     * Send PFE application notification email
     */
    public void sendPFEApplicationNotification(String recipientEmail, String recipientName, 
                                             String applicantName, String pfeTitle, Long pfeId) {
        try {
            String subject = "New application for your PFE project: " + pfeTitle;
            String htmlContent = createPFEApplicationEmailTemplate(
                recipientName, applicantName, pfeTitle, pfeId
            );
            
            sendHtmlEmail(recipientEmail, subject, htmlContent);
            log.info("PFE application notification sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send PFE application notification to: {}", recipientEmail, e);
        }
    }

    /**
     * Create HTML template for blog comment notification
     */
    private String createBlogCommentEmailTemplate(String recipientName, String commenterName, 
                                                 String blogTitle, String commentText, Long blogId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>New Comment Notification</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .comment-box { background: white; padding: 20px; border-left: 4px solid #667eea; margin: 20px 0; border-radius: 5px; }
                    .button { display: inline-block; background: #667eea; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>💬 New Comment on Your Blog</h1>
                </div>
                <div class="content">
                    <p>Hi <strong>%s</strong>,</p>
                    <p><strong>%s</strong> just commented on your blog post "<strong>%s</strong>":</p>
                    
                    <div class="comment-box">
                        <p><em>"%s"</em></p>
                    </div>
                    
                    <p>Click the button below to view the full comment and reply:</p>
                    <a href="%s/blogs/%d" class="button">View Comment</a>
                    
                    <p>Thank you for being part of our learning community!</p>
                    
                    <div class="footer">
                        <p>Best regards,<br>The Coding Factory Team</p>
                        <p><small>This is an automated notification. Please do not reply to this email.</small></p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            recipientName, commenterName, blogTitle, 
            commentText.length() > 150 ? commentText.substring(0, 150) + "..." : commentText,
            baseUrl, blogId
        );
    }

    /**
     * Create HTML template for course enrollment notification
     */
    private String createCourseEnrollmentEmailTemplate(String recipientName, String courseName, Long courseId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Course Enrollment Confirmation</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #10b981; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🎓 Welcome to Your New Course!</h1>
                </div>
                <div class="content">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>Congratulations! You have successfully enrolled in "<strong>%s</strong>".</p>
                    
                    <p>You can now access all course materials, lessons, and start your learning journey.</p>
                    
                    <a href="%s/courses/%d" class="button">Start Learning</a>
                    
                    <p>Happy learning!</p>
                    
                    <div class="footer">
                        <p>Best regards,<br>The Coding Factory Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            recipientName, courseName, baseUrl, courseId
        );
    }

    /**
     * Create HTML template for PFE application notification
     */
    private String createPFEApplicationEmailTemplate(String recipientName, String applicantName, 
                                                   String pfeTitle, Long pfeId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>New PFE Application</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #f59e0b; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>📋 New PFE Application</h1>
                </div>
                <div class="content">
                    <p>Hi <strong>%s</strong>,</p>
                    <p><strong>%s</strong> has applied for your PFE project "<strong>%s</strong>".</p>
                    
                    <p>Please review the application and respond to the candidate.</p>
                    
                    <a href="%s/pfe/%d" class="button">Review Application</a>
                    
                    <div class="footer">
                        <p>Best regards,<br>The Coding Factory Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            recipientName, applicantName, pfeTitle, baseUrl, pfeId
        );
    }

    /**
     * Test email functionality
     */
    public void sendTestEmail(String to) {
        try {
            String subject = "Test Email from Coding Factory";
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Test Email</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #667eea; color: white; padding: 20px; text-align: center; border-radius: 10px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>✅ Email Configuration Test</h1>
                    </div>
                    <div style="padding: 20px;">
                        <p>This is a test email to verify that the SMTP configuration is working correctly.</p>
                        <p>If you received this email, the Gmail SMTP integration is working properly!</p>
                        <p><strong>Timestamp:</strong> %s</p>
                    </div>
                </body>
                </html>
                """, java.time.LocalDateTime.now());
            
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Test email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send test email to: {}", to, e);
            throw new RuntimeException("Failed to send test email", e);
        }
    }

    /**
     * Send PFE acceptance notification to candidate
     */
    public void sendPFEAcceptanceNotification(String recipientEmail, String recipientName, 
                                             String pfeTitle, Long pfeId) {
        try {
            String subject = "Félicitations ! Votre candidature PFE a été acceptée";
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>PFE Accepted</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .button { display: inline-block; background: #10b981; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>🎉 Félicitations !</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Nous avons le plaisir de vous informer que <strong>votre candidature au PFE "<strong>%s</strong>" a été acceptée</strong> !</p>
                        <p>Vous pouvez maintenant commencer votre projet de fin d'études.</p>
                        <p>Rendez-vous dans la plateforme pour télécharger les documents nécessaires et commencer votre travail.</p>
                        <a href="%s/pfe/%d" class="button">Accéder au PFE</a>
                        <p>Bon courage pour votre projet !</p>
                        <div class="footer">
                            <p>Meilleures salutations,<br>L'équipe Coding Factory</p>
                        </div>
                    </div>
                </body>
                </html>
                """, recipientName, pfeTitle, baseUrl, pfeId);
            
            sendHtmlEmail(recipientEmail, subject, htmlContent);
            log.info("PFE acceptance notification sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send PFE acceptance notification to: {}", recipientEmail, e);
        }
    }

    /**
     * Send PFE completion success message to candidate
     */
    public void sendPFECompletionSuccess(String recipientEmail, String recipientName, 
                                        String pfeTitle, String grade, Long pfeId) {
        try {
            String subject = "🎓 Félicitations ! Votre PFE est complété avec succès";
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>PFE Completed</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .success-box { background: white; padding: 20px; border-left: 4px solid #10b981; margin: 20px 0; border-radius: 5px; }
                        .button { display: inline-block; background: #667eea; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>🎓 Félicitations !</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <div class="success-box">
                            <p style="font-size: 18px; font-weight: bold; color: #10b981;">✅ Toutes les étapes de votre PFE ont été complétées avec succès !</p>
                            <p>Votre projet "<strong>%s</strong>" a été évalué et complété.</p>
                            %s
                        </div>
                        <p>Vous pouvez consulter tous les détails et les résultats dans la plateforme.</p>
                        <a href="%s/pfe/%d" class="button">Voir les détails</a>
                        <p>Félicitations pour ce travail remarquable !</p>
                        <div class="footer">
                            <p>Meilleures salutations,<br>L'équipe Coding Factory</p>
                        </div>
                    </div>
                </body>
                </html>
                """, recipientName, pfeTitle, 
                grade != null ? "<p><strong>Note obtenue : " + grade + "</strong></p>" : "",
                baseUrl, pfeId);
            
            sendHtmlEmail(recipientEmail, subject, htmlContent);
            log.info("PFE completion success email sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send PFE completion success email to: {}", recipientEmail, e);
        }
    }

    /**
     * Send complaint notification to teacher
     */
    public void sendComplaintNotification(com.example.learning_service.entity.Complaint complaint) {
        try {
            User teacher = complaint.getTeacher();
            User student = complaint.getStudent();
            Course course = complaint.getCourse();
            
            if (teacher == null || student == null || course == null) {
                log.error("Cannot send complaint notification: missing data for complaint {}", complaint.getId());
                return;
            }
            
            String subject = "New Complaint/Report: " + complaint.getSubject();
            String body = "Bonjour " + teacher.getFirstName() + ",\n\n" +
                    "Vous avez reçu une réclamation/rapport concernant votre cours '" + course.getTitle() + "'.\n\n" +
                    "Étudiant: " + student.getFirstName() + " " + student.getLastName() + " (" + student.getEmail() + ")\n" +
                    "Sujet: " + complaint.getSubject() + "\n\n" +
                    "Message:\n" + complaint.getMessage() + "\n\n" +
                    "Vous pouvez répondre à cette réclamation depuis votre espace enseignant.\n\n" +
                    "— Plateforme Coding Factory";
            sendSimpleEmail(teacher.getEmail(), subject, body);
            log.info("Complaint notification sent to teacher: {} for complaint: {}", teacher.getEmail(), complaint.getId());
        } catch (Exception e) {
            log.error("Error sending complaint notification for complaint {}: {}", complaint.getId(), e.getMessage(), e);
        }
    }

    /**
     * Send complaint response notification to student
     */
    public void sendComplaintResponse(com.example.learning_service.entity.Complaint complaint) {
        try {
            User student = complaint.getStudent();
            User teacher = complaint.getTeacher();
            Course course = complaint.getCourse();
            
            if (student == null || teacher == null || course == null) {
                log.error("Cannot send complaint response: missing data for complaint {}", complaint.getId());
                return;
            }
            
            String subject = "Response to your complaint: " + complaint.getSubject();
            String body = "Bonjour " + student.getFirstName() + ",\n\n" +
                    "Le professeur " + teacher.getFirstName() + " " + teacher.getLastName() +
                    " a répondu à votre réclamation concernant le cours '" + course.getTitle() + "'.\n\n" +
                    "Réponse:\n" + (complaint.getResponse() != null ? complaint.getResponse() : "Aucune réponse disponible") + "\n\n" +
                    "Vous pouvez consulter tous les détails dans votre espace étudiant.\n\n" +
                    "— Plateforme Coding Factory";
            sendSimpleEmail(student.getEmail(), subject, body);
            log.info("Complaint response sent to student: {} for complaint: {}", student.getEmail(), complaint.getId());
        } catch (Exception e) {
            log.error("Error sending complaint response for complaint {}: {}", complaint.getId(), e.getMessage(), e);
        }
    }
}
