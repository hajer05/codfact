import { Routes } from '@angular/router';
import { QuizDeactivateGuard } from './guards/quiz-deactivate.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./components/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'courses',
    loadComponent: () => import('./components/courses/courses.component').then(m => m.CoursesComponent)
  },
  {
    path: 'courses/:id',
    loadComponent: () => import('./components/course-details/course-details.component').then(m => m.CourseDetailsComponent)
  },
  {
    path: 'cart',
    loadComponent: () => import('./components/cart/cart.component').then(m => m.CartComponent)
  },
  {
    path: 'checkout',
    loadComponent: () => import('./components/checkout/checkout.component').then(m => m.CheckoutComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./components/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./components/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'profile',
    loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent)
  },
  {
    path: 'complaints',
    loadComponent: () => import('./components/complaints/my-complaints.component').then(m => m.MyComplaintsComponent)
  },
  {
    path: 'transactions',
    loadComponent: () => import('./components/transactions/transactions.component').then(m => m.TransactionsComponent)
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
        loadComponent: () => import('./components/blog/blog-create/blog-create.component').then(m => m.BlogCreateComponent)
      },
      {
        path: ':id',
        loadComponent: () => import('./components/blog/blog-detail/blog-detail.component').then(m => m.BlogDetailComponent)
      }
    ]
  },
  {
    path: 'consulting',
    children: [
      {
        path: '',
        loadComponent: () => import('./components/consulting/consulting.component').then(m => m.ConsultingComponent)
      },
      {
        path: 'my-requests',
        loadComponent: () => import('./components/consulting/my-consulting-requests/my-consulting-requests.component').then(m => m.MyConsultingRequestsComponent)
      }
    ]
  },
  {
    path: 'pfe',
    children: [
      {
        path: '',
        loadComponent: () => import('./components/pfe/pfe-browse/pfe-browse.component').then(m => m.PFEBrowseComponent)
      },
      {
        path: 'my-applications',
        loadComponent: () => import('./components/pfe/my-applications/my-applications.component').then(m => m.MyApplicationsComponent)
      },
      {
        path: 'workflow/:id',
        loadComponent: () => import('./components/pfe/pfe-workflow/pfe-workflow.component').then(m => m.PFEWorkflowComponent)
      },
      {
        path: ':id',
        loadComponent: () => import('./components/pfe/pfe-detail/pfe-detail.component').then(m => m.PFEDetailComponent)
      },
      {
        path: ':id/apply',
        loadComponent: () => import('./components/pfe/pfe-apply/pfe-apply.component').then(m => m.PFEApplyComponent)
      }
    ]
  },
  {
    path: 'quiz-take/:quizId',
    loadComponent: () => import('./components/quiz/quiz-take/quiz-take.component').then(m => m.QuizTakeComponent),
    canDeactivate: [QuizDeactivateGuard]
  },
  {
    path: 'quiz-result/:attemptId',
    loadComponent: () => import('./components/quiz/quiz-result/quiz-result.component').then(m => m.QuizResultComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
