# CODINGFACT - Learning Management Platform

A comprehensive e-learning platform built with microservices architecture, featuring course management, PFE (Final Year Project) management, consulting services, blog system, and integrated payment processing.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Features](#features)
- [User Roles](#user-roles)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
  - [Running Locally](#running-locally)
  - [Running with Docker](#running-with-docker)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Monitoring](#monitoring)
- [Testing](#testing)
- [Project Structure](#project-structure)

## 🎯 Overview

CODINGFACT is a full-stack learning management system designed for educational institutions and training centers. It provides a complete ecosystem for managing courses, student enrollments, final year projects (PFE), consulting services, and educational content through blogs.

The platform consists of three main components:
- **Backend Service**: Spring Boot REST API with PostgreSQL database
- **Backoffice**: Angular-based admin dashboard for content management
- **Frontoffice**: Angular-based student/user interface

## 🏗️ Architecture

The application follows a **microservices architecture** with the following components:

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Layer                             │
├──────────────────────┬──────────────────────────────────────┤
│   Backoffice (4200)  │      Frontoffice (4201)              │
│   - Admin Dashboard  │      - Student Portal                │
│   - Content Mgmt     │      - Course Browsing               │
│   - User Mgmt        │      - Shopping Cart                 │
└──────────────────────┴──────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Learning Service (8090)                         │
│              - REST API (Spring Boot)                        │
│              - JWT Authentication                            │
│              - Business Logic                                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL Database (5432)                      │
│              - User Data                                     │
│              - Course Content                                │
│              - Transactions                                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│         Monitoring Stack                                     │
├──────────────────────┬──────────────────────────────────────┤
│   Prometheus (9090)  │      Grafana (3000)                  │
│   - Metrics          │      - Dashboards                    │
│   - Scraping         │      - Visualization                 │
└──────────────────────┴──────────────────────────────────────┘
```

### Key Architectural Patterns

- **RESTful API**: Stateless communication between frontend and backend
- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control (RBAC)**: Fine-grained permission system
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: Data transfer between layers
- **Service Layer**: Business logic encapsulation

## 🛠️ Technologies

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.5.5** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database access
- **PostgreSQL 15** - Relational database
- **JWT (jjwt 0.12.3)** - Token-based authentication
- **Flyway** - Database migration
- **Lombok** - Boilerplate code reduction
- **Maven** - Build tool

### Frontend
- **Angular 19.2.0** - Web framework
- **TypeScript 5.7.2** - Programming language
- **RxJS 7.8.0** - Reactive programming
- **Stripe.js** - Payment integration (Frontoffice)
- **jsPDF** - PDF generation (Frontoffice)

### Integrations
- **Stripe** - Payment processing
- **OpenAI GPT-3** - AI-powered chatbot
- **WebSocket** - Real-time notifications
- **Email Service** - Notification system

### DevOps & Monitoring
- **Docker & Docker Compose** - Containerization
- **Prometheus** - Metrics collection
- **Grafana** - Metrics visualization
- **JaCoCo** - Code coverage
- **SonarQube** - Code quality analysis
- **Apache JMeter** - Performance testing
- **Nexus** - Artifact repository

### Testing
- **JUnit** - Unit testing (Backend)
- **Jasmine & Karma** - Unit testing (Frontend)
- **Selenium** - End-to-end testing
- **pytest** - Test automation

## ✨ Features

### Course Management
- **Course Creation & Editing**: Teachers can create comprehensive courses with modules and lessons
- **Module Organization**: Structured content organization
- **Lesson Management**: Video, text, and multimedia content support
- **File Uploads**: Support for course materials and resources
- **Course Enrollment**: Student registration and tracking
- **Progress Tracking**: Monitor student progress through courses
- **Certificates**: Automatic certificate generation upon course completion

### Quiz System
- **Quiz Creation**: Teachers can create quizzes with multiple question types
- **Quiz Taking**: Timed quiz attempts with auto-save
- **Results & Analytics**: Detailed performance analysis
- **Quiz Management**: Edit, delete, and manage quiz questions

### PFE (Final Year Project) Management
- **Project Posting**: Teachers/consultants can post PFE opportunities
- **Application System**: Students can apply for projects
- **Application Management**: Review and manage student applications
- **Workflow Tracking**: Monitor project progress and milestones
- **Status Updates**: Real-time project status tracking

### Consulting Services
- **Consulting Requests**: Students can request consulting sessions
- **Request Management**: Consultants can manage and respond to requests
- **Session Scheduling**: Book and manage consulting appointments

### Blog System
- **Blog Creation**: Create and publish educational content
- **Rich Text Editor**: Format blog posts with multimedia
- **Blog Management**: Edit, delete, and organize blog posts
- **Public Access**: Browse and read blog articles

### E-Commerce Features
- **Shopping Cart**: Add courses to cart
- **Stripe Integration**: Secure payment processing
- **Order Management**: Track purchases and transactions
- **Transaction History**: View payment history

### User Management
- **User Registration & Login**: Secure authentication system
- **Profile Management**: Update user information
- **Role-Based Access**: Admin, Teacher, Consultant, Student roles
- **User Administration**: Admin dashboard for user management

### Communication
- **AI Chatbot**: OpenAI-powered assistant for course and consulting queries
- **Real-time Notifications**: WebSocket-based notification system
- **Email Notifications**: Automated email alerts
- **Complaint System**: Submit and track complaints

### Monitoring & Analytics
- **Prometheus Metrics**: Application performance monitoring
- **Grafana Dashboards**: Visual analytics and insights
- **Health Checks**: Service health monitoring
- **Performance Metrics**: Request rates, response times, resource usage

## 👥 User Roles

### 1. **ADMIN**
- Full system access
- User management (create, edit, delete users)
- View all courses, PFEs, and content
- Manage orders and transactions
- Handle complaints
- Access to all administrative features

### 2. **TEACHER**
- Create and manage courses
- Create modules and lessons
- Create and manage quizzes
- Post PFE opportunities
- Manage student applications
- View student progress
- Handle course-related complaints

### 3. **CONSULTANT**
- Manage consulting requests
- Post PFE opportunities
- Manage PFE applications
- View consulting analytics

### 4. **STUDENT** (Default)
- Browse and enroll in courses
- Take quizzes and view results
- Apply for PFE opportunities
- Request consulting services
- Purchase courses via Stripe
- View transaction history
- Submit complaints
- Interact with AI chatbot

## 📦 Prerequisites

### For Local Development
- **Java 17** or higher
- **Node.js 18+** and npm
- **PostgreSQL 15**
- **Maven 3.8+**
- **Angular CLI 19+**

### For Docker Deployment
- **Docker 20.10+**
- **Docker Compose 2.0+**

## 🚀 Installation

### Running Locally

#### 1. Database Setup

```bash
# Install PostgreSQL 15
# Create database
createdb learning_db

# Or using psql
psql -U postgres
CREATE DATABASE learning_db;
```

#### 2. Backend Setup

```bash
# Navigate to backend directory
cd learning-service

# Configure database connection
# Edit src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/learning_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Backend will be available at http://localhost:8090
```

#### 3. Backoffice Setup

```bash
# Navigate to backoffice directory
cd backoffice

# Install dependencies
npm install

# Start development server
npm start

# Backoffice will be available at http://localhost:4200
```

#### 4. Frontoffice Setup

```bash
# Navigate to frontoffice directory
cd frontoffice

# Install dependencies
npm install

# Start development server
npm start

# Frontoffice will be available at http://localhost:4201
```

### Running with Docker

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd CODINGFACT
```

#### 2. Start All Services

```bash
# Build and start all containers
docker-compose up -d

# View logs
docker-compose logs -f

# Check container status
docker-compose ps
```

#### 3. Stop All Services

```bash
# Stop containers
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v
```

### Docker Services

After running `docker-compose up -d`, the following services will be available:

| Service | URL | Description |
|---------|-----|-------------|
| **Backoffice** | http://localhost:4200 | Admin dashboard |
| **Frontoffice** | http://localhost:4201 | Student portal |
| **Backend API** | http://localhost:8090 | REST API |
| **PostgreSQL** | localhost:5432 | Database |
| **Prometheus** | http://localhost:9090 | Metrics collection |
| **Grafana** | http://localhost:3000 | Dashboards (admin/admin) |

## ⚙️ Configuration

### Backend Configuration

Edit `learning-service/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/learning_db
spring.datasource.username=postgres
spring.datasource.password=root

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# Stripe Configuration
stripe.api.key=your-stripe-secret-key

# OpenAI Configuration
openai.api.key=your-openai-api-key

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Frontend Configuration

#### Backoffice API Configuration
Edit `backoffice/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8090/api'
};
```

#### Frontoffice API Configuration
Edit `frontoffice/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8090/api',
  stripePublicKey: 'your-stripe-public-key'
};
```

## 📚 API Documentation

### Authentication Endpoints

```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - Login user
POST   /api/auth/refresh-token     - Refresh JWT token
```

### Course Endpoints

```
GET    /api/courses                - Get all courses
GET    /api/courses/{id}           - Get course by ID
POST   /api/courses                - Create course (TEACHER, ADMIN)
PUT    /api/courses/{id}           - Update course (TEACHER, ADMIN)
DELETE /api/courses/{id}           - Delete course (TEACHER, ADMIN)
GET    /api/courses/{id}/modules   - Get course modules
POST   /api/courses/{id}/enroll    - Enroll in course
```

### Module & Lesson Endpoints

```
POST   /api/modules                - Create module
PUT    /api/modules/{id}           - Update module
DELETE /api/modules/{id}           - Delete module
POST   /api/lessons                - Create lesson
PUT    /api/lessons/{id}           - Update lesson
DELETE /api/lessons/{id}           - Delete lesson
POST   /api/lessons/progress       - Update lesson progress
```

### Quiz Endpoints

```
GET    /api/quizzes                - Get all quizzes
POST   /api/quizzes                - Create quiz
GET    /api/quizzes/{id}           - Get quiz by ID
POST   /api/quizzes/{id}/attempt   - Start quiz attempt
POST   /api/quizzes/submit         - Submit quiz answers
GET    /api/quizzes/results/{id}   - Get quiz results
```

### PFE Endpoints

```
GET    /api/pfe                    - Get all PFE opportunities
POST   /api/pfe                    - Create PFE (TEACHER, CONSULTANT)
GET    /api/pfe/{id}               - Get PFE by ID
POST   /api/pfe/{id}/apply         - Apply for PFE
GET    /api/pfe/{id}/applications  - Get PFE applications
PUT    /api/pfe/applications/{id}  - Update application status
```

### User Endpoints

```
GET    /api/users                  - Get all users (ADMIN)
GET    /api/users/{id}             - Get user by ID
PUT    /api/users/{id}             - Update user
DELETE /api/users/{id}             - Delete user (ADMIN)
GET    /api/users/profile          - Get current user profile
```

### Blog Endpoints

```
GET    /api/blogs                  - Get all blogs
POST   /api/blogs                  - Create blog
GET    /api/blogs/{id}             - Get blog by ID
PUT    /api/blogs/{id}             - Update blog
DELETE /api/blogs/{id}             - Delete blog
```

### Payment Endpoints

```
POST   /api/orders/create          - Create order
POST   /api/orders/stripe-payment  - Process Stripe payment
GET    /api/orders                 - Get user orders
GET    /api/orders/{id}            - Get order by ID
```

### Consulting Endpoints

```
POST   /api/consulting/request     - Create consulting request
GET    /api/consulting/requests    - Get consulting requests
PUT    /api/consulting/{id}        - Update consulting request
```

### Notification Endpoints

```
GET    /api/notifications          - Get user notifications
PUT    /api/notifications/{id}/read - Mark notification as read
DELETE /api/notifications/{id}     - Delete notification
```

### Chatbot Endpoints

```
POST   /api/chatbot/ask            - Ask AI chatbot
```

## 📊 Monitoring

The application includes comprehensive monitoring using Prometheus and Grafana.

### Accessing Monitoring Tools

1. **Prometheus**: http://localhost:9090
   - View metrics and targets
   - Query metrics data
   - Check service health

2. **Grafana**: http://localhost:3000
   - Login: `admin` / `admin`
   - Pre-configured dashboards
   - Visual analytics

### Available Metrics

- **HTTP Requests**: Request rate, duration, status codes
- **JVM Metrics**: Memory usage, garbage collection, threads
- **Database**: Connection pool, query performance
- **System**: CPU usage, disk I/O

### Setting up Grafana Dashboards

```bash
# Access Grafana
http://localhost:3000

# Add Prometheus data source
Configuration → Data Sources → Add Prometheus
URL: http://prometheus:9090

# Import Spring Boot Dashboard
Dashboards → Import → Dashboard ID: 4701 or 11378
```

For detailed monitoring setup, see [PROMETHEUS_SETUP.md](PROMETHEUS_SETUP.md).

## 🧪 Testing

### Backend Tests

```bash
cd learning-service

# Run all tests
mvn test

# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Frontend Tests

```bash
# Backoffice tests
cd backoffice
npm test

# Frontoffice tests
cd frontoffice
npm test
```

### End-to-End Tests

```bash
# Install dependencies
pip install -r requirements.txt

# Run Selenium tests
pytest test_app.py
```

### Performance Testing

```bash
cd learning-service/apache-jmeter-5.6.3

# Run JMeter tests
./bin/jmeter -n -t test-plan.jmx -l results.jtl
```

## 📁 Project Structure

```
CODINGFACT/
├── backoffice/                    # Admin dashboard (Angular)
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/       # UI components
│   │   │   │   ├── admin/        # Admin management
│   │   │   │   ├── auth/         # Authentication
│   │   │   │   ├── blog/         # Blog management
│   │   │   │   ├── complaints/   # Complaint handling
│   │   │   │   ├── courses/      # Course management
│   │   │   │   ├── dashboard/    # Dashboard
│   │   │   │   ├── orders/       # Order management
│   │   │   │   ├── pfe/          # PFE management
│   │   │   │   └── quiz/         # Quiz management
│   │   │   ├── guards/           # Route guards
│   │   │   ├── interceptors/     # HTTP interceptors
│   │   │   ├── models/           # TypeScript models
│   │   │   └── services/         # API services
│   │   └── environments/         # Environment configs
│   ├── Dockerfile
│   └── package.json
│
├── frontoffice/                   # Student portal (Angular)
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/       # UI components
│   │   │   │   ├── auth/         # Login/Register
│   │   │   │   ├── blog/         # Blog browsing
│   │   │   │   ├── cart/         # Shopping cart
│   │   │   │   ├── checkout/     # Payment checkout
│   │   │   │   ├── consulting/   # Consulting requests
│   │   │   │   ├── courses/      # Course browsing
│   │   │   │   ├── home/         # Landing page
│   │   │   │   ├── pfe/          # PFE browsing/apply
│   │   │   │   ├── profile/      # User profile
│   │   │   │   └── quiz/         # Quiz taking
│   │   │   ├── guards/           # Route guards
│   │   │   ├── interceptors/     # HTTP interceptors
│   │   │   ├── models/           # TypeScript models
│   │   │   └── services/         # API services
│   │   └── environments/         # Environment configs
│   ├── Dockerfile
│   └── package.json
│
├── learning-service/              # Backend API (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/learning_service/
│   │   │   │   ├── config/       # Configuration classes
│   │   │   │   ├── controller/   # REST controllers
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── BlogController.java
│   │   │   │   │   ├── CartController.java
│   │   │   │   │   ├── CertificateController.java
│   │   │   │   │   ├── ChatbotController.java
│   │   │   │   │   ├── ComplaintController.java
│   │   │   │   │   ├── ConsultingController.java
│   │   │   │   │   ├── CourseController.java
│   │   │   │   │   ├── EmailController.java
│   │   │   │   │   ├── EnrollmentController.java
│   │   │   │   │   ├── FileController.java
│   │   │   │   │   ├── LessonController.java
│   │   │   │   │   ├── LessonProgressController.java
│   │   │   │   │   ├── ModuleController.java
│   │   │   │   │   ├── NotificationController.java
│   │   │   │   │   ├── OrderController.java
│   │   │   │   │   ├── PFEController.java
│   │   │   │   │   ├── QuizController.java
│   │   │   │   │   └── UserController.java
│   │   │   │   ├── dto/          # Data Transfer Objects
│   │   │   │   ├── entity/       # JPA entities
│   │   │   │   ├── repository/   # Data repositories
│   │   │   │   ├── security/     # Security configs
│   │   │   │   ├── service/      # Business logic
│   │   │   │   └── util/         # Utility classes
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/ # Flyway migrations
│   │   └── test/                 # Unit tests
│   ├── Dockerfile
│   └── pom.xml
│
├── latex/                         # LaTeX templates (certificates)
├── docker-compose.yml             # Docker orchestration
├── prometheus.yml                 # Prometheus configuration
├── PROMETHEUS_SETUP.md            # Monitoring setup guide
├── requirements.txt               # Python dependencies (testing)
└── README.md                      # This file
```

## 🔐 Security

- **JWT Authentication**: Secure token-based authentication
- **Password Encryption**: BCrypt password hashing
- **CORS Configuration**: Controlled cross-origin access
- **Role-Based Access Control**: Fine-grained permissions
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- **XSS Protection**: Angular built-in sanitization
- **HTTPS Ready**: Production-ready SSL/TLS support

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is proprietary software. All rights reserved.

## 📧 Support

For support and questions, please contact the development team.

## 🎓 Credits

Developed by the CODINGFACT team for educational institutions and training centers.

---

**Happy Learning! 🚀**
