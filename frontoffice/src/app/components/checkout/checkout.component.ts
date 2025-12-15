import { Component, OnInit, AfterViewChecked, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { OrderService, Order } from '../../services/order.service';
import { CartService } from '../../services/cart.service';

declare var Stripe: any;

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit, AfterViewChecked {
  @ViewChild('paymentElementContainer', { static: false }) paymentElementContainer!: ElementRef;

  order: Order | null = null;
  loading = false;
  error: string | null = null;
  paymentProcessing = false;
  stripe: any;
  elements: any;
  paymentElement: any;
  clientSecret: string | null = null;

  private elementMounted = false;
  private paymentElementCreated = false;

  constructor(
    private orderService: OrderService,
    private cartService: CartService,
    public router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  async ngOnInit() {
    this.loading = true;
    
    try {
      // Create order from cart
      this.order = await this.orderService.createOrder().toPromise() || null;
      
      if (!this.order) {
        throw new Error('Failed to create order');
      }

      // Get payment intent
      const paymentIntent = await this.orderService.createPaymentIntent(
        this.order.id,
        this.order.totalAmount,
        'USD'
      ).toPromise();

      if (!paymentIntent) {
        throw new Error('Failed to create payment intent');
      }

      // Persist order/payment identifiers for post-redirect confirmation
      try {
        localStorage.setItem('lastOrderId', String(this.order.id));
        if (paymentIntent.paymentIntentId) {
          localStorage.setItem('lastPaymentIntentId', paymentIntent.paymentIntentId);
        }
      } catch {}

      // Initialize Stripe
      this.stripe = Stripe('pk_test_51RP7SIRslzO02W4LkMzYpSIW5GNkLvUDQpWSAy4zUQiloVqNwSNn5JMSVbtQkPI7717EaFH3RUEX6hxWu0cj7Ocl00lxUjVsTM');
      
      // Store client secret
      this.clientSecret = paymentIntent.clientSecret;
      
      // Create payment element
      this.elements = this.stripe.elements({ 
        clientSecret: paymentIntent.clientSecret,
        appearance: {
          theme: 'stripe'
        }
      });

      this.paymentElement = this.elements.create('payment');
      this.paymentElementCreated = true;
      
      // Set loading to false to show the payment element
      this.loading = false;
      
      // Force change detection to render the DOM
      this.cdr.detectChanges();
      
      // Wait for the next render cycle to mount the element
      setTimeout(() => {
        this.mountPaymentElement();
      }, 50);
    } catch (err) {
      console.error('Error initializing checkout:', err);
      this.error = 'Failed to initialize checkout';
      this.loading = false;
    }
  }

  ngAfterViewChecked() {
    // Try to mount the element after each view check if it's ready
    if (!this.elementMounted && this.paymentElementCreated) {
      this.mountPaymentElement();
    }
  }

  private mountPaymentElement() {
    const element = document.getElementById('payment-element');
    if (!this.elementMounted && this.paymentElement && element) {
      try {
        this.paymentElement.mount('#payment-element');
        this.elementMounted = true;
        console.log('Payment element mounted successfully');
      } catch (err) {
        console.error('Error mounting payment element:', err);
      }
    }
  }

  async handleSubmit() {
    if (!this.order || !this.paymentElement || !this.clientSecret) {
      return;
    }

    this.paymentProcessing = true;
    this.error = null;

    try {
      // Store order ID and payment intent ID before redirect (they will be used on return)
      localStorage.setItem('lastOrderId', String(this.order.id));
      
      // Extract payment intent ID from client secret (format: pi_xxx_secret_xxx)
      // Or use paymentIntentId if we stored it separately
      const paymentIntentId = localStorage.getItem('lastPaymentIntentId') || 
        this.clientSecret.split('_secret_')[0]; // Extract pi_xxx from client secret
      localStorage.setItem('lastPaymentIntentId', paymentIntentId);

      const { error: submitError } = await this.stripe.confirmPayment({
        elements: this.elements,
        confirmParams: {
          return_url: `${window.location.origin}/courses`,
        },
      });

      if (submitError) {
        this.error = submitError.message;
        this.paymentProcessing = false;
        // Clean up on error
        localStorage.removeItem('lastOrderId');
        localStorage.removeItem('lastPaymentIntentId');
      } else {
        // Stripe will redirect, so we don't need to navigate here
        // The confirmation will happen in courses.component.ts when Stripe redirects back
      }
    } catch (err: any) {
      console.error('Payment error:', err);
      this.error = err.message || 'Payment failed';
      this.paymentProcessing = false;
      // Clean up on error
      localStorage.removeItem('lastOrderId');
      localStorage.removeItem('lastPaymentIntentId');
    }
  }

  async confirmPayment() {
    if (!this.order) {
      return;
    }

    this.paymentProcessing = true;

    try {
      // Call backend to confirm payment
      const confirmedOrder = await this.orderService.confirmPayment({
        paymentIntentId: 'confirmed',
        orderId: this.order.id
      }).toPromise();

      // Clear cart
      await this.cartService.clearCart().toPromise();
      
      this.router.navigate(['/courses'], { queryParams: { purchased: 'true' } });
    } catch (err) {
      console.error('Error confirming payment:', err);
      this.error = 'Payment confirmation failed';
      this.paymentProcessing = false;
    }
  }

  formatPrice(price: number): string {
    return `$${price.toFixed(2)}`;
  }

  goToCart() {
    this.router.navigate(['/cart']);
  }
}

