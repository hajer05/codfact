import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Avoid loops: do not refresh if this is the refresh call or already retried
          const isRefreshCall = req.url.includes('/api/auth/refresh');
          const alreadyRetried = req.headers.has('X-Retry');
          const refreshToken = this.authService.getToken() && localStorage.getItem('refresh_token');

          if (!isRefreshCall && !alreadyRetried && refreshToken) {
            return this.authService.refreshToken().pipe(
              switchMap(() => {
                const newToken = this.authService.getToken();
                const headers = req.headers
                  .set('X-Retry', 'true')
                  .set('Authorization', `Bearer ${newToken}`);
                const newReq = req.clone({ headers });
                return next.handle(newReq);
              }),
              // If refresh fails, propagate the error without logging the user out automatically
              catchError(() => throwError(() => error))
            );
          }
        }
        return throwError(() => error);
      })
    );
  }
}
