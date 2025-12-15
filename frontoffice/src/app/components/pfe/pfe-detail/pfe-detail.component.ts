import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PFEService, PFESubject, Application } from '../../../services/pfe.service';

@Component({
  selector: 'app-pfe-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './pfe-detail.component.html',
  styleUrls: ['./pfe-detail.component.scss']
})
export class PFEDetailComponent implements OnInit {
  subject?: PFESubject;
  loading = false;
  myApplications: Application[] = [];
  hasAccepted = false;
  acceptedApplication?: Application;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pfeService: PFEService
  ) {}

  ngOnInit() {
    this.loadMyApplications();
    this.route.params.subscribe(params => {
      const subjectId = +params['id'];
      if (subjectId) {
        this.loadSubject(subjectId);
      }
    });
  }

  loadMyApplications() {
    this.pfeService.getMyApplications().subscribe({
      next: (applications) => {
        this.myApplications = applications;
        this.hasAccepted = this.pfeService.hasAcceptedApplication(applications);
        this.acceptedApplication = this.pfeService.getAcceptedApplication(applications);
      },
      error: (error) => {
        console.error('Error loading applications:', error);
      }
    });
  }

  loadSubject(id: number) {
    this.loading = true;
    this.pfeService.getSubjectById(id).subscribe({
      next: (subject) => {
        this.subject = subject;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading PFE subject:', error);
        this.loading = false;
        this.router.navigate(['/pfe']);
      }
    });
  }

  canApply(): boolean {
    return !this.hasAccepted;
  }

  getApplyTooltip(): string {
    if (this.hasAccepted && this.acceptedApplication) {
      return `You already have an accepted application for "${this.acceptedApplication.subjectTitle}"`;
    }
    return 'Apply to this PFE project';
  }
}
