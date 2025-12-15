import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PFEService, Application } from '../../../services/pfe.service';

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './my-applications.component.html',
  styleUrls: ['./my-applications.component.scss']
})
export class MyApplicationsComponent implements OnInit {
  applications: Application[] = [];
  loading = false;

  constructor(private pfeService: PFEService) {}

  ngOnInit() {
    this.loadMyApplications();
    // Also check if there are any open subjects available
    this.pfeService.getOpenSubjects().subscribe({
      next: (subjects) => {
        console.log('Available PFE subjects:', subjects);
      },
      error: (error) => {
        console.error('Error loading PFE subjects:', error);
      }
    });
  }

  loadMyApplications() {
    this.loading = true;
    console.log('Loading my applications...');
    this.pfeService.getMyApplications().subscribe({
      next: (applications) => {
        console.log('Applications loaded:', applications);
        this.applications = applications;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading applications:', error);
        this.loading = false;
      }
    });
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
      case 'PENDING': return 'Under Review';
      case 'ACCEPTED': return 'Accepted';
      case 'REJECTED': return 'Rejected';
      default: return status;
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'PENDING': return 'fas fa-clock';
      case 'ACCEPTED': return 'fas fa-check-circle';
      case 'REJECTED': return 'fas fa-times-circle';
      default: return 'fas fa-question-circle';
    }
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
