# HostelHub - Hostel Complaint & Maintenance Management System

A production-grade Java Spring Boot backend built step-by-step through clean architectural milestones.

---

* **Milestone 1**: Java 21 + Maven project foundation & package structure
* **Milestone 2**: Spring Boot REST API + Controller/Service/Repository architecture + In-Memory storage
* **Milestone 3**: MySQL + Spring Data JPA + Hibernate Object-Relational Mapping & Database Persistence
* **Milestone 4**: Spring Security + JWT Authentication + Role-Based Authorization & BCrypt Password Hashing
* **Milestone 5**: Hostel Group & Group Membership Management
* **Milestone 6**: Advanced Complaint Management (JPA Entity Relationships, Priority System, Warden Assignment, Controlled Status Workflow, Role Visibility Scoping & Dynamic Filtering)

---

## 🛠️ Technology Stack (Milestone 6)

* **Language**: Java 21
* **Framework**: Spring Boot 3.2.4 (Spring Web, Spring Security, Spring Data JPA, Validation, Spring Boot Test)
* **Authentication**: JSON Web Tokens (JJWT 0.12.5)
* **Password Hashing**: BCrypt (`BCryptPasswordEncoder`)
* **ORM Engine**: Hibernate 6.4 (JPA `@ManyToOne` Relationships)
* **Database**: MySQL Server 9.x
* **Build Tool**: Maven 3.9+
* **Testing**: JUnit 5 (42 automated unit & integration tests passing), H2 In-Memory Database

---

## 🏛️ System Architecture

```text
                                [ Client / Postman ]
                                         │
                                         │ HTTP Request (Bearer <JWT>)
                                         ▼
                            [ JwtAuthenticationFilter ]
                                         │ Validates JWT & sets SecurityContext
                                         ▼
                             [ Spring Security Filter ]
                                         │ Checks Role Authorization (401 / 403)
                                         ▼
                            [ Controller Layer (REST) ]
                                         │ @Valid DTOs
                                         ▼
                             [ Service Layer (@Transactional) ]
                                         │ BCrypt / Business Logic
                                         ▼
                           [ JpaRepository Interfaces ]
                                         │
                                         ▼
                            [ Hibernate ORM Engine ]
                                         │
                                         ▼
                         [ MySQL Database (hostelhub) ]
                            ├── users table
                            └── complaints table
```

---

## 🔐 Key Security & Educational Concepts

### 1. Authentication vs Authorization
* **Authentication ("Who are you?")**: Verifies user identity using credentials (email + password). In HostelHub, `POST /api/auth/login` authenticates the user and issues a signed JWT.
* **Authorization ("What are you allowed to do?")**: Determines whether an authenticated user has permission to perform an action. In HostelHub:
  * `STUDENT`: Can create and view complaints.
  * `WARDEN` / `ADMIN`: Can update complaint status (`PATCH /api/complaints/{id}/status`) and delete complaints (`DELETE /api/complaints/{id}`).

### 2. Password Hashing & BCrypt
Passwords are **never stored as plain text**. HostelHub uses `BCryptPasswordEncoder`, a one-way salt-hashed algorithm. Even if database records are exposed, raw passwords cannot be reverse-engineered.

### 3. JSON Web Tokens (JWT) & Bearer Authentication
* **JWT Structure**: Composed of three Base64URL parts separated by dots (`header.payload.signature`).
* **Bearer Token**: The client sends the token in the HTTP Header:
  ```http
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
  ```
* **Statelessness**: The server verifies the cryptographic signature of the incoming JWT without needing session memory on the server.

### 4. HTTP 401 vs HTTP 403
* **401 Unauthorized**: Missing, expired, or invalid JWT token (Identity unverified).
* **403 Forbidden**: Valid token provided, but user lacks required role (e.g., a `STUDENT` trying to update complaint status).

---

## 📡 REST API Reference

### Public Endpoints (No Token Required)
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Health Check | `200 OK` |
| `POST` | `/api/auth/register` | Register new student account | `201 Created` / `409 Conflict` |
| `POST` | `/api/auth/login` | Authenticate & get JWT token | `200 OK` / `401 Unauthorized` |

