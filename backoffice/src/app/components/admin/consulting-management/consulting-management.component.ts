import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConsultingService, ConsultingRequest, UpdateConsultingRequest } from '../../../services/consulting.service';

@Component({
  selector: 'app-consulting-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './consulting-management.component.html',
  styleUrls: ['./consulting-management.component.scss']
})
export class ConsultingManagementComponent implements OnInit {
  requests: ConsultingRequest[] = [];
  filteredRequests: ConsultingRequest[] = [];
  selectedRequest?: ConsultingRequest;
  loading = false;
  showModal = false;
  
  statusFilter: string = 'ALL';
  searchTerm: string = '';

  updateForm: UpdateConsultingRequest = {
    status: undefined,
    adminNotes: '',
    assignedToId: undefined
  };

  statusOptions = [
    { value: 'PENDING', label: 'En Attente', color: 'yellow' },
    { value: 'IN_PROGRESS', label: 'En Cours', color: 'blue' },
    { value: 'ANSWERED', label: 'Répondu', color: 'green' },
    { value: 'CLOSED', label: 'Fermé', color: 'gray' },
    { value: 'REJECTED', label: 'Refusé', color: 'red' }
  ];

  constructor(private consultingService: ConsultingService) {}

  ngOnInit() {
    this.loadRequests();
  }

  loadRequests() {
    this.loading = true;
    
    if (this.statusFilter === 'ALL') {
      this.consultingService.getAllRequests().subscribe({
        next: (data) => {
          this.requests = data;
          this.filteredRequests = data;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading requests:', error);
          this.loading = false;
        }
      });
    } else {
      this.consultingService.getRequestsByStatus(this.statusFilter).subscribe({
        next: (data) => {
          this.requests = data;
          this.filteredRequests = data;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading requests:', error);
          this.loading = false;
        }
      });
    }
  }

  onStatusFilterChange() {
    this.loadRequests();
  }

  onSearch() {
    if (!this.searchTerm.trim()) {
      this.filteredRequests = this.requests;
      return;
    }

    const search = this.searchTerm.toLowerCase();
    this.filteredRequests = this.requests.filter(request =>
      request.companyName.toLowerCase().includes(search) ||
      request.contactName.toLowerCase().includes(search) ||
      request.email.toLowerCase().includes(search) ||
      request.serviceType.toLowerCase().includes(search)
    );
  }

  openModal(request: ConsultingRequest) {
    this.selectedRequest = request;
    this.updateForm = {
      status: request.status,
      adminNotes: request.adminNotes || '',
      assignedToId: request.assignedToId
    };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedRequest = undefined;
    this.updateForm = {
      status: undefined,
      adminNotes: '',
      assignedToId: undefined
    };
  }

  updateRequest() {
    if (!this.selectedRequest) return;

    this.loading = true;
    
    this.consultingService.updateRequest(this.selectedRequest.id, this.updateForm).subscribe({
      next: (updated) => {
        const index = this.requests.findIndex(r => r.id === updated.id);
        if (index !== -1) {
          this.requests[index] = updated;
        }
        
        this.loadRequests();
        this.closeModal();
        this.loading = false;
        alert('Demande mise à jour avec succès !');
      },
      error: (error) => {
        console.error('Error updating request:', error);
        this.loading = false;
        alert('Erreur lors de la mise à jour');
      }
    });
  }

  getStatusLabel(status: string): string {
    const option = this.statusOptions.find(opt => opt.value === status);
    return option ? option.label : status;
  }

  getStatusColor(status: string): string {
    const option = this.statusOptions.find(opt => opt.value === status);
    const color = option ? option.color : 'gray';
    
    const colors: { [key: string]: string } = {
      yellow: 'bg-yellow-100 text-yellow-800',
      blue: 'bg-blue-100 text-blue-800',
      green: 'bg-green-100 text-green-800',
      red: 'bg-red-100 text-red-800',
      gray: 'bg-gray-100 text-gray-800'
    };
    
    return colors[color] || colors['gray'];
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}

