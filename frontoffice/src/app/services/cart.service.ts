import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface Cart {
  id: number;
  userId: number;
  items: CartItem[];
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  id: number;
  courseId: number;
  courseTitle: string;
  coursePrice: number;
  price: number;
  thumbnailImage?: string;
  addedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private apiUrl = 'http://localhost:8090/api';
  private cartSubject = new BehaviorSubject<Cart | null>(null);
  public cart$ = this.cartSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadCart();
  }

  getCart(): Observable<Cart> {
    return this.http.get<Cart>(`${this.apiUrl}/cart`).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  addToCart(courseId: number): Observable<Cart> {
    return this.http.post<Cart>(`${this.apiUrl}/cart/add`, { courseId }).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  removeFromCart(cartItemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/cart/remove/${cartItemId}`).pipe(
      tap(() => this.loadCart())
    );
  }

  clearCart(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/cart/clear`).pipe(
      tap(() => this.cartSubject.next(null))
    );
  }

  private loadCart() {
    this.getCart().subscribe();
  }

  getCartItemCount(): number {
    const cart = this.cartSubject.value;
    return cart?.items?.length || 0;
  }
}

