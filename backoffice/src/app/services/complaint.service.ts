import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ComplaintService {
  private readonly API_URL = 'http://localhost:8090/api/complaints';

  constructor(private http: HttpClient) {}

  getTeacherComplaints(teacherId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/teacher/${teacherId}`);
  }

  getAllComplaints(): Observable<any[]> {
    // For admin - get all complaints (we'll need to add this endpoint or use a workaround)
    return this.http.get<any[]>(this.API_URL + '/all');
  }

  respondToComplaint(complaintId: number, response: string): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${complaintId}/respond`, { response });
  }

  updateComplaintStatus(complaintId: number, status: string): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/${complaintId}/status`, { status });
  }

  getCourseComplaints(courseId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/course/${courseId}`);
  }

  deleteComplaint(complaintId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${complaintId}`);
  }
}

