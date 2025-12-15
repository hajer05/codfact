import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Order {
  id: number;
  orderNumber: string;
  userId: number;
  userName?: string;
  userEmail?: string;
  items: OrderItem[];
  totalAmount: number;
  status: string;
  payment?: Payment;
  createdAt: string;
  completedAt?: string;
}

export interface OrderItem {
  id: number;
  courseId: number;
  courseTitle: string;
  price: number;
  thumbnailImage?: string;
}

export interface Payment {
  id: number;
  amount: number;
  method: string;
  status: string;
  stripePaymentIntentId?: string;
  createdAt: string;
  processedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = 'http://localhost:8090/api';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/orders/admin/all`, { headers: this.getHeaders() });
  }

  getOrder(orderId: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/orders/${orderId}`, { headers: this.getHeaders() });
  }
}

