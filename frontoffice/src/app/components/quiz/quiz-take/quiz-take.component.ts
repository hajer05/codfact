import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { QuizService, Quiz, QuizQuestion, QuizAttempt } from '../../../services/quiz.service';
import { AuthService } from '../../../services/auth.service';
import { CanComponentDeactivate } from '../../../guards/quiz-deactivate.guard';
import { QuizExitModalComponent } from './quiz-exit-modal.component';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-quiz-take',
  standalone: true,
  imports: [CommonModule, FormsModule, QuizExitModalComponent],
  templateUrl: './quiz-take.component.html',
  styleUrls: ['./quiz-take.component.scss']
})
export class QuizTakeComponent implements OnInit, OnDestroy, CanComponentDeactivate {
  quiz: Quiz | null = null;
  attempt: QuizAttempt | null = null;
  answers: { [key: number]: string } = {};
  currentQuestionIndex = 0;
  loading = false;
  error = '';
  
  // Timer
  remainingSeconds = 0;
  timerSubscription?: Subscription;
  timeWarning = false;
  
  // État
  quizStarted = false;
  quizSubmitted = false;
  showExitModal = false;
  pendingNavigation: (() => void) | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    const quizId = Number(this.route.snapshot.paramMap.get('quizId'));
    if (quizId) {
      this.loadQuiz(quizId);
    }
  }

  ngOnDestroy(): void {
    // Arrêter le timer quand le composant est détruit
    this.stopTimer();
    console.log('⏹️ Quiz component destroyed - timer stopped');
  }

  // Prévenir la navigation pendant le quiz (fermeture onglet/navigateur)
  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any): void {
    if (this.quizStarted && !this.quizSubmitted) {
      // Message standard du navigateur (ne peut pas être personnalisé pour des raisons de sécurité)
      $event.preventDefault();
      $event.returnValue = '⚠️ QUIZ EN COURS - Vos réponses seront perdues si vous quittez !';
      return $event.returnValue;
    }
  }

  loadQuiz(quizId: number): void {
    this.loading = true;
    this.quizService.getQuizForStudent(quizId).subscribe({
      next: (quiz) => {
        console.log('Quiz chargé:', quiz);
        console.log('Nombre de questions:', quiz.questions?.length || 0);
        this.quiz = quiz;
        
        // Vérifier si les questions sont chargées
        if (!quiz.questions || quiz.questions.length === 0) {
          this.error = 'Aucune question disponible pour ce quiz';
          console.error('Le quiz ne contient pas de questions');
        }
        
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Impossible de charger le quiz';
        this.loading = false;
        console.error('Erreur lors du chargement du quiz:', err);
      }
    });
  }

  startQuiz(): void {
    if (!this.quiz) {
      console.error('Pas de quiz disponible');
      return;
    }
    
    if (!this.quiz.questions || this.quiz.questions.length === 0) {
      this.error = 'Le quiz ne contient pas de questions';
      console.error('Quiz sans questions:', this.quiz);
      return;
    }
    
    const user = this.authService.getCurrentUser();
    if (!user) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    console.log('Démarrage du quiz avec', this.quiz.questions.length, 'questions');
    
    this.quizService.startQuizAttempt(this.quiz.id, user.id).subscribe({
      next: (attempt) => {
        console.log('Quiz attempt créé:', attempt);
        this.attempt = attempt;
        this.quizStarted = true;
        this.loading = false;
        
        // Initialiser le timer
        if (attempt.remainingTimeSeconds) {
          this.remainingSeconds = attempt.remainingTimeSeconds;
          this.startTimer();
        } else if (this.quiz) {
          // Fallback: utiliser la durée du quiz si remainingTime n'est pas fourni
          this.remainingSeconds = this.quiz.durationMinutes * 60;
          this.startTimer();
        }
        
        console.log('Quiz démarré, timer:', this.remainingSeconds, 'secondes');
      },
      error: (err) => {
        this.error = 'Impossible de démarrer le quiz: ' + (err.error?.message || err.message);
        this.loading = false;
        console.error('Erreur lors du démarrage:', err);
      }
    });
  }

  startTimer(): void {
    this.timerSubscription = interval(1000).subscribe(() => {
      this.remainingSeconds--;
      
      // Warning à 2 minutes
      if (this.remainingSeconds === 120) {
        this.timeWarning = true;
      }
      
      // Temps écoulé
      if (this.remainingSeconds <= 0) {
        this.submitQuiz(true);
      }
    });
  }

  stopTimer(): void {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
      console.log('⏸️ Timer arrêté');
    }
  }

  get currentQuestion(): QuizQuestion | null {
    if (!this.quiz || !this.quiz.questions) return null;
    return this.quiz.questions[this.currentQuestionIndex] || null;
  }

  get progress(): number {
    if (!this.quiz) return 0;
    return ((this.currentQuestionIndex + 1) / this.quiz.questions.length) * 100;
  }

  get formattedTime(): string {
    const minutes = Math.floor(this.remainingSeconds / 60);
    const seconds = this.remainingSeconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  nextQuestion(): void {
    if (this.quiz && this.currentQuestionIndex < this.quiz.questions.length - 1) {
      this.currentQuestionIndex++;
    }
  }

  previousQuestion(): void {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  goToQuestion(index: number): void {
    this.currentQuestionIndex = index;
  }

  isQuestionAnswered(index: number): boolean {
    if (!this.quiz) return false;
    const question = this.quiz.questions[index];
    return this.answers[question.id] !== undefined && this.answers[question.id] !== '';
  }

  get answeredCount(): number {
    return Object.keys(this.answers).filter(key => this.answers[Number(key)] !== '').length;
  }

  submitQuiz(autoSubmit = false): void {
    if (!this.attempt) return;

    if (!autoSubmit) {
      if (!confirm('Êtes-vous sûr de vouloir soumettre vos réponses ? Vous ne pourrez plus les modifier.')) {
        return;
      }
    }

    this.stopTimer();
    this.loading = true;

    const submitRequest = {
      attemptId: this.attempt.id,
      answers: this.answers
    };

    this.quizService.submitQuizAttempt(submitRequest).subscribe({
      next: (result) => {
        console.log('✅ Quiz soumis avec succès ! Résultat:', result);
        console.log('📊 Redirection vers les résultats, attemptId:', result.id);
        this.quizSubmitted = true;
        this.loading = false;
        // Rediriger vers les résultats
        this.router.navigate(['/quiz-result', result.id]);
      },
      error: (err) => {
        this.error = autoSubmit 
          ? 'Le temps est écoulé. Le quiz a été soumis automatiquement.'
          : 'Erreur lors de la soumission du quiz';
        this.loading = false;
        console.error(err);
        
        // Même en cas d'erreur, rediriger après 2 secondes
        setTimeout(() => {
          if (this.quiz) {
            this.router.navigate(['/course-details', this.quiz.courseId]);
          }
        }, 2000);
      }
    });
  }

  exitQuiz(): void {
    if (this.quizStarted && !this.quizSubmitted) {
      // Afficher la modal personnalisée
      this.showExitModal = true;
    } else {
      // Si le quiz n'est pas démarré, retour direct
      this.navigateAway();
    }
  }

  confirmExit(): void {
    this.showExitModal = false;
    this.stopTimer();
    this.quizStarted = false;
    console.log('🚪 Utilisateur a quitté le quiz');
    
    // Exécuter la navigation en attente ou retourner à la page du cours
    if (this.pendingNavigation) {
      this.pendingNavigation();
      this.pendingNavigation = null;
    } else {
      this.navigateAway();
    }
  }

  cancelExit(): void {
    this.showExitModal = false;
    this.pendingNavigation = null;
  }

  navigateAway(): void {
    if (this.quiz) {
      this.router.navigate(['/course-details', this.quiz.courseId]);
    } else {
      this.router.navigate(['/courses']);
    }
  }

  // Implémentation de CanComponentDeactivate pour bloquer la navigation
  canDeactivate(): boolean {
    // Si le quiz est démarré mais pas encore soumis, afficher la modal
    if (this.quizStarted && !this.quizSubmitted) {
      // Stocker la navigation en attente
      this.showExitModal = true;
      
      // Retourner false pour bloquer la navigation immédiate
      // L'utilisateur devra confirmer via la modal
      return false;
    }
    
    // Autoriser la navigation si le quiz n'est pas démarré ou déjà soumis
    return true;
  }
}

