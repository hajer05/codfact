import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ComplaintService {
  private readonly API_URL = 'http://localhost:8090/api/complaints';

  constructor(private http: HttpClient) {}

  createComplaint(studentId: number, courseId: number, subject: string, message: string): Observable<any> {
    return this.http.post<any>(this.API_URL, {
      studentId,
      courseId,
      subject,
      message
    });
  }

  getStudentComplaints(studentId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/student/${studentId}`);
  }

  getCourseComplaints(courseId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_URL}/course/${courseId}`);
  }
}

