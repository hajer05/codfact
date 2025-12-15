import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ConsultingService, ConsultingRequest } from '../../../services/consulting.service';

@Component({
  selector: 'app-my-consulting-requests',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './my-consulting-requests.component.html',
  styleUrls: ['./my-consulting-requests.component.scss']
})
export class MyConsultingRequestsComponent implements OnInit {
  requests: ConsultingRequest[] = [];
  loading = false;
  selectedRequest?: ConsultingRequest;
  showDetailsModal = false;

  constructor(private consultingService: ConsultingService) {}

  ngOnInit() {
    this.loadMyRequests();
  }

  loadMyRequests() {
    this.loading = true;
    this.consultingService.getMyRequests().subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading requests:', error);
        this.loading = false;
      }
    });
  }

  openDetails(request: ConsultingRequest) {
    this.selectedRequest = request;
    this.showDetailsModal = true;
  }

  closeDetails() {
    this.showDetailsModal = false;
    this.selectedRequest = undefined;
  }

  getStatusLabel(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'En Attente',
      'IN_PROGRESS': 'En Cours',
      'ANSWERED': 'Répondu',
      'CLOSED': 'Fermé',
      'REJECTED': 'Refusé'
    };
    return statusMap[status] || status;
  }

  getStatusColor(status: string): string {
    const colorMap: { [key: string]: string } = {
      'PENDING': 'bg-yellow-100 text-yellow-800 border-yellow-300',
      'IN_PROGRESS': 'bg-blue-100 text-blue-800 border-blue-300',
      'ANSWERED': 'bg-green-100 text-green-800 border-green-300',
      'CLOSED': 'bg-gray-100 text-gray-800 border-gray-300',
      'REJECTED': 'bg-red-100 text-red-800 border-red-300'
    };
    return colorMap[status] || 'bg-gray-100 text-gray-800';
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}

