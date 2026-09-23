# Student Admission Management System

A full-stack web application for managing the student admission lifecycle — from registration and course selection to application submission, document verification, payment processing, and final admission.

The system provides separate workflows for students and administrators and is designed around a RESTful Spring Boot backend with a React + Vite frontend.

---

## Live Application

**Frontend:**  
https://student-admission-frontend-df4e.onrender.com

**Backend API:**  
https://student-admission-backend-290q.onrender.com

**Backend Repository:**  
https://github.com/KRITHI1022/student-admission-system

**Frontend Repository:**  
https://github.com/KRITHI1022/student-admission-frontend

---

## Project Overview

The Student Admission Management System digitizes the admission process through a centralized platform.

### Student workflow

1. Register an account
2. Log in securely
3. Browse available courses
4. Check course eligibility
5. Submit an admission application
6. Upload required documents
7. Track document verification status
8. Make the application payment
9. Track application status
10. View final admission status

### Administrator workflow

1. Authenticate as an administrator
2. Create and manage courses
3. View student applications
4. Filter applications by status
5. Review applications
6. Update application status
7. Review uploaded documents
8. Verify or reject documents
9. Monitor payment status
10. Complete the admission process

---

## Key Features

### Authentication & Authorization

- Student registration and login
- JWT-based authentication
- BCrypt password hashing
- Role-based authorization
- Separate STUDENT and ADMIN access
- Stateless Spring Security configuration
- Protected REST endpoints

### Course Management

- Admin course creation
- Course duration
- Total seat management
- Available seat tracking
- Minimum eligibility percentage
- Application fee
- Redis caching for course retrieval

### Application Management

- Student-specific applications
- Course eligibility validation
- Duplicate application prevention
- Academic percentage validation
- Application status workflow

Supported application statuses:

```text
SUBMITTED
UNDER_REVIEW
APPROVED
REJECTED
ADMITTED
```


Application status transitions are controlled by backend business rules.

### Document Management

Students can upload admission documents such as:
```text
Aadhaar
10th Marksheet
12th Marksheet
Photograph
Signature
```

The system supports:
```text
File type validation
File extension validation
5 MB maximum file size
UUID-based stored filenames
Path traversal protection
Student ownership validation
Admin document verification
```

Document verification states:
```text
PENDING
VERIFIED
REJECTED
```
### Payment Integration

The application integrates Razorpay for admission application payments.

Payment workflow:
```text
Create Order
     ↓
Razorpay Checkout
     ↓
Payment
     ↓
Signature Verification
     ↓
Payment Status Update
```

The backend verifies:
```text
Application ownership
Razorpay order ID
Razorpay payment signature
Payment replay attempts
```

Payment states:
```text
PENDING
SUCCESS
FAILED
REFUNDED
```
### Admission & Seat Allocation

When an application reaches:
```text
APPROVED → ADMITTED
```

the system verifies seat availability and reduces the course's available seat count.

Seat allocation is handled by the backend to prevent students from manipulating admission state from the frontend.

### Notifications

Application status changes trigger asynchronous notification processing.

### Technology Stack
#### Frontend
- React
- Vite
- React Router
- Axios
- Bootstrap
- Bootstrap Icons
- JavaScript
- HTML
- CSS

#### Backend
- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- Bean Validation
- Spring Cache
- Spring Async
- REST APIs
- Maven 

#### Database & Infrastructure
- MySQL
- Aiven MySQL
- Redis
- Render

#### Payment
- Razorpay

#### Development & API Testing
- IntelliJ IDEA
- MySQL Workbench
- Postman
- Swagger / OpenAPI
- Git
- GitHub

### Architecture
                    ┌──────────────────────────┐
                    │     React + Vite         │
                    │      Frontend            │
                    │     Render Static Site   │
                    └────────────┬─────────────┘
                                 │
                                 │ HTTPS / REST API
                                 ▼
                    ┌──────────────────────────┐
                    │      Spring Boot         │
                    │       Backend            │
                    │     Render Web Service   │
                    └───────┬────────┬─────────┘
                            │        │
                 ┌──────────┘        └───────────┐
                 ▼                               ▼
        ┌──────────────────┐             ┌────────────────┐
        │   Aiven MySQL    │             │     Redis      │
        │  Production DB   │             │     Cache      │
        └──────────────────┘             └────────────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │    Razorpay     │
                   │ Payment Gateway │
                   └─────────────────┘

### Application Status Flow

The backend enforces controlled status transitions:
```text 
SUBMITTED
    │
    ├──────────────► REJECTED
    │
    ▼
UNDER_REVIEW
    │
    ├──────────────► REJECTED
    │
    ▼
APPROVED
    │
    ▼
ADMITTED
```

Invalid status transitions are rejected by the backend.

### API Overview
#### Authentication
```text
POST /api/auth/register
POST /api/auth/login
```
#### Courses
```text
GET  /api/courses
POST /api/courses/admin/create
```
#### Applications
```text
POST /api/applications
GET  /api/applications/my
GET  /api/applications/admin/all
PUT  /api/applications/admin/{applicationId}/status
```
#### Documents
```text
POST /api/documents/upload/{applicationId}
GET  /api/documents/application/{applicationId}
GET  /api/documents/view/{documentId}
PUT  /api/documents/admin/{documentId}/verify
```
#### Payments
```text
POST /api/payments/create-order/{applicationId}
POST /api/payments/verify
```
Swagger/OpenAPI documentation is available from the deployed backend.

### Security

The application implements multiple layers of backend validation and authorization.

#### Authentication

JWT tokens are used to authenticate API requests.

#### Authorization

