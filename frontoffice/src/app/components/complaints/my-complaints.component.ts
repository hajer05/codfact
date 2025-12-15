import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ComplaintService } from '../../services/complaint.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-my-complaints',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './my-complaints.component.html',
  styleUrls: ['./my-complaints.component.scss']
})
export class MyComplaintsComponent implements OnInit {
  complaints: any[] = [];
  loading = false;
  error = '';

  constructor(
    private complaintService: ComplaintService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.loadComplaints();
  }

  loadComplaints() {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      this.error = 'Please log in to view your complaints';
      return;
    }

    this.loading = true;
    this.error = '';
    
    this.complaintService.getStudentComplaints(currentUser.id).subscribe({
      next: (complaints) => {
        this.complaints = complaints || [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading complaints:', error);
        this.error = 'Failed to load complaints';
        this.loading = false;
      }
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'REVIEWED':
        return 'bg-blue-100 text-blue-800';
      case 'RESOLVED':
        return 'bg-green-100 text-green-800';
      case 'CLOSED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'Pending';
      case 'REVIEWED':
        return 'Reviewed';
      case 'RESOLVED':
        return 'Resolved';
      case 'CLOSED':
        return 'Closed';
      default:
        return status;
    }
  }
}

