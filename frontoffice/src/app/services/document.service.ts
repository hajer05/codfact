import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DocumentDto {
  id: number;
  fileName: string;
  originalFileName: string;
  documentType: string;
  description?: string;
  fileSize: number;
  uploadedByName: string;
  uploadedByEmail: string;
  uploadedAt: string;
  subjectId: number;
  subjectTitle: string;
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
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  uploadDocument(subjectId: number, file: File, documentType: string, description?: string): Observable<DocumentDto> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post<DocumentDto>(`${this.apiUrl}/subjects/${subjectId}/documents`, formData, {
      headers: new HttpHeaders({
        'Authorization': localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : ''
      })
    });
  }

  getSubjectDocuments(subjectId: number): Observable<DocumentDto[]> {
    return this.http.get<DocumentDto[]>(`${this.apiUrl}/subjects/${subjectId}/documents`, { headers: this.getHeaders() });
  }

  deleteDocument(documentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/documents/${documentId}`, { headers: this.getHeaders() });
  }

  downloadDocument(documentId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/documents/${documentId}/download`, {
      headers: this.getHeaders(),
      responseType: 'blob'
    });
  }
}
