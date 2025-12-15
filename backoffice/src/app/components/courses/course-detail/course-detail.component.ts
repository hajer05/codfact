import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseService } from '../../../services/course.service';
import { ModuleService, Module } from '../../../services/module.service';
import { Course } from '../../../models/course.model';

@Component({
  selector: 'app-course-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './course-detail.component.html',
  styleUrls: ['./course-detail.component.scss']
})
export class CourseDetailComponent implements OnInit {
  course: Course | null = null;
  modules: Module[] = [];
  loading = true;
  courseId: number | null = null;
  Math = Math;
  String = String;
  students: any[] = [];
  studentsLoading = false;
  showStudents = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private courseService: CourseService,
    private moduleService: ModuleService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.courseId = +params['id'];
      if (this.courseId) {
        this.loadCourseDetails();
        this.loadModules();
        this.loadCourseStudents();
      }
    });
  }

  loadCourseDetails() {
    if (this.courseId) {
      this.courseService.getCourseDetails(this.courseId).subscribe({
        next: (courseDetails) => {
          this.course = courseDetails;
          this.modules = courseDetails.modules || [];
          this.loading = false;
          console.log('Course details loaded:', courseDetails);
          console.log('Modules:', this.modules);
        },
        error: (error) => {
          console.error('Error loading course details:', error);
          this.loading = false;
        }
      });
    }
  }

  loadModules() {
    // No longer needed as modules are loaded with course details
  }

  getTotalLessons(): number {
    return this.modules.reduce((total, module) => total + (module.lessons?.length || 0), 0);
  }

  getTotalDuration(): number {
    let totalDuration = 0;
    this.modules.forEach(module => {
      if (module.lessons) {
        module.lessons.forEach(lesson => {
          totalDuration += lesson.videoDuration || 0;
        });
      }
    });
    return Math.floor(totalDuration / 60); // Convert to minutes
  }

  editCourse() {
    if (this.courseId) {
      this.router.navigate(['/courses', this.courseId, 'edit']);
    }
  }

  goBack() {
    this.router.navigate(['/courses']);
  }

  loadCourseStudents() {
    if (this.courseId) {
      this.studentsLoading = true;
      this.courseService.getCourseStudents(this.courseId).subscribe({
        next: (data) => {
          this.students = data.students || [];
          this.studentsLoading = false;
          console.log('Course students loaded:', data);
        },
        error: (error) => {
          console.error('Error loading course students:', error);
          this.studentsLoading = false;
        }
      });
    }
  }

  toggleStudents() {
    this.showStudents = !this.showStudents;
    if (this.showStudents && this.students.length === 0) {
      this.loadCourseStudents();
    }
  }
}
