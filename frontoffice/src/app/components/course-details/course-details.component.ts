import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { LessonProgressService } from '../../services/lesson-progress.service';
import { CertificateService, Certificate } from '../../services/certificate.service';
import { AuthService } from '../../services/auth.service';
import { ComplaintService } from '../../services/complaint.service';
import { ToastNotificationService } from '../../services/toast-notification.service';
import { QuizService, Quiz, QuizAttempt } from '../../services/quiz.service';
import { Course, CourseLevel } from '../../models/course.model';
import { EnrollmentResponse } from '../../services/enrollment.service';
import { Subscription } from 'rxjs';
import { take, filter } from 'rxjs/operators';

@Component({
  selector: 'app-course-details',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './course-details.component.html',
  styleUrls: ['./course-details.component.scss']
})
export class CourseDetailsComponent implements OnInit, OnDestroy {
  course: Course | null = null;
  courseDetails: any = null;
  loading = true;
  error = false;
  courseId: number = 0;
  Math = Math; // Make Math available in template
  
  // Enrollment state
  isEnrolled = false;
  enrollmentLoading = false;
  enrollmentError = '';
  
  // Progress tracking
  courseProgress = 0;
  lessonProgressMap = new Map<number, any>();
  
  // Video player state
  showVideoPlayer = false;
  currentVideoUrl = '';
  currentLessonTitle = '';
  currentLessonDescription = '';
  currentLessonId: number | null = null;
  
  // Certificate state
  certificate: Certificate | null = null;
  hasCertificate = false;
  certificateLoading = false;

  // Quiz state
  courseQuiz: Quiz | null = null;
  hasQuiz = false;
  hasPassedQuiz = false;
  quizLoading = false;
  lastQuizAttempt: QuizAttempt | null = null;

  // Complaint state
  showComplaintForm = false;
  complaintSubject = '';
  complaintMessage = '';
  complaintLoading = false;
  complaintSuccess = false;

