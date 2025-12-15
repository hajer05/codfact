import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { NotificationService, Notification } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  unreadCount = 0;
  loading = false;
  showDropdown = false;
  private subscriptions: Subscription[] = [];

  constructor(
    private notificationService: NotificationService,
    public authService: AuthService,
    public router: Router
  ) {}

  ngOnInit(): void {
    // Subscribe to notifications
    this.subscriptions.push(
      this.notificationService.notifications$.subscribe(notifications => {
        this.notifications = notifications;
      })
    );

    // Subscribe to unread count
    this.subscriptions.push(
      this.notificationService.unreadCount$.subscribe(count => {
        this.unreadCount = count;
      })
    );

    // Load initial data
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
    if (this.showDropdown) {
      this.loadNotifications();
    }
  }

  loadNotifications(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    this.loading = true;
    this.notificationService.loadNotifications();
    this.notificationService.loadUnreadCount();
    this.loading = false;
  }

  markAsRead(notification: Notification, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    if (!notification.isRead) {
      this.notificationService.markAsReadAndUpdate(notification.id);
    }
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsReadAndUpdate();
  }

  onNotificationClick(notification: Notification): void {
    // Mark as read
    this.markAsRead(notification);

    // Navigate based on notification type
    this.navigateToReference(notification);

    // Close dropdown
    this.showDropdown = false;
  }

  navigateToReference(notification: Notification): void {
    switch (notification.type) {
      case 'BLOG_COMMENT':
        if (notification.referenceId) {
          this.router.navigate(['/blogs', notification.referenceId]);
        }
        break;
      case 'COURSE_ENROLLMENT':
        if (notification.referenceId) {
          this.router.navigate(['/courses', notification.referenceId]);
        }
        break;
      case 'PFE_APPLICATION':
        if (notification.referenceId) {
          this.router.navigate(['/pfe', notification.referenceId]);
        }
        break;
      default:
        // Default action or no navigation
        break;
    }
  }

  getNotificationIcon(type: string): string {
    return this.notificationService.getNotificationIcon(type);
  }

  getNotificationColor(type: string): string {
    return this.notificationService.getNotificationColor(type);
  }

  getTimeAgo(dateString: string): string {
    return this.notificationService.getTimeAgo(dateString);
  }

  // Test function for development
  sendTestNotification(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.notificationService.sendTestNotification(
        currentUser.id,
        'This is a test notification!'
      ).subscribe({
        next: () => {
          console.log('Test notification sent');
          this.loadNotifications();
        },
        error: (error) => {
          console.error('Error sending test notification:', error);
        }
      });
    }
  }

  // TrackBy function for performance
  trackByNotificationId(index: number, notification: Notification): number {
    return notification.id;
  }
}
