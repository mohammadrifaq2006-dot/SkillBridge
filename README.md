# SkillBridge – Online Learning Platform

**BTech Technical Training Mini-Project**

## 1. Project Introduction

**SkillBridge** is a full-stack online learning platform where students can browse technical courses, register or log in, enroll in courses, track module-by-module completion progress on a personalized Learner Dashboard, and download a completion certificate after finishing a course (100% completion).

Authorized administrators can add, edit, and delete courses, view registered users, monitor course enrollments, and review submitted contact inquiries. The platform also includes an AI Learning Assistant endpoint that retrieves relevant course records from MySQL to demonstrate practical Retrieval-Augmented Generation (RAG) before calling an external LLM API.

---

## 2. Key Features

- **Home Page & Course Catalog**: Browse courses by keyword search and subject category filter, with clear Free/Paid badges, duration, instructor details, syllabi, and prerequisites.
- **Authentication & Security**: User registration, login, and verified password reset backed by **Spring Security**, **JWT (JSON Web Tokens)**, and **BCrypt** password hashing.
- **Role-Based Access Control (RBAC)**: Enforced on both the React frontend (`ProtectedRoute`) and Spring Boot backend (`SecurityConfig` and `@PreAuthorize`) for `LEARNER` and `ADMIN` roles.
- **Learner Dashboard & Certificate Download**: New learners start with an empty dashboard; each learner sees only their own real enrollments and progress stored in MySQL. Completed courses (`100%`) unlock a downloadable PNG completion certificate.
- **Admin Course Management**: Create, edit (with confirmation modal), and delete (with confirmation modal) courses, plus tabular views for registered users, enrollments, and contact inquiries.
- **About, FAQ & Contact Persistence**: Validated contact form that saves inquiries in MySQL (`contact_inquiries` table) and displays a confirmation reference ID only after database persistence.
- **AI Learning Assistant (RAG)**: Backend `/api/chatbot/ask` endpoint retrieves matching course records from MySQL to build context for an external LLM API (`LLM_API_KEY` / `GEMINI_API_KEY`). If no API key is configured, it displays a clear configuration notice rather than generating fake AI answers.

---

## 3. Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Frontend** | React 18, Vite 5, React Router v6, Axios (with JWT interceptors), Plain Responsive CSS |
| **Backend** | Java 21, Spring Boot 3.3.4, Spring Web, Spring Cache, Spring Boot Actuator |
| **Database & ORM** | MySQL 8.0, Spring Data JPA, Hibernate |
| **Security** | Spring Security 6, JJWT (`0.12.6`), BCrypt Password Hashing |
| **Validation & Error Handling** | Jakarta Validation (`@Valid`), Centralized `@ControllerAdvice` (`GlobalExceptionHandler`) |
| **API Documentation** | SpringDoc OpenAPI 3 / Swagger UI |
| **Testing** | JUnit 5, Mockito, Spring Security Test (`MockMvc`) |
| **Containerization** | Docker (Multi-stage Dockerfiles), Docker Compose, Nginx |

---

## 4. Folder Structure

```text
skillbridge-lms/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/skillbridge/lms/
│       │   │   ├── SkillBridgeApplication.java
│       │   │   ├── config/          # SecurityConfig, OpenApiConfig, DataInitializer
│       │   │   ├── controller/      # Course, Auth, Enrollment, Admin, Contact, Chatbot Controllers
│       │   │   ├── dto/             # Request/Response DTOs (never exposes password hashes)
│       │   │   ├── entity/          # User, Course, Enrollment, Progress, ContactInquiry, Role
│       │   │   ├── exception/       # GlobalExceptionHandler (@ControllerAdvice) & Custom Exceptions
│       │   │   ├── repository/      # Spring Data JPA Repositories
│       │   │   ├── security/        # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
│       │   │   └── service/         # Business logic & MySQL RAG retrieval
│       │   └── resources/
│       │       └── application.properties
│       └── test/
│           └── java/com/skillbridge/lms/ # JUnit 5 & Mockito unit and security tests
├── frontend/
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/axiosClient.js       # Axios instance with JWT request/response interceptors
│       ├── components/              # Navbar, Footer, CourseCard, ConfirmModal, Pagination, ProtectedRoute
│       ├── context/AuthContext.jsx  # Authentication context & session management
│       ├── pages/                   # Home, Catalog, CourseDetails, Login, Register, Reset, Dashboards, Chatbot, About
│       ├── App.jsx
│       ├── index.css
│       └── main.jsx
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 5. Database Setup (MySQL)

1. Ensure **MySQL Server 8.0+** is installed and running on `localhost:3306`.
2. The application connects to `skillbridge_lms_db` and automatically creates the database (`createDatabaseIfNotExist=true`) and schema (`spring.jpa.hibernate.ddl-auto=update`) via JPA entities:
   - `users`: `id`, `name`, `email` (unique), `password_hash`, `role`, `creation_date`
   - `courses`: `id`, `title`, `category`, `description`, `instructor`, `duration`, `price`, `syllabus`, `prerequisites`, `image_url`
   - `enrollments`: `id`, `user_id` (FK), `course_id` (FK), `enrollment_date`, `status` — with unique constraint `uk_enrollment_user_course (user_id, course_id)` preventing duplicate enrollments
   - `progress`: `id`, `enrollment_id` (unique FK), `completion_percentage`, `last_updated`
   - `contact_inquiries`: `id`, `name`, `email`, `subject`, `message`, `creation_date`

### Managing Sample Catalog Courses & Admin Account
- **No Fake Student Activity**: The application never inserts fake student accounts, fake enrollments, or fake progress records. Every registered learner starts with an empty dashboard.
- **Sample Catalog Courses**: On first startup, `DataInitializer` seeds 7 sample technical courses if the `courses` table is empty and `SEED_SAMPLE_COURSES=true` (default).
  - To disable sample course seeding, set `SEED_SAMPLE_COURSES=false`.
  - To remove sample courses at any time, log in to the Admin Dashboard (`/admin`) and delete them, or run `DELETE FROM courses;` in MySQL.
- **Bootstrap Administrator**: On startup, if `admin@skillbridge.edu` does not exist, it is created with role `ADMIN` (password configured via `ADMIN_PASSWORD` environment variable, or `Admin@123` for local development if `ADMIN_PASSWORD` is unset).

---

## 6. Environment Variable Setup

Configure the following environment variables before starting the backend (see `.env.example`):

| Variable | Description | Default (Local Dev) |
| :--- | :--- | :--- |
| `DB_URL` | JDBC connection URL for MySQL | `jdbc:mysql://localhost:3306/skillbridge_lms_db?...` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | *(Set to your local MySQL password)* |
| `JWT_SECRET` | Secret key (32+ chars) for signing JWT tokens | *(Dev fallback provided; override in prod)* |
| `ADMIN_EMAIL` | Bootstrap administrator email | `admin@skillbridge.edu` |
| `ADMIN_PASSWORD` | Bootstrap administrator password | `Admin@123` (if unset) |
| `SEED_SAMPLE_COURSES` | Seed initial course catalog when database is empty | `true` |
| `LLM_API_KEY` | API key for external LLM (`GEMINI_API_KEY` also supported) | *(Empty by default)* |
| `LLM_PROVIDER` | `gemini` or `openai` | `gemini` |
| `LLM_MODEL` | Model identifier | `gemini-2.5-flash` |

