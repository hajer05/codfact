import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LessonRequest {
  title: string;
  description?: string;
  moduleId: number;
  orderIndex?: number;
  type?: 'VIDEO' | 'TEXT' | 'QUIZ' | 'ASSIGNMENT';
  videoUrl?: string;
  videoFileName?: string;
  videoDuration?: number;
  content?: string;
  attachmentUrl?: string;
  attachmentFileName?: string;
  isFree?: boolean;
}

export interface Lesson {
  id: number;
  title: string;
  description?: string;
  orderIndex: number;
  type: 'VIDEO' | 'TEXT' | 'QUIZ' | 'ASSIGNMENT';
  videoUrl?: string;
  videoFileName?: string;
  videoDuration?: number;
  content?: string;
  attachmentUrl?: string;
  attachmentFileName?: string;
  isFree: boolean;
  createdAt: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class LessonService {
  private apiUrl = 'http://localhost:8090/api/lessons';

  constructor(private http: HttpClient) {}

  createLesson(lessonData: LessonRequest): Observable<Lesson> {
    return this.http.post<Lesson>(this.apiUrl, lessonData);
  }

  createLessonWithVideo(lessonData: LessonRequest, videoFile?: File): Observable<Lesson> {
    const formData = new FormData();
    formData.append('title', lessonData.title);
    formData.append('description', lessonData.description || '');
    formData.append('moduleId', lessonData.moduleId.toString());
    formData.append('videoDuration', (lessonData.videoDuration || 0).toString());
    formData.append('type', lessonData.type || 'VIDEO');
    
    if (videoFile) {
      formData.append('videoFile', videoFile);
    }
    
    return this.http.post<Lesson>(`${this.apiUrl}/with-video`, formData);
  }

  updateLesson(id: number, lessonData: LessonRequest): Observable<Lesson> {
    return this.http.put<Lesson>(`${this.apiUrl}/${id}`, lessonData);
  }

  updateLessonWithVideo(id: number, lessonData: LessonRequest, videoFile?: File): Observable<Lesson> {
    const formData = new FormData();
    formData.append('title', lessonData.title);
    formData.append('description', lessonData.description || '');
    formData.append('moduleId', lessonData.moduleId.toString());
    formData.append('videoDuration', (lessonData.videoDuration || 0).toString());
    formData.append('type', lessonData.type || 'VIDEO');
    
    if (videoFile) {
      formData.append('videoFile', videoFile);
    }
    
    return this.http.put<Lesson>(`${this.apiUrl}/${id}/with-video`, formData);
  }

  deleteLesson(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getLessonsByModule(moduleId: number): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${this.apiUrl}/module/${moduleId}`);
  }

  getLessonsByCourse(courseId: number): Observable<Lesson[]> {
    return this.http.get<Lesson[]>(`${this.apiUrl}/course/${courseId}`);
  }

  getLessonById(id: number): Observable<Lesson> {
    return this.http.get<Lesson>(`${this.apiUrl}/${id}`);
  }
}
