import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService, Order } from '../../services/order.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  loading = false;
  error: string | null = null;
  selectedOrder: Order | null = null;

  // Filters
  filterStatus = 'ALL';
  filterSearch = '';
  sortBy = 'date';
  sortOrder: 'asc' | 'desc' = 'desc';

  constructor(
    private orderService: OrderService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.loading = true;
    this.error = null;
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders || [];
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading orders:', err);
        this.error = 'Failed to load orders';
        this.loading = false;
        this.notificationService.error('Erreur lors du chargement des commandes');
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.orders];

    // Status filter
    if (this.filterStatus !== 'ALL') {
      filtered = filtered.filter(o => o.status === this.filterStatus);
    }

    // Search filter
    if (this.filterSearch.trim()) {
      const search = this.filterSearch.toLowerCase();
      filtered = filtered.filter(o => 
        o.orderNumber.toLowerCase().includes(search) ||
        (o.userName && o.userName.toLowerCase().includes(search)) ||
        (o.userEmail && o.userEmail.toLowerCase().includes(search))
      );
    }

    // Sort
    filtered.sort((a, b) => {
      let comparison = 0;
      switch (this.sortBy) {
        case 'amount':
          comparison = a.totalAmount - b.totalAmount;
          break;
        case 'status':
          comparison = a.status.localeCompare(b.status);
          break;
        case 'date':
        default:
          const dateA = new Date(a.createdAt).getTime();
          const dateB = new Date(b.createdAt).getTime();
          comparison = dateA - dateB;
          break;
      }
      return this.sortOrder === 'asc' ? comparison : -comparison;
    });

    this.filteredOrders = filtered;
  }

  exportToCSV(): void {
    const headers = ['Order Number', 'User', 'Email', 'Date', 'Amount', 'Status', 'Payment Status', 'Payment Method', 'Stripe ID'];
    const rows = this.filteredOrders.map(order => [
      order.orderNumber,
      order.userName || `User #${order.userId}`,
      order.userEmail || '',
      this.formatDate(order.createdAt),
      `$${order.totalAmount.toFixed(2)}`,
      order.status,
      order.payment?.status || 'N/A',
      order.payment?.method || 'N/A',
      order.payment?.stripePaymentIntentId || 'N/A'
    ]);

    const csvContent = [headers, ...rows]
      .map(row => row.map(cell => `"${cell}"`).join(','))
      .join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `transactions_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    this.notificationService.success('Rapport exporté avec succès!');
  }

  openOrderDetails(order: Order) {
    this.selectedOrder = order;
  }

  closeOrderDetails() {
    this.selectedOrder = null;
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getPaymentStatusBadgeClass(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
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

  getTotalRevenue(): number {
    return this.orders
      .filter(o => o.status === 'COMPLETED')
      .reduce((sum, o) => sum + o.totalAmount, 0);
  }

  getTotalOrders(): number {
    return this.orders.length;
  }

  getCompletedOrders(): number {
    return this.orders.filter(o => o.status === 'COMPLETED').length;
  }

  getPaymentMethodName(method?: string): string {
    if (!method) return 'N/A';
    switch (method?.toUpperCase()) {
      case 'STRIPE':
        return 'Credit Card';
      case 'CASH':
        return 'Cash';
      case 'BANK_TRANSFER':
        return 'Bank Transfer';
      default:
        return method;
    }
  }
}

