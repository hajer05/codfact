import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BlogService, CreateBlogRequest } from '../../../services/blog.service';

@Component({
  selector: 'app-blog-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './blog-create.component.html',
  styleUrls: ['./blog-create.component.scss']
})
export class BlogCreateComponent implements OnInit {
  blogForm: FormGroup;
  submitting = false;
  tagInput = '';
  previewMode = false;

  constructor(
    private fb: FormBuilder,
    private blogService: BlogService,
    private router: Router
  ) {
    this.blogForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(500)]],
      content: ['', [Validators.required, Validators.minLength(50)]],
      photo: [''],
      tags: [[]]
    });
  }

  ngOnInit() {}

  addTag() {
    if (this.tagInput.trim()) {
      const currentTags = this.blogForm.get('tags')?.value || [];
      const newTag = this.tagInput.trim().toLowerCase();
      
      if (!currentTags.includes(newTag) && currentTags.length < 5) {
        this.blogForm.patchValue({
          tags: [...currentTags, newTag]
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

  togglePreview() {
    this.previewMode = !this.previewMode;
  }

  onSubmit() {
    if (this.blogForm.valid) {
      this.submitting = true;
      const blogData: CreateBlogRequest = this.blogForm.value;

      this.blogService.createBlog(blogData).subscribe({
        next: (blog) => {
          this.submitting = false;
          this.router.navigate(['/blogs', blog.id]);
        },
        error: (error) => {
          console.error('Error creating blog:', error);
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

  getCurrentDate(): Date {
    return new Date();
  }

  get title() { return this.blogForm.get('title'); }
  get content() { return this.blogForm.get('content'); }
  get photo() { return this.blogForm.get('photo'); }
  get tags() { return this.blogForm.get('tags')?.value || []; }
}
