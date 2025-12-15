import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DocumentDto {
  id: number;
  subjectId: number;
  fileName: string;
  originalFileName: string;
  fileUrl: string;
  fileSize: number;
  documentType: 'PROJECT' | 'REPORT';
  uploadedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = 'http://localhost:8090/api/pfe';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  getSubjectDocuments(subjectId: number): Observable<DocumentDto[]> {
    return this.http.get<DocumentDto[]>(
      `${this.apiUrl}/subjects/${subjectId}/documents`,
      { headers: this.getHeaders() }
    );
  }

  downloadDocument(documentId: number): Observable<Blob> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': token ? `Bearer ${token}` : ''
    });
    return this.http.get(
      `${this.apiUrl}/documents/${documentId}/download`,
      { headers, responseType: 'blob' }
    );
  }
}
