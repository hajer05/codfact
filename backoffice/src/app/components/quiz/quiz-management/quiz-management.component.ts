import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QuizService, Quiz, GenerateQuizRequest } from '../../../services/quiz.service';
import { CourseService } from '../../../services/course.service';
import { AuthService } from '../../../services/auth.service';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-quiz-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './quiz-management.component.html',
  styleUrls: ['./quiz-management.component.scss']
})
export class QuizManagementComponent implements OnInit {
  courses: any[] = [];
  quizzes: Quiz[] = [];
  selectedCourseId: number | null = null;
  loading = false;
  error = '';
  
  // Generate Quiz Form
  showGenerateForm = false;
  generateRequest: GenerateQuizRequest = {
    courseId: 0,
    numberOfQuestions: 10,
    difficulty: 'MEDIUM',
    focusTopics: ''
  };
  generating = false;
  
  // View Quiz
  selectedQuiz: Quiz | null = null;
  showQuizDetails = false;

  constructor(
    private quizService: QuizService,
    private courseService: CourseService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.loading = true;
    const currentUser = this.authService.getCurrentUser();
    
    // Les enseignants voient uniquement leurs propres cours
    // Les admins voient tous les cours
    const isTeacher = currentUser?.roles?.includes('TEACHER') && !currentUser?.roles?.includes('ADMIN');
    
    const coursesObservable = isTeacher 
      ? this.courseService.getMyCourses()
      : this.courseService.getAllCourses();
    
    coursesObservable.subscribe({
      next: (courses) => {
        this.courses = courses;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des cours';
        this.loading = false;
        console.error(err);
      }
    });
  }

  selectCourse(courseId: number): void {
    this.selectedCourseId = courseId;
    this.loadCourseQuizzes(courseId);
  }

  loadCourseQuizzes(courseId: number): void {
    this.loading = true;
    this.quizService.getCourseQuizzes(courseId).subscribe({
      next: (quizzes) => {
        this.quizzes = quizzes;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des quiz';
        this.loading = false;
        console.error(err);
      }
    });
  }

  openGenerateForm(courseId: number): void {
    this.generateRequest.courseId = courseId;
    this.showGenerateForm = true;
  }

  closeGenerateForm(): void {
    this.showGenerateForm = false;
    this.generateRequest = {
      courseId: 0,
      numberOfQuestions: 10,
      difficulty: 'MEDIUM',
      focusTopics: ''
    };
  }

  generateQuiz(): void {
    if (!this.generateRequest.courseId) return;

    this.generating = true;
    this.error = '';

    this.quizService.generateQuiz(this.generateRequest).subscribe({
      next: (quiz) => {
        this.generating = false;
        this.closeGenerateForm();
        alert('Quiz généré avec succès avec l\'IA !');
        if (this.selectedCourseId) {
          this.loadCourseQuizzes(this.selectedCourseId);
        }
      },
      error: (err) => {
        this.generating = false;
        this.error = 'Erreur lors de la génération du quiz : ' + (err.error?.message || err.message);
        console.error(err);
      }
    });
  }

  viewQuiz(quiz: Quiz): void {
    this.selectedQuiz = quiz;
    this.showQuizDetails = true;
  }

  closeQuizDetails(): void {
    this.showQuizDetails = false;
    this.selectedQuiz = null;
  }

  approveQuiz(quizId: number): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    if (!confirm('Êtes-vous sûr de vouloir approuver ce quiz ?')) return;

    this.quizService.approveQuiz(quizId, user.id).subscribe({
      next: () => {
        alert('Quiz approuvé avec succès !');
        if (this.selectedCourseId) {
          this.loadCourseQuizzes(this.selectedCourseId);
        }
        this.closeQuizDetails();
      },
      error: (err) => {
        this.error = 'Erreur lors de l\'approbation du quiz';
        console.error(err);
      }
    });
  }

  rejectQuiz(quizId: number): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    if (!confirm('Êtes-vous sûr de vouloir rejeter ce quiz ?')) return;

    this.quizService.rejectQuiz(quizId, user.id).subscribe({
      next: () => {
        alert('Quiz rejeté');
        if (this.selectedCourseId) {
          this.loadCourseQuizzes(this.selectedCourseId);
        }
        this.closeQuizDetails();
      },
      error: (err) => {
        this.error = 'Erreur lors du rejet du quiz';
        console.error(err);
      }
    });
  }

  viewResults(quizId: number): void {
    this.router.navigate(['/quiz-results', quizId]);
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'APPROVED':
        return 'badge-success';
      case 'PENDING_APPROVAL':
        return 'badge-warning';
      case 'REJECTED':
        return 'badge-danger';
      default:
        return 'badge-secondary';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'APPROVED':
        return 'Approuvé';
      case 'PENDING_APPROVAL':
        return 'En attente';
      case 'REJECTED':
        return 'Rejeté';
      default:
        return status;
    }
  }
}

