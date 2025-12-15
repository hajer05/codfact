import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RegisterRequest, RegisterResponse } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8090/api/auth';

  constructor(private http: HttpClient) {}

  register(userData: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, userData).pipe(
      catchError(error => {
        console.error('Error during registration:', error);
        // Return error response instead of throwing to prevent logout
        throw error;
      })
    );
  }
}
