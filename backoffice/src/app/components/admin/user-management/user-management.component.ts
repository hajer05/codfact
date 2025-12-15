import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { User } from '../../../models/user.model';
import { UserService } from '../../../services/user.service';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  loading = false;
  showCreateForm = false;
  userForm: FormGroup;

  roles = [
    { value: 'ADMIN', label: 'Administrator' },
    { value: 'TEACHER', label: 'Teacher' },
    { value: 'CONSULTANT', label: 'Consultant' },
    { value: 'ETUDIANT', label: 'Student' }
  ];

  constructor(
    private fb: FormBuilder,
    private userService: UserService
  ) {
    this.userForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      roles: [['ETUDIANT'], Validators.required]
    });
  }

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.loading = true;
    this.userService.getAllUsers()
      .pipe(
        catchError(error => {
          console.error('Error loading users:', error);
          return of([]);
        })
      )
      .subscribe(users => {
        this.users = users;
        this.loading = false;
      });
  }

  toggleCreateForm() {
    this.showCreateForm = !this.showCreateForm;
    if (!this.showCreateForm) {
      this.userForm.reset();
      this.userForm.patchValue({ roles: ['ETUDIANT'] });
    }
  }

  onSubmit() {
    if (this.userForm.valid) {
      console.log('Creating user:', this.userForm.value);
      // Would call user service to create user
      this.toggleCreateForm();
    }
  }

  getRoleDisplayName(role: string): string {
    const roleObj = this.roles.find(r => r.value === role);
    return roleObj ? roleObj.label : role;
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'ADMIN':
        return 'bg-red-100 text-red-800';
      case 'TEACHER':
        return 'bg-blue-100 text-blue-800';
      case 'CONSULTANT':
        return 'bg-purple-100 text-purple-800';
      case 'ETUDIANT':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  deleteUser(user: User) {
    if (confirm(`Are you sure you want to delete ${user.firstName} ${user.lastName}?`)) {
      if (user.id) {
        this.userService.deleteUser(user.id)
          .pipe(
            catchError(error => {
              console.error('Error deleting user:', error);
              alert('Failed to delete user');
              return of(null);
            })
          )
          .subscribe(() => {
            // Remove from local list after successful deletion
            this.users = this.users.filter(u => u.id !== user.id);
          });
      }
    }
  }
}
