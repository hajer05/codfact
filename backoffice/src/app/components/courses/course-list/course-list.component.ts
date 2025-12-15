import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CourseService } from '../../../services/course.service';
import { AuthService } from '../../../services/auth.service';
import { NotificationService } from '../../../services/notification.service';
import { Course } from '../../../models/course.model';
import { User } from '../../../models/user.model';

@Component({
  selector: 'app-course-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './course-list.component.html',
  styleUrls: ['./course-list.component.scss']
})
export class CourseListComponent implements OnInit {
  courses: Course[] = [];
  loading = true;
  currentUser: User | null = null;
  viewMode: 'my-courses' | 'all-courses' = 'my-courses';

  constructor(
    private courseService: CourseService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.determineViewMode();
      this.loadCourses();
    });
  }

  determineViewMode() {
    // Default to 'my-courses' for all users (teachers and admins)
    // Admin can toggle to 'all-courses' to see all courses
    this.viewMode = 'my-courses';
  }

  toggleViewMode() {
    if (this.currentUser?.roles.includes('ADMIN')) {
      this.viewMode = this.viewMode === 'my-courses' ? 'all-courses' : 'my-courses';
      this.loadCourses();
    }
  }

  loadCourses() {
    this.loading = true;
    
    if (this.viewMode === 'all-courses') {
      this.courseService.getAllCourses().subscribe({
        next: (courses) => {
          this.courses = courses;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading courses:', error);
          this.loading = false;
        }
      });
    } else {
      this.courseService.getMyCourses().subscribe({
        next: (courses) => {
          this.courses = courses;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading my courses:', error);
          this.loading = false;
        }
      });
    }
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PUBLISHED':
        return 'bg-green-100 text-green-800';
      case 'DRAFT':
        return 'bg-yellow-100 text-yellow-800';
      case 'ARCHIVED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getLevelBadgeClass(level: string): string {
    switch (level) {
      case 'BEGINNER':
        return 'bg-blue-100 text-blue-800';
      case 'INTERMEDIATE':
        return 'bg-purple-100 text-purple-800';
      case 'ADVANCED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  canEditCourse(course: Course): boolean {
    return this.currentUser?.roles.includes('ADMIN') === true || 
           (this.currentUser?.roles.includes('TEACHER') === true && course.teacherId === this.currentUser.id);
  }

  publishCourse(course: Course) {
    if (course.status === 'DRAFT') {
      this.courseService.publishCourse(course.id).subscribe({
        next: (updatedCourse) => {
          const index = this.courses.findIndex(c => c.id === course.id);
          if (index !== -1) {
            this.courses[index] = updatedCourse;
          }
        },
        error: (error) => {
          console.error('Error publishing course:', error);
        }
      });
    }
  }

  deleteCourse(course: Course) {
    // First, get delete info to check for enrollments and orders
    this.courseService.getCourseDeleteInfo(course.id).subscribe({
      next: (deleteInfo) => {
        let confirmMessage = `Êtes-vous sûr de vouloir supprimer le cours "${deleteInfo.courseTitle}" ?\n\n`;
        
        // Check if course has complaints
        // If admin, inform that complaints will be deleted automatically
        // If teacher, prevent deletion
        if (deleteInfo.hasComplaints) {
          const isAdmin = this.currentUser?.roles.includes('ADMIN');
          if (isAdmin) {
            confirmMessage += '⚠️ ATTENTION : Ce cours a des réclamations qui seront supprimées automatiquement !\n\n';
            confirmMessage += `📋 Réclamations : ${deleteInfo.complaintsCount} réclamation(s) active(s)\n\n`;
          } else {
            confirmMessage += '❌ IMPOSSIBLE DE SUPPRIMER : Ce cours a des réclamations en cours !\n\n';
            confirmMessage += `📋 Réclamations : ${deleteInfo.complaintsCount} réclamation(s) active(s)\n\n`;
            confirmMessage += '⚠️ Veuillez résoudre ou supprimer toutes les réclamations avant de supprimer ce cours.\n';
            alert(confirmMessage);
            return;
          }
        }
        
        if (deleteInfo.hasEnrollments || deleteInfo.hasPaidOrders) {
          confirmMessage += '⚠️ ATTENTION : Ce cours a des utilisateurs actifs !\n\n';
          
          if (deleteInfo.hasEnrollments) {
            confirmMessage += `📚 Inscriptions : ${deleteInfo.activeEnrollments} utilisateur(s) inscrit(s) actuellement\n`;
            if (deleteInfo.totalEnrollments > deleteInfo.activeEnrollments) {
              confirmMessage += `   (${deleteInfo.totalEnrollments} au total incluant les historiques)\n`;
            }
          }
          
          if (deleteInfo.hasPaidOrders && deleteInfo.paidOrdersCount > 0) {
            confirmMessage += `💰 Commandes payantes : ${deleteInfo.paidOrdersCount} commande(s) payée(s)\n`;
            confirmMessage += `   Des utilisateurs ont PAYÉ pour ce cours !\n\n`;
          }
          
          confirmMessage += '\n⚠️ La suppression de ce cours supprimera :\n';
          confirmMessage += '- Toutes les inscriptions\n';
          confirmMessage += '- L\'historique des progressions\n';
          confirmMessage += '- Tous les contenus (vidéos, modules, leçons)\n\n';
          confirmMessage += 'Cette action est IRRÉVERSIBLE !\n\n';
          confirmMessage += 'Êtes-vous vraiment sûr de vouloir continuer ?';
        } else {
          confirmMessage += 'Aucun utilisateur inscrit ou commande payante détectée.\n\n';
          confirmMessage += 'Cette action est IRRÉVERSIBLE !\n\n';
          confirmMessage += 'Êtes-vous sûr de vouloir supprimer ce cours ?';
        }
        
        if (confirm(confirmMessage)) {
          // Double confirmation if there are enrollments or paid orders
          if (deleteInfo.hasEnrollments || deleteInfo.hasPaidOrders) {
            const doubleConfirm = confirm(
              `⚠️ DERNIÈRE CONFIRMATION ⚠️\n\n` +
              `Vous êtes sur le point de supprimer un cours avec ${deleteInfo.activeEnrollments} utilisateur(s) inscrit(s)` +
              (deleteInfo.hasPaidOrders ? ` et ${deleteInfo.paidOrdersCount} commande(s) payée(s)` : '') +
              `.\n\n` +
              `Cette action supprimera définitivement toutes les données associées.\n\n` +
              `Tapez OK pour confirmer la suppression.`
            );
            
            if (!doubleConfirm) {
              return; // User cancelled
            }
          }
          
          // Proceed with deletion
          this.courseService.deleteCourse(course.id).subscribe({
            next: () => {
              this.notificationService.success('✅ Cours supprimé avec succès !');
              this.loadCourses();
            },
            error: (error) => {
              console.error('Error deleting course:', error);
              const errorMsg = error.error?.message || error.message || 'Erreur inconnue';
              if (errorMsg.includes('registered') || errorMsg.includes('enrolled')) {
                this.notificationService.warning('⚠️ Impossible de supprimer le cours : il y a des utilisateurs inscrits ou des commandes payées associées.');
              } else {
                this.notificationService.error('❌ Erreur lors de la suppression du cours : ' + errorMsg);
              }
            }
          });
        }
      },
      error: (error) => {
        console.error('Error fetching delete info:', error);
        // Fallback to simple confirmation if info fetch fails
        if (confirm(`Êtes-vous sûr de vouloir supprimer le cours "${course.title}" ?\n\nCette action est IRRÉVERSIBLE !`)) {
          this.courseService.deleteCourse(course.id).subscribe({
            next: () => {
              this.notificationService.success('✅ Cours supprimé avec succès !');
              this.loadCourses();
            },
            error: (error) => {
              console.error('Error deleting course:', error);
              const errorMsg = error.error?.message || error.message || 'Erreur inconnue';
              if (errorMsg.includes('registered') || errorMsg.includes('enrolled')) {
                this.notificationService.warning('⚠️ Impossible de supprimer : des utilisateurs sont inscrits à ce cours.');
              } else {
                this.notificationService.error('❌ Erreur lors de la suppression du cours.');
              }
            }
          });
        }
      }
    });
  }

  deleteAllCourses() {
    if (this.currentUser?.roles.includes('ADMIN')) {
      const confirmMessage = '⚠️ ATTENTION : Vous êtes sur le point de supprimer TOUS les cours !\n\n' +
                              'Cette action est IRRÉVERSIBLE !\n\n' +
                              'Note : Les cours ayant des réclamations ne pourront pas être supprimés.\n\n' +
                              'Êtes-vous vraiment sûr de vouloir continuer ?';
      
      if (confirm(confirmMessage)) {
        // Double confirmation
        if (confirm('⚠️ DERNIÈRE CONFIRMATION ⚠️\n\n' +
                   'Vous êtes sur le point de supprimer TOUS les cours.\n\n' +
                   'Tapez OK pour confirmer la suppression.')) {
          this.courseService.deleteAllCourses().subscribe({
            next: () => {
              this.notificationService.success('✅ Tous les cours ont été supprimés avec succès !');
              this.loadCourses();
            },
            error: (error) => {
              console.error('Error deleting all courses:', error);
              const errorMessage = error?.error?.message || error?.message || 'Échec de la suppression';
              
              // Show detailed error message if available
              if (errorMessage.includes('complaint') || errorMessage.includes('Deleted')) {
                this.notificationService.warning('⚠️ ' + errorMessage);
              } else {
                this.notificationService.error('❌ Échec de la suppression de tous les cours.');
              }
              
              this.loadCourses(); // Reload to refresh the list
            }
          });
        }
      }
    }
  }
}
