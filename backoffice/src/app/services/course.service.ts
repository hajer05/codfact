import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Course, CreateCourseRequest } from '../models/course.model';

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private readonly API_URL = 'http://localhost:8090/api/courses';

  constructor(private http: HttpClient) {}

  getAllCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(this.API_URL);
  }

  getPublishedCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.API_URL}/public`);
  }

  getMyCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.API_URL}/my-courses`);
  }

  getCourseById(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.API_URL}/${id}`);
  }

  getCourseDetails(id: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${id}/details`);
  }

  createCourse(courseData: CreateCourseRequest): Observable<Course> {
    return this.http.post<Course>(this.API_URL, courseData);
  }

  updateCourse(id: number, courseData: CreateCourseRequest): Observable<Course> {
    return this.http.put<Course>(`${this.API_URL}/${id}`, courseData);
  }

  uploadThumbnail(courseId: number, file: File): Observable<Course> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Course>(`${this.API_URL}/${courseId}/thumbnail`, formData);
  }

  uploadPreviewVideo(courseId: number, file: File): Observable<Course> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Course>(`${this.API_URL}/${courseId}/preview-video`, formData);
  }

  publishCourse(id: number): Observable<Course> {
    return this.http.put<Course>(`${this.API_URL}/${id}/publish`, {});
  }

  getCourseDeleteInfo(id: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${id}/delete-info`);
  }

  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  deleteAllCourses(): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/admin/delete-all`);
  }

  getCourseStudents(courseId: number): Observable<any> {
    return this.http.get<any>(`${this.API_URL}/${courseId}/students`);
  }
}
