import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CourseService } from '../../../services/course.service';
import { ModuleService, Module, ModuleRequest } from '../../../services/module.service';
import { LessonService, Lesson, LessonRequest } from '../../../services/lesson.service';
import { NotificationService } from '../../../services/notification.service';
import { CourseLevel, CreateCourseRequest } from '../../../models/course.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-course-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './course-create.component.html',
  styleUrls: ['./course-create.component.scss']
})
export class CourseCreateComponent implements OnInit {
  @ViewChild('thumbnailInput') thumbnailInput!: ElementRef<HTMLInputElement>;
  @ViewChild('videoInput') videoInput!: ElementRef<HTMLInputElement>;
  
  currentStep = 1;
  totalSteps = 5;
  courseForm: FormGroup;
  loading = false;
  Math = Math; // Make Math available in template
  
  courseId: number | null = null;
  thumbnailFile: File | null = null;
  previewVideoFile: File | null = null;
  thumbnailPreview: string | null = null;
  videoPreview: string | null = null;
  
  // Curriculum data
  modules: Module[] = [];
  currentModule: any = { title: '', description: '', lessons: [] };
  currentLesson: any = { title: '', description: '', videoFile: null, duration: 0 };
  editingLessonId: number | null = null; // Track which lesson is being edited
  lessonVideoPreview: string | null = null; // Preview URL for lesson video
  lessonForm!: FormGroup;
  showModuleForm = false;
  showLessonForm = false;

  courseLevels = [
    { value: CourseLevel.BEGINNER, label: 'Beginner' },
    { value: CourseLevel.INTERMEDIATE, label: 'Intermediate' },
    { value: CourseLevel.ADVANCED, label: 'Advanced' }
  ];

  categories = [
    'Web Development',
    'Mobile Development',
    'Data Science',
    'Machine Learning',
    'DevOps',
    'Cybersecurity',
    'UI/UX Design',
    'Business',
    'Marketing',
    'Other'
  ];

  steps = [
    { number: 1, title: 'Basic Info', description: 'Course title, description, and category' },
    { number: 2, title: 'Media', description: 'Upload thumbnail and preview video' },
    { number: 3, title: 'Curriculum', description: 'Add modules and lessons' },
    { number: 4, title: 'Pricing', description: 'Set course price and level' },
    { number: 5, title: 'Review', description: 'Review and publish your course' }
  ];

