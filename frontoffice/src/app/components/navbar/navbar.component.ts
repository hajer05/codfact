import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { User } from '../../models/user.model';
import { NotificationsComponent } from '../notifications/notifications.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, NotificationsComponent],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  mobileMenuOpen = false;
  dropdownOpen = false;
  currentUser: User | null = null;
  cartItemCount = 0;

  constructor(
    private authService: AuthService,
    private cartService: CartService
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      console.log('Navbar: currentUser$ changed:', user);
      this.currentUser = user;
      if (user) {
        this.loadCartItemCount();
      } else {
        this.cartItemCount = 0;
      }
    });
    
    // Load cart items count on init
    this.loadCartItemCount();
    
    // Subscribe to cart changes for real-time updates
    this.cartService.cart$.subscribe(cart => {
      if (cart) {
        this.cartItemCount = cart.items?.length || 0;
      } else {
        this.cartItemCount = 0;
      }
    });
  }
  
  loadCartItemCount() {
    if (this.isAuthenticated()) {
      this.cartService.getCart().subscribe({
        next: (cart) => {
          this.cartItemCount = cart?.items?.length || 0;
        },
        error: (err) => {
          console.error('Error loading cart:', err);
          this.cartItemCount = 0;
        }
      });
    }
  }

  toggleMobileMenu() {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  toggleDropdown() {
    this.dropdownOpen = !this.dropdownOpen;
  }

  logout() {
    this.authService.explicitLogout();
    this.dropdownOpen = false;
  }

  getInitials(): string {
    if (!this.currentUser) return '';
    return `${this.currentUser.firstName.charAt(0)}${this.currentUser.lastName.charAt(0)}`.toUpperCase();
  }

  isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }
}
