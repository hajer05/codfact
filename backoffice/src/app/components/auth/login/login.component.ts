import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(3)]]
    });
  }

  onSubmit() {
    console.log('Form submitted');
    console.log('Form valid:', this.loginForm.valid);
    console.log('Form value:', this.loginForm.value);
    console.log('Form errors:', this.loginForm.errors);
    console.log('Email errors:', this.email?.errors);
    console.log('Password errors:', this.password?.errors);
    
    if (this.loginForm.valid) {
      this.loading = true;
      this.error = '';

      console.log('Making API call with:', this.loginForm.value);
      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          console.log('Login successful:', response);
          this.loading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          console.log('Login error:', error);
          this.loading = false;
          this.error = 'Invalid email or password';
        }
      });
    } else {
      console.log('Form is invalid, not submitting');
      // Mark all fields as touched to show validation errors
      this.loginForm.markAllAsTouched();
    }
  }

  get email() { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }
}