### Protected Endpoints (Requires `Authorization: Bearer <JWT>`)
| Method | Endpoint | Required Role | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/complaints` | `STUDENT`, `WARDEN`, `ADMIN` | List complaints (Scoped by Role + Filterable by `status`, `priority`, `category`, `groupId`) | `200 OK` / `401` |
| `POST` | `/api/complaints` | `STUDENT`, `WARDEN`, `ADMIN` | Create complaint (Auto-maps creator `User` & `HostelGroup`) | `201 Created` / `401` |
| `GET` | `/api/complaints/{id}` | `STUDENT`, `WARDEN`, `ADMIN` | Get complaint by ID | `200 OK` / `404` / `403` |
| `PUT` | `/api/complaints/{id}` | `STUDENT`, `WARDEN`, `ADMIN` | Update complaint details | `200 OK` / `404` / `403` |
| `PATCH` | `/api/complaints/{id}/status` | `WARDEN`, `ADMIN` | Transition status (`PENDING` -> `ASSIGNED` -> `IN_PROGRESS` -> `RESOLVED` -> `CLOSED`) | `200 OK` / `400` / `403` |
| `PATCH` | `/api/complaints/{id}/assign/{wardenId}` | `WARDEN`, `ADMIN` | Assign complaint to warden (Sets status to `ASSIGNED`) | `200 OK` / `400` / `403` |
| `PATCH` | `/api/complaints/{id}/priority` | `WARDEN`, `ADMIN` | Update complaint priority (`LOW`, `MEDIUM`, `HIGH`, `URGENT`) | `200 OK` / `400` / `403` |
| `DELETE` | `/api/complaints/{id}` | `WARDEN`, `ADMIN` | Delete a complaint | `204 No Content` / `403` |
| `POST` | `/api/groups` | `ADMIN` | Create new hostel group/section | `201 Created` / `409` / `403` |
| `GET` | `/api/groups` | `STUDENT`, `WARDEN`, `ADMIN` | List all hostel groups | `200 OK` / `401` |
| `POST` | `/api/groups/{groupId}/members/{userId}` | `ADMIN`, `WARDEN` | Add user membership to group | `201 Created` / `409` / `403` |

---

## 🧪 Postman & cURL Testing Guide

### 1. Register a Student Account
* **URL**: `POST http://localhost:8080/api/auth/register`
* **Headers**: `Content-Type: application/json`
* **Body**:
```json
{
  "name": "Rohan Sharma",
  "email": "rohan@example.com",
  "password": "Password@123"
}
```
* **Response (`201 Created`)**:
```json
{
  "id": 1,
  "name": "Rohan Sharma",
  "email": "rohan@example.com",
  "role": "STUDENT",
  "createdAt": "2026-10-07T19:40:00.000000"
}
```

### 2. Login & Copy JWT Token
* **URL**: `POST http://localhost:8080/api/auth/login`
* **Body**:
```json
{
  "email": "rohan@example.com",
  "password": "Password@123"
}
```
* **Response (`200 OK`)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb2hhbkBleGFtcGxlLmNvbSIs... ",
  "type": "Bearer",
  "user": {
    "id": 1,
    "name": "Rohan Sharma",
    "email": "rohan@example.com",
    "role": "STUDENT"
  }
}
```

### 3. Access Protected Endpoint with Bearer Token
* **URL**: `POST http://localhost:8080/api/complaints`
* **Headers**:
  * `Authorization: Bearer <YOUR_JWT_TOKEN>`
  * `Content-Type: application/json`
* **Body**:
```json
{
  "title": "Water Heater Leakage",
  "description": "Geyser is dripping water in bathroom",
  "category": "PLUMBING",
  "roomNumber": "A-301",
  "studentName": "Rohan Sharma"
}
```

---

## 🔍 Direct Database Verification

Connect to MySQL:
```bash
mysql -u hostelhub_user -phostelhub_pass hostelhub
```

Inspect registered users & BCrypt hashed passwords:
```sql
SELECT id, name, email, role, password FROM users;
```

Sample Database Output:
```text
+----+--------------+-------------------+---------+--------------------------------------------------------------+
| id | name         | email             | role    | password                                                     |
+----+--------------+-------------------+---------+--------------------------------------------------------------+
|  1 | Rohan Sharma | rohan@example.com | STUDENT | $2a$10$dL5X6rgl7hmrDiF4Ie/gseqFhbagbAwleLWleXekRGeTLiOkv7BMG |
+----+--------------+-------------------+---------+--------------------------------------------------------------+
```

---

## ⚡ Running Automated Tests

Run the test suite (includes authentication, JWT verification, password hashing, and role checks):
```bash
mvn clean test
```
