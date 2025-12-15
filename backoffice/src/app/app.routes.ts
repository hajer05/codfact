import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./components/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: '',
    loadComponent: () => import('./components/layout/dashboard-layout/dashboard-layout.component').then(m => m.DashboardLayoutComponent),
    canActivate: [AuthGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'courses',
        children: [
          {
            path: '',
            loadComponent: () => import('./components/courses/course-list/course-list.component').then(m => m.CourseListComponent)
          },
          {
            path: 'create',
            loadComponent: () => import('./components/courses/course-create/course-create.component').then(m => m.CourseCreateComponent),
            canActivate: [RoleGuard],
            data: { roles: ['TEACHER', 'ADMIN'] }
          },
          {
            path: ':id/edit',
            loadComponent: () => import('./components/courses/course-create/course-create.component').then(m => m.CourseCreateComponent),
            canActivate: [RoleGuard],
            data: { roles: ['TEACHER', 'ADMIN'] }
          },
          {
            path: 'all',
            loadComponent: () => import('./components/courses/course-list/course-list.component').then(m => m.CourseListComponent),
            canActivate: [RoleGuard],
            data: { roles: ['CONSULTANT', 'ADMIN'] }
          },
          {
            path: ':id/detail',
            loadComponent: () => import('./components/courses/course-detail/course-detail.component').then(m => m.CourseDetailComponent)
          }
        ]
      },
      {
        path: 'users',
        loadComponent: () => import('./components/admin/user-management/user-management.component').then(m => m.UserManagementComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ADMIN'] }
      },
      {
        path: 'pfe',
        children: [
          {
            path: '',
            loadComponent: () => import('./components/pfe/pfe-list/pfe-list.component').then(m => m.PFEListComponent)
          },
          {
            path: 'create',
            loadComponent: () => import('./components/pfe/pfe-form/pfe-form.component').then(m => m.PFEFormComponent),
            canActivate: [RoleGuard],
            data: { roles: ['TEACHER', 'CONSULTANT', 'ADMIN'] }
          },
          {
            path: ':id/edit',
            loadComponent: () => import('./components/pfe/pfe-form/pfe-form.component').then(m => m.PFEFormComponent),
            canActivate: [RoleGuard],
            data: { roles: ['TEACHER', 'CONSULTANT', 'ADMIN'] }
          },
          {
            path: ':id/applications',
            loadComponent: () => import('./components/pfe/application-management/application-management.component').then(m => m.ApplicationManagementComponent),
            canActivate: [RoleGuard],
            data: { roles: ['TEACHER', 'CONSULTANT', 'ADMIN'] }
          }
        ]
      },
      {
        path: 'blogs',
        children: [
          {
            path: '',
            loadComponent: () => import('./components/blog/blog-list/blog-list.component').then(m => m.BlogListComponent)
          },
          {
            path: 'create',
            loadComponent: () => import('./components/blog/blog-form/blog-form.component').then(m => m.BlogFormComponent)
          },
          {
            path: ':id/edit',
            loadComponent: () => import('./components/blog/blog-form/blog-form.component').then(m => m.BlogFormComponent)
          },
          {
            path: ':id/detail',
            loadComponent: () => import('./components/blog/blog-detail/blog-detail.component').then(m => m.BlogDetailComponent)
          }
        ]
      },
      {
        path: 'consulting',
        loadComponent: () => import('./components/admin/consulting-management/consulting-management.component').then(m => m.ConsultingManagementComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ADMIN', 'CONSULTANT'] }
      },
      {
        path: 'orders',
        loadComponent: () => import('./components/orders/orders.component').then(m => m.OrdersComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ADMIN'] }
      },
      {
        path: 'complaints',
        loadComponent: () => import('./components/complaints/complaints.component').then(m => m.ComplaintsComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ADMIN', 'TEACHER'] }
      },
      {
        path: 'quiz-management',
        loadComponent: () => import('./components/quiz/quiz-management/quiz-management.component').then(m => m.QuizManagementComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ADMIN', 'TEACHER'] }
      },
      {
        path: 'quiz-results/:quizId',
        loadComponent: () => import('./components/quiz/quiz-results/quiz-results.component').then(m => m.QuizResultsComponent),
        canActivate: [RoleGuard],
        data: { roles: ['ADMIN', 'TEACHER'] }
      }
    ]
  },
  { path: '**', redirectTo: '/dashboard' }
];                                                                                                
