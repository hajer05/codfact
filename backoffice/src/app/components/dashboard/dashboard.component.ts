import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CourseService } from '../../services/course.service';
import { UserService } from '../../services/user.service';
import { OrderService } from '../../services/order.service';
import { Course } from '../../models/course.model';
import { User } from '../../models/user.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  recentCourses: Course[] = [];
  currentDate = new Date();
  stats = {
    totalCourses: 0,
    publishedCourses: 0,
    totalStudents: 0,
    totalRevenue: 0
  };

  constructor(
    private authService: AuthService,
    private courseService: CourseService,
    private userService: UserService,
    private orderService: OrderService
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.loadDashboardData();
    });
  }

  loadDashboardData() {
    // Only load courses for TEACHER and ADMIN roles
    // Consultants should not see course data
    if (this.currentUser && (this.currentUser.roles.includes('TEACHER') || this.currentUser.roles.includes('ADMIN'))) {
      // Load courses
      this.courseService.getMyCourses().subscribe(courses => {
        this.recentCourses = courses.slice(0, 3);
        this.stats.totalCourses = courses.length;
        this.stats.publishedCourses = courses.filter(c => c.status === 'PUBLISHED').length;
      });
      
      // Load statistics for ADMIN only
      if (this.currentUser.roles.includes('ADMIN')) {
        forkJoin({
          users: this.userService.getAllUsers(),
          orders: this.orderService.getAllOrders()
        }).subscribe({
          next: (result) => {
            // Count students (users with ROLE_STUDENT or ETUDIANT)
            this.stats.totalStudents = result.users.filter(user => 
              user.roles.some(role => role === 'ROLE_STUDENT' || role === 'ETUDIANT' || role === 'STUDENT')
            ).length;
            
            // Calculate total revenue from completed orders
            this.stats.totalRevenue = result.orders
              .filter(order => order.status === 'COMPLETED' || order.status === 'PAID')
              .reduce((total, order) => total + order.totalAmount, 0);
            
            console.log('📊 Dashboard stats updated:', this.stats);
            console.log('  - Total students:', this.stats.totalStudents);
            console.log('  - Total revenue: $', this.stats.totalRevenue);
            console.log('  - Total courses:', this.stats.totalCourses);
            console.log('  - Published courses:', this.stats.publishedCourses);
          },
          error: (error) => {
            console.error('❌ Error loading dashboard statistics:', error);
            // Set default values on error
            this.stats.totalStudents = 0;
            this.stats.totalRevenue = 0;
          }
        });
      } else if (this.currentUser.roles.includes('TEACHER')) {
        // For teachers, load only orders related to their courses
        this.orderService.getAllOrders().subscribe({
          next: (orders) => {
            // Filter orders for teacher's courses
            const teacherCourseIds = this.recentCourses.map(c => c.id);
            const teacherOrders = orders.filter(order => 
              order.items.some(item => teacherCourseIds.includes(item.courseId))
            );
            
            // Calculate revenue from teacher's courses
            this.stats.totalRevenue = teacherOrders
              .filter(order => order.status === 'COMPLETED' || order.status === 'PAID')
              .reduce((total, order) => total + order.totalAmount, 0);
            
            console.log('📊 Teacher stats updated - Revenue: $', this.stats.totalRevenue);
          },
          error: (error) => {
            console.error('Error loading teacher statistics:', error);
          }
        });
      }
    }
    // Consultants don't load course data - they only handle complaints/consultations
  }

  getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  }

  getRoleDisplayName(): string {
    if (!this.currentUser?.roles.length) return '';
    const role = this.currentUser.roles[0];
    switch (role) {
      case 'ADMIN': return 'Administrator';
      case 'TEACHER': return 'Teacher';
      case 'CONSULTANT': return 'Consultant';
      case 'ETUDIANT': return 'Student';
      default: return role;
    }
  }
}
