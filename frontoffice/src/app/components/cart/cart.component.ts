import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { OrderService, Order } from '../../services/order.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {
  cart: any = null;
  loading = false;
  error: string | null = null;

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadCart();
  }

  loadCart() {
    this.cartService.cart$.subscribe(cart => {
      this.cart = cart;
    });

    this.cartService.getCart().subscribe({
      next: () => {},
      error: (err) => {
        console.error('Error loading cart:', err);
      }
    });
  }

  removeItem(cartItemId: number) {
    this.loading = true;
    this.cartService.removeFromCart(cartItemId).subscribe({
      next: () => {
        this.loading = false;
      },
      error: (err) => {
        console.error('Error removing item:', err);
        this.loading = false;
      }
    });
  }

  proceedToCheckout() {
    if (this.cart && this.cart.items && this.cart.items.length > 0) {
      this.router.navigate(['/checkout']);
    }
  }

  get totalAmount(): number {
    return this.cart?.totalAmount || 0;
  }
}

