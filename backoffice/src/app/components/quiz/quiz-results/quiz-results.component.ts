import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { QuizService, Quiz, QuizAttempt } from '../../../services/quiz.service';

@Component({
  selector: 'app-quiz-results',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './quiz-results.component.html',
  styleUrls: ['./quiz-results.component.scss']
})
export class QuizResultsComponent implements OnInit {
  quiz: Quiz | null = null;
  attempts: QuizAttempt[] = [];
  loading = false;
  error = '';
  quizId: number = 0;

  // Statistics
  totalAttempts = 0;
  passedAttempts = 0;
  averageScore = 0;
  highestScore = 0;
  lowestScore = 100;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService
  ) { }

  ngOnInit(): void {
    this.quizId = Number(this.route.snapshot.paramMap.get('quizId'));
    if (this.quizId) {
      this.loadQuiz();
      this.loadResults();
    }
  }

  loadQuiz(): void {
    this.quizService.getQuiz(this.quizId).subscribe({
      next: (quiz) => {
        this.quiz = quiz;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du quiz';
        console.error(err);
      }
    });
  }

  loadResults(): void {
    this.loading = true;
    this.quizService.getAllQuizResults(this.quizId).subscribe({
      next: (attempts) => {
        this.attempts = attempts.filter(a => a.isCompleted);
        this.calculateStatistics();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des résultats';
        this.loading = false;
        console.error(err);
      }
    });
  }

  calculateStatistics(): void {
    this.totalAttempts = this.attempts.length;
    this.passedAttempts = this.attempts.filter(a => a.passed).length;

    if (this.totalAttempts > 0) {
      const scores = this.attempts.map(a => a.score || 0);
      this.averageScore = Math.round(scores.reduce((a, b) => a + b, 0) / this.totalAttempts);
      this.highestScore = Math.max(...scores);
      this.lowestScore = Math.min(...scores);
    }
  }

  getPassRate(): number {
    if (this.totalAttempts === 0) return 0;
    return Math.round((this.passedAttempts / this.totalAttempts) * 100);
  }

  getScoreClass(score: number | undefined): string {
    if (!score) return '';
    if (score >= 80) return 'excellent';
    if (score >= 70) return 'good';
    if (score >= 50) return 'average';
    return 'poor';
  }

  goBack(): void {
    this.router.navigate(['/quiz-management']);
  }

  exportResults(): void {
    // Implement CSV export
    alert('Fonctionnalité d\'export en cours de développement');
  }

  calculateDuration(attempt: QuizAttempt): string {
    if (!attempt.startedAt || !attempt.completedAt) return 'N/A';
    
    const start = new Date(attempt.startedAt).getTime();
    const end = new Date(attempt.completedAt).getTime();
    const durationMs = end - start;
    
    const minutes = Math.floor(durationMs / 60000);
    const seconds = Math.floor((durationMs % 60000) / 1000);
    
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }
}

