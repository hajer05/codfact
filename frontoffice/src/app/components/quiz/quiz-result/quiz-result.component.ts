import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { QuizService, QuizAttempt } from '../../../services/quiz.service';
import { CertificateService } from '../../../services/certificate.service';
import { AuthService } from '../../../services/auth.service';
import { ToastNotificationService } from '../../../services/toast-notification.service';

@Component({
  selector: 'app-quiz-result',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './quiz-result.component.html',
  styleUrls: ['./quiz-result.component.scss']
})
export class QuizResultComponent implements OnInit {
  attempt: QuizAttempt | null = null;
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private certificateService: CertificateService,
    private authService: AuthService,
    private toastService: ToastNotificationService
  ) { }

  ngOnInit(): void {
    const attemptId = Number(this.route.snapshot.paramMap.get('attemptId'));
    if (attemptId) {
      this.loadAttemptResults(attemptId);
    }
  }

  loadAttemptResults(attemptId: number): void {
    this.loading = true;
    this.error = '';
    
    this.quizService.getAttemptResults(attemptId).subscribe({
      next: (attempt) => {
        this.attempt = attempt;
        this.loading = false;
        
        // Afficher un message de toast selon le résultat
        if (attempt.passed) {
          console.log('✅ Quiz réussi ! Score:', attempt.score + '%');
          this.toastService.success(`🎉 Félicitations ! Vous avez réussi le quiz avec ${attempt.score}%`);
        } else {
          console.log('❌ Quiz échoué. Score:', attempt.score + '%');
          this.toastService.info(`Vous avez obtenu ${attempt.score}%. Révisez le cours et réessayez !`);
        }
      },
      error: (err) => {
        console.error('Erreur lors du chargement des résultats:', err);
        this.error = 'Impossible de charger les résultats du quiz';
        this.loading = false;
        this.toastService.error('Erreur lors du chargement des résultats');
      }
    });
  }

  get scorePercentage(): number {
    if (!this.attempt) return 0;
    return this.attempt.score || 0;
  }

  get scoreClass(): string {
    const score = this.scorePercentage;
    if (score >= 80) return 'excellent';
    if (score >= 70) return 'good';
    if (score >= 50) return 'average';
    return 'poor';
  }

  get resultMessage(): string {
    if (!this.attempt) return '';
    
    if (this.attempt.passed) {
      return 'Félicitations ! Vous avez réussi le quiz.';
    } else {
      return 'Vous n\'avez pas atteint le score minimum requis.';
    }
  }

  retakeQuiz(): void {
    if (this.attempt && this.attempt.quizId) {
      this.router.navigate(['/quiz-take', this.attempt.quizId]);
    }
  }

  goToCourse(): void {
    if (this.attempt && this.attempt.courseId) {
      this.router.navigate(['/course-details', this.attempt.courseId]);
    }
  }

  downloadCertificate(): void {
    if (!this.attempt?.passed) {
      this.toastService.error('Vous devez réussir le quiz pour obtenir le certificat');
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.toastService.error('Vous devez être connecté');
      return;
    }

    this.loading = true;
    this.toastService.show('Génération du certificat en cours...', 'info');

    // First check if certificate already exists
    this.certificateService.hasCertificate(currentUser.id, this.attempt.courseId).subscribe({
      next: (hasCert) => {
        if (hasCert) {
          // Certificate exists, get it and download
          this.getCertificateAndDownload(currentUser.id, this.attempt!.courseId);
        } else {
          // Generate new certificate
          this.generateNewCertificate(currentUser.id, this.attempt!.courseId);
        }
      },
      error: (error) => {
        console.error('Error checking certificate:', error);
        // Try to generate anyway
        this.generateNewCertificate(currentUser.id, this.attempt!.courseId);
      }
    });
  }

  private generateNewCertificate(userId: number, courseId: number): void {
    this.certificateService.generateCertificate(userId, courseId).subscribe({
      next: (certificate) => {
        this.loading = false;
        this.toastService.success('Certificat généré avec succès!');
        // Download the certificate using the filename
        if (certificate.certificateFileName) {
          const downloadUrl = this.certificateService.downloadCertificate(certificate.certificateFileName);
          window.open(downloadUrl, '_blank');
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error generating certificate:', error);
        this.toastService.error('Erreur lors de la génération du certificat');
      }
    });
  }

  private getCertificateAndDownload(userId: number, courseId: number): void {
    this.certificateService.getCertificate(userId, courseId).subscribe({
      next: (certificate) => {
        this.loading = false;
        if (certificate && certificate.certificateFileName) {
          const downloadUrl = this.certificateService.downloadCertificate(certificate.certificateFileName);
          window.open(downloadUrl, '_blank');
          this.toastService.success('Certificat téléchargé avec succès!');
        } else {
          this.toastService.error('Certificat introuvable');
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error getting certificate:', error);
        this.toastService.error('Erreur lors du téléchargement du certificat');
      }
    });
  }

  getCorrectAnswersCount(): number {
    if (!this.attempt?.questionResults) return 0;
    return this.attempt.questionResults.filter(q => q.isCorrect).length;
  }
}

