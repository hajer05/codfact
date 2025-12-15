import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { PFEService, CreatePFESubjectRequest, PFESubject } from '../../../services/pfe.service';

@Component({
  selector: 'app-pfe-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './pfe-form.component.html',
  styleUrls: ['./pfe-form.component.scss']
})
export class PFEFormComponent implements OnInit {
  pfeForm: FormGroup;
  isEditMode = false;
  subjectId?: number;
  loading = false;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private pfeService: PFEService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.pfeForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(500)]],
      description: ['', [Validators.required]],
      requirements: ['', [Validators.required]]
    });
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.subjectId = +params['id'];
        this.loadSubject();
      }
    });
  }

  loadSubject() {
    if (!this.subjectId) return;
    
    this.loading = true;
    this.pfeService.getSubjectById(this.subjectId).subscribe({
      next: (subject) => {
        this.pfeForm.patchValue({
          title: subject.title,
          description: subject.description,
          requirements: subject.requirements
        });
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading PFE subject:', error);
        this.loading = false;
        this.router.navigate(['/pfe']);
      }
    });
  }

  onSubmit() {
    if (this.pfeForm.valid) {
      this.submitting = true;
      const subjectData: CreatePFESubjectRequest = this.pfeForm.value;

      const operation = this.isEditMode 
        ? this.pfeService.updateSubject(this.subjectId!, subjectData)
        : this.pfeService.createSubject(subjectData);

      operation.subscribe({
        next: (subject) => {
          this.submitting = false;
          this.router.navigate(['/pfe']);
        },
        error: (error) => {
          console.error('Error saving PFE subject:', error);
          this.submitting = false;
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched() {
    Object.keys(this.pfeForm.controls).forEach(key => {
      const control = this.pfeForm.get(key);
      control?.markAsTouched();
    });
  }

  cancel() {
    this.router.navigate(['/pfe']);
  }

  get title() { return this.pfeForm.get('title'); }
  get description() { return this.pfeForm.get('description'); }
  get requirements() { return this.pfeForm.get('requirements'); }
}