---

## 7. Commands to Build and Run Locally

### A. Start the Spring Boot Backend (Port 8080)

Open a terminal in `backend/`:

```powershell
cd backend
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"
# Optional: enable live AI chatbot responses
# $env:LLM_API_KEY="your_gemini_api_key"

mvn spring-boot:run
```

To build a production JAR:

```powershell
cd backend
mvn clean package
java -jar target/skillbridge-lms-backend-1.0.0.jar
```

### B. Start the React Frontend (Port 5173)

Open a second terminal in `frontend/`:

```powershell
cd frontend
npm install
npm run dev
```

Open **`http://localhost:5173`** in your browser.

To create a production frontend bundle:

```powershell
cd frontend
npm run build
npm run preview
```

---

## 8. API Documentation (Swagger / OpenAPI)

While the Spring Boot backend is running on port `8080`:

- **Swagger UI Interactive Console**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI 3 JSON Specification**: `http://localhost:8080/api-docs`
- **Spring Boot Actuator Health Check**: `http://localhost:8080/actuator/health`

To test protected endpoints in Swagger UI:
1. Call `POST /api/auth/login` or `POST /api/auth/register` and copy the `token` field from the JSON response.
2. Click the **Authorize** button at the top of Swagger UI, paste the token, and click **Authorize**.

---

## 9. Testing Instructions

Backend unit and security tests use **JUnit 5**, **Mockito**, and **Spring Security Test** (`MockMvc`) covering user registration validation, duplicate email detection, BCrypt password hashing, login authentication, course CRUD operations, enrollment & progress completion, and role-based endpoint restrictions.

Run all tests with:

```powershell
cd backend
mvn test
```

---

## 10. Docker Instructions

1. Copy `.env.example` to `.env` in the project root and set `DB_PASSWORD`, `JWT_SECRET`, and (optionally) `LLM_API_KEY`.
2. Build and start MySQL, the Spring Boot backend, and the React/Nginx frontend using Docker Compose:

```powershell
docker compose up --build -d
```

- Frontend UI: `http://localhost:5173`
- Backend API & Swagger UI: `http://localhost:8080/swagger-ui.html`
- Stop containers: `docker compose down`

---

## 11. Deployment Preparation Guide

The project is prepared for deployment (not claimed as live-deployed until hosted with your cloud credentials):

### Backend + MySQL Deployment (e.g., Render, Railway, or AWS Elastic Beanstalk)
1. Provision a managed MySQL 8 instance and note the JDBC URL, username, and password.
2. Deploy the `backend/` directory using `backend/Dockerfile` (or `mvn clean package -DskipTests`).
3. Configure environment variables on the cloud host:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
   - `JWT_SECRET` (strong 256-bit secret)
   - `ADMIN_EMAIL`, `ADMIN_PASSWORD`
   - `LLM_API_KEY`
   - `CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com`

### Frontend Deployment (e.g., Vercel, Netlify, or Render Static Site)
1. Connect the `frontend/` directory.
2. Set build command to `npm run build` and publish directory to `dist`.
3. Set environment variable `VITE_API_BASE_URL=https://your-backend-domain.com/api`.
4. Configure SPA rewrite rules (`/* -> /index.html`) so React Router paths resolve properly on page refresh.
