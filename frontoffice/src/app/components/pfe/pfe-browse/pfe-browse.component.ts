import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PFEService, PFESubject, Application } from '../../../services/pfe.service';

@Component({
  selector: 'app-pfe-browse',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './pfe-browse.component.html',
  styleUrls: ['./pfe-browse.component.scss']
})
export class PFEBrowseComponent implements OnInit {
  subjects: PFESubject[] = [];
  loading = false;
  searchTerm = '';
  myApplications: Application[] = [];
  hasAccepted = false;
  acceptedApplication?: Application;

  constructor(private pfeService: PFEService) {}

  ngOnInit() {
    this.loadMyApplications();
    this.loadOpenSubjects();
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

  loadOpenSubjects() {
    this.loading = true;
    this.pfeService.getOpenSubjects().subscribe({
      next: (subjects) => {
        this.subjects = subjects;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading PFE subjects:', error);
        this.loading = false;
      }
    });
  }

  searchSubjects() {
    if (this.searchTerm.trim()) {
      this.loading = true;
      this.pfeService.searchSubjects(this.searchTerm).subscribe({
        next: (subjects) => {
          this.subjects = subjects.filter(s => s.status === 'OPEN');
          this.loading = false;
        },
        error: (error) => {
          console.error('Error searching subjects:', error);
          this.loading = false;
        }
      });
    } else {
      this.loadOpenSubjects();
    }
  }

  get filteredSubjects() {
    return this.subjects;
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
