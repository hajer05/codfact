import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ModuleRequest {
  title: string;
  description?: string;
  courseId: number;
  orderIndex?: number;
}

export interface Module {
  id: number;
  title: string;
  description?: string;
  orderIndex: number;
  createdAt: string;
  updatedAt?: string;
  lessons: Lesson[];
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
export class ModuleService {
  private apiUrl = 'http://localhost:8090/api/modules';

  constructor(private http: HttpClient) {}

  createModule(moduleData: ModuleRequest): Observable<Module> {
    return this.http.post<Module>(this.apiUrl, moduleData);
  }

  updateModule(id: number, moduleData: ModuleRequest): Observable<Module> {
    return this.http.put<Module>(`${this.apiUrl}/${id}`, moduleData);
  }

  deleteModule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getModulesByCourse(courseId: number): Observable<Module[]> {
    return this.http.get<Module[]>(`${this.apiUrl}/course/${courseId}`);
  }

  getModuleById(id: number): Observable<Module> {
    return this.http.get<Module>(`${this.apiUrl}/${id}`);
  }
}
