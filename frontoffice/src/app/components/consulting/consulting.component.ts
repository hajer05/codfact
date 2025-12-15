import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ConsultingService, CreateConsultingRequest } from '../../services/consulting.service';

interface Service {
  name: string;
  description: string;
  icon: string;
  features: string[];
}

@Component({
  selector: 'app-consulting',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './consulting.component.html',
  styleUrls: ['./consulting.component.scss']
})
export class ConsultingComponent implements OnInit {
  showForm = false;
  submitted = false;
  loading = false;
  success = false;

  request: CreateConsultingRequest = {
    companyName: '',
    contactName: '',
    email: '',
    phone: '',
    serviceType: '',
    projectDescription: '',
    needs: '',
    budget: '',
    timeline: ''
  };

  services: Service[] = [
    {
      name: 'Web Development',
      description: 'Développement de sites web et applications web modernes',
      icon: 'M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4',
      features: ['Responsive Design', 'E-Commerce', 'CMS', 'API Development']
    },
    {
      name: 'Mobile Development',
      description: 'Applications mobiles iOS et Android natives et cross-platform',
      icon: 'M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z',
      features: ['iOS Apps', 'Android Apps', 'React Native', 'Flutter']
    },
    {
      name: 'Data Science & AI',
      description: 'Solutions d\'intelligence artificielle et analyse de données',
      icon: 'M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z',
      features: ['Machine Learning', 'Deep Learning', 'Big Data', 'Business Intelligence']
    },
    {
      name: 'Cloud & DevOps',
      description: 'Infrastructure cloud, déploiement et automatisation',
      icon: 'M3 15a4 4 0 004 4h9a5 5 0 10-.1-9.999 5.002 5.002 0 10-9.78 2.096A4.001 4.001 0 003 15z',
      features: ['AWS/Azure/GCP', 'Docker & Kubernetes', 'CI/CD', 'Monitoring']
    },
    {
      name: 'Cybersecurity',
      description: 'Sécurité informatique et protection des données',
      icon: 'M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z',
      features: ['Penetration Testing', 'Security Audit', 'Risk Assessment', 'Compliance']
    },
    {
      name: 'Digital Transformation',
      description: 'Accompagnement dans la transformation digitale',
      icon: 'M13 10V3L4 14h7v7l9-11h-7z',
      features: ['Strategy', 'Process Optimization', 'Digitalization', 'Training']
    }
  ];

  constructor(private consultingService: ConsultingService) {}

  ngOnInit() {}

  onSubmit() {
    this.submitted = true;
    
    if (!this.isFormValid()) {
      return;
    }

    this.loading = true;
    
    this.consultingService.createRequest(this.request).subscribe({
      next: (response) => {
        this.loading = false;
        this.success = true;
        this.showForm = false;
        this.resetForm();
        
        // Scroll to top to show success message
        window.scrollTo({ top: 0, behavior: 'smooth' });
        
        // Hide success message after 5 seconds
        setTimeout(() => {
          this.success = false;
        }, 5000);
      },
      error: (error) => {
        console.error('Error creating request:', error);
        this.loading = false;
        alert('Une erreur est survenue. Veuillez réessayer.');
      }
    });
  }

  isFormValid(): boolean {
    return !!(this.request.companyName && 
              this.request.contactName && 
              this.request.email && 
              this.request.serviceType && 
              this.request.projectDescription && 
              this.request.needs);
  }

  resetForm() {
    this.request = {
      companyName: '',
      contactName: '',
      email: '',
      phone: '',
      serviceType: '',
      projectDescription: '',
      needs: '',
      budget: '',
      timeline: ''
    };
    this.submitted = false;
  }

  selectService(serviceName: string) {
    this.request.serviceType = serviceName;
    this.showForm = true;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}

