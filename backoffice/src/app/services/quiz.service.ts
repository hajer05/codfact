import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface QuizQuestion {
  id: number;
  quizId: number;
  question: string;
  type: 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'SHORT_ANSWER';
  options: string[];
  correctAnswer: string;
  explanation?: string;
  points: number;
  questionOrder: number;
}

export interface Quiz {
  id: number;
  courseId: number;
  courseTitle?: string;
  title: string;
  description: string;
  status: 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'ARCHIVED';
  passingScore: number;
  durationMinutes: number;
  createdAt: string;
  approvedAt?: string;
  approvedBy?: number;
  approvedByName?: string;
  generatedByAI: boolean;
  questions: QuizQuestion[];
  totalQuestions?: number;
  totalPoints?: number;
}

export interface QuizAttempt {
  id: number;
  quizId: number;
  userId: number;
  courseId: number;
  startedAt: string;
  completedAt?: string;
  expiresAt: string;
  isExpired: boolean;
  score?: number;
  totalPoints?: number;
  earnedPoints?: number;
  passed?: boolean;
  isCompleted: boolean;
  answers?: { [key: number]: string };
  quizTitle?: string;
  userName?: string;
  courseTitle?: string;
  remainingTimeSeconds?: number;
  questionResults?: QuestionResult[];
}

export interface QuestionResult {
  questionId: number;
  question: string;
  studentAnswer: string;
  correctAnswer: string;
  isCorrect: boolean;
  points: number;
  earnedPoints: number;
  explanation: string;
}

export interface GenerateQuizRequest {
  courseId: number;
  numberOfQuestions: number;
  difficulty: string;
  focusTopics?: string;
}

@Injectable({
  providedIn: 'root'
})
export class QuizService {
  private apiUrl = `${environment.apiUrl}/quizzes`;

  constructor(private http: HttpClient) { }

  /**
   * Générer un quiz avec IA (Admin ou Enseignant)
   */
  generateQuiz(request: GenerateQuizRequest): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.apiUrl}/generate`, request);
  }

  /**
   * Approuver un quiz
   */
  approveQuiz(quizId: number, approvedBy: number): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.apiUrl}/${quizId}/approve`, null, {
      params: { approvedBy: approvedBy.toString() }
    });
  }

  /**
   * Rejeter un quiz
   */
  rejectQuiz(quizId: number, rejectedBy: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${quizId}/reject`, null, {
      params: { rejectedBy: rejectedBy.toString() }
    });
  }

  /**
   * Obtenir tous les quiz d'un cours (Admin/Enseignant)
   */
  getCourseQuizzes(courseId: number, includeQuestions: boolean = true): Observable<Quiz[]> {
    return this.http.get<Quiz[]>(`${this.apiUrl}/course/${courseId}`, {
      params: { includeQuestions: includeQuestions.toString() }
    });
  }

  /**
   * Obtenir un quiz par ID (Admin/Enseignant - avec réponses)
   */
  getQuiz(quizId: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/${quizId}`);
  }

  /**
   * Obtenir tous les résultats de quiz pour un cours (Admin/Enseignant)
   */
  getAllCourseQuizResults(courseId: number): Observable<QuizAttempt[]> {
    return this.http.get<QuizAttempt[]>(`${this.apiUrl}/results/course/${courseId}`);
  }

  /**
   * Obtenir tous les résultats d'un quiz spécifique (Admin/Enseignant)
   */
  getAllQuizResults(quizId: number): Observable<QuizAttempt[]> {
    return this.http.get<QuizAttempt[]>(`${this.apiUrl}/results/quiz/${quizId}`);
  }

  /**
   * Vérifier si un cours a un quiz approuvé
   */
  courseHasApprovedQuiz(courseId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/course/${courseId}/has-quiz`);
  }
}

