import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PFEService, Application, PFESubject } from '../../../services/pfe.service';
import { DocumentService, DocumentDto } from '../../../services/document.service';

@Component({
  selector: 'app-pfe-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './pfe-admin-dashboard.component.html',
  styleUrls: ['./pfe-admin-dashboard.component.scss']
})
export class PFEAdminDashboardComponent implements OnInit {
  mySubjects: PFESubject[] = [];
  allApplications: Application[] = [];
  loading = false;
  selectedSubject: number | null = null;
  searchTerm = '';

  constructor(
    private pfeService: PFEService,
    private documentService: DocumentService
  ) {}

  ngOnInit() {
    this.loadMySubjects();
    this.loadConsultantApplications();
  }

  loadMySubjects() {
    this.pfeService.getMySubjects().subscribe({
      next: (subjects) => {
        this.mySubjects = subjects;
      },
      error: (error) => {
        console.error('Error loading subjects:', error);
      }
    });
  }

  loadConsultantApplications() {
    this.loading = true;
    this.pfeService.getConsultantApplications().subscribe({
      next: (applications) => {
        this.allApplications = applications;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading applications:', error);
        this.loading = false;
      }
    });
  }

  get filteredApplications(): Application[] {
    let filtered = this.allApplications;

    // Filter by selected subject
    if (this.selectedSubject) {
      filtered = filtered.filter(app => app.subjectId === this.selectedSubject);
    }

    // Filter by search term
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(app =>
        app.studentName.toLowerCase().includes(term) ||
        app.subjectTitle.toLowerCase().includes(term) ||
        app.studentEmail.toLowerCase().includes(term)
      );
    }

    return filtered;
  }

  getWorkflowStepLabel(step: string): string {
    const labels: { [key: string]: string } = {
      'APPLICATION_REVIEW': 'Application Review',
      'PROJECT_UPLOAD': 'Waiting for Project',
      'PROJECT_REVIEW': 'Project Review',
      'REPORT_UPLOAD': 'Waiting for Report',
      'REPORT_REVIEW': 'Report Review',
      'JURY_SCHEDULING': 'Jury Scheduling',
      'COMPLETED': 'Completed'
    };
    return labels[step] || step;
  }

  getWorkflowStepClass(step: string): string {
    const classes: { [key: string]: string } = {
      'APPLICATION_REVIEW': 'step-pending',
      'PROJECT_UPLOAD': 'step-waiting',
      'PROJECT_REVIEW': 'step-review',
      'REPORT_UPLOAD': 'step-waiting',
      'REPORT_REVIEW': 'step-review',
      'JURY_SCHEDULING': 'step-scheduling',
      'COMPLETED': 'step-completed'
    };
    return classes[step] || 'step-pending';
  }

  getWorkflowProgress(step: string): number {
    const progress: { [key: string]: number } = {
      'APPLICATION_REVIEW': 14,
      'PROJECT_UPLOAD': 28,
      'PROJECT_REVIEW': 42,
      'REPORT_UPLOAD': 57,
      'REPORT_REVIEW': 71,
      'JURY_SCHEDULING': 85,
      'COMPLETED': 100
    };
    return progress[step] || 0;
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'PENDING': 'status-pending',
      'ACCEPTED': 'status-accepted',
      'REJECTED': 'status-rejected'
    };
    return classes[status] || '';
  }

  getStatistics() {
    const total = this.allApplications.length;
    const pending = this.allApplications.filter(app => app.status === 'PENDING').length;
    const accepted = this.allApplications.filter(app => app.status === 'ACCEPTED').length;
    const rejected = this.allApplications.filter(app => app.status === 'REJECTED').length;
    const completed = this.allApplications.filter(app => app.workflowStep === 'COMPLETED').length;
    const inProgress = this.allApplications.filter(app => 
      app.status === 'ACCEPTED' && app.workflowStep !== 'COMPLETED'
    ).length;

    return { total, pending, accepted, rejected, completed, inProgress };
  }

  downloadCV(applicationId: number, fileName: string) {
    this.pfeService.downloadCV(applicationId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName || 'cv.pdf';
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading CV:', error);
        alert('Error downloading CV');
      }
    });
  }
}
