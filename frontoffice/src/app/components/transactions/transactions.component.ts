import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { OrderService, Order } from '../../services/order.service';
import { ToastNotificationService } from '../../services/toast-notification.service';
import { AuthService } from '../../services/auth.service';
// @ts-ignore
import { jsPDF } from 'jspdf';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss']
})
export class TransactionsComponent implements OnInit {
  transactions: Order[] = [];
  loading = false;
  error: string | null = null;
  
  // Filtering
  filterStatus = 'ALL'; // ALL, COMPLETED, PENDING, CANCELLED
  sortBy = 'date'; // date, amount, status
  sortOrder: 'asc' | 'desc' = 'desc';

  constructor(
    private orderService: OrderService,
    private toastNotificationService: ToastNotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.loading = true;
    this.error = null;
    
    this.orderService.getOrders().subscribe({
      next: (orders) => {
        this.transactions = orders || [];
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading transactions:', error);
        this.error = 'Failed to load transactions. Please try again later.';
        this.loading = false;
        this.toastNotificationService.error('Failed to load transaction history');
      }
    });
  }

  applyFilters(): void {
    // Filtering and sorting is handled in the template with filteredTransactions getter
  }

  get filteredTransactions(): Order[] {
    let filtered = [...this.transactions];

    // Apply status filter
    if (this.filterStatus !== 'ALL') {
      filtered = filtered.filter(t => 
        t.status === this.filterStatus || 
        t.status === this.filterStatus.toUpperCase()
      );
    }

    // Apply sorting
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

    return filtered;
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      case 'REFUNDED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getPaymentStatusClass(status?: string): string {
    if (!status) return 'bg-gray-100 text-gray-800';
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      case 'REFUNDED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
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

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  viewOrderDetails(orderId: number): void {
    // Navigate to order details or show modal
    console.log('View order details:', orderId);
  }

  exportTransactions(): void {
    if (this.filteredTransactions.length === 0) {
      this.toastNotificationService.warning('No transactions to export');
      return;
    }

    try {
      this.exportToPDF();
    } catch (error) {
      console.error('Error exporting transactions:', error);
      this.toastNotificationService.error('Failed to export transactions. Please make sure jsPDF is installed: npm install jspdf');
    }
  }

  private exportToPDF(): void {
    // @ts-ignore
    const doc = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4'
    });

    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 20;
    const maxWidth = pageWidth - (2 * margin);
    let yPos = margin;

    // Colors
    const primaryColor = { r: 30, g: 144, b: 255 }; // Blue
    const darkColor = { r: 51, g: 51, b: 51 }; // Dark gray
    const lightGray = { r: 245, g: 245, b: 245 };
    const successColor = { r: 34, g: 197, b: 94 }; // Green

    // Header with gradient effect
    doc.setFillColor(primaryColor.r, primaryColor.g, primaryColor.b);
    doc.rect(0, 0, pageWidth, 50, 'F');
    
    // White text on blue background
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(24);
    doc.setFont('helvetica', 'bold');
    doc.text('CODING FACTORY', margin, 25);
    
    doc.setFontSize(14);
    doc.setFont('helvetica', 'normal');
    doc.text('Transaction Receipt', margin, 35);
    
    yPos = 60;

    // User information section
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      doc.setTextColor(darkColor.r, darkColor.g, darkColor.b);
      doc.setFontSize(12);
      doc.setFont('helvetica', 'bold');
      doc.text('Customer Information', margin, yPos);
      
