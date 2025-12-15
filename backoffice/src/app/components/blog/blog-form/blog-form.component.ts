import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { BlogService, CreateBlogRequest, Blog } from '../../../services/blog.service';

@Component({
  selector: 'app-blog-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './blog-form.component.html',
  styleUrls: ['./blog-form.component.scss']
})
export class BlogFormComponent implements OnInit {
  blogForm: FormGroup;
  isEditMode = false;
  blogId?: number;
  loading = false;
  submitting = false;
  tagInput = '';
  isValidImageUrl = true;

  constructor(
    private fb: FormBuilder,
    private blogService: BlogService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.blogForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(500)]],
      content: ['', [Validators.required]],
      photo: [''],
      tags: [[]]
    });
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.blogId = +params['id'];
        this.loadBlog();
      }
    });

    // Reset image validation when photo URL changes
    this.blogForm.get('photo')?.valueChanges.subscribe(() => {
      this.isValidImageUrl = true;
    });
  }

  loadBlog() {
    if (!this.blogId) return;
    
    this.loading = true;
    this.blogService.getBlogById(this.blogId).subscribe({
      next: (blog) => {
        this.blogForm.patchValue({
          title: blog.title,
          content: blog.content,
          photo: blog.photo,
          tags: blog.tags
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading blog:', error);
        this.loading = false;
        this.router.navigate(['/blogs']);
      }
    });
  }

  addTag() {
    if (this.tagInput.trim()) {
      const currentTags = this.blogForm.get('tags')?.value || [];
      if (!currentTags.includes(this.tagInput.trim())) {
        this.blogForm.patchValue({
          tags: [...currentTags, this.tagInput.trim()]
        });
      }
      this.tagInput = '';
    }
  }

  removeTag(index: number) {
    const currentTags = this.blogForm.get('tags')?.value || [];
    currentTags.splice(index, 1);
    this.blogForm.patchValue({ tags: currentTags });
  }

  onTagInputKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.addTag();
    }
  }

  onImageError() {
    this.isValidImageUrl = false;
  }

  onSubmit() {
    if (this.blogForm.valid) {
      this.submitting = true;
      const blogData: CreateBlogRequest = this.blogForm.value;

      const operation = this.isEditMode 
        ? this.blogService.updateBlog(this.blogId!, blogData)
        : this.blogService.createBlog(blogData);

      operation.subscribe({
        next: (blog) => {
          this.submitting = false;
          this.router.navigate(['/blogs']);
        },
        error: (error) => {
          console.error('Error saving blog:', error);
          this.submitting = false;
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched() {
    Object.keys(this.blogForm.controls).forEach(key => {
      const control = this.blogForm.get(key);
      control?.markAsTouched();
    });
  }

  cancel() {
    this.router.navigate(['/blogs']);
  }

  get title() { return this.blogForm.get('title'); }
  get content() { return this.blogForm.get('content'); }
  get photo() { return this.blogForm.get('photo'); }
  get tags() { return this.blogForm.get('tags')?.value || []; }
}
