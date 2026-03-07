# TaskFlow – Smart Task Management System

A production-ready Spring Boot REST API for managing projects and tasks with JWT authentication.

---

## Table of Contents
1. [Tech Stack](#tech-stack)
2. [Project Structure](#project-structure)
3. [How to Run (Dev)](#how-to-run-dev)
4. [How to Connect MySQL (Prod)](#how-to-connect-mysql-prod)
5. [API Reference](#api-reference)
6. [Architecture Decisions](#architecture-decisions)
7. [Business Rules](#business-rules)
8. [Continuation Guide (Daily Progress)](#continuation-guide)
9. [What to Build Next](#what-to-build-next)

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Spring Boot | 3.2.0 | Framework |
| Spring Security | (included) | Auth + JWT filter |
| Spring Data JPA | (included) | Database abstraction |
| H2 Database | (included) | Dev in-memory DB |
| MySQL | 8.x | Prod database |
| JJWT | 0.11.5 | JWT generation & validation |
| Lombok | (latest) | Boilerplate reduction |
| SpringDoc OpenAPI | 2.3.0 | Swagger UI |
| BCrypt | (included) | Password hashing |

---

## Project Structure

```
src/main/java/com/taskflow/
│
├── TaskflowApplication.java         ← Entry point
│
├── controller/
│   ├── AuthController.java          ← POST /auth/register, /auth/login
│   ├── ProjectController.java       ← CRUD for /projects
│   └── TaskController.java          ← CRUD for /projects/{id}/tasks
│
├── service/
│   ├── AuthService.java             ← Registration + login logic
│   ├── ProjectService.java          ← Project business logic
│   └── TaskService.java             ← Task business logic + rules
│
├── repository/
│   ├── UserRepository.java
│   ├── ProjectRepository.java
│   └── TaskRepository.java          ← Custom queries (filter, overdue)
│
├── model/
│   ├── User.java                    ← Implements UserDetails
│   ├── Project.java
│   ├── Task.java
│   ├── Role.java                    ← Enum: USER, ADMIN
│   ├── TaskStatus.java              ← Enum: TODO, IN_PROGRESS, DONE
│   └── Priority.java                ← Enum: LOW, MEDIUM, HIGH
│
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── ProjectRequest.java
│   │   └── TaskRequest.java
│   └── response/
│       ├── ApiResponse.java         ← Standard response wrapper
│       ├── AuthResponse.java
│       ├── ProjectResponse.java     ← Includes progress %
│       └── TaskResponse.java        ← Includes overdue flag
│
├── security/
│   ├── JwtService.java              ← Token generation + validation
│   ├── JwtFilter.java               ← Per-request JWT interceptor
│   └── SecurityConfig.java          ← Security rules, public URLs
│
├── config/
│   ├── AppConfig.java               ← BCrypt, AuthManager, UserDetailsService
│   └── OpenApiConfig.java           ← Swagger + JWT auth button
│
└── exception/
    ├── GlobalExceptionHandler.java   ← @RestControllerAdvice
    ├── ResourceNotFoundException.java  ← 404
    ├── UnauthorizedException.java      ← 403
    └── BadRequestException.java        ← 400
```

---

## How to Run (Dev)

### Prerequisites
- Java 17+
- Maven 3.6+ (or use the included `mvnw` wrapper)

### Steps

```bash
# 1. Clone or navigate to the project folder
cd taskflow

# 2. Run the application (H2 in-memory database, no setup needed)
./mvnw spring-boot:run

# On Windows:
mvnw.cmd spring-boot:run
```

The app starts at **http://localhost:8080**

### Dev URLs
| URL | Purpose |
|---|---|
| http://localhost:8080/swagger-ui.html | Interactive API docs |
| http://localhost:8080/h2-console | H2 database browser |
| http://localhost:8080/api-docs | Raw OpenAPI JSON |

### H2 Console Connection Settings
```
JDBC URL:  jdbc:h2:mem:taskflowdb
Username:  sa
Password:  (leave empty)
```

---

## How to Connect MySQL (Prod)

### Step 1 — Create the database

```sql
CREATE DATABASE taskflow_db;
CREATE USER 'taskflow_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON taskflow_db.* TO 'taskflow_user'@'localhost';
```

### Step 2 — Set environment variables

**Linux/Mac:**
```bash
export DB_URL=jdbc:mysql://localhost:3306/taskflow_db
export DB_USERNAME=taskflow_user
export DB_PASSWORD=your_password
export JWT_SECRET=your-super-secret-key-minimum-32-characters
```

**Windows (Command Prompt):**
```cmd
set DB_URL=jdbc:mysql://localhost:3306/taskflow_db
set DB_USERNAME=taskflow_user
set DB_PASSWORD=your_password
set JWT_SECRET=your-super-secret-key-minimum-32-characters
```

**Alternatively — create a `.env` file (do NOT commit this to GitHub):**
```
DB_URL=jdbc:mysql://localhost:3306/taskflow_db
DB_USERNAME=taskflow_user
DB_PASSWORD=your_password
JWT_SECRET=your-super-secret-key-minimum-32-characters
```

### Step 3 — Run with prod profile

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Step 4 — Tables are created automatically
Spring JPA with `ddl-auto=update` will create all tables on first run.

---

## API Reference

### Authentication (Public — no token needed)

| Method | URL | Body | Description |
|---|---|---|---|
| POST | `/auth/register` | `{name, email, password}` | Register new user |
| POST | `/auth/login` | `{email, password}` | Login, get JWT token |

**Register Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123"
}
```

**Login/Register Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGci...",
    "type": "Bearer",
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

---

### How to Use the Token

Add this header to every protected request:
```
Authorization: Bearer eyJhbGci...
```

---

### Projects (Requires JWT)

| Method | URL | Body | Description |
|---|---|---|---|
| POST | `/projects` | `{name, description}` | Create project |
| GET | `/projects` | — | Get all your projects |
| GET | `/projects/{id}` | — | Get project with tasks + progress |
| DELETE | `/projects/{id}` | — | Delete project + all its tasks |

**Project Response includes progress:**
```json
{
  "id": 1,
  "name": "My App",
  "description": "A cool project",
  "totalTasks": 5,
  "completedTasks": 2,
  "progressPercent": 40.0
}
```

---

### Tasks (Requires JWT)

| Method | URL | Body | Description |
|---|---|---|---|
| POST | `/projects/{id}/tasks` | TaskRequest | Create task |
| GET | `/projects/{id}/tasks` | — | Get all tasks |
| GET | `/projects/{id}/tasks/priority/{priority}` | — | Filter by LOW/MEDIUM/HIGH |
| GET | `/projects/{id}/tasks/overdue` | — | Get overdue tasks |
| PUT | `/projects/{id}/tasks/{taskId}` | TaskRequest (partial) | Update task |
| DELETE | `/projects/{id}/tasks/{taskId}` | — | Delete task |

**Task Request:**
```json
{
  "title": "Design login page",
  "description": "Create wireframes",
  "status": "TODO",
  "priority": "HIGH",
  "deadline": "2025-12-31"
}
```

**Task Response:**
```json
{
  "id": 3,
  "title": "Design login page",
  "status": "TODO",
  "priority": "HIGH",
  "deadline": "2025-12-31",
  "overdue": false,
  "projectId": 1
}
```

---

## Architecture Decisions

### Why nested URLs for tasks?
`/projects/{projectId}/tasks` instead of `/tasks/{id}`:
- Ownership is clear from the URL structure
- Reduces need to pass projectId in request body
- Security: ownership verified at project level before touching tasks

### Why ApiResponse wrapper?
Every endpoint returns `{success, message, data, timestamp}`:
- Consistent shape across all endpoints
- Easy for frontend to handle errors uniformly
- `success: false` for errors without changing HTTP status reliance

### Why User implements UserDetails?
Avoids the extra UserDetails adapter class. User entity is directly usable by Spring Security, reducing boilerplate.

### Why @AuthenticationPrincipal?
Injects the authenticated User directly from the security context into controller methods. Cleaner than manually calling `SecurityContextHolder.getContext().getAuthentication()`.

---

## Business Rules

| Rule | Where Enforced |
|---|---|
| Email must be unique | UserRepository + AuthService |
| Password minimum 6 chars | RegisterRequest @Size |
| DONE task cannot be reverted | TaskService.updateTask() |
| Deadline must be future date | TaskRequest @Future |
| Users can only access own projects | ProjectRepository.findByIdAndUser() |
| Deleting project deletes all tasks | Project entity CascadeType.ALL |

---

## Continuation Guide

Since this project is being built across multiple daily sessions, here's how to pick up where you left off:

### Session Checklist
1. Pull latest code from GitHub (if pushed)
2. Run `./mvnw spring-boot:run` to verify it starts
3. Check Swagger UI at `/swagger-ui.html`
4. Test a quick endpoint via Postman or Swagger
5. Continue from the next item in "What to Build Next"

### Committing Daily Progress
```bash
git add .
git commit -m "Day X: <what you built>"
git push origin main
```

### If H2 data is lost between runs
That's expected — H2 is in-memory. Switch to `application-dev.properties`:
```properties
spring.datasource.url=jdbc:h2:file:./data/taskflowdb
```
This persists H2 to a file locally (good for dev testing without MySQL).

---

## What to Build Next

Track your progress by checking off items:

### Phase 1 — Done ✅
- [x] Project setup + dependencies
- [x] All database models
- [x] Repositories with custom queries
- [x] DTOs (request + response)
- [x] JWT security (JwtService + JwtFilter + SecurityConfig)
- [x] AuthService (register + login)
- [x] ProjectService (CRUD + ownership)
- [x] TaskService (CRUD + business rules)
- [x] All controllers
- [x] Global exception handling
- [x] Swagger/OpenAPI docs

### Phase 2 — Next Session
- [ ] **Postman collection** — test all 11 endpoints
- [ ] **Update endpoint** `PUT /projects/{id}` — edit project name/description
- [ ] **User profile endpoint** `GET /users/me` — see current user info
- [ ] **Pagination** on task list — `GET /projects/{id}/tasks?page=0&size=10`

### Phase 3 — Stretch Goals
- [ ] **Task search** — `GET /projects/{id}/tasks?search=keyword`
- [ ] **Analytics endpoint** — project summary stats
- [ ] **Role-based access** — ADMIN can see all users/projects
- [ ] **File upload** — attach files to tasks
- [ ] **Email notifications** — deadline reminders
- [ ] **Docker** — containerize the application
- [ ] **Deploy to Render/Railway** — live deployment

---

## Environment Variables Reference

| Variable | Required In | Description |
|---|---|---|
| `DB_URL` | prod | MySQL JDBC URL |
| `DB_USERNAME` | prod | MySQL username |
| `DB_PASSWORD` | prod | MySQL password |
| `JWT_SECRET` | prod | Secret key (min 32 chars) |

In dev, these are not needed — H2 is used automatically.

---

## Running Tests

```bash
./mvnw test
```

The context load test verifies all beans wire up correctly.

---

*Built with Spring Boot 3.2, Java 17, JWT, H2/MySQL*