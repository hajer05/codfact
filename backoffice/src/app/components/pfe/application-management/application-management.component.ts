import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PFEService, Application, ReviewApplicationRequest } from '../../../services/pfe.service';
import { DocumentService, DocumentDto } from '../../../services/document.service';
import { NotificationService } from '../../../services/notification.service';
import { LoadingSpinnerComponent } from '../../shared/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-application-management',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, LoadingSpinnerComponent],
  templateUrl: './application-management.component.html',
  styleUrls: ['./application-management.component.scss']
})
export class ApplicationManagementComponent implements OnInit {
  applications: Application[] = [];
  subjectId?: number;
  subjectTitle = '';
  loading = false;
  reviewingApplicationId: number | null = null;
  reviewComment = '';

  // Workflow management properties
  reviewingWorkflowId: number | null = null;
  reviewType: 'project' | 'report' | 'jury' | null = null;
  workflowComment = '';
  juryDate = '';
  juryLocation = '';

  // Documents for each application
  applicationDocuments: { [applicationId: number]: DocumentDto[] } = {};

  // Confirmation dialog
  showDeleteDialog = false;
  deleteApplicationId: number | null = null;

  constructor(
    private pfeService: PFEService,
    private documentService: DocumentService,
    private route: ActivatedRoute,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.subjectId = +params['id'];
        this.loadApplications();
      }
    });
  }

  loadApplications() {
    if (!this.subjectId) return;

    this.loading = true;
    this.pfeService.getSubjectApplications(this.subjectId).subscribe({
      next: (applications) => {
        this.applications = applications;
        if (applications.length > 0) {
          this.subjectTitle = applications[0].subjectTitle;
          // Load documents for each application
          applications.forEach(app => {
            this.loadDocumentsForApplication(app);
          });
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading applications:', error);
        this.notificationService.error('Failed to load applications. Please try again.');
        this.loading = false;
      }
    });
  }

  loadDocumentsForApplication(application: Application) {
    this.documentService.getSubjectDocuments(application.subjectId).subscribe({
      next: (documents: DocumentDto[]) => {
        this.applicationDocuments[application.id] = documents;
      },
      error: (error: any) => {
        console.error('Error loading documents:', error);
      }
    });
  }

  reviewApplication(applicationId: number, status: 'ACCEPTED' | 'REJECTED') {
    const request: ReviewApplicationRequest = {
      status: status,
      reviewComment: this.reviewComment
    };

    this.pfeService.reviewApplication(applicationId, request).subscribe({
      next: (updatedApplication) => {
        const index = this.applications.findIndex(app => app.id === applicationId);
        if (index !== -1) {
          this.applications[index] = updatedApplication;
        }
        this.reviewingApplicationId = null;
        this.reviewComment = '';
        this.notificationService.success(`Application ${status === 'ACCEPTED' ? 'accepted' : 'rejected'} successfully!`);
      },
      error: (error: any) => {
        console.error('Error reviewing application:', error);
        this.notificationService.error('Failed to review application. Please try again.');
        this.reviewingApplicationId = null;
      }
    });
  }

  startReview(applicationId: number) {
    this.reviewingApplicationId = applicationId;
    this.reviewComment = '';
  }

  cancelReview() {
    this.reviewingApplicationId = null;
    this.reviewComment = '';
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'status-pending';
      case 'ACCEPTED': return 'status-accepted';
      case 'REJECTED': return 'status-rejected';
      default: return '';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'PENDING': return 'Pending';
      case 'ACCEPTED': return 'Accepted';
      case 'REJECTED': return 'Rejected';
      default: return status;
    }
  }

  // Workflow management methods
  getWorkflowStepText(step: string): string {
    const stepTexts: { [key: string]: string } = {
      'APPLICATION_REVIEW': 'Application Review',
      'PROJECT_UPLOAD': 'Waiting for Project',
      'PROJECT_REVIEW': 'Project Review Required',
      'REPORT_UPLOAD': 'Waiting for Report',
      'REPORT_REVIEW': 'Report Review Required',
      'JURY_SCHEDULING': 'Jury Scheduling Required',
      'COMPLETED': 'Completed'
    };
    return stepTexts[step] || step;
  }

  startWorkflowReview(applicationId: number, type: 'project' | 'report' | 'jury') {
    this.reviewingWorkflowId = applicationId;
    this.reviewType = type;
    this.workflowComment = '';
    this.juryDate = '';
    this.juryLocation = '';
  }

  cancelWorkflowReview() {
    this.reviewingWorkflowId = null;
    this.reviewType = null;
    this.workflowComment = '';
    this.juryDate = '';
    this.juryLocation = '';
  }

  submitWorkflowReview(applicationId: number, type: 'project' | 'report', approved: boolean) {
    if (type === 'project') {
      this.pfeService.reviewProject(applicationId, {
        approved: approved,
        comment: this.workflowComment
      }).subscribe({
        next: () => {
          this.loadApplications();
          this.cancelWorkflowReview();
          this.notificationService.success(`Project ${approved ? 'approved' : 'rejected'} successfully!`);
        },
        error: (error: any) => {
          console.error('Error reviewing project:', error);
          this.notificationService.error('Failed to review project. Please try again.');
        }
      });
    } else if (type === 'report') {
      this.pfeService.reviewReport(applicationId, {
        approved: approved,
        comment: this.workflowComment
      }).subscribe({
        next: () => {
          this.loadApplications();
          this.cancelWorkflowReview();
          this.notificationService.success(`Report ${approved ? 'approved' : 'rejected'} successfully!`);
        },
        error: (error: any) => {
          console.error('Error reviewing report:', error);
          this.notificationService.error('Failed to review report. Please try again.');
        }
      });
    }
  }

  submitJuryScheduling(applicationId: number) {
    if (!this.juryDate || !this.juryLocation) {
      this.notificationService.warning('Please fill in both jury date and location.');
      return;
    }

    this.pfeService.scheduleJury(applicationId, {
      juryDate: this.juryDate,
      juryLocation: this.juryLocation,
      comment: this.workflowComment
    }).subscribe({
      next: () => {
        this.loadApplications();
        this.cancelWorkflowReview();
        this.notificationService.success('Jury scheduled successfully!');
      },
      error: (error: any) => {
        console.error('Error scheduling jury:', error);
        this.notificationService.error('Failed to schedule jury. Please try again.');
      }
    });
  }

  isStepCompleted(step: string, currentStep: string): boolean {
    const stepOrder = [
      'APPLICATION_REVIEW',
      'PROJECT_UPLOAD',
      'PROJECT_REVIEW',
      'REPORT_UPLOAD',
      'REPORT_REVIEW',
      'JURY_SCHEDULING',
      'COMPLETED'
    ];
    
    const stepIndex = stepOrder.indexOf(step);
    const currentIndex = stepOrder.indexOf(currentStep);
    
    return stepIndex < currentIndex || (stepIndex === currentIndex && currentStep !== 'APPLICATION_REVIEW');
  }

  // CV and Document download methods
  downloadCV(applicationId: number, fileName: string) {
    this.pfeService.downloadCV(applicationId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName || 'cv.pdf';
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Error downloading CV:', error);
        this.notificationService.error('Failed to download CV. Please try again.');
      }
    });
  }

  downloadDocument(documentId: number, fileName: string) {
    this.documentService.downloadDocument(documentId).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error: any) => {
        console.error('Error downloading document:', error);
        this.notificationService.error('Failed to download document. Please try again.');
      }
    });
  }

  getProjectDocuments(applicationId: number): DocumentDto[] {
    return (this.applicationDocuments[applicationId] || []).filter(doc => doc.documentType === 'PROJECT');
  }

  getReportDocuments(applicationId: number): DocumentDto[] {
    return (this.applicationDocuments[applicationId] || []).filter(doc => doc.documentType === 'REPORT');
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
