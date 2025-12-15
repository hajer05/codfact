import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Notification {
  id: number;
  title: string;
  message: string;
  type: 'BLOG_COMMENT' | 'COURSE_ENROLLMENT' | 'PFE_APPLICATION' | string;
  isRead: boolean;
  createdAt: string;
  senderName?: string;
  referenceId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = 'http://localhost:8090/api/notifications';
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  private unreadCountSubject = new BehaviorSubject<number>(0);
  
  public notifications$ = this.notificationsSubject.asObservable();
  public unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  loadNotifications(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    this.http.get<{ content: Notification[] }>(`${this.apiUrl}?page=0&size=50`, { headers: this.getHeaders() })
      .subscribe({
        next: (response) => {
          const notifications = response.content || [];
          this.notificationsSubject.next(notifications);
          this.updateUnreadCount(notifications);
        },
        error: (error) => {
          console.error('Error loading notifications:', error);
          // Fallback to unread notifications if main endpoint fails
          this.loadUnreadNotifications();
        }
      });
  }

  loadUnreadNotifications(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    this.http.get<Notification[]>(`${this.apiUrl}/unread`, { headers: this.getHeaders() })
      .subscribe({
        next: (notifications) => {
          this.notificationsSubject.next(notifications);
          this.updateUnreadCount(notifications);
        },
        error: (error) => {
          console.error('Error loading unread notifications:', error);
        }
      });
  }

  loadUnreadCount(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    this.http.get<{ count: number }>(`${this.apiUrl}/unread/count`, { headers: this.getHeaders() })
      .subscribe({
        next: (response) => {
          this.unreadCountSubject.next(response.count || 0);
        },
        error: (error) => {
          console.error('Error loading unread count:', error);
        }
      });
  }

  private updateUnreadCount(notifications: Notification[]): void {
    const unreadCount = notifications.filter(n => !n.isRead).length;
    this.unreadCountSubject.next(unreadCount);
  }

  markAsReadAndUpdate(notificationId: number): void {
    this.http.put(`${this.apiUrl}/${notificationId}/read`, {}, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          // Update local state
          const notifications = this.notificationsSubject.value;
          const updated = notifications.map(n => 
            n.id === notificationId ? { ...n, isRead: true } : n
          );
          this.notificationsSubject.next(updated);
          this.updateUnreadCount(updated);
        },
        error: (error) => {
          console.error('Error marking notification as read:', error);
        }
      });
  }

  markAllAsReadAndUpdate(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    this.http.put(`${this.apiUrl}/read-all`, {}, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          // Update local state
          const notifications = this.notificationsSubject.value;
          const updated = notifications.map(n => ({ ...n, isRead: true }));
          this.notificationsSubject.next(updated);
          this.unreadCountSubject.next(0);
        },
        error: (error) => {
          console.error('Error marking all notifications as read:', error);
        }
      });
  }

  getNotificationIcon(type: string): string {
    switch (type) {
      case 'BLOG_COMMENT':
        return 'fas fa-comment';
      case 'COURSE_ENROLLMENT':
        return 'fas fa-book';
      case 'PFE_APPLICATION':
        return 'fas fa-file-alt';
      default:
        return 'fas fa-bell';
    }
  }

  getNotificationColor(type: string): string {
    switch (type) {
      case 'BLOG_COMMENT':
        return '#3b82f6'; // blue
      case 'COURSE_ENROLLMENT':
        return '#10b981'; // green
      case 'PFE_APPLICATION':
        return '#f59e0b'; // amber
      default:
        return '#6b7280'; // gray
    }
  }

  getTimeAgo(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 60) {
      return 'Just now';
    } else if (diffInSeconds < 3600) {
      const minutes = Math.floor(diffInSeconds / 60);
      return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 86400) {
      const hours = Math.floor(diffInSeconds / 3600);
      return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 604800) {
      const days = Math.floor(diffInSeconds / 86400);
      return `${days} day${days > 1 ? 's' : ''} ago`;
    } else {
      return date.toLocaleDateString();
    }
  }

  sendTestNotification(userId: number, message: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/test`, { recipientId: userId, message }, { headers: this.getHeaders() });
  }
}
