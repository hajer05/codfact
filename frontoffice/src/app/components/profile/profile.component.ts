import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { EnrollmentService, Enrollment } from '../../services/enrollment.service';
import { CertificateService, Certificate } from '../../services/certificate.service';
import { QuizService, QuizAttempt } from '../../services/quiz.service';
import { LessonProgressService, LessonProgress } from '../../services/lesson-progress.service';
import { CourseService } from '../../services/course.service';
import { User } from '../../models/user.model';
import { Router, RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  isEditing = false;
  editForm = {
    firstName: '',
    lastName: '',
    email: ''
  };

  // Statistics
  statistics = {
    coursesEnrolled: 0,
    certificatesEarned: 0,
    learningTime: '0h',
    achievements: 0,
    quizzesTaken: 0,
    quizzesPassed: 0
  };
  loading = true;
  
  // Quiz history
  quizAttempts: QuizAttempt[] = [];
  showQuizHistory = false;

  constructor(
    private authService: AuthService,
    private enrollmentService: EnrollmentService,
    private certificateService: CertificateService,
    private quizService: QuizService,
    private lessonProgressService: LessonProgressService,
    private courseService: CourseService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
      if (user) {
        this.editForm = {
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email
        };
        this.loadStatistics(user.id);
      }
    });

    // Redirect if not authenticated
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
    }
  }

  loadStatistics(userId: number): void {
    this.loading = true;
    
    forkJoin({
      enrollments: this.enrollmentService.getMyEnrollments(),
      certificates: this.certificateService.getUserCertificates(userId)
    }).pipe(
      switchMap(result => {
        // Get all course details to calculate actual learning time
        const courseRequests = result.enrollments.map(enrollment => {
          const courseId = (enrollment as any).courseId || enrollment.course?.id;
          if (!courseId) return of(null);
          
          return this.courseService.getCourseById(courseId).pipe(
            switchMap(course => 
              this.lessonProgressService.getUserLessonProgress(courseId).pipe(
                map(progressList => ({ course, progressList, enrollment }))
              )
            )
          );
        });
        
        return forkJoin(courseRequests.length > 0 ? courseRequests : [of(null)]).pipe(
          map(courseData => ({ ...result, courseData: courseData.filter(d => d !== null) }))
        );
      })
    ).subscribe({
      next: (result) => {
        // Courses enrolled
        this.statistics.coursesEnrolled = result.enrollments.length;
        
        // Certificates earned
        this.statistics.certificatesEarned = result.certificates.length;
        
        // Calculate actual learning time from completed lessons
        const totalLearningMinutes = this.calculateActualLearningTime(result.courseData);
        this.statistics.learningTime = this.formatLearningTime(totalLearningMinutes);
        
        // Achievements: count completed lessons + certificates
        const completedLessonsCount = this.countCompletedLessons(result.courseData);
        this.statistics.achievements = completedLessonsCount + result.certificates.length;
        
        // Load quiz attempts
        this.loadQuizHistory(userId);
        
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading statistics:', error);
        this.loading = false;
      }
    });
  }
  
  loadQuizHistory(userId: number): void {
    this.quizService.getUserQuizAttempts(userId).subscribe({
      next: (attempts) => {
        this.quizAttempts = attempts.sort((a, b) => 
          new Date(b.completedAt || '').getTime() - new Date(a.completedAt || '').getTime()
        );
        this.statistics.quizzesTaken = attempts.length;
        this.statistics.quizzesPassed = attempts.filter(a => a.passed).length;
      },
      error: (error) => {
        console.error('Error loading quiz history:', error);
      }
    });
  }

  calculateLearningTime(enrollments: Enrollment[]): number {
    // Estimate learning time based on progress
    // Assuming average course = 10 hours, calculate based on progress
    let totalMinutes = 0;
    enrollments.forEach(enrollment => {
      const estimatedCourseHours = 10; // Average course duration
      const progressHours = (enrollment.progress / 100) * estimatedCourseHours;
      totalMinutes += progressHours * 60;
    });
    return totalMinutes;
  }

  calculateActualLearningTime(courseData: any[]): number {
    // Calculate actual learning time from completed lessons
    let totalMinutes = 0;
    
    courseData.forEach(data => {
      if (!data || !data.course || !data.progressList) return;
      
      // Get all modules and lessons from the course
      const modules = data.course.modules || [];
      
      modules.forEach((module: any) => {
        const lessons = module.lessons || [];
        
        lessons.forEach((lesson: any) => {
          // Check if this lesson is completed
          const lessonProgress = data.progressList.find((p: LessonProgress) => 
            p.lesson?.id === lesson.id
          );
          
          if (lessonProgress && lessonProgress.completed) {
            // Add the lesson duration (convert from minutes to total minutes)
            const lessonDuration = lesson.videoDuration || lesson.duration || 0;
            totalMinutes += lessonDuration;
          }
        });
      });
    });
    
    return totalMinutes;
  }

  countCompletedLessons(courseData: any[]): number {
    // Count total completed lessons across all courses
    let completedCount = 0;
    
    courseData.forEach(data => {
      if (!data || !data.progressList) return;
      
      // Count completed lessons in this course
      completedCount += data.progressList.filter((p: LessonProgress) => p.completed).length;
    });
    
    return completedCount;
  }

  formatLearningTime(minutes: number): string {
    if (minutes < 60) {
      return `${Math.round(minutes)}m`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = Math.round(minutes % 60);
    if (remainingMinutes === 0) {
      return `${hours}h`;
    }
    return `${hours}h ${remainingMinutes}m`;
  }

  startEditing(): void {
    this.isEditing = true;
  }

  cancelEditing(): void {
    this.isEditing = false;
    if (this.user) {
      this.editForm = {
        firstName: this.user.firstName,
        lastName: this.user.lastName,
        email: this.user.email
      };
    }
  }

  saveProfile(): void {
    // TODO: Implement profile update API call
    console.log('Saving profile:', this.editForm);
    
    // For now, just update the local user object
    if (this.user) {
      this.user.firstName = this.editForm.firstName;
      this.user.lastName = this.editForm.lastName;
      this.user.email = this.editForm.email;
      
      // Update localStorage
      localStorage.setItem('user', JSON.stringify(this.user));
    }
    
    this.isEditing = false;
    alert('Profile updated successfully!');
  }

  logout(): void {
    this.authService.explicitLogout();
  }

  getRoleDisplayName(role: string): string {
    switch (role) {
      case 'ROLE_STUDENT':
        return 'Student';
      case 'ROLE_TEACHER':
        return 'Teacher';
      case 'ROLE_ADMIN':
        return 'Administrator';
      case 'ROLE_CONSULTANT':
        return 'Consultant';
      default:
        return role.replace('ROLE_', '');
    }
  }

  getJoinDate(): string {
    if (this.user?.createdAt) {
      return new Date(this.user.createdAt).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    }
    return 'Unknown';
  }
  
  toggleQuizHistory(): void {
    this.showQuizHistory = !this.showQuizHistory;
  }
  
  viewQuizResult(attemptId: number): void {
    this.router.navigate(['/quiz-result', attemptId]);
  }
  
  getQuizStatusClass(attempt: QuizAttempt): string {
    if (attempt.passed) return 'success';
    return 'failed';
  }
}
