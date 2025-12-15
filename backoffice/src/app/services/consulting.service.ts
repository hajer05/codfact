import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

export interface ConsultingRequest {
  id: number;
  companyName: string;
  contactName: string;
  email: string;
  phone?: string;
  serviceType: string;
  projectDescription: string;
  needs: string;
  budget?: string;
  timeline?: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'ANSWERED' | 'CLOSED' | 'REJECTED';
  adminNotes?: string;
  requestedById?: number;
  requestedByName?: string;
  assignedToId?: number;
  assignedToName?: string;
  createdAt: string;
  updatedAt?: string;
  answeredAt?: string;
}

export interface UpdateConsultingRequest {
  status?: 'PENDING' | 'IN_PROGRESS' | 'ANSWERED' | 'CLOSED' | 'REJECTED';
  adminNotes?: string;
  assignedToId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ConsultingService {
  private apiUrl = 'http://localhost:8090/api/consulting';

  constructor(private http: HttpClient) {}

  getAllRequests(): Observable<ConsultingRequest[]> {
    return this.http.get<ConsultingRequest[]>(`${this.apiUrl}/admin/all`)
      .pipe(catchError(this.handleError));
  }

  getRequestsByStatus(status: string): Observable<ConsultingRequest[]> {
    return this.http.get<ConsultingRequest[]>(`${this.apiUrl}/admin/status/${status}`)
      .pipe(catchError(this.handleError));
  }

  getRequestById(id: number): Observable<ConsultingRequest> {
    return this.http.get<ConsultingRequest>(`${this.apiUrl}/request/${id}`)
      .pipe(catchError(this.handleError));
  }

  updateRequest(id: number, request: UpdateConsultingRequest): Observable<ConsultingRequest> {
    return this.http.put<ConsultingRequest>(`${this.apiUrl}/admin/request/${id}`, request)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: any): Observable<never> {
    console.error('ConsultingService error:', error);
    return throwError(() => error);
  }
}

