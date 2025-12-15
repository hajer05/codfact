import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PFEService, Application } from '../../../services/pfe.service';
import { DocumentService, DocumentDto } from '../../../services/document.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-pfe-workflow',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './pfe-workflow.component.html',
  styleUrls: ['./pfe-workflow.component.scss']
})
export class PFEWorkflowComponent implements OnInit {
  application?: Application;
  documents: DocumentDto[] = [];
  loading = false;
  uploading = false;
  
  // Upload form
  selectedFile?: File;
  description = '';
  
  // Review form
  showReviewModal = false;
  reviewComment = '';
  reviewType: 'application' | 'project' | 'report' | 'jury' | null = null;
  juryDate = '';
  juryLocation = '';
  submittingReview = false;
  
  workflowSteps = [
    { key: 'APPLICATION_REVIEW', label: 'Application Review', description: 'Consultant reviewing your application' },
    { key: 'PROJECT_UPLOAD', label: 'Project Upload', description: 'Upload your project as RAR file' },
    { key: 'PROJECT_REVIEW', label: 'Project Review', description: 'Consultant reviewing your project' },
    { key: 'REPORT_UPLOAD', label: 'Report Upload', description: 'Upload your final report (PDF/DOCX)' },
    { key: 'REPORT_REVIEW', label: 'Report Review', description: 'Consultant reviewing your report' },
    { key: 'JURY_SCHEDULING', label: 'Jury Scheduling', description: 'Consultant scheduling jury date' },
    { key: 'COMPLETED', label: 'Completed', description: 'Congratulations! Your PFE is complete' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pfeService: PFEService,
    private documentService: DocumentService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        const applicationId = +params['id'];
        this.loadApplication(applicationId);
      }
    });
  }

  loadApplication(applicationId: number) {
    this.loading = true;
    // You'll need to add this method to PFEService
    this.pfeService.getApplicationById(applicationId).subscribe({
      next: (application) => {
        this.application = application;
        this.loadDocuments();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading application:', error);
        this.loading = false;
        this.router.navigate(['/pfe/my-applications']);
      }
    });
  }

  loadDocuments() {
    if (!this.application) return;
    
    this.documentService.getSubjectDocuments(this.application.subjectId).subscribe({
      next: (documents) => {
        this.documents = documents;
      },
      error: (error) => {
        console.error('Error loading documents:', error);
      }
    });
  }

  getCurrentStepIndex(): number {
    if (!this.application) return 0;
    return this.workflowSteps.findIndex(step => step.key === this.application!.workflowStep);
  }

  isStepCompleted(stepIndex: number): boolean {
    return stepIndex < this.getCurrentStepIndex();
  }

  isCurrentStep(stepIndex: number): boolean {
    return stepIndex === this.getCurrentStepIndex();
  }

  canUploadFile(): boolean {
    if (!this.application) return false;
    return this.application.workflowStep === 'PROJECT_UPLOAD' || 
           this.application.workflowStep === 'REPORT_UPLOAD';
  }

  getRequiredFileType(): string {
    if (!this.application) return '';
    if (this.application.workflowStep === 'PROJECT_UPLOAD') return 'RAR';
    if (this.application.workflowStep === 'REPORT_UPLOAD') return 'PDF/DOCX';
    return '';
  }

  getDocumentType(): string {
    if (!this.application) return '';
    if (this.application.workflowStep === 'PROJECT_UPLOAD') return 'PROJECT';
    if (this.application.workflowStep === 'REPORT_UPLOAD') return 'REPORT';
    return '';
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.validateFile(file);
    }
  }

  validateFile(file: File): boolean {
    const fileType = this.getRequiredFileType();
    const fileName = file.name.toLowerCase();
    
    if (fileType === 'RAR' && !fileName.endsWith('.rar')) {
      alert('Please upload a RAR file for your project');
      return false;
    }
    
    if (fileType === 'PDF/DOCX' && 
        !fileName.endsWith('.pdf') && 
        !fileName.endsWith('.doc') && 
        !fileName.endsWith('.docx')) {
      alert('Please upload a PDF or DOCX file for your report');
      return false;
    }
    
    // File size validation
    const maxSize = fileType === 'RAR' ? 50 * 1024 * 1024 : 10 * 1024 * 1024; // 50MB for RAR, 10MB for reports
    if (file.size > maxSize) {
      const maxSizeMB = maxSize / (1024 * 1024);
      alert(`File size must be less than ${maxSizeMB}MB`);
      return false;
    }
    
    return true;
  }

  uploadFile() {
    if (!this.selectedFile || !this.application) return;
    
    if (!this.validateFile(this.selectedFile)) return;
    
    this.uploading = true;
    const documentType = this.getDocumentType();
    
    this.documentService.uploadDocument(
      this.application.subjectId,
      this.selectedFile,
      documentType,
      this.description
    ).subscribe({
      next: (document) => {
        this.documents.unshift(document);
        this.resetUploadForm();
        this.uploading = false;
        
        // Show success message
        const docTypeName = documentType === 'PROJECT' ? 'Project' : 'Report';
        alert(`${docTypeName} uploaded successfully! Your consultant will review it soon.`);
        
        // Refresh application to get updated workflow step
        this.loadApplication(this.application!.id);
      },
      error: (error) => {
        console.error('Error uploading document:', error);
        const errorMessage = error.error?.message || error.error || 'Unknown error';
        alert(`Error uploading document: ${errorMessage}`);
        this.uploading = false;
      }
    });
  }

  resetUploadForm() {
    this.selectedFile = undefined;
    this.description = '';
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  downloadDocument(document: DocumentDto) {
    this.documentService.downloadDocument(document.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = window.document.createElement('a');
        link.href = url;
        link.download = document.originalFileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading document:', error);
        alert('Error downloading document. Please try again.');
      }
    });
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  getStatusIcon(stepIndex: number): string {
    if (this.isStepCompleted(stepIndex)) return 'fas fa-check-circle text-success';
    if (this.isCurrentStep(stepIndex)) return 'fas fa-clock text-warning';
    return 'fas fa-circle text-muted';
  }

  getJuryDateFormatted(): string {
    if (!this.application?.juryDate) return '';
    return new Date(this.application.juryDate).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  downloadCV() {
    if (!this.application) return;
    
    this.pfeService.downloadCV(this.application.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = this.application!.cvOriginalFileName || 'cv.pdf';
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading CV:', error);
        alert('Error downloading CV. Please try again.');
      }
    });
  }

  getProjectDocuments(): DocumentDto[] {
    return this.documents.filter(doc => doc.documentType === 'PROJECT');
  }

  getReportDocuments(): DocumentDto[] {
    return this.documents.filter(doc => doc.documentType === 'REPORT');
  }

  // Admin/Consultant methods
  isConsultant(): boolean {
    return this.authService.hasAnyRole(['CONSULTANT', 'ADMIN']);
  }

  isMySubject(): boolean {
    if (!this.application) return false;
    const currentUser = this.authService.getCurrentUser();
    return currentUser?.id === this.application.subjectId;
  }

  canReview(): boolean {
    return this.isConsultant();
  }

  openReviewModal(type: 'application' | 'project' | 'report' | 'jury') {
    this.reviewType = type;
    this.showReviewModal = true;
    this.reviewComment = '';
    if (type === 'jury') {
      this.juryDate = '';
      this.juryLocation = '';
    }
  }

  closeReviewModal() {
    this.showReviewModal = false;
    this.reviewType = null;
    this.reviewComment = '';
    this.juryDate = '';
    this.juryLocation = '';
  }

  submitReview(approved: boolean) {
    if (!this.application || !this.reviewType) return;

    this.submittingReview = true;

    let reviewObservable;
    
    switch (this.reviewType) {
      case 'application':
        reviewObservable = this.pfeService.reviewApplication(
          this.application.id,
          approved,
          this.reviewComment
        );
        break;
      case 'project':
        reviewObservable = this.pfeService.reviewProject(
          this.application.id,
          approved,
          this.reviewComment
        );
        break;
      case 'report':
        reviewObservable = this.pfeService.reviewReport(
          this.application.id,
          approved,
          this.reviewComment
        );
        break;
      case 'jury':
        if (!this.juryDate || !this.juryLocation) {
          alert('Please provide both jury date and location');
          this.submittingReview = false;
          return;
        }
        reviewObservable = this.pfeService.scheduleJury(
          this.application.id,
          this.juryDate,
          this.juryLocation
        );
        break;
      default:
        this.submittingReview = false;
        return;
    }

    reviewObservable.subscribe({
      next: (updatedApplication) => {
        this.application = updatedApplication;
        this.submittingReview = false;
        this.closeReviewModal();
        const action = approved ? 'approved' : 'rejected';
        alert(`Successfully ${action} the ${this.reviewType}!`);
        this.loadApplication(this.application.id);
      },
      error: (error) => {
        console.error('Error submitting review:', error);
        alert('Error submitting review. Please try again.');
        this.submittingReview = false;
      }
    });
  }

  needsReview(step: string): boolean {
    if (!this.canReview() || !this.application) return false;
    
    switch (step) {
      case 'APPLICATION_REVIEW':
        return this.application.workflowStep === 'APPLICATION_REVIEW' && 
               this.application.status === 'PENDING';
      case 'PROJECT_REVIEW':
        return this.application.workflowStep === 'PROJECT_REVIEW' && 
               this.getProjectDocuments().length > 0;
      case 'REPORT_REVIEW':
        return this.application.workflowStep === 'REPORT_REVIEW' && 
               this.getReportDocuments().length > 0;
      case 'JURY_SCHEDULING':
        return this.application.workflowStep === 'JURY_SCHEDULING' && 
               !this.application.juryDate;
      default:
        return false;
    }
  }
}
