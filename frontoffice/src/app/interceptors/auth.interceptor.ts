import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    
    return next(authReq).pipe(
      catchError(error => {
        // Don't automatically logout on API errors
        // Let individual services handle their own error responses
        console.error('HTTP Error:', error);
        throw error; // Re-throw to let the calling service handle it
      })
    );
  }

  return next(req).pipe(
    catchError(error => {
      // Don't automatically logout on API errors for non-authenticated requests either
      console.error('HTTP Error:', error);
      throw error; // Re-throw to let the calling service handle it
    })
  );
};
