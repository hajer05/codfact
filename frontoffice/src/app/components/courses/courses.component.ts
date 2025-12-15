import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { CourseService } from '../../services/course.service';
import { CartService } from '../../services/cart.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { ToastNotificationService } from '../../services/toast-notification.service';
import { Course, CourseLevel, CourseFilter } from '../../models/course.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.scss']
})
export class CoursesComponent implements OnInit {
  showSuccessMessage = false;
  courses: Course[] = [];
  filteredCourses: Course[] = [];
  enrolledCoursesIds = new Set<number>();
  categories: string[] = [];
  tags: string[] = [];
  loading = false;
  addingToCart: { [courseId: number]: boolean } = {};
  
  // Filter properties
  filter: CourseFilter = {};
  searchTerm = '';
  selectedCategory = '';
  selectedLevel = '';
  selectedRating = 0;
  priceRange = { min: 0, max: 10000 };
  showFreeOnly = false;
  selectedTags: string[] = [];

  // UI state
  showFilters = false;
  sortBy = 'studentsCount';
  sortOrder: 'asc' | 'desc' = 'desc'; // Default to descending for popularity

  // Pagination
  currentPage = 1;
  itemsPerPage = 6;
  totalPages = 0;
  paginatedCourses: Course[] = [];

  courseLevels = Object.values(CourseLevel);

  purchasedCoursesIds = new Set<number>(); // Track purchased courses

  constructor(
    private courseService: CourseService,
    private cartService: CartService,
    private enrollmentService: EnrollmentService,
    private orderService: OrderService,
    private authService: AuthService,
    private toastNotificationService: ToastNotificationService,
    private router: Router
  ) {
    // Subscribe to enrolled courses
    this.enrollmentService.enrolledCourses$.subscribe(enrolledIds => {
      this.enrolledCoursesIds = enrolledIds;
    });
  }

  async ngOnInit() {
    // Check if user came from successful payment
    const queryParams = new URLSearchParams(window.location.search);
    const paymentIntent = queryParams.get('payment_intent');
    const redirectStatus = queryParams.get('redirect_status');
    const purchased = queryParams.get('purchased');
    
    // If Stripe redirected back with a payment_intent, confirm order on backend
    if (paymentIntent && redirectStatus === 'succeeded') {
      try {
        const lastOrderId = Number(localStorage.getItem('lastOrderId'));
        // Use payment_intent from URL first (Stripe provides this), fallback to localStorage
        const paymentIntentId = paymentIntent || localStorage.getItem('lastPaymentIntentId') || '';
        if (lastOrderId && paymentIntentId) {
          console.log('Confirming payment for order:', lastOrderId, 'paymentIntent:', paymentIntentId);
          await this.orderService.confirmPayment({
            orderId: lastOrderId,
            paymentIntentId: paymentIntentId
          }).toPromise();
          
          console.log('Payment confirmed successfully, refreshing enrollments...');
          
          // Cleanup stored IDs after confirmation
          localStorage.removeItem('lastOrderId');
          localStorage.removeItem('lastPaymentIntentId');
          
          // Refresh enrollments and purchased courses
          setTimeout(() => {
            this.enrollmentService.refreshEnrollments();
            this.loadPurchasedCourses();
          }, 500);
          
          // Show success notification about receipt
          this.toastNotificationService.success(
            'Payment successful! Your receipt is ready. Go to your dashboard to view and download your transactions.',
            8000
          );
          
          // Navigate to transactions page after a short delay to show notification
          setTimeout(() => {
            this.router.navigate(['/transactions']);
          }, 2000);
        } else {
          console.warn('Missing orderId or paymentIntentId for confirmation:', { lastOrderId, paymentIntentId });
        }
      } catch (error) {
        console.error('Error confirming payment:', error);
        alert('Erreur lors de la confirmation du paiement. Veuillez contacter le support.');
      }
    }
    
    // Show success message if payment was successful
    if ((paymentIntent && redirectStatus === 'succeeded') || purchased === 'true') {
      this.showSuccessMessage = true;
      
      // Hide message after 5 seconds
      setTimeout(() => {
        this.showSuccessMessage = false;
        // Clean URL by removing query params
        window.history.replaceState({}, document.title, '/courses');
      }, 5000);
    }
    
    this.loadCourses();
    this.loadCategories();
    this.loadTags();
    this.loadPurchasedCourses();
  }

