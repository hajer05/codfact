import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';

export interface LessonProgress {
  id: number;
  completed: boolean;
  completedAt?: string;
  watchedDuration?: number;
  lastAccessedAt: string;
  enrollment: any;
  lesson: any;
}

@Injectable({
  providedIn: 'root'
})
export class LessonProgressService {
  private apiUrl = 'http://localhost:8090/api/lesson-progress';
  private progressSubject = new BehaviorSubject<Map<number, LessonProgress>>(new Map());
  public progress$ = this.progressSubject.asObservable();
  private readonly STORAGE_KEY_PREFIX = 'lesson_progress_cache_user_';

  constructor(private http: HttpClient, private authService: AuthService) {
    // Load cached progress from localStorage on service initialization
    this.loadProgressFromCache();
  }

  private getStorageKey(): string {
    const currentUser = this.authService.getCurrentUser();
    return currentUser ? `${this.STORAGE_KEY_PREFIX}${currentUser.id}` : this.STORAGE_KEY_PREFIX + 'guest';
  }

  private loadProgressFromCache(): void {
    try {
      const storageKey = this.getStorageKey();
      const cached = localStorage.getItem(storageKey);
      if (cached) {
        const progressArray = JSON.parse(cached) as Array<[number, LessonProgress]>;
        const progressMap = new Map(progressArray);
        this.progressSubject.next(progressMap);
        console.log('📦 Loaded', progressMap.size, 'cached progress records from localStorage for user');
      }
    } catch (error) {
      console.error('Error loading progress from cache:', error);
    }
  }

  private saveProgressToCache(progressMap: Map<number, LessonProgress>): void {
    try {
      const storageKey = this.getStorageKey();
      const progressArray = Array.from(progressMap.entries());
      localStorage.setItem(storageKey, JSON.stringify(progressArray));
      console.log('💾 Saved', progressMap.size, 'progress records to localStorage for user');
    } catch (error) {
      console.error('Error saving progress to cache:', error);
    }
  }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  markLessonAsCompleted(lessonId: number): Observable<LessonProgress> {
    const currentUser = this.authService.getCurrentUser();
    const userId = currentUser?.id || 1;
    return this.http.post<LessonProgress>(`${this.apiUrl}/mark-completed/${lessonId}`, {}, {
      headers: this.getHeaders()
    }).pipe(
      tap((progress) => {
        console.log('Backend response - lesson marked as completed:', progress);
        // Update local progress state immediately
        const currentProgress = new Map(this.progressSubject.value);
        
        // Ensure progress object has completed=true
        const updatedProgress: LessonProgress = {
          ...progress,
          completed: true,
          completedAt: progress.completedAt || new Date().toISOString()
        };
        
        currentProgress.set(lessonId, updatedProgress);
        this.progressSubject.next(currentProgress);
        
        // Save to localStorage
        this.saveProgressToCache(currentProgress);
        
        // Also trigger a reload to get all progress updates
        console.log('Updated progress map for lesson:', lessonId, updatedProgress);
      }),
      catchError(error => {
        console.error('Error marking lesson as completed:', error);
        // Return a mock progress object to prevent logout
        return of({
          id: 0,
          completed: false,
          lastAccessedAt: new Date().toISOString(),
          enrollment: null,
          lesson: { id: lessonId }
        } as LessonProgress);
      })
    );
  }

  updateLessonProgress(lessonId: number, watchedDuration: number): Observable<LessonProgress> {
    const currentUser = this.authService.getCurrentUser();
    const userId = currentUser?.id || 1;
    return this.http.put<LessonProgress>(`${this.apiUrl}/update/${lessonId}?userId=${userId}`, {
      watchedDuration: watchedDuration
    }, {
      headers: this.getHeaders()
    }).pipe(
      tap((progress) => {
        // Update local progress state
        const currentProgress = new Map(this.progressSubject.value);
        currentProgress.set(lessonId, progress);
        this.progressSubject.next(currentProgress);
        
        // Save to localStorage
        this.saveProgressToCache(currentProgress);
      }),
      catchError(error => {
        console.error('Error updating lesson progress:', error);
        // Return a mock progress object to prevent logout
        return of({
          id: 0,
          completed: false,
          watchedDuration: watchedDuration,
          lastAccessedAt: new Date().toISOString(),
          enrollment: null,
          lesson: { id: lessonId }
        } as LessonProgress);
      })
    );
  }

  getUserLessonProgress(courseId: number): Observable<LessonProgress[]> {
    const currentUser = this.authService.getCurrentUser();
    const userId = currentUser?.id || 1;
    console.log('📡 Fetching lesson progress from backend for course:', courseId, 'userId:', userId);
    
    return this.http.get<LessonProgress[]>(`${this.apiUrl}/course/${courseId}?userId=${userId}`, {
      headers: this.getHeaders()
    }).pipe(
      tap((progressList) => {
        console.log('✅ Received', progressList.length, 'lesson progress records from backend');
        
        // Update local progress state
        const currentProgress = new Map(this.progressSubject.value); // Create new map
        progressList.forEach(progress => {
          currentProgress.set(progress.lesson.id, progress);
          console.log(`  📝 Lesson ${progress.lesson.id}: completed=${progress.completed}`);
        });
        this.progressSubject.next(currentProgress);
        
        // Save to localStorage
        this.saveProgressToCache(currentProgress);
        
        console.log('📊 Progress map updated, total size:', currentProgress.size);
      }),
      catchError(error => {
        console.error('❌ Error fetching user lesson progress:', error);
        // Return empty array to prevent logout
        return of([]);
      })
    );
  }

  getLessonProgress(lessonId: number): Observable<LessonProgress | any> {
    const currentUser = this.authService.getCurrentUser();
    const userId = currentUser?.id || 1;
    return this.http.get<LessonProgress | any>(`${this.apiUrl}/lesson/${lessonId}?userId=${userId}`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(error => {
        console.error('Error fetching lesson progress:', error);
        // Return null to prevent logout
        return of(null);
      })
    );
  }

  getCourseProgressPercentage(courseId: number): Observable<{percentage: number}> {
    const currentUser = this.authService.getCurrentUser();
    const userId = currentUser?.id || 1;
    return this.http.get<{percentage: number}>(`${this.apiUrl}/course/${courseId}/percentage?userId=${userId}`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(error => {
        console.error('Error fetching course progress percentage:', error);
        // Return 0% progress to prevent logout
        return of({ percentage: 0 });
      })
    );
  }

  isLessonCompleted(lessonId: number): boolean {
    const progress = this.progressSubject.value.get(lessonId);
    const isCompleted = progress ? (progress.completed === true) : false;
    return isCompleted;
  }

  getLessonWatchedDuration(lessonId: number): number {
    const progress = this.progressSubject.value.get(lessonId);
    return progress ? (progress.watchedDuration || 0) : 0;
  }

  loadCourseProgress(courseId: number): void {
    console.log('🔄 Loading course progress for course:', courseId);
    this.getUserLessonProgress(courseId).subscribe({
      next: (progress) => {
        console.log('✅ Course progress loaded successfully');
      },
      error: (error) => {
        console.error('❌ Error loading course progress:', error);
      }
    });
  }

  clearCache(): void {
    console.log('🗑️ Clearing progress cache');
    try {
      const storageKey = this.getStorageKey();
      localStorage.removeItem(storageKey);
      this.progressSubject.next(new Map());
      console.log('✅ Progress cache cleared');
    } catch (error) {
      console.error('Error clearing progress cache:', error);
    }
  }
}
