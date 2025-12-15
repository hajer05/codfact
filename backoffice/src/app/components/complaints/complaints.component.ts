import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComplaintService } from '../../services/complaint.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-complaints',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './complaints.component.html',
  styleUrls: ['./complaints.component.scss']
})
export class ComplaintsComponent implements OnInit {
  complaints: any[] = [];
  loading = false;
  error = '';
  currentUser: any = null;
  isAdmin = false;
  
  // For responding to complaints
  selectedComplaint: any = null;
  responseText = '';
  responding = false;

  constructor(
    private complaintService: ComplaintService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.isAdmin = user.roles?.some((r: any) => r.name === 'ADMIN' || r === 'ADMIN');
        this.loadComplaints();
      }
    });
  }

  loadComplaints() {
    if (!this.currentUser) return;

    this.loading = true;
    this.error = '';

    // If admin, try to get all complaints, otherwise get teacher complaints
    if (this.isAdmin) {
      // For now, we'll use teacher endpoint with admin's ID, or create a workaround
      // You might need to add an /all endpoint in the backend
      this.complaintService.getTeacherComplaints(this.currentUser.id).subscribe({
        next: (complaints) => {
          this.complaints = complaints || [];
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading complaints:', error);
          // Try alternative approach for admin
          this.loadAllComplaints();
        }
      });
    } else {
      // Teacher - get their complaints
      this.complaintService.getTeacherComplaints(this.currentUser.id).subscribe({
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
  }

  loadAllComplaints() {
    // Try to load from each course the user has
    // This is a workaround - ideally backend should have /all endpoint
    this.complaintService.getAllComplaints().subscribe({
      next: (complaints) => {
        this.complaints = complaints || [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading all complaints:', error);
        this.error = 'Failed to load complaints';
        this.loading = false;
      }
    });
  }

  openResponseModal(complaint: any) {
    this.selectedComplaint = complaint;
    this.responseText = complaint.response || '';
  }

  closeResponseModal() {
    this.selectedComplaint = null;
    this.responseText = '';
  }

  submitResponse() {
    if (!this.selectedComplaint || !this.responseText.trim()) {
      return;
    }

    this.responding = true;
    this.complaintService.respondToComplaint(this.selectedComplaint.id, this.responseText.trim()).subscribe({
      next: () => {
        this.responding = false;
        this.closeResponseModal();
        this.loadComplaints();
        alert('Response submitted successfully!');
      },
      error: (error) => {
        console.error('Error submitting response:', error);
        this.responding = false;
        alert('Failed to submit response');
      }
    });
  }

  updateStatus(complaintId: number, status: string) {
    this.complaintService.updateComplaintStatus(complaintId, status).subscribe({
      next: () => {
        this.loadComplaints();
        alert('Status updated successfully!');
      },
      error: (error) => {
        console.error('Error updating status:', error);
        alert('Failed to update status');
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

  deleteComplaint(complaint: any) {
    const confirmMessage = 'Êtes-vous sûr de vouloir supprimer cette réclamation ?\n\n' +
                           `Cours: ${complaint.course?.title}\n` +
                           `Sujet: ${complaint.subject}\n\n` +
                           'Cette action est IRRÉVERSIBLE !';
    
    if (confirm(confirmMessage)) {
      this.complaintService.deleteComplaint(complaint.id).subscribe({
        next: () => {
          this.loadComplaints();
          alert('Réclamation supprimée avec succès !');
        },
        error: (error) => {
          console.error('Error deleting complaint:', error);
          const errorMsg = error?.error?.message || error?.message || 'Erreur inconnue';
          alert('Échec de la suppression : ' + errorMsg);
        }
      });
    }
  }

  canDeleteComplaint(complaint: any): boolean {
    // Admin can delete any complaint
    // Teacher can delete complaints for their courses
    if (this.isAdmin) return true;
    if (!this.currentUser) return false;
    return complaint.teacher?.id === this.currentUser.id;
  }
}

