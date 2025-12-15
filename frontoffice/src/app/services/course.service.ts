import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { Course, CourseFilter } from '../models/course.model';

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private apiUrl = 'http://localhost:8090/api/courses';

  constructor(private http: HttpClient) {}

  getAllCourses(filter?: CourseFilter): Observable<Course[]> {
    let params = new HttpParams();
    
    if (filter) {
      if (filter.search) params = params.set('search', filter.search);
      if (filter.category) params = params.set('category', filter.category);
      if (filter.level) params = params.set('level', filter.level);
      if (filter.isFree !== undefined) params = params.set('isFree', filter.isFree.toString());
      if (filter.rating) params = params.set('minRating', filter.rating.toString());
      if (filter.priceRange) {
        params = params.set('minPrice', filter.priceRange.min.toString());
        params = params.set('maxPrice', filter.priceRange.max.toString());
      }
      if (filter.tags && filter.tags.length > 0) {
        params = params.set('tags', filter.tags.join(','));
      }
    }

    return this.http.get<Course[]>(this.apiUrl, { params }).pipe(
      catchError(error => {
        console.error('Error fetching courses:', error);
        // Return empty array instead of throwing to prevent logout
        return of([]);
      })
    );
  }

  getCourseById(id: number): Observable<Course> {
    return this.http.get<Course>(`${this.apiUrl}/${id}`).pipe(
      catchError(error => {
        console.error('Error fetching course by ID:', error);
        // Return null or throw error based on your preference
        throw error;
      })
    );
  }

  getCourseDetails(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}/details`).pipe(
      catchError(error => {
        console.error('Error fetching course details:', error);
        // Return null or throw error based on your preference
        throw error;
      })
    );
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/categories`).pipe(
      catchError(error => {
        console.error('Error fetching categories:', error);
        // Return empty array instead of throwing to prevent logout
        return of([]);
      })
    );
  }

  getTags(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/tags`).pipe(
      catchError(error => {
        console.error('Error fetching tags:', error);
        // Return empty array instead of throwing to prevent logout
        return of([]);
      })
    );
  }
}
