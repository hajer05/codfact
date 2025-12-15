import { Injectable, Injector } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { User, LoginRequest, LoginResponse } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8090/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
    private injector: Injector
  ) {
    console.log('AuthService constructor called');
    // Check if user is already logged in
    this.loadUserFromStorage();
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          console.log('Login response received:', response);
          // Map backend response to frontend format
          const user: User = {
            id: response.userId,
            email: response.email,
            firstName: response.firstName,
            lastName: response.lastName,
            roles: Array.from(response.roles || []),
            createdAt: new Date().toISOString()
          };
          const token = response.accessToken;
          this.setCurrentUser(user, token);
        })
      );
  }

  logout(): void {
    console.log('logout() called - clearing auth data');
    console.trace('Logout call stack');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
    
    // Clear progress cache to prevent data leakage between users
    // Use lazy injection to avoid circular dependency
    try {
      const lessonProgressService = this.injector.get('LessonProgressService' as any);
      if (lessonProgressService && typeof lessonProgressService.clearCache === 'function') {
        lessonProgressService.clearCache();
      }
    } catch (error) {
      console.warn('Could not clear progress cache:', error);
    }
    
    this.router.navigate(['/']);
  }

  // Method for explicit logout (called by user action)
  explicitLogout(): void {
    this.logout();
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    const user = this.getCurrentUser();
    console.log('isAuthenticated check:', { token: !!token, user: !!user });
    return !!token && !!user;
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.roles.includes(role) || false;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.getCurrentUser();
    if (!user) return false;
    return roles.some(role => user.roles.includes(role));
  }

  private setCurrentUser(user: User, token: string): void {
    console.log('setCurrentUser called with:', { user, token });
    if (token && user) {
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      this.currentUserSubject.next(user);
      console.log('Auth data saved to localStorage');
    } else {
      console.error('Invalid token or user data:', { token, user });
    }
  }

  private loadUserFromStorage(): void {
    console.log('loadUserFromStorage called');
    const token = this.getToken();
    const userStr = localStorage.getItem('user');

    console.log('Storage check:', {
      hasToken: !!token,
      hasUser: !!userStr,
      tokenValue: token,
      userValue: userStr
    });

    // Check for corrupted data (string "undefined")
    if (token === 'undefined' || userStr === 'undefined') {
      console.log('Found corrupted localStorage data, clearing...');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      this.currentUserSubject.next(null);
      return;
    }

    if (token && userStr && token !== 'null' && userStr !== 'null') {
      try {
        const user = JSON.parse(userStr);
        console.log('Setting user from storage:', user);
        this.currentUserSubject.next(user);
      } catch (error) {
        console.error('Error parsing user from storage:', error);
        // Clear corrupted data but don't redirect
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        this.currentUserSubject.next(null);
      }
    } else {
      console.log('No valid auth data found, setting user to null');
      this.currentUserSubject.next(null);
    }
  }
}