  // Subscription management
  private progressSubscription?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private enrollmentService: EnrollmentService,
    private lessonProgressService: LessonProgressService,
    private certificateService: CertificateService,
    private authService: AuthService,
    private complaintService: ComplaintService,
    private toastNotificationService: ToastNotificationService,
    private quizService: QuizService
  ) {}

  ngOnInit() {
    this.courseId = Number(this.route.snapshot.paramMap.get('id'));
    console.log('🚀 Component initialized - Course ID:', this.courseId);
    
    // Subscribe to progress updates EARLY to get cached data immediately
    this.subscribeToProgressUpdates();
    
    // IMPORTANT: Load course details FIRST, then check enrollment and load progress
    // This ensures courseDetails.modules is available when recalculating progress
    this.loadCourseDetails();
    
    // Check quiz status (doesn't depend on enrollment)
    this.checkQuizStatus();
  }
  
  private subscribeToProgressUpdates(): void {
    // Subscribe to progress updates from service (includes cached data from localStorage)
    this.progressSubscription = this.lessonProgressService.progress$.subscribe(progressMap => {
      console.log('📊 Progress map updated from service, size:', progressMap.size);
      this.lessonProgressMap = new Map(progressMap); // Create new map to trigger change detection
      
      // Recalculate progress whenever progress map changes (if course details are loaded)
      if (this.courseDetails?.modules) {
        console.log('📊 Recalculating progress after map update...');
        this.recalculateCourseProgress();
      }
    });
  }

  loadCourseDetails() {
    this.loading = true;
    this.error = false;

    console.log('📚 Loading course details for course:', this.courseId);

    // Load basic course info
    this.courseService.getCourseById(this.courseId).subscribe({
      next: (course) => {
        console.log('✅ Course basic info loaded:', course.title);
        this.course = course;
      },
      error: (error) => {
        console.error('❌ Error loading course:', error);
        this.error = true;
        this.loading = false;
      }
    });

    // Load detailed course info with modules and lessons
    this.courseService.getCourseDetails(this.courseId).subscribe({
      next: (details) => {
        console.log('✅ Course details loaded with', details.modules?.length, 'modules');
        this.courseDetails = details;
        this.loading = false;
        
        // NOW that courseDetails is loaded, check enrollment and load progress
        console.log('📊 Course details ready, now checking enrollment...');
        this.checkEnrollmentStatus();
      },
      error: (error) => {
        console.error('❌ Error loading course details:', error);
        this.error = true;
        this.loading = false;
      }
    });
  }

  getLevelBadgeClass(level: CourseLevel): string {
    switch (level) {
      case CourseLevel.BEGINNER:
        return 'bg-green-100 text-green-800';
      case CourseLevel.INTERMEDIATE:
        return 'bg-yellow-100 text-yellow-800';
      case CourseLevel.ADVANCED:
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getRatingStars(rating: number): string[] {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= rating ? 'filled' : 'empty');
    }
    return stars;
  }


  goBack() {
    this.router.navigate(['/courses']);
  }

  getTotalDuration(): number {
    if (!this.courseDetails?.modules) return 0;
    
    return this.courseDetails.modules.reduce((total: number, module: any) => {
      if (module.lessons) {
        return total + module.lessons.reduce((moduleTotal: number, lesson: any) => {
          // Backend returns videoDuration in seconds, convert to minutes
          const durationInSeconds = lesson.videoDuration || lesson.duration || 0;
          return moduleTotal + Math.floor(durationInSeconds / 60); // Convert to minutes
        }, 0);
      }
      return total;
    }, 0);
  }

  getTotalLessons(): number {
    if (!this.courseDetails?.modules) return 0;
    
    return this.courseDetails.modules.reduce((total: number, module: any) => {
      return total + (module.lessons ? module.lessons.length : 0);
    }, 0);
  }

  getModuleDuration(module: any): number {
    if (!module?.lessons) return 0;
    return module.lessons.reduce((total: number, lesson: any) => {
      // Backend returns videoDuration in seconds, convert to minutes
      const durationInSeconds = lesson.videoDuration || lesson.duration || 0;
      return total + Math.floor(durationInSeconds / 60); // Convert to minutes
    }, 0);
  }

  getLessonDurationInMinutes(lesson: any): number {
    // Backend returns videoDuration in seconds, convert to minutes
    const durationInSeconds = lesson.videoDuration || lesson.duration || 0;
    return Math.floor(durationInSeconds / 60);
  }

  getLessonDurationInSeconds(lesson: any): number {
    // Return duration in seconds
    return lesson.videoDuration || lesson.duration || 0;
  }

  getInitials(name: string): string {
    if (!name) return 'T';
    return name.charAt(0).toUpperCase();
  }



  getVideoUrl(lesson: any): string {
    const baseUrl = 'http://localhost:8090/api/';
    
    // Debug logging
    console.log('getVideoUrl called for lesson:', lesson);
    console.log('Lesson video properties:', {
      videoUrl: lesson?.videoUrl,
      video_url: lesson?.video_url,
      videoPath: lesson?.videoPath,
      videoFileName: lesson?.videoFileName,
      type: lesson?.type
    });
    
    // Check if lesson has video content (might be hidden for paid courses)
    if (!lesson.videoUrl && !lesson.video_url && !lesson.videoPath && !lesson.videoFileName) {
      console.warn('No video content found for lesson:', lesson.title);
      return '';
    }
    
    // Priority: videoFileName (for uploaded files) > videoUrl/video_url/videoPath
    if (lesson.videoFileName) {
      // videoFileName should be the stored filename (UUID + extension)
      const videoUrl = baseUrl + 'lessons/videos/' + lesson.videoFileName;
      console.log('Using videoFileName, constructed URL:', videoUrl);
      return videoUrl;
    }
    
    // If videoUrl contains the path like "lessons/videos/uuid.mp4", extract filename
    const videoPath = lesson?.videoUrl || lesson?.video_url || lesson?.videoPath || '';
    if (videoPath) {
      // Check if it's already a full URL
      if (videoPath.startsWith('http')) {
        console.log('Using full URL from videoPath:', videoPath);
        return videoPath;
      }
      
      // If it's a path like "lessons/videos/uuid.mp4", extract just the filename
      if (videoPath.includes('/')) {
        const filename = videoPath.substring(videoPath.lastIndexOf('/') + 1);
        const videoUrl = baseUrl + 'lessons/videos/' + filename;
        console.log('Extracted filename from videoPath:', filename, 'URL:', videoUrl);
        return videoUrl;
      }
      
      // Otherwise treat as relative path
      const videoUrl = baseUrl + videoPath;
      console.log('Using videoPath as relative path, URL:', videoUrl);
      return videoUrl;
    }
    
    console.warn('Could not construct video URL for lesson:', lesson.title);
    return '';
  }

  checkEnrollmentStatus(): void {
    console.log('🔍 Checking enrollment status for course:', this.courseId);
    this.enrollmentService.checkEnrollment(this.courseId).subscribe({
      next: (response) => {
        this.isEnrolled = response.enrolled;
        console.log('✅ Enrollment status:', this.isEnrolled);
        
        // Load progress and certificate AFTER enrollment status is determined
        if (this.isEnrolled) {
          console.log('📊 User is enrolled, loading progress...');
          this.loadCourseProgress();
          this.checkCertificateStatus();
        } else {
          console.log('❌ User is not enrolled');
        }
      },
      error: (error) => {
        console.error('❌ Error checking enrollment status:', error);
        this.isEnrolled = false;
      }
    });
  }

  enrollInCourse(): void {
    this.enrollmentLoading = true;
    this.enrollmentError = '';

    this.enrollmentService.enrollInCourse(this.courseId).subscribe({
      next: (response: EnrollmentResponse) => {
        this.enrollmentLoading = false;
        if (response.success) {
          this.isEnrolled = true;
          console.log('Successfully enrolled in course:', response);
          // Load progress and certificate after enrollment
          this.loadCourseProgress();
          this.checkCertificateStatus();
        } else {
          this.enrollmentError = response.message;
          console.error('Enrollment failed:', response.message);
        }
      },
      error: (error) => {
        this.enrollmentLoading = false;
        this.enrollmentError = error.error?.error || 'Failed to enroll in course';
        console.error('Enrollment error:', error);
      }
    });
  }

  unenrollFromCourse(): void {
    this.enrollmentLoading = true;
    this.enrollmentError = '';

    this.enrollmentService.unenrollFromCourse(this.courseId).subscribe({
      next: () => {
        this.isEnrolled = false;
        this.enrollmentLoading = false;
        console.log('Successfully unenrolled from course');
      },
      error: (error) => {
        this.enrollmentLoading = false;
        this.enrollmentError = error.error?.error || 'Failed to unenroll from course';
        console.error('Unenrollment error:', error);
      }
    });
  }

  loadCourseProgress(): void {
    console.log('📊 loadCourseProgress called');
    console.log('courseDetails available:', !!this.courseDetails);
    console.log('courseDetails.modules:', this.courseDetails?.modules?.length);
    
    if (!this.courseDetails?.modules) {
      console.error('❌ Cannot load progress: courseDetails.modules not available yet!');
      return;
    }
    
    // Load lesson progress from backend - this will update the service's BehaviorSubject
    // which will automatically trigger our subscription from ngOnInit
    console.log('📡 Fetching lesson progress from backend...');
    this.lessonProgressService.getUserLessonProgress(this.courseId).subscribe({
      next: (progressList) => {
        console.log('✅ Received', progressList.length, 'lesson progress records from backend');
        progressList.forEach(p => {
          console.log(`  - Lesson ${p.lesson.id}: completed=${p.completed}`);
        });
        
        // Recalculate progress after loading from backend
        if (this.courseDetails?.modules) {
          this.recalculateCourseProgress();
        }
      },
      error: (error) => {
        console.error('❌ Error loading lesson progress:', error);
      }
    });

    // Load overall course progress percentage from backend
    this.lessonProgressService.getCourseProgressPercentage(this.courseId).subscribe({
      next: (response) => {
        // Backend returns percentage based on completed lessons
        const backendProgress = Math.min(100, Math.round(response.percentage));
        
        // Use local calculation if it's more up-to-date (optimistic updates)
        if (this.courseDetails?.modules) {
          this.recalculateCourseProgress();
          // Prefer local calculation for immediate UI updates
          console.log('Course progress - Backend:', backendProgress + '%, Local:', this.courseProgress + '%');
        } else {
          this.courseProgress = backendProgress;
        }
      },
      error: (error) => {
        console.error('Error loading course progress:', error);
        // Fallback to local calculation
        if (this.courseDetails?.modules) {
          this.recalculateCourseProgress();
        }
      }
    });
  }

  markLessonAsCompleted(lessonId: number): void {
    console.log('✓ Marking lesson', lessonId, 'as completed');
    
    // Optimistically update UI immediately
    const optimisticProgress: any = {
      id: 0,
      completed: true,
      completedAt: new Date().toISOString(),
      lastAccessedAt: new Date().toISOString(),
      enrollment: null,
      lesson: { id: lessonId }
    };
    
    // Update local state immediately for instant UI feedback
    const currentProgress = this.lessonProgressMap;
    currentProgress.set(lessonId, optimisticProgress);
    this.lessonProgressMap = new Map(currentProgress);
    
    // Recalculate progress immediately
    this.recalculateCourseProgress();
    
    // Call backend to mark as completed
    this.lessonProgressService.markLessonAsCompleted(lessonId).subscribe({
      next: (progress) => {
        console.log('Lesson marked as completed:', progress);
        
        // Update with real backend response
        if (progress && progress.completed) {
          const currentProgress = this.lessonProgressMap;
          currentProgress.set(lessonId, progress);
          this.lessonProgressMap = new Map(currentProgress);
        }
        
        // The progress is already updated optimistically and via the service's tap operator
        // Recalculate progress to reflect the change immediately
        this.recalculateCourseProgress();
        const progressPercentage = this.getCourseProgressPercentage();
        
        if (progressPercentage === 100) {
          // Check if course has quiz
          if (this.hasQuiz && !this.hasPassedQuiz) {
            this.toastNotificationService.success('🎉 Félicitations ! Vous avez terminé toutes les leçons ! Passez maintenant le quiz pour obtenir votre certificat.');
          } else if (this.hasQuiz && this.hasPassedQuiz) {
            this.toastNotificationService.success('🎉 Félicitations ! Vous avez terminé le cours ! Le certificat est maintenant disponible.');
          } else {
            // No quiz required
            this.toastNotificationService.success('🎉 Félicitations ! Vous avez terminé le cours ! Le certificat est maintenant disponible.');
          }
        } else {
          this.toastNotificationService.success(`✓ Leçon marquée comme complétée ! Progression : ${progressPercentage}%`);
        }
      },
      error: (error) => {
        console.error('Error marking lesson as completed:', error);
        
        // Revert optimistic update on error
        const currentProgress = this.lessonProgressMap;
        currentProgress.delete(lessonId);
        this.lessonProgressMap = new Map(currentProgress);
        this.recalculateCourseProgress();
        
        this.showErrorMessage('Erreur lors de la mise à jour. Veuillez réessayer.');
      }
    });
  }

  recalculateCourseProgress(): void {
    if (!this.courseDetails?.modules) {
      console.warn('⚠️ Cannot recalculate: courseDetails.modules not available');
      return;
    }
    
    let totalLessons = 0;
    let completedLessons = 0;
    
    // Count all lessons across all modules (not based on duration, just completion)
    this.courseDetails.modules.forEach((module: any) => {
      if (module.lessons && module.lessons.length > 0) {
        totalLessons += module.lessons.length;
        module.lessons.forEach((lesson: any) => {
          if (this.isLessonCompleted(lesson.id)) {
            completedLessons++;
          }
        });
      }
    });
    
    if (totalLessons > 0) {
      // Calculate progress purely based on completed lessons / total lessons
      const calculatedProgress = (completedLessons / totalLessons) * 100;
      this.courseProgress = Math.min(100, Math.max(0, Math.round(calculatedProgress)));
      
      // Ensure it's exactly 100% when all lessons are completed
      if (completedLessons === totalLessons && totalLessons > 0) {
        this.courseProgress = 100;
      }
      
      console.log('📊 Course progress:', this.courseProgress + '%', `(${completedLessons}/${totalLessons} completed)`);
    } else {
      // No lessons in course, progress is 0
      this.courseProgress = 0;
      console.log('⚠️ No lessons found, progress set to 0%');
    }
    
    // Force change detection
    if ((window as any).ng) {
      // Angular change detection will pick this up automatically
    }
  }

  showSuccessMessage(message: string): void {
    this.toastNotificationService.success(message);
  }

  showErrorMessage(message: string): void {
    this.toastNotificationService.error(message);
  }

  isLessonCompleted(lessonId: number): boolean {
    // Use local map for consistency
    const progress = this.lessonProgressMap.get(lessonId);
    const isCompleted = progress ? (progress.completed === true) : false;
    // console.log(`isLessonCompleted(${lessonId}): ${isCompleted}, progress:`, progress);
    return isCompleted;
  }

  getCourseProgressPercentage(): number {
    // Just return the current progress - don't recalculate here to avoid infinite loops
    // Recalculation happens automatically when progress map changes
    // Ensure it's between 0 and 100
    const progress = Math.min(100, Math.max(0, Math.round(this.courseProgress)));
    return progress;
  }

  ngOnDestroy(): void {
    // Clean up subscription
    if (this.progressSubscription) {
      this.progressSubscription.unsubscribe();
    }
  }

  getModuleProgress(module: any): number {
    if (!module.lessons || !this.isEnrolled || module.lessons.length === 0) return 0;
    
    const completedLessons = module.lessons.filter((lesson: any) => 
      this.isLessonCompleted(lesson.id)
    ).length;
    
    // Calculate based on completed lessons / total lessons
    const progress = Math.round((completedLessons / module.lessons.length) * 100);
    
    // Ensure exactly 100% when all lessons are completed
    if (completedLessons === module.lessons.length) {
      return 100;
    }
    
    return progress;
  }

  // Video player methods
  playVideo(lesson: any): void {
    // Check if user has access to this lesson content
    if (!this.canAccessLessonContent(lesson)) {
      alert('Vous devez être inscrit à ce cours pour accéder au contenu des vidéos.');
      return;
    }

    const videoUrl = this.getVideoUrl(lesson);
    console.log('playVideo - lesson:', lesson.title, 'videoUrl:', videoUrl);
    
    if (!videoUrl) {
      alert('Aucune vidéo disponible pour cette leçon.');
      return;
    }

    this.currentVideoUrl = videoUrl;
    this.currentLessonTitle = lesson.title;
    this.currentLessonDescription = lesson.description || '';
    this.currentLessonId = lesson.id;
    this.showVideoPlayer = true;
    
    // Auto-mark lesson as completed when video ends
    setTimeout(() => {
      const videoElement = document.querySelector('video');
      if (videoElement) {
        videoElement.addEventListener('loadeddata', () => {
          console.log('Video loaded successfully:', videoUrl);
        });
        videoElement.addEventListener('error', (e) => {
          console.error('Video load error:', e);
          this.showErrorMessage('Erreur lors du chargement de la vidéo. Veuillez réessayer.');
        });
        videoElement.addEventListener('ended', () => {
          this.markLessonAsCompleted(lesson.id);
        });
      }
    }, 1000);
  }

  canAccessLessonContent(lesson: any): boolean {
    // Free lessons are always accessible
    if (lesson.isFree || lesson.free) {
      return true;
    }

    // For paid courses, check if user is enrolled
    if (this.course && this.course.price > 0) {
      return this.isEnrolled;
    }

    // Free courses are accessible to everyone
    return true;
  }

  canViewLessonContent(lesson: any): boolean {
    // Check if lesson has content (video, text, etc.)
    const hasContent = lesson.videoUrl || lesson.video_url || lesson.videoPath || lesson.content;
    
    if (!hasContent) {
      return false;
    }

    // If lesson is marked as free, content is visible
    if (lesson.isFree || lesson.free) {
      return true;
    }

    // For paid courses, only enrolled users can see content
    if (this.course && this.course.price > 0) {
      return this.isEnrolled;
    }

    // Free courses are accessible to everyone
    return true;
  }

  closeVideoPlayer(): void {
    this.showVideoPlayer = false;
    this.currentVideoUrl = '';
    this.currentLessonTitle = '';
    this.currentLessonDescription = '';
    this.currentLessonId = null;
  }

  hasVideo(lesson: any): boolean {
    // Check if lesson is a video type
    const isVideoType = (lesson?.type === 'VIDEO' || lesson?.type === 'video');
    
    // Check if lesson has video content (but content might be hidden)
    const hasVideoContent = !!(lesson?.videoUrl || lesson?.video_url || lesson?.videoPath || lesson?.videoFileName);
    
    return isVideoType && hasVideoContent;
  }

  // Quiz methods
  checkQuizStatus(): void {
    // Check if course has a quiz
    this.quizLoading = true;
    this.quizService.courseHasApprovedQuiz(this.courseId).subscribe({
      next: (hasQuiz) => {
        this.hasQuiz = hasQuiz;
        console.log('Course has quiz:', hasQuiz);
        if (hasQuiz) {
          this.loadCourseQuiz();
          this.checkIfUserPassedQuiz();
        } else {
          this.quizLoading = false;
        }
      },
      error: (error) => {
        console.error('Error checking quiz status:', error);
        // Ne pas changer hasQuiz en cas d'erreur - garder l'état actuel
        this.quizLoading = false;
      }
    });
  }

  loadCourseQuiz(): void {
    this.quizService.getApprovedQuizForCourse(this.courseId).subscribe({
      next: (quiz) => {
        this.courseQuiz = quiz;
        console.log('Course quiz loaded:', quiz);
        this.quizLoading = false;
      },
      error: (error) => {
        console.error('Error loading course quiz:', error);
        this.toastNotificationService.error('Erreur lors du chargement du quiz');
        // Ne pas réinitialiser hasQuiz - l'utilisateur peut réessayer
        this.quizLoading = false;
      }
    });
  }

  checkIfUserPassedQuiz(): void {
    // Use currentUser$ observable with filter and take to avoid race conditions
    this.authService.currentUser$.pipe(
      filter(user => user !== null),
      take(1)
    ).subscribe(currentUser => {
      this.quizService.hasPassedCourseQuiz(currentUser.id, this.courseId).subscribe({
        next: (passed) => {
          this.hasPassedQuiz = passed;
          console.log('User has passed quiz:', passed);
          
          // Charger la dernière tentative réussie si le quiz est passé
          if (passed) {
            this.loadLastPassedAttempt();
          }
        },
        error: (error) => {
          console.error('Error checking if user passed quiz:', error);
          // Ne pas changer hasPassedQuiz - garder false par défaut
        }
      });
    });
  }

  loadLastPassedAttempt(): void {
    // Use currentUser$ observable with filter and take to avoid race conditions
    this.authService.currentUser$.pipe(
      filter(user => user !== null),
      take(1)
    ).subscribe(user => {
      if (this.course) {
        this.quizService.getLastPassedAttempt(user.id, this.course.id).subscribe({
          next: (attempt) => {
            this.lastQuizAttempt = attempt;
            console.log('Dernière tentative réussie:', attempt);
          },
          error: (err) => {
            console.error('Erreur lors du chargement de la dernière tentative:', err);
          }
        });
      }
    });
  }

  viewQuizResults(): void {
    if (this.lastQuizAttempt) {
      this.router.navigate(['/quiz-result', this.lastQuizAttempt.id]);
    }
  }

  startQuiz(): void {
    if (!this.courseQuiz) {
      console.error('Aucun quiz disponible');
      this.toastNotificationService.error('Quiz non disponible');
      return;
    }
    
    console.log('Navigation vers le quiz:', this.courseQuiz.id);
    this.router.navigate(['/quiz-take', this.courseQuiz.id]);
  }

  // Certificate methods
  checkCertificateStatus(): void {
    // Use currentUser$ observable with filter and take to avoid race conditions
    // Wait for a non-null user, then take only the first emission and auto-unsubscribe
    this.authService.currentUser$.pipe(
      filter(user => user !== null),
      take(1)
    ).subscribe(currentUser => {
      console.log('Checking certificate status - User:', currentUser, 'Enrolled:', this.isEnrolled);
      if (this.isEnrolled) {
        this.certificateService.hasCertificate(currentUser.id, this.courseId).subscribe({
          next: (hasCert) => {
            this.hasCertificate = hasCert;
            console.log('Has certificate:', hasCert);
            if (hasCert) {
              this.loadCertificate();
            }
          },
          error: (error) => {
            console.error('Error checking certificate status:', error);
          }
        });
      }
    });
  }

  loadCertificate(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.certificateService.getCertificate(currentUser.id, this.courseId).subscribe({
        next: (cert) => {
          this.certificate = cert;
        },
        error: (error) => {
          console.error('Error loading certificate:', error);
        }
      });
    }
  }

  generateCertificate(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      console.error('User not authenticated');
      this.toastNotificationService.error('Veuillez vous connecter pour générer un certificat');
      return;
    }

    if (!this.isEnrolled) {
      console.error('User not enrolled in course');
      this.toastNotificationService.error('Vous devez être inscrit à ce cours pour générer un certificat');
      return;
    }

    const progress = this.getCourseProgressPercentage();
    console.log('Current course progress:', progress, 'Enrolled:', this.isEnrolled);
    
    if (progress < 100) {
      console.error('Course must be 100% completed to generate certificate. Current progress:', progress);
      this.toastNotificationService.error(`Le cours doit être complété à 100% pour générer le certificat. Progression actuelle : ${progress}%`);
      return;
    }

    // Check if course has a quiz and if user passed it
    if (this.hasQuiz && !this.hasPassedQuiz) {
      this.toastNotificationService.error('Vous devez réussir le quiz du cours avant de pouvoir générer le certificat');
      return;
    }

    this.certificateLoading = true;
    console.log('Generating certificate for user:', currentUser.id, 'course:', this.courseId);
    
    this.certificateService.generateCertificate(currentUser.id, this.courseId).subscribe({
      next: (cert) => {
        this.certificate = cert;
        this.hasCertificate = true;
        this.certificateLoading = false;
        console.log('Certificate generated successfully:', cert);
        this.toastNotificationService.success('🏆 Certificat généré avec succès ! Félicitations pour avoir terminé le cours !');
      },
      error: (error) => {
        this.certificateLoading = false;
        console.error('Error generating certificate:', error);
        const errorMessage = error.error?.message || error.message || 'Unknown error occurred';
        this.toastNotificationService.error('Erreur lors de la génération du certificat : ' + errorMessage);
      }
    });
  }

  downloadCertificate(): void {
    if (this.certificate) {
      const downloadUrl = this.certificateService.downloadCertificate(this.certificate.certificateFileName);
      window.open(downloadUrl, '_blank');
    }
  }

  submitComplaint(): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser || !this.courseId) {
      this.showErrorMessage('You must be logged in to submit a complaint');
      return;
    }

    if (!this.complaintSubject.trim() || !this.complaintMessage.trim()) {
      this.showErrorMessage('Please fill in both subject and message');
      return;
    }

    this.complaintLoading = true;
    this.complaintService.createComplaint(
      currentUser.id,
      this.courseId,
      this.complaintSubject.trim(),
      this.complaintMessage.trim()
    ).subscribe({
      next: () => {
        this.complaintLoading = false;
        this.complaintSuccess = true;
        this.complaintSubject = '';
        this.complaintMessage = '';
        this.showComplaintForm = false;
        this.toastNotificationService.success('✅ Réclamation soumise avec succès ! Le professeur sera notifié.');
        setTimeout(() => {
          this.complaintSuccess = false;
        }, 5000);
      },
      error: (error) => {
        console.error('Error submitting complaint:', error);
        this.complaintLoading = false;
        this.toastNotificationService.error('❌ Erreur lors de la soumission de la réclamation. Veuillez réessayer.');
      }
    });
  }
}
