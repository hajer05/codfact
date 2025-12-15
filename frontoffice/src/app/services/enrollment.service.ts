import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

export interface Enrollment {
  id: number;
  enrolledAt: string;
  completedAt?: string;
  progress: number;
  status: 'ACTIVE' | 'COMPLETED' | 'DROPPED';
  student: any;
  course: any;
}

export interface EnrollmentResponse {
  id?: number;
  courseId?: number;
  courseTitle?: string;
  studentId?: number;
  studentName?: string;
  enrolledAt?: string;
  progress?: number;
  status?: string;
  success: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class EnrollmentService {
  private apiUrl = 'http://localhost:8090/api/enrollments';
  private enrolledCoursesSubject = new BehaviorSubject<Set<number>>(new Set());
  public enrolledCourses$ = this.enrolledCoursesSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUserEnrollments();
  }

  private getAuthHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  enrollInCourse(courseId: number): Observable<EnrollmentResponse> {
    return this.http.post<EnrollmentResponse>(`${this.apiUrl}/enroll/${courseId}`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap((response) => {
        // Update local enrollment state only if successful
        if (response.success && response.courseId) {
          const currentEnrollments = this.enrolledCoursesSubject.value;
          currentEnrollments.add(response.courseId);
          this.enrolledCoursesSubject.next(new Set(currentEnrollments));
        }
      }),
      catchError(error => {
        console.error('Enrollment error:', error);
        // Return a failed response instead of throwing to prevent logout
        return of({ 
          success: false, 
          message: error.error?.message || 'Enrollment failed. Please try again.' 
        });
      })
    );
  }

  unenrollFromCourse(courseId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/unenroll/${courseId}?userId=1`, {
      headers: this.getAuthHeaders()
    }).pipe(
      tap(() => {
        // Update local enrollment state
        const currentEnrollments = this.enrolledCoursesSubject.value;
        currentEnrollments.delete(courseId);
        this.enrolledCoursesSubject.next(new Set(currentEnrollments));
      })
    );
  }

  getMyEnrollments(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/my-enrollments`, {
      headers: this.getAuthHeaders()
    });
  }

  checkEnrollment(courseId: number): Observable<{enrolled: boolean}> {
    return this.http.get<{enrolled: boolean}>(`${this.apiUrl}/check/${courseId}`, {
      headers: this.getAuthHeaders()
    });
  }

  updateProgress(enrollmentId: number, progress: number): Observable<Enrollment> {
    return this.http.put<Enrollment>(`${this.apiUrl}/${enrollmentId}/progress`, {
      progress: progress
    }, {
      headers: this.getAuthHeaders()
    });
  }

  getCourseEnrollments(courseId: number): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/course/${courseId}`, {
      headers: this.getAuthHeaders()
    });
  }

  getCourseEnrollmentCount(courseId: number): Observable<{count: number}> {
    return this.http.get<{count: number}>(`${this.apiUrl}/course/${courseId}/count`);
  }

  isEnrolled(courseId: number): boolean {
    return this.enrolledCoursesSubject.value.has(courseId);
  }

  private loadUserEnrollments(): void {
    this.getMyEnrollments().subscribe({
      next: (enrollments) => {
        const courseIds = new Set(
          enrollments
            .map(e => (e as any).courseId || e.course?.id)
            .filter(id => id !== undefined)
        );
        console.log('📚 Loaded enrollments for courses:', Array.from(courseIds));
        this.enrolledCoursesSubject.next(courseIds);
      },
      error: (error) => {
        console.error('Error loading user enrollments:', error);
        // Don't fail silently - keep empty set
        this.enrolledCoursesSubject.next(new Set());
      }
    });
  }

  refreshEnrollments(): void {
    this.loadUserEnrollments();
  }
}
