import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface Certificate {
  id: number;
  userId: number;
  courseId: number;
  certificateFileName: string;
  certificateUrl: string;
  issuedAt: string;
  studentName: string;
  courseName: string;
  instructorName: string;
}

@Injectable({
  providedIn: 'root'
})
export class CertificateService {
  private apiUrl = 'http://localhost:8090/api/certificates';

  constructor(private http: HttpClient) {}

  generateCertificate(userId: number, courseId: number): Observable<Certificate> {
    return this.http.post<Certificate>(`${this.apiUrl}/generate`, null, {
      params: { userId: userId.toString(), courseId: courseId.toString() }
    }).pipe(
      catchError(error => {
        console.error('Error generating certificate:', error);
        throw error;
      })
    );
  }

  getUserCertificates(userId: number): Observable<Certificate[]> {
    return this.http.get<Certificate[]>(`${this.apiUrl}/user/${userId}`).pipe(
      catchError(error => {
        console.error('Error loading user certificates:', error);
        return of([]);
      })
    );
  }

  hasCertificate(userId: number, courseId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check`, {
      params: { userId: userId.toString(), courseId: courseId.toString() }
    }).pipe(
      catchError(error => {
        console.error('Error checking certificate:', error);
        return of(false);
      })
    );
  }

  getCertificate(userId: number, courseId: number): Observable<Certificate | null> {
    return this.http.get<Certificate>(`${this.apiUrl}/get`, {
      params: { userId: userId.toString(), courseId: courseId.toString() }
    }).pipe(
      catchError(error => {
        console.error('Error getting certificate:', error);
        return of(null);
      })
    );
  }

  downloadCertificate(fileName: string): string {
    return `${this.apiUrl}/download/${fileName}`;
  }
}