  constructor(
    private fb: FormBuilder,
    private courseService: CourseService,
    private moduleService: ModuleService,
    private lessonService: LessonService,
    private router: Router,
    private route: ActivatedRoute,
    private notificationService: NotificationService
  ) {
    this.courseForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(5)]],
      shortDescription: ['', [Validators.required, Validators.maxLength(200)]],
      description: ['', [Validators.required, Validators.minLength(20)]],
      category: ['', Validators.required],
      language: ['French', Validators.required],
      price: [0, [Validators.required, Validators.min(0)]],
      level: ['', Validators.required]
    });
  }

  ngOnInit() {
    // Check if we're editing an existing course
    this.route.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.courseId = +id;
        this.loadCourseForEdit();
      }
    });
  }

  loadCourseForEdit() {
    if (!this.courseId) return;
    
    this.loading = true;
    
    // Load course details
    this.courseService.getCourseById(this.courseId).subscribe({
      next: (course) => {
        // Populate form with existing course data
        this.courseForm.patchValue({
          title: course.title,
          shortDescription: course.shortDescription || '',
          description: course.description || '',
          category: course.category || '',
          language: course.language || 'French',
          price: course.price || 0,
          level: course.level || CourseLevel.BEGINNER
        });

        // Load thumbnail and preview video if they exist
        if (course.thumbnailImage) {
          this.thumbnailPreview = `http://localhost:8090/uploads/${course.thumbnailImage}`;
        }
        if (course.previewVideo) {
          this.videoPreview = `http://localhost:8090/uploads/${course.previewVideo}`;
        }

        // Load modules and lessons
        this.loadModules();
        
        this.loading = false;
        // Allow navigation to all steps since course already exists
        this.currentStep = 1;
      },
      error: (error) => {
        console.error('Error loading course for edit:', error);
        this.loading = false;
        // Redirect to course list if course not found
        this.router.navigate(['/courses']);
      }
    });
  }

  loadModules() {
    if (this.courseId) {
      this.moduleService.getModulesByCourse(this.courseId).subscribe({
        next: (modules) => {
          // Ensure each module has a lessons array
          this.modules = modules.map(module => ({
            ...module,
            lessons: module.lessons || []
          }));
        },
        error: (error) => {
          console.error('Error loading modules:', error);
        }
      });
    }
  }

  nextStep() {
    if (this.currentStep < this.totalSteps) {
      if (this.currentStep === 1 && this.isStep1Valid()) {
        this.saveDraftCourse();
      } else if (this.currentStep > 1) {
        this.currentStep++;
      }
    }
  }

  prevStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
      
      // Charger les médias quand on revient à l'étape 2
      if (this.currentStep === 2 && this.courseId) {
        this.loadCourseMedia();
      }
    }
  }

  goToStep(step: number) {
    // In edit mode, allow navigation to any step since course already exists
    if (this.courseId || step <= this.currentStep || (step === 2 && this.courseId)) {
      this.currentStep = step;
      
      // Charger les médias quand on navigue vers l'étape 2
      if (step === 2 && this.courseId) {
        this.loadCourseMedia();
      }
    }
  }

  isStep1Valid(): boolean {
    const step1Fields = ['title', 'shortDescription', 'description', 'category', 'language'];
    return step1Fields.every(field => this.courseForm.get(field)?.valid);
  }

  isStep3Valid(): boolean {
    // Check if there's at least one module
    if (this.modules.length === 0) {
      return false;
    }
    
    // Check if every module has at least one lesson
    return this.modules.every(module => {
      // Ensure lessons array exists and has at least one lesson
      const lessons = module?.lessons || [];
      return lessons.length > 0;
    });
  }

  isStep4Valid(): boolean {
    const step4Fields = ['price', 'level'];
    return step4Fields.every(field => this.courseForm.get(field)?.valid);
  }

  saveDraftCourse() {
    if (!this.isStep1Valid()) return;
    
    this.loading = true;
    const courseData: CreateCourseRequest = {
      title: this.courseForm.value.title,
      description: this.courseForm.value.description,
      shortDescription: this.courseForm.value.shortDescription,
      category: this.courseForm.value.category,
      language: this.courseForm.value.language,
      price: this.courseId ? this.courseForm.value.price : 0, // Use current price if editing
      level: this.courseId ? this.courseForm.value.level : CourseLevel.BEGINNER // Use current level if editing
    };

    if (this.courseId) {
      // Update existing course
      this.courseService.updateCourse(this.courseId, courseData).subscribe({
        next: (course) => {
          // Recharger le cours pour obtenir toutes les informations à jour (y compris les médias)
          this.loadCourseMedia();
          this.loading = false;
          this.currentStep = 2;
        },
        error: (error) => {
          this.loading = false;
          console.error('Error updating course:', error);
          this.notificationService.error('❌ Erreur lors de la mise à jour du cours.');
        }
      });
    } else {
      // Create new course
      this.courseService.createCourse(courseData).subscribe({
        next: (course) => {
          this.courseId = course.id;
          this.loadModules(); // Load existing modules after course creation
          this.loading = false;
          this.currentStep = 2;
          this.notificationService.success('✅ Cours créé avec succès !');
        },
        error: (error) => {
          this.loading = false;
          console.error('Error creating course:', error);
          this.notificationService.error('❌ Erreur lors de la création du cours.');
        }
      });
    }
  }

  loadCourseMedia() {
    if (!this.courseId) return;
    
    this.courseService.getCourseById(this.courseId).subscribe({
      next: (course) => {
        // Load thumbnail and preview video if they exist
        if (course.thumbnailImage) {
          this.thumbnailPreview = `http://localhost:8090/uploads/${course.thumbnailImage}`;
        }
        if (course.previewVideo) {
          this.videoPreview = `http://localhost:8090/uploads/${course.previewVideo}`;
        }
      },
      error: (error) => {
        console.error('Error loading course media:', error);
      }
    });
  }

  onThumbnailSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.type.startsWith('image/')) {
      this.thumbnailFile = file;
      
      const reader = new FileReader();
      reader.onload = (e) => {
        this.thumbnailPreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  onVideoSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.type.startsWith('video/')) {
      this.previewVideoFile = file;
      
      const reader = new FileReader();
      reader.onload = (e) => {
        this.videoPreview = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  clearThumbnail() {
    this.thumbnailPreview = null;
    this.thumbnailFile = null;
    if (this.thumbnailInput) {
      this.thumbnailInput.nativeElement.value = '';
    }
  }

  clearVideo() {
    this.videoPreview = null;
    this.previewVideoFile = null;
    if (this.videoInput) {
      this.videoInput.nativeElement.value = '';
    }
  }

  uploadMedia() {
    if (!this.courseId) return;

    this.loading = true;
    const uploads = [];

    if (this.thumbnailFile) {
      uploads.push(this.courseService.uploadThumbnail(this.courseId, this.thumbnailFile));
    }

    if (this.previewVideoFile) {
      uploads.push(this.courseService.uploadPreviewVideo(this.courseId, this.previewVideoFile));
    }

    if (uploads.length > 0) {
      forkJoin(uploads).subscribe({
        next: (results) => {
          // Recharger le cours pour obtenir les chemins d'images mis à jour
          this.courseService.getCourseById(this.courseId!).subscribe({
            next: (course) => {
              // Mettre à jour les aperçus avec les chemins du serveur
              if (course.thumbnailImage) {
                this.thumbnailPreview = `http://localhost:8090/uploads/${course.thumbnailImage}`;
              }
              if (course.previewVideo) {
                this.videoPreview = `http://localhost:8090/uploads/${course.previewVideo}`;
              }
              
              // Réinitialiser les fichiers car ils sont maintenant sur le serveur
              this.thumbnailFile = null;
              this.previewVideoFile = null;
              
              this.loading = false;
              this.currentStep = 3;
              this.notificationService.success('✅ Médias téléchargés avec succès !');
            },
            error: (error) => {
              console.error('Error reloading course:', error);
              // Continuer quand même même si le rechargement échoue
              this.loading = false;
              this.currentStep = 3;
              this.notificationService.success('✅ Médias téléchargés avec succès !');
            }
          });
        },
        error: (error) => {
          this.loading = false;
          console.error('Error uploading media:', error);
          this.notificationService.error('❌ Erreur lors du téléchargement des médias.');
        }
      });
    } else {
      this.loading = false;
      this.currentStep = 3;
    }
  }

  // Curriculum management methods
  addModule() {
    this.showModuleForm = true;
    this.currentModule = { title: '', description: '', lessons: [] };
  }

  saveModule() {
    if (this.currentModule.title.trim() && this.courseId) {
      this.loading = true;
      const moduleRequest: ModuleRequest = {
        title: this.currentModule.title,
        description: this.currentModule.description,
        courseId: this.courseId
      };

      this.moduleService.createModule(moduleRequest).subscribe({
        next: (module) => {
          // Ensure lessons array is initialized
          const moduleWithLessons = {
            ...module,
            lessons: module.lessons || []
          };
          this.modules.push(moduleWithLessons);
          this.currentModule = { title: '', description: '', lessons: [] };
          this.showModuleForm = false;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error saving module:', error);
          this.loading = false;
        }
      });
    }
  }

  cancelModule() {
    this.showModuleForm = false;
    this.currentModule = { title: '', description: '', lessons: [] };
  }

  deleteModule(index: number) {
    const module = this.modules[index];
    if (module.id) {
      this.loading = true;
      this.moduleService.deleteModule(module.id).subscribe({
        next: () => {
          this.modules.splice(index, 1);
          this.loading = false;
        },
        error: (error) => {
          console.error('Error deleting module:', error);
          this.loading = false;
        }
      });
    } else {
      this.modules.splice(index, 1);
    }
  }

  addLesson(moduleIndex: number) {
    this.editingLessonId = null;
    this.showLessonForm = true;
    this.lessonForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      duration: [0, [Validators.min(0)]]
    });
    this.currentLesson = { 
      title: '', 
      description: '', 
      videoFile: null, 
      duration: 0,
      moduleIndex: moduleIndex
    };
    this.lessonVideoPreview = null;
    console.log('addLesson - currentLesson initialized:', this.currentLesson);
  }

  editLesson(moduleIndex: number, lessonIndex: number, lesson: any) {
    this.editingLessonId = lesson.id;
    this.showLessonForm = true;
    this.lessonForm = this.fb.group({
      title: [lesson.title || '', Validators.required],
      description: [lesson.description || ''],
      duration: [lesson.videoDuration ? Math.floor(lesson.videoDuration / 60) : 0, [Validators.min(0)]]
    });
    this.currentLesson = { 
      title: lesson.title, 
      description: lesson.description, 
      videoFile: null, 
      duration: lesson.videoDuration ? Math.floor(lesson.videoDuration / 60) : 0,
      moduleIndex: moduleIndex,
      lessonIndex: lessonIndex
    };
    
    // Load video preview if video exists
    if (lesson.videoFileName) {
      this.lessonVideoPreview = this.getLessonVideoUrl(lesson);
    } else {
      this.lessonVideoPreview = null;
    }
  }

  getLessonVideoUrl(lesson: any): string {
    if (!lesson) return '';
    const baseUrl = 'http://localhost:8090/api/';
    
    if (lesson.videoFileName) {
      return baseUrl + 'lessons/videos/' + lesson.videoFileName;
    }
    
    if (lesson.videoUrl) {
      if (lesson.videoUrl.startsWith('http')) {
        return lesson.videoUrl;
      }
      if (lesson.videoUrl.includes('/')) {
        const filename = lesson.videoUrl.substring(lesson.videoUrl.lastIndexOf('/') + 1);
        return baseUrl + 'lessons/videos/' + filename;
      }
      return baseUrl + lesson.videoUrl;
    }
    
    return '';
  }

  saveLesson() {
    console.log('saveLesson called');
    
    if (!this.lessonForm) {
      console.error('Lesson form not initialized');
      return;
    }
    
    console.log('lessonForm values:', this.lessonForm.value);
    console.log('lessonForm valid:', this.lessonForm.valid);
    console.log('currentLesson moduleIndex:', this.currentLesson.moduleIndex);
    console.log('modules:', this.modules);
    
    if (this.lessonForm.valid && this.currentLesson.moduleIndex !== undefined) {
      const moduleIndex = this.currentLesson.moduleIndex;
      const module = this.modules[moduleIndex];
      const formValues = this.lessonForm.value;
      
      console.log('moduleIndex:', moduleIndex);
      console.log('module:', module);
      console.log('formValues:', formValues);
      
      if (module && module.id && formValues.title && formValues.title.trim()) {
        this.loading = true;
        
        // Ensure lessons array is initialized
        if (!module.lessons) {
          module.lessons = [];
        }
        
        // Convert minutes to seconds for backend (backend stores duration in seconds)
        const durationInMinutes = formValues.duration || 0;
        const durationInSeconds = durationInMinutes * 60;
        
        const lessonRequest: LessonRequest = {
          title: formValues.title.trim(),
          description: formValues.description || '',
          moduleId: module.id,
          videoDuration: durationInSeconds, // Backend expects seconds
          type: 'VIDEO'
        };

        console.log('Creating lesson with request:', lessonRequest);
        console.log('Video file:', this.currentLesson.videoFile);

        // Use the new video upload endpoint if there's a video file
        // Check if we're editing or creating
        if (this.editingLessonId) {
          // Update existing lesson
          const updateObservable = this.currentLesson.videoFile 
            ? this.lessonService.updateLessonWithVideo(this.editingLessonId, lessonRequest, this.currentLesson.videoFile)
            : this.lessonService.updateLesson(this.editingLessonId, lessonRequest);
          
          updateObservable.subscribe({
            next: (lesson) => {
              console.log('Lesson updated successfully:', lesson);
              // Reload modules to get updated lesson data
              this.loadModules();
              this.editingLessonId = null;
              this.currentLesson = { title: '', description: '', videoFile: null, duration: 0 };
              this.lessonVideoPreview = null;
              this.showLessonForm = false;
              this.loading = false;
              this.showSuccessMessage('Leçon modifiée avec succès !');
              this.lessonForm.reset();
            },
            error: (error) => {
              console.error('Error updating lesson:', error);
              this.loading = false;
              this.showErrorMessage('Erreur lors de la modification de la leçon. Veuillez réessayer.');
            }
          });
        } else {
          // Create new lesson
          const lessonObservable = this.currentLesson.videoFile 
            ? this.lessonService.createLessonWithVideo(lessonRequest, this.currentLesson.videoFile)
            : this.lessonService.createLesson(lessonRequest);

          lessonObservable.subscribe({
            next: (lesson) => {
              console.log('Lesson created successfully:', lesson);
              // Ensure lessons array exists
              if (!this.modules[moduleIndex].lessons) {
                this.modules[moduleIndex].lessons = [];
              }
              this.modules[moduleIndex].lessons.push(lesson);
              this.currentLesson = { title: '', description: '', videoFile: null, duration: 0 };
              this.lessonVideoPreview = null; // Clear video preview
              this.showLessonForm = false;
              this.loading = false;
              
              // Show success message
              this.showSuccessMessage('Leçon créée avec succès !');
              
              // Reset the lesson form
              this.lessonForm.reset();
            
            // Force change detection to update the button state
            setTimeout(() => {
              console.log('Modules after lesson save:', this.modules);
              console.log('Step 3 valid:', this.isStep3Valid());
            }, 0);
          },
          error: (error) => {
            console.error('Error saving lesson:', error);
            this.loading = false;
            this.showErrorMessage('Erreur lors de la création de la leçon. Veuillez réessayer.');
          }
        });
        }
      } else {
        console.error('Validation failed - module:', module, 'title:', formValues?.title);
      }
    } else {
      console.error('Form validation failed - valid:', this.lessonForm?.valid, 'moduleIndex:', this.currentLesson.moduleIndex);
    }
  }

  cancelLesson() {
    this.showLessonForm = false;
    this.editingLessonId = null;
    this.currentLesson = { title: '', description: '', videoFile: null, duration: 0 };
    this.lessonVideoPreview = null;
  }

  deleteLesson(moduleIndex: number, lessonIndex: number) {
    const module = this.modules[moduleIndex];
    const lessons = module?.lessons || [];
    
    if (lessons.length > lessonIndex) {
      const lesson = lessons[lessonIndex];
      if (lesson?.id) {
        this.loading = true;
        this.lessonService.deleteLesson(lesson.id).subscribe({
          next: () => {
            // Ensure lessons array exists
            if (this.modules[moduleIndex] && this.modules[moduleIndex].lessons) {
              this.modules[moduleIndex].lessons.splice(lessonIndex, 1);
            }
            this.loading = false;
          },
          error: (error) => {
            console.error('Error deleting lesson:', error);
            this.loading = false;
          }
        });
      } else {
        if (this.modules[moduleIndex] && this.modules[moduleIndex].lessons) {
          this.modules[moduleIndex].lessons.splice(lessonIndex, 1);
        }
      }
    }
  }

  onLessonVideoSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.type.startsWith('video/')) {
      this.currentLesson.videoFile = file;
      console.log('Video file selected:', file.name, 'Size:', file.size, 'Type:', file.type);
      
      // Create preview URL for the video
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.lessonVideoPreview = e.target.result;
      };
      reader.readAsDataURL(file);
      
      // Try to extract duration from video file (client-side estimation)
      const video = document.createElement('video');
      video.preload = 'metadata';
      video.onloadedmetadata = () => {
        window.URL.revokeObjectURL(video.src);
        const durationInSeconds = Math.floor(video.duration);
        const durationInMinutes = Math.floor(durationInSeconds / 60);
        console.log('Video duration detected:', durationInSeconds, 'seconds (', durationInMinutes, 'minutes)');
        
        // Auto-fill duration if not already set or is 0
        if (this.lessonForm) {
          const currentDuration = this.lessonForm.get('duration')?.value || 0;
          if (currentDuration === 0 || !currentDuration) {
            this.lessonForm.patchValue({ duration: durationInMinutes || 1 });
            console.log('Auto-filled duration:', durationInMinutes, 'minutes');
          }
        }
      };
      video.onerror = () => {
        console.warn('Could not extract video duration automatically');
      };
      video.src = URL.createObjectURL(file);
    } else if (file) {
      this.notificationService.warning('⚠️ Veuillez sélectionner un fichier vidéo valide');
      event.target.value = ''; // Clear invalid file selection
      this.lessonVideoPreview = null;
    }
  }

  showSuccessMessage(message: string): void {
    this.notificationService.success(message);
  }

  showErrorMessage(message: string): void {
    this.notificationService.error(message);
  }

  proceedToPricing() {
    this.currentStep = 4;
  }

  updatePricing() {
    if (!this.courseId || !this.isStep4Valid()) return;

    this.loading = true;
    const courseData: CreateCourseRequest = {
      ...this.courseForm.value
    };

    this.courseService.updateCourse(this.courseId, courseData).subscribe({
      next: (course) => {
        this.loading = false;
        this.currentStep = 5;
      },
      error: (error) => {
        this.loading = false;
        console.error('Error updating course:', error);
      }
    });
  }

  publishCourse() {
    if (!this.courseId) return;

    this.loading = true;
    this.courseService.publishCourse(this.courseId).subscribe({
      next: (course) => {
        this.loading = false;
        this.router.navigate(['/courses']);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error publishing course:', error);
      }
    });
  }

  saveDraft() {
    if (!this.courseId) return;

    this.loading = true;
    const courseData: CreateCourseRequest = {
      ...this.courseForm.value
    };

    this.courseService.updateCourse(this.courseId, courseData).subscribe({
      next: (course) => {
        this.loading = false;
        this.router.navigate(['/courses']);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error saving draft:', error);
      }
    });
  }

  getStepStatus(stepNumber: number): string {
    if (stepNumber < this.currentStep) return 'completed';
    if (stepNumber === this.currentStep) return 'current';
    return 'upcoming';
  }

  getTotalLessons(): number {
    return this.modules.reduce((total, module) => {
      return total + (module?.lessons ? module?.lessons.length : 0);
    }, 0);
  }
}
