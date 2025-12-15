import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PFEService, PFESubject, CreateApplicationRequest } from '../../../services/pfe.service';

@Component({
  selector: 'app-pfe-apply',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './pfe-apply.component.html',
  styleUrls: ['./pfe-apply.component.scss']
})
export class PFEApplyComponent implements OnInit {
  applicationForm: FormGroup;
  subject?: PFESubject;
  subjectId?: number;
  loading = false;
  submitting = false;
  selectedCVFile?: File;
  cvFileName?: string;

  constructor(
    private fb: FormBuilder,
    private pfeService: PFEService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.applicationForm = this.fb.group({
      motivation: ['', [Validators.required, Validators.minLength(50)]],
      cvFile: [null]
    });
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
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
        this.subject = subject;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading PFE subject:', error);
        this.loading = false;
        this.router.navigate(['/pfe']);
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Validate file type
      if (file.type !== 'application/pdf') {
        alert('Please select a PDF file for your CV');
        event.target.value = '';
        return;
      }
      
      // Validate file size (5MB max)
      if (file.size > 5 * 1024 * 1024) {
        alert('CV file size must be less than 5MB');
        event.target.value = '';
        return;
      }
      
      this.selectedCVFile = file;
      this.cvFileName = file.name;
      this.applicationForm.patchValue({ cvFile: file });
    }
  }

  removeCVFile() {
    this.selectedCVFile = undefined;
    this.cvFileName = undefined;
    this.applicationForm.patchValue({ cvFile: null });
  }

  onSubmit() {
    if (this.applicationForm.valid && this.subjectId) {
      this.submitting = true;
      const motivation = this.applicationForm.get('motivation')?.value;

      this.pfeService.applyToSubject(this.subjectId, motivation, this.selectedCVFile).subscribe({
        next: (application) => {
          this.submitting = false;
          this.router.navigate(['/pfe/my-applications']);
        },
        error: (error) => {
          console.error('Error submitting application:', error);
          alert('Error submitting application: ' + (error.error || 'Unknown error'));
          this.submitting = false;
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched() {
    Object.keys(this.applicationForm.controls).forEach(key => {
      const control = this.applicationForm.get(key);
      control?.markAsTouched();
    });
  }

  cancel() {
    if (this.subjectId) {
      this.router.navigate(['/pfe', this.subjectId]);
    } else {
      this.router.navigate(['/pfe']);
    }
  }

  get motivation() { return this.applicationForm.get('motivation'); }
}
