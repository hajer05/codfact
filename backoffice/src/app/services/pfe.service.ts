import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PFESubject {
  id?: number;
  title: string;
  description: string;
  requirements: string;
  createdById: number;
  createdByName: string;
  createdByRole: string;
  status: 'OPEN' | 'ASSIGNED' | 'COMPLETED';
  createdAt: string;
  updatedAt: string;
  applicationCount: number;
  pendingApplications: number;
  acceptedApplications: number;
  hasEvaluation: boolean;
  evaluationGrade?: number;
}

export interface Application {
  id: number;
  studentId: number;
  studentName: string;
  studentEmail: string;
  subjectId: number;
  subjectTitle: string;
  subjectCreatedBy: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  motivation: string;
  cvFileName?: string;
  cvFileUrl?: string;
  cvOriginalFileName?: string;
  appliedAt: string;
  reviewedAt?: string;
  reviewedById?: number;
  reviewedByName?: string;
  reviewComment?: string;
  workflowStep?: 'APPLICATION_REVIEW' | 'PROJECT_UPLOAD' | 'PROJECT_REVIEW' | 'REPORT_UPLOAD' | 'REPORT_REVIEW' | 'JURY_SCHEDULING' | 'COMPLETED';
  projectReviewComment?: string;
  projectReviewedAt?: string;
  reportReviewComment?: string;
  reportReviewedAt?: string;
  juryDate?: string;
  juryLocation?: string;
}

export interface CreatePFESubjectRequest {
  title: string;
  description: string;
  requirements: string;
}

export interface CreateApplicationRequest {
  motivation: string;
}

export interface ReviewApplicationRequest {
  status: 'ACCEPTED' | 'REJECTED';
  reviewComment: string;
}

export interface CreateEvaluationRequest {
  grade: number;
  comment: string;
}

export interface ReviewProjectRequest {
  approved: boolean;
  comment: string;
}

export interface ReviewReportRequest {
  approved: boolean;
  comment: string;
}

export interface ScheduleJuryRequest {
  juryDate: string;
  juryLocation: string;
  comment?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PFEService {
  private apiUrl = 'http://localhost:8090/api/pfe';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  // PFE Subject operations
  createSubject(subject: CreatePFESubjectRequest): Observable<PFESubject> {
    return this.http.post<PFESubject>(`${this.apiUrl}/subjects`, subject, { headers: this.getHeaders() });
  }

  getAllSubjects(): Observable<PFESubject[]> {
    return this.http.get<PFESubject[]>(`${this.apiUrl}/subjects`, { headers: this.getHeaders() });
  }

  getOpenSubjects(): Observable<PFESubject[]> {
    return this.http.get<PFESubject[]>(`${this.apiUrl}/subjects/open`, { headers: this.getHeaders() });
  }

  getMySubjects(): Observable<PFESubject[]> {
    return this.http.get<PFESubject[]>(`${this.apiUrl}/subjects/my`, { headers: this.getHeaders() });
  }

  getSubjectById(id: number): Observable<PFESubject> {
    return this.http.get<PFESubject>(`${this.apiUrl}/subjects/${id}`, { headers: this.getHeaders() });
  }

  updateSubject(id: number, subject: CreatePFESubjectRequest): Observable<PFESubject> {
    return this.http.put<PFESubject>(`${this.apiUrl}/subjects/${id}`, subject, { headers: this.getHeaders() });
  }

  deleteSubject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/subjects/${id}`, { headers: this.getHeaders() });
  }

  searchSubjects(query: string): Observable<PFESubject[]> {
    return this.http.get<PFESubject[]>(`${this.apiUrl}/subjects/search?q=${encodeURIComponent(query)}`, { headers: this.getHeaders() });
  }

  // Application operations
  applyToSubject(subjectId: number, application: CreateApplicationRequest): Observable<Application> {
    return this.http.post<Application>(`${this.apiUrl}/subjects/${subjectId}/apply`, application, { headers: this.getHeaders() });
  }

  getMyApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/applications/my`, { headers: this.getHeaders() });
  }

  getSubjectApplications(subjectId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/subjects/${subjectId}/applications`, { headers: this.getHeaders() });
  }

  getConsultantApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/applications/consultant`, { headers: this.getHeaders() });
  }

  reviewApplication(applicationId: number, review: ReviewApplicationRequest): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/applications/${applicationId}/review`, review, { headers: this.getHeaders() });
  }

  // Evaluation operations
  evaluateProject(subjectId: number, evaluation: CreateEvaluationRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/subjects/${subjectId}/evaluate`, evaluation, { headers: this.getHeaders() });
  }

  // Workflow operations
  reviewProject(applicationId: number, review: ReviewProjectRequest): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/applications/${applicationId}/review-project`, review, { headers: this.getHeaders() });
  }

  reviewReport(applicationId: number, review: ReviewReportRequest): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/applications/${applicationId}/review-report`, review, { headers: this.getHeaders() });
  }

  scheduleJury(applicationId: number, schedule: ScheduleJuryRequest): Observable<Application> {
    return this.http.put<Application>(`${this.apiUrl}/applications/${applicationId}/schedule-jury`, schedule, { headers: this.getHeaders() });
  }

  // CV download
  downloadCV(applicationId: number): Observable<Blob> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': token ? `Bearer ${token}` : ''
    });
    return this.http.get(`${this.apiUrl}/applications/${applicationId}/cv/download`, { 
      headers, 
      responseType: 'blob' 
    });
  }
}
