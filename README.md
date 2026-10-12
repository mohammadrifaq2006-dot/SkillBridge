# SkillBridge – Online Learning Platform

## 1. Project Overview

**SkillBridge** is a full-stack online learning platform built with **React + Vite**, **Java 21 + Spring Boot**, and **MySQL** (Aiven MySQL in production, local MySQL in development). Students can browse published courses and categories without logging in, register for a learner account, log in with JWT authentication, enroll in courses, update module completion progress, download completion certificates at 100%, and ask questions in the MySQL RAG-powered AI Learning Assistant.

---

## 2. Technology Stack

- **Frontend**: React 18, Vite 5, React Router v6, Axios (with JWT request/response interceptors and automatic API base URL normalization), Plain Responsive CSS (`#14213D` Deep Navy, `#2563EB` Royal Blue, `#14B8A6` Teal, `#FFFFFF` / `#F3F6FB` surfaces)
- **Backend**: Java 21, Spring Boot 3.3.4, Spring Data JPA, Hibernate, Spring Security 6, JJWT (`0.12.6`), BCrypt, Jakarta Validation, Spring Cache, Spring Boot Actuator, SpringDoc OpenAPI 3
- **Database**: MySQL 8 (Aiven Cloud MySQL `defaultdb` in production)
- **Testing**: JUnit 5, Mockito, Spring Security Test (`MockMvc`) — 26 automated tests

---

## 3. Environment Variables

### Backend Environment Variables (Render Web Service)

| Variable Name | Description |
| :--- | :--- |
| `DB_URL` | Aiven MySQL JDBC URL (`jdbc:mysql://<host>:<port>/defaultdb?sslMode=REQUIRED`) or `mysql://...` service URI |
| `DB_USERNAME` | Aiven MySQL username |
| `DB_PASSWORD` | Aiven MySQL password |
| `JWT_SECRET` | Strong 32+ character secret string for signing JWT tokens |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins (e.g., `https://your-frontend.onrender.com,http://localhost:5173`) |
| `ADMIN_EMAIL` | Administrator account email to provision on startup |
| `ADMIN_PASSWORD` | Administrator account password (hashed with BCrypt on startup) |
| `SEED_SAMPLE_COURSES` | `true` to seed initial catalog courses when database is empty |
| `LLM_API_KEY` | Gemini API key for the AI Learning Assistant (`GEMINI_API_KEY` also supported) |
| `LLM_PROVIDER` | `gemini` (default) or `openai` |
| `LLM_MODEL` | `gemini-2.5-flash` (default) |

### Frontend Environment Variables (Render Static Site)

| Variable Name | Expected Production Value |
| :--- | :--- |
| `VITE_API_BASE_URL` | `https://skillbridge-backend-api.onrender.com/api` |

---

## 4. Local Development & Testing

### Backend (Port 8080)

```powershell
cd backend
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_local_mysql_password"
$env:ADMIN_EMAIL="admin@skillbridge.edu"
$env:ADMIN_PASSWORD="your_local_admin_password"
mvn spring-boot:run
```

Run automated tests (`26` unit & security tests):

```powershell
cd backend
mvn test
```

### Frontend (Port 5173)

```powershell
cd frontend
npm install
npm run dev
```

Build production static bundle:

```powershell
cd frontend
npm run build
```

---

## 5. Render Deployment Settings

### A. Backend — Render Web Service (`https://skillbridge-backend-api.onrender.com`)
- **Repository**: `https://github.com/mohammadrifaq2006-dot/SkillBridge`
- **Branch**: `main`
- **Root Directory**: `backend`
- **Runtime**: `Docker`
- **Dockerfile Path**: `Dockerfile` (inside `backend/`)
- **Health Check Path**: `/actuator/health`

### B. Frontend — Render Static Site
- **Repository**: `https://github.com/mohammadrifaq2006-dot/SkillBridge`
- **Branch**: `main`
- **Root Directory**: `frontend`
- **Build Command**: `npm install && npm run build`
- **Publish Directory**: `dist`
- **Environment Variable**: `VITE_API_BASE_URL=https://skillbridge-backend-api.onrender.com/api`
- **SPA Rewrite Rule**: Configured automatically via `frontend/public/_redirects` (`/* /index.html 200`), or in Render Dashboard under **Redirects/Rewrites**: Source `/*`, Destination `/index.html`, Action `Rewrite`.