Administrative endpoints are protected using Spring Security roles.
```text
STUDENT
ADMIN
```

Students cannot access administrative operations.

#### Input Validation

The backend validates:

- Required fields
- Email format
- Password length
- Academic percentages
- Course IDs
- Application ownership
- Payment information
- Document types
- Uploaded file size and type

#### File Security

Uploaded files are protected through:

- Allowed MIME types
- Allowed extensions
- Maximum file size
- UUID filenames
- Normalized file paths
- Path traversal checks
- Ownership checks

#### Payment Security

Payment verification validates the Razorpay signature before marking a payment as successful.

#### Business Rule Enforcement

Critical application state is controlled on the backend rather than trusting frontend input.

Examples:

- Student identity comes from authentication
- Application status is controlled by backend transitions
- Duplicate applications are rejected
- Eligibility is checked server-side
- Seat allocation occurs during admission
- Students cannot modify administrative state

### Database

The application uses MySQL with Spring Data JPA and Hibernate.

Major entities include:
```text
User
Student
Course
Application
Document
Payment
```

Relationships are maintained using JPA entity mappings.

The production database is hosted using Aiven MySQL.

### Redis Caching

Course retrieval uses Spring Cache with Redis.

Course data is cached for repeated reads and the cache is evicted when course-related data changes.

This reduces unnecessary database queries for frequently accessed course information.

### Local Development

#### Prerequisites

Install:

- Java 21
- Maven
- Node.js
- MySQL
- Redis
- Git

#### Backend

Clone the repository:
```text 
git clone https://github.com/KRITHI1022/student-admission-system.git
cd student-admission-system
```
Configure the required environment variables before starting the backend.

Example:
```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
REDIS_SSL
RAZORPAY_KEY_ID
RAZORPAY_KEY_SECRET
FRONTEND_URL
```

Then start the application:
```text
./mvnw spring-boot:run
```
On Windows:
```text
mvnw.cmd spring-boot:run
```
The backend runs on:
```text
http://localhost:8080
```
#### Frontend

Clone the frontend repository:
```text
git clone https://github.com/KRITHI1022/student-admission-frontend.git
cd student-admission-frontend
```
Install dependencies:
```text
npm install
```

Create a frontend environment file:
```text
VITE_API_BASE_URL=http://localhost:8080
```
Start the development server:
```
npm run dev
```
The frontend runs on the Vite development server.

### Environment Variables
#### Backend
```
DB_URL=
DB_USERNAME=
DB_PASSWORD=

JWT_SECRET=
JWT_EXPIRATION=

REDIS_HOST=
REDIS_PORT=
REDIS_PASSWORD=
REDIS_SSL=

RAZORPAY_KEY_ID=
RAZORPAY_KEY_SECRET=

FRONTEND_URL=
```

#### Frontend
```
VITE_API_BASE_URL=
```

Never commit passwords, JWT secrets, database credentials, or payment gateway secrets to GitHub.

### Deployment
#### Frontend

The React/Vite frontend is deployed as a Render Static Site.

Build command:
```
npm install && npm run build
```
Publish directory:
```
dist
```
A Render rewrite routes:
```
/*
```

to:
```
/index.html
```
This allows React Router client-side routes to work correctly in production.

#### Backend

The Spring Boot backend is deployed on Render using Docker.

The application uses Java 21 and listens on the Render-provided PORT.

#### Database

Production MySQL is hosted on Aiven.

#### Cache

Redis is used for application caching.

### Testing

The application has been tested across authentication, authorization, validation, document handling, payment verification, application workflows, and production deployment.

#### Security scenarios tested
- Student access to admin endpoints
- Unauthorized application status modification
- Unauthorized document verification
- Cross-student document access
- JWT tampering
- Invalid authentication
- Role injection during registration
- Student ID injection
- Application status injection
- Invalid application status transitions
- Duplicate applications
- Invalid academic percentages
- Invalid course IDs
- Eligibility validation
- Zero-seat admission handling
- Invalid document types
- Oversized files
- Renamed executable/archive files
- File path traversal protection
- Payment ownership validation
- Invalid payment order IDs
- Invalid Razorpay signatures
- Payment replay attempts

#### End-to-end workflow

A complete admission workflow has been verified:
```
Student Registration
        ↓
Login
        ↓
Browse Courses
        ↓
Submit Application
        ↓
Upload Document
        ↓
Admin Verifies Document
        ↓
Student Makes Payment
        ↓
Payment SUCCESS
        ↓
SUBMITTED
        ↓
UNDER_REVIEW
        ↓
APPROVED
        ↓
ADMITTED
        ↓
Course Seat Reduced
```

The complete workflow has also been verified in the deployed production environment.

### Project Structure
#### Backend
```
student-admission-system/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/kirithika/studentadmission/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── enums/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── Dockerfile
├── pom.xml
├── mvnw
└── README.md
```
#### Frontend
```
student-admission-frontend/
│
├── src/
│   ├── api/
│   ├── assets/
│   ├── components/
│   ├── context/
│   ├── pages/
│   │   ├── admin/
│   │   └── student/
│   ├── App.jsx
│   ├── main.jsx
│   └── index.css
│
├── package.json
├── vite.config.js
└── README.md
```

### Future Improvements

Potential future enhancements include:

- Email notification provider integration
- Cloud object storage for uploaded documents
- Application search and pagination
- Advanced admin analytics
- Audit logging
- Production monitoring
- Automated CI/CD pipeline
- Refresh-token based authentication
- Automated database migrations using Flyway or Liquibase
- Additional payment reconciliation features

### Author

Kirithika R.



##### GitHub:

https://github.com/KRITHI1022