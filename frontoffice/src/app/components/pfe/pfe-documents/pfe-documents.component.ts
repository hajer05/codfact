import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DocumentService, DocumentDto } from '../../../services/document.service';
import { PFEService, PFESubject } from '../../../services/pfe.service';

@Component({
  selector: 'app-pfe-documents',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './pfe-documents.component.html',
  styleUrls: ['./pfe-documents.component.scss']
})
export class PFEDocumentsComponent implements OnInit {
  subjectId?: number;
  subject?: PFESubject;
  documents: DocumentDto[] = [];
  loading = false;
  uploading = false;
  
  // Upload form
  selectedFile?: File;
  documentType = 'REPORT';
  description = '';
  
  documentTypes = [
    { value: 'REPORT', label: 'Final Report' },
    { value: 'PRESENTATION', label: 'Presentation' },
    { value: 'OTHER', label: 'Other Document' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private documentService: DocumentService,
    private pfeService: PFEService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.subjectId = +params['id'];
        this.loadSubject();
        this.loadDocuments();
      }
    });
  }

  loadSubject() {
    if (!this.subjectId) return;
    
    this.pfeService.getSubjectById(this.subjectId).subscribe({
      next: (subject) => {
        this.subject = subject;
      },
      error: (error) => {
        console.error('Error loading subject:', error);
        this.router.navigate(['/pfe']);
      }
    });
  }

  loadDocuments() {
    if (!this.subjectId) return;
    
    this.loading = true;
    this.documentService.getSubjectDocuments(this.subjectId).subscribe({
      next: (documents) => {
        this.documents = documents;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading documents:', error);
        this.loading = false;
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      // Check file size (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        alert('File size must be less than 10MB');
        return;
      }
      
      // Check file type
      const allowedTypes = [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-powerpoint',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation'
      ];
      
      if (!allowedTypes.includes(file.type)) {
        alert('Only PDF, Word, and PowerPoint files are allowed');
        return;
      }
      
      this.selectedFile = file;
    }
  }

  uploadDocument() {
    if (!this.selectedFile || !this.subjectId) return;
    
    this.uploading = true;
    this.documentService.uploadDocument(
      this.subjectId,
      this.selectedFile,
      this.documentType,
      this.description
    ).subscribe({
      next: (document) => {
        this.documents.unshift(document);
        this.resetUploadForm();
        this.uploading = false;
      },
      error: (error) => {
        console.error('Error uploading document:', error);
        alert('Error uploading document. Please try again.');
        this.uploading = false;
      }
    });
  }

  deleteDocument(documentId: number) {
    if (!confirm('Are you sure you want to delete this document?')) return;
    
    this.documentService.deleteDocument(documentId).subscribe({
      next: () => {
        this.documents = this.documents.filter(doc => doc.id !== documentId);
      },
      error: (error) => {
        console.error('Error deleting document:', error);
        alert('Error deleting document. Please try again.');
      }
    });
  }

  downloadDocument(document: DocumentDto) {
    this.documentService.downloadDocument(document.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = window.document.createElement('a');
        link.href = url;
        link.download = document.originalFileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading document:', error);
        alert('Error downloading document. Please try again.');
      }
    });
  }

  resetUploadForm() {
    this.selectedFile = undefined;
    this.documentType = 'REPORT';
    this.description = '';
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  getDocumentIcon(documentType: string): string {
    switch (documentType) {
      case 'REPORT': return 'fas fa-file-alt';
      case 'PRESENTATION': return 'fas fa-file-powerpoint';
      default: return 'fas fa-file';
    }
  }
}
