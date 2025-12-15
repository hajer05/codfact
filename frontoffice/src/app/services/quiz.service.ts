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
  correctAnswer?: string;
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

export interface SubmitQuizRequest {
  attemptId: number;
  answers: { [key: number]: string };
}

@Injectable({
  providedIn: 'root'
})
export class QuizService {
  private apiUrl = `${environment.apiUrl}/quizzes`;

  constructor(private http: HttpClient) { }

  /**
   * Obtenir les quiz approuvés d'un cours
   */
  getApprovedCourseQuizzes(courseId: number): Observable<Quiz[]> {
    return this.http.get<Quiz[]>(`${this.apiUrl}/course/${courseId}/approved`);
  }

  /**
   * Obtenir un quiz pour un étudiant (sans les réponses)
   */
  getQuizForStudent(quizId: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/${quizId}/student`);
  }

  /**
   * Commencer une tentative de quiz
   */
  startQuizAttempt(quizId: number, userId: number): Observable<QuizAttempt> {
    return this.http.post<QuizAttempt>(`${this.apiUrl}/${quizId}/start`, null, {
      params: { userId: userId.toString() }
    });
  }

  /**
   * Soumettre les réponses du quiz
   */
  submitQuizAttempt(request: SubmitQuizRequest): Observable<QuizAttempt> {
    return this.http.post<QuizAttempt>(`${this.apiUrl}/attempts/submit`, request);
  }

  /**
   * Obtenir les tentatives d'un utilisateur pour un cours
   */
  getUserCourseAttempts(userId: number, courseId: number): Observable<QuizAttempt[]> {
    return this.http.get<QuizAttempt[]>(`${this.apiUrl}/attempts/user/${userId}/course/${courseId}`);
  }

  /**
   * Obtenir toutes les tentatives de quiz d'un utilisateur
   */
  getUserQuizAttempts(userId: number): Observable<QuizAttempt[]> {
    return this.http.get<QuizAttempt[]>(`${this.apiUrl}/attempts/user/${userId}`);
  }

  /**
   * Vérifier si l'utilisateur a réussi le quiz du cours
   */
  hasPassedCourseQuiz(userId: number, courseId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check-passed/user/${userId}/course/${courseId}`);
  }

  /**
   * Vérifier si un cours a un quiz approuvé
   */
  courseHasApprovedQuiz(courseId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/course/${courseId}/has-quiz`);
  }

  /**
   * Obtenir le quiz approuvé d'un cours
   */
  getApprovedQuizForCourse(courseId: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/course/${courseId}/approved-quiz`);
  }

  /**
   * Obtenir les résultats d'une tentative de quiz
   */
  getAttemptResults(attemptId: number): Observable<QuizAttempt> {
    return this.http.get<QuizAttempt>(`${this.apiUrl}/attempts/${attemptId}/results`);
  }

  /**
   * Obtenir la dernière tentative réussie d'un utilisateur pour un cours
   */
  getLastPassedAttempt(userId: number, courseId: number): Observable<QuizAttempt | null> {
    return this.http.get<QuizAttempt | null>(`${this.apiUrl}/attempts/user/${userId}/course/${courseId}/last-passed`);
  }
}