  loadCourses() {
    this.loading = true;
    this.courseService.getAllCourses(this.filter).subscribe({
      next: (courses) => {
        console.log('Courses loaded from backend:', courses);
        this.courses = courses;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading courses:', error);
        this.loading = false;
        // Mock data for development
        this.courses = this.getMockCourses();
        this.tags = ['JavaScript', 'React', 'Angular', 'Node.js', 'Python', 'Java', 'CSS', 'HTML'];
        this.applyFilters();
      }
    });
  }

  loadCategories() {
    this.courseService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: () => {
        this.categories = ['Web Development', 'Mobile Development', 'Data Science', 'DevOps', 'UI/UX Design'];
      }
    });
  }

  loadTags() {
    this.courseService.getTags().subscribe({
      next: (tags) => {
        this.tags = tags;
      },
      error: () => {
        this.tags = ['JavaScript', 'React', 'Angular', 'Node.js', 'Python', 'Java', 'CSS', 'HTML'];
      }
    });
  }

  applyFilters() {
    console.log('Applying filters with:', {
      searchTerm: this.searchTerm,
      selectedCategory: this.selectedCategory,
      selectedLevel: this.selectedLevel,
      priceRange: this.priceRange,
      selectedRating: this.selectedRating,
      showFreeOnly: this.showFreeOnly,
      selectedTags: this.selectedTags,
      totalCourses: this.courses.length
    });

    this.filteredCourses = this.courses.filter(course => {
      const matchesSearch = !this.searchTerm || 
        course.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        course.description.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (course.instructor && course.instructor.toLowerCase().includes(this.searchTerm.toLowerCase())) ||
        (course.teacherName && course.teacherName.toLowerCase().includes(this.searchTerm.toLowerCase()));

      const matchesCategory = !this.selectedCategory || course.category === this.selectedCategory;
      const matchesLevel = !this.selectedLevel || course.level === this.selectedLevel;
      const matchesPrice = course.price >= this.priceRange.min && course.price <= this.priceRange.max;
      const matchesRating = !this.selectedRating || (course.rating && course.rating >= this.selectedRating);
      const matchesFree = !this.showFreeOnly || course.price === 0;

      // Tags filter
      let matchesTags = true;
      if (this.selectedTags.length > 0 && course.tags) {
        matchesTags = this.selectedTags.some(tag => course.tags!.includes(tag));
      }

      const passes = matchesSearch && matchesCategory && matchesLevel && matchesPrice && matchesRating && matchesFree && matchesTags;
      
      if (!passes) {
        console.log('Course filtered out:', course.title, {
          price: course.price,
          priceRange: this.priceRange,
          matchesPrice,
          matchesSearch,
          matchesCategory,
          matchesLevel,
          matchesRating,
          matchesFree,
          matchesTags
        });
      }

      return passes;
    });

    console.log('Filtered courses count:', this.filteredCourses.length);
    this.sortCourses();
    this.updatePagination();
  }

  sortCourses() {
    this.filteredCourses.sort((a, b) => {
      let aValue: any;
      let bValue: any;

      // Handle special sorting cases
      if (this.sortBy === 'studentsCount') {
        // Use enrollments count or studentsCount if available
        aValue = (a as any).studentsCount || (a as any).enrollmentsCount || 0;
        bValue = (b as any).studentsCount || (b as any).enrollmentsCount || 0;
      } else if (this.sortBy === 'rating') {
        // Handle rating with default value
        aValue = a.rating || 0;
        bValue = b.rating || 0;
      } else {
        aValue = a[this.sortBy as keyof Course];
        bValue = b[this.sortBy as keyof Course];
      }

      // Handle undefined/null values
      if (aValue === undefined || aValue === null) aValue = '';
      if (bValue === undefined || bValue === null) bValue = '';

      // String comparison
      if (typeof aValue === 'string' && typeof bValue === 'string') {
        aValue = aValue.toLowerCase();
        bValue = bValue.toLowerCase();
      }

      // Sort logic
      if (this.sortOrder === 'asc') {
        return aValue > bValue ? 1 : aValue < bValue ? -1 : 0;
      } else {
        return aValue < bValue ? 1 : aValue > bValue ? -1 : 0;
      }
    });
    
    this.updatePagination();
  }

  onSearchChange() {
    this.applyFilters();
  }

  onFilterChange() {
    this.applyFilters();
  }

  toggleTag(tag: string) {
    const index = this.selectedTags.indexOf(tag);
    if (index > -1) {
      this.selectedTags.splice(index, 1);
    } else {
      this.selectedTags.push(tag);
    }
    this.applyFilters();
  }

  clearFilters() {
    this.searchTerm = '';
    this.selectedCategory = '';
    this.selectedLevel = '';
    this.selectedRating = 0;
    this.priceRange = { min: 0, max: 10000 };
    this.showFreeOnly = false;
    this.selectedTags = [];
    this.currentPage = 1;
    this.applyFilters();
  }

  updatePagination() {
    this.totalPages = Math.ceil(this.filteredCourses.length / this.itemsPerPage);
    
    // Reset to page 1 if current page is beyond total pages
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = 1;
    }
    
    // Calculate start and end indices
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    
    // Get courses for current page
    this.paginatedCourses = this.filteredCourses.slice(startIndex, endIndex);
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePagination();
    }
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagination();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePagination();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisiblePages = 5;
    
    if (this.totalPages <= maxVisiblePages) {
      for (let i = 1; i <= this.totalPages; i++) {
        pages.push(i);
      }
    } else {
      const half = Math.floor(maxVisiblePages / 2);
      let start = Math.max(1, this.currentPage - half);
      let end = Math.min(this.totalPages, start + maxVisiblePages - 1);
      
      if (end - start + 1 < maxVisiblePages) {
        start = Math.max(1, end - maxVisiblePages + 1);
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }

  getEndIndex(): number {
    return Math.min(this.currentPage * this.itemsPerPage, this.filteredCourses.length);
  }

  toggleFilters() {
    this.showFilters = !this.showFilters;
  }

  isCourseEnrolled(courseId: number): boolean {
    return this.enrolledCoursesIds.has(courseId);
  }

  isCoursePurchased(courseId: number): boolean {
    return this.purchasedCoursesIds.has(courseId);
  }

  loadPurchasedCourses() {
    if (!this.authService?.isAuthenticated()) {
      this.purchasedCoursesIds = new Set<number>();
      return;
    }

    this.orderService.getOrders().subscribe({
      next: (orders) => {
        const purchasedIds = new Set<number>();
        if (orders && orders.length > 0) {
          orders.forEach(order => {
            // Check both string and enum status
            const isCompleted = order.status === 'COMPLETED' || 
                               order.status === 'Completed' || 
                               (order as any).status === 'COMPLETED';
            if (isCompleted && order.items && order.items.length > 0) {
              order.items.forEach(item => {
                if (item.courseId) {
                  purchasedIds.add(item.courseId);
                }
              });
            }
          });
        }
        this.purchasedCoursesIds = purchasedIds;
      },
      error: (error) => {
        console.error('Error loading purchased courses:', error);
        this.purchasedCoursesIds = new Set<number>();
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

  addToCart(courseId: number) {
    // Check if already purchased or enrolled
    if (this.isCoursePurchased(courseId) || this.isCourseEnrolled(courseId)) {
      alert('You already have access to this course');
      return;
    }
    
    this.addingToCart[courseId] = true;
    
    this.cartService.addToCart(courseId).subscribe({
      next: () => {
        this.addingToCart[courseId] = false;
        // Navigate to cart to show user what was added
        this.router.navigate(['/cart']);
      },
      error: (err) => {
        console.error('Error adding to cart:', err);
        this.addingToCart[courseId] = false;
        const errorMessage = err?.error?.error || err?.error || 'Failed to add course to cart';
        alert(errorMessage);
      }
    });
  }

  encodeURIComponent(text: string): string {
    return encodeURIComponent(text);
  }

  getMockCourses(): Course[] {
    return [
      
    ];
  }
}
