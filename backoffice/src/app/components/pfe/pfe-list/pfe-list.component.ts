import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PFEService, PFESubject } from '../../../services/pfe.service';

@Component({
  selector: 'app-pfe-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './pfe-list.component.html',
  styleUrls: ['./pfe-list.component.scss']
})
export class PFEListComponent implements OnInit {
  subjects: PFESubject[] = [];
  loading = false;
  searchTerm = '';

  constructor(private pfeService: PFEService) {}

  ngOnInit() {
    this.loadSubjects();
  }

  loadSubjects() {
    this.loading = true;
    this.pfeService.getAllSubjects().subscribe({
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

  deleteSubject(id: number) {
    if (confirm('Are you sure you want to delete this PFE subject?')) {
      this.pfeService.deleteSubject(id).subscribe({
        next: () => {
          this.subjects = this.subjects.filter(subject => subject.id !== id);
        },
        error: (error) => {
          console.error('Error deleting subject:', error);
        }
      });
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'OPEN': return 'status-open';
      case 'ASSIGNED': return 'status-assigned';
      case 'COMPLETED': return 'status-completed';
      default: return '';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'OPEN': return 'Open';
      case 'ASSIGNED': return 'Assigned';
      case 'COMPLETED': return 'Completed';
      default: return status;
    }
  }

  get filteredSubjects() {
    if (!this.searchTerm) {
      return this.subjects;
    }
    return this.subjects.filter(subject => 
      subject.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      subject.description.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      subject.createdByName.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }
}