      yPos += 8;
      doc.setFont('helvetica', 'normal');
      doc.setFontSize(10);
      doc.text(`Name: ${currentUser.firstName} ${currentUser.lastName}`, margin, yPos);
      yPos += 6;
      doc.text(`Email: ${currentUser.email}`, margin, yPos);
      yPos += 10;
    }

    // Summary box
    const summaryY = yPos;
    doc.setFillColor(lightGray.r, lightGray.g, lightGray.b);
    doc.rect(margin, summaryY, maxWidth, 20, 'F');
    
    doc.setFontSize(11);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(darkColor.r, darkColor.g, darkColor.b);
    doc.text('Summary', margin + 5, summaryY + 8);
    
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.text(`Total Transactions: ${this.filteredTransactions.length}`, margin + 5, summaryY + 14);
    doc.text(`Total Amount: ${this.formatCurrency(this.getTotalAmount())}`, margin + 5, summaryY + 18);
    doc.text(`Completed: ${this.getCompletedTransactionsCount()}`, margin + maxWidth / 2, summaryY + 14);
    doc.text(`Date Generated: ${new Date().toLocaleDateString('fr-FR', { year: 'numeric', month: 'long', day: 'numeric' })}`, margin + maxWidth / 2, summaryY + 18);
    
    yPos = summaryY + 30;

    // Transactions table
    this.filteredTransactions.forEach((order, index) => {
      // Check if we need a new page
      if (yPos > pageHeight - 60) {
        doc.addPage();
        yPos = margin;
      }

      // Transaction card background
      doc.setFillColor(lightGray.r, lightGray.g, lightGray.b);
      doc.rect(margin, yPos, maxWidth, 35, 'F');
      
      const cardStartY = yPos;
      yPos += 8;

      // Order number and date
      doc.setFontSize(11);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(primaryColor.r, primaryColor.g, primaryColor.b);
      doc.text(`Order #${order.orderNumber || order.id}`, margin + 5, yPos);
      
      doc.setFontSize(9);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(100, 100, 100);
      doc.text(this.formatDate(order.createdAt), pageWidth - margin - 5, yPos, { align: 'right' });
      
      yPos += 8;

      // Courses list
      doc.setFontSize(9);
      doc.setTextColor(darkColor.r, darkColor.g, darkColor.b);
      if (order.items && order.items.length > 0) {
        order.items.forEach((item, itemIndex) => {
          if (itemIndex < 3) { // Show max 3 courses per transaction
            doc.text(`• ${item.courseTitle}`, margin + 8, yPos);
            yPos += 5;
          }
        });
        if (order.items.length > 3) {
          doc.setTextColor(100, 100, 100);
          doc.text(`... and ${order.items.length - 3} more course(s)`, margin + 8, yPos);
          yPos += 5;
        }
      } else {
        doc.text('No courses', margin + 8, yPos);
        yPos += 5;
      }

      yPos -= 2; // Adjust spacing

      // Amount and status
      doc.setFontSize(11);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(successColor.r, successColor.g, successColor.b);
      doc.text(this.formatCurrency(order.totalAmount), margin + 5, cardStartY + 30);
      
      // Status badge
      const statusText = order.status || 'N/A';
      const statusColor = order.status === 'COMPLETED' 
        ? { r: successColor.r, g: successColor.g, b: successColor.b }
        : { r: 234, g: 179, b: 8 }; // Green or yellow
      doc.setFillColor(statusColor.r, statusColor.g, statusColor.b);
      const statusWidth = doc.getTextWidth(statusText) + 4;
      doc.rect(pageWidth - margin - statusWidth - 2, cardStartY + 24, statusWidth + 4, 6, 'F');
      doc.setTextColor(255, 255, 255);
      doc.setFontSize(8);
      doc.text(statusText, pageWidth - margin - statusWidth, cardStartY + 28);

      yPos = cardStartY + 40; // Space for next transaction
    });

    // Footer
    const footerY = pageHeight - 20;
    doc.setDrawColor(200, 200, 200);
    doc.line(margin, footerY, pageWidth - margin, footerY);
    
    doc.setFontSize(8);
    doc.setTextColor(150, 150, 150);
    doc.text('This is a computer-generated receipt.', margin, footerY + 8);
    doc.text('Thank you for your purchase!', pageWidth - margin, footerY + 8, { align: 'right' });

    // Save PDF
    const fileName = `transactions_receipt_${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(fileName);
    
    this.toastNotificationService.success(`PDF exported successfully! ${this.filteredTransactions.length} transaction(s) included.`);
  }

  getTotalAmount(): number {
    return this.filteredTransactions.reduce((sum, t) => sum + t.totalAmount, 0);
  }

  getCompletedTransactionsCount(): number {
    return this.filteredTransactions.filter(t => t.status === 'COMPLETED').length;
  }
}

