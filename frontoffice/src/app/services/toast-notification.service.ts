import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface ToastNotification {
  id: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastNotificationService {
  private notificationsSubject = new BehaviorSubject<ToastNotification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private notificationIdCounter = 0;

  show(message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info', duration: number = 5000) {
    const notification: ToastNotification = {
      id: `notification-${++this.notificationIdCounter}-${Date.now()}`,
      message,
      type,
      duration
    };

    const current = this.notificationsSubject.value;
    this.notificationsSubject.next([...current, notification]);

    // Auto remove after duration
    if (duration > 0) {
      setTimeout(() => {
        this.remove(notification.id);
      }, duration);
    }

    return notification.id;
  }

  success(message: string, duration: number = 5000) {
    return this.show(message, 'success', duration);
  }

  error(message: string, duration: number = 7000) {
    return this.show(message, 'error', duration);
  }

  warning(message: string, duration: number = 6000) {
    return this.show(message, 'warning', duration);
  }

  info(message: string, duration: number = 5000) {
    return this.show(message, 'info', duration);
  }

  remove(id: string) {
    const current = this.notificationsSubject.value;
    this.notificationsSubject.next(current.filter(n => n.id !== id));
  }

  clear() {
    this.notificationsSubject.next([]);
  }
}

