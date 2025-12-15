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
  workflowStep: 'APPLICATION_REVIEW' | 'PROJECT_UPLOAD' | 'PROJECT_REVIEW' | 'REPORT_UPLOAD' | 'REPORT_REVIEW' | 'JURY_SCHEDULING' | 'COMPLETED';
  projectReviewComment?: string;
  projectReviewedAt?: string;
  reportReviewComment?: string;
  reportReviewedAt?: string;
  juryDate?: string;
  juryLocation?: string;
}

export interface CreateApplicationRequest {
  motivation: string;
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

  // Student operations
  getOpenSubjects(): Observable<PFESubject[]> {
    return this.http.get<PFESubject[]>(`${this.apiUrl}/subjects/open`, { headers: this.getHeaders() });
  }

  getSubjectById(id: number): Observable<PFESubject> {
    return this.http.get<PFESubject>(`${this.apiUrl}/subjects/${id}`, { headers: this.getHeaders() });
  }

  searchSubjects(query: string): Observable<PFESubject[]> {
    return this.http.get<PFESubject[]>(`${this.apiUrl}/subjects/search?q=${encodeURIComponent(query)}`, { headers: this.getHeaders() });
  }

  applyToSubject(subjectId: number, motivation: string, cvFile?: File): Observable<Application> {
    const formData = new FormData();
    formData.append('motivation', motivation);
    if (cvFile) {
      formData.append('cvFile', cvFile);
    }
    
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': token ? `Bearer ${token}` : ''
    });
    
    return this.http.post<Application>(`${this.apiUrl}/subjects/${subjectId}/apply`, formData, { headers });
  }

  getMyApplications(): Observable<Application[]> {
    console.log('Making API call to get my applications');
    console.log('API URL:', `${this.apiUrl}/applications/my`);
    const token = localStorage.getItem('token');
    console.log('Token exists:', !!token);
    console.log('Token preview:', token ? token.substring(0, 50) + '...' : 'No token');
    console.log('Headers:', this.getHeaders());
    return this.http.get<Application[]>(`${this.apiUrl}/applications/my`, { headers: this.getHeaders() });
  }

  getApplicationById(applicationId: number): Observable<Application> {
    return this.http.get<Application>(`${this.apiUrl}/applications/${applicationId}`, { headers: this.getHeaders() });
  }

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

  // Consultant/Admin operations
  getMySubjects(): Observable<PFESubject[]> {
    return this.http.get<PFESubject[]>(`${this.apiUrl}/subjects/my`, { headers: this.getHeaders() });
  }

  getConsultantApplications(): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/applications/consultant`, { headers: this.getHeaders() });
  }

  getSubjectApplications(subjectId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.apiUrl}/subjects/${subjectId}/applications`, { headers: this.getHeaders() });
  }

  // Review operations
  reviewApplication(applicationId: number, approved: boolean, comment: string): Observable<Application> {
    return this.http.put<Application>(
      `${this.apiUrl}/applications/${applicationId}/review`,
      { approved, comment },
      { headers: this.getHeaders() }
    );
  }

  reviewProject(applicationId: number, approved: boolean, comment: string): Observable<Application> {
    return this.http.put<Application>(
      `${this.apiUrl}/applications/${applicationId}/review-project`,
      { approved, comment },
      { headers: this.getHeaders() }
    );
  }

  reviewReport(applicationId: number, approved: boolean, comment: string): Observable<Application> {
    return this.http.put<Application>(
      `${this.apiUrl}/applications/${applicationId}/review-report`,
      { approved, comment },
      { headers: this.getHeaders() }
    );
  }

  scheduleJury(applicationId: number, juryDate: string, juryLocation: string): Observable<Application> {
    return this.http.put<Application>(
      `${this.apiUrl}/applications/${applicationId}/schedule-jury`,
      { juryDate, juryLocation },
      { headers: this.getHeaders() }
    );
  }

  // Check if student has an accepted application
  hasAcceptedApplication(applications: Application[]): boolean {
    return applications.some(app => app.status === 'ACCEPTED');
  }

  // Get the accepted application if exists
  getAcceptedApplication(applications: Application[]): Application | undefined {
    return applications.find(app => app.status === 'ACCEPTED');
  }
}
