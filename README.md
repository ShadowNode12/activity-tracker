# Daily Activity Tracker 🚀

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21 target">
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.1-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot 4.1.1">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql" alt="MySQL 8.0">
  <img src="https://img.shields.io/badge/Security-JWT_%26_BCrypt-red?style=for-the-badge&logo=jsonwebtokens" alt="JWT and BCrypt">
  <img src="https://img.shields.io/badge/AI-Google_Gemini-purple?style=for-the-badge&logo=google" alt="Google Gemini">
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven" alt="Maven">
</p>

Daily Activity Tracker is a productivity application backend built with Java and Spring Boot. It allows users to manage daily tasks, monitor productivity, view motivational quotes, and receive AI-generated coaching based on their progress.

The project focuses on backend engineering practices, including stateless authentication, database-backed persistence, input validation, centralized exception handling, automated testing, and external AI integration.

---

## 🏛️ Core Architectural Highlights

- **Layered Architecture:** Separates controllers, services, repositories, entities, DTOs, security, and exception handling.
- **Stateless Authentication:** Uses JSON Web Tokens (JWT) for authentication and BCrypt for password hashing.
- **User-Specific Data Access:** Retrieves personal task data using the authenticated user's identity and applies ownership checks to task operations.
- **Server-Side AI Integration:** Calls Google Gemini from the backend, keeping the API key out of frontend code.
- **Bounded AI Context:** Limits detailed task history to the 30 most recent entries and applies a 12,000-character prompt budget to control input size.
- **Centralized Exception Handling:** Uses `@ControllerAdvice` to return structured JSON errors for supported application exceptions.
- **Database Migrations:** Uses Flyway to version and apply database schema changes.
- **Automated Testing:** Uses JUnit and Mockito to test service behavior, authentication utilities, error handling, and AI-related logic.

---

## 🚀 Features

### Implemented

- **User Authentication**
  - User signup and login.
  - BCrypt password hashing.
  - JWT generation and authentication.
  - Duplicate email handling with `409 Conflict`.

- **Daily Task Management**
  - Create, retrieve, update, and delete tasks.
  - Store task dates, descriptions, and completion status.
  - Enforce user-specific task access.

- **Progress Analytics**
  - Calculate total and completed tasks.
  - Calculate completion rate and daily progress.
  - Track the current productivity streak.

- **Daily Quote Engine**
  - Store and retrieve motivational quotes.
  - Maintain a daily quote through scheduled processing.

- **AI Productivity Coach**
  - Integrate Google Gemini through the backend.
  - Generate structured coaching responses containing `summary`, `feedback`, `suggestion`, and `motivation`.
  - Use aggregate progress statistics and recent task history to build the prompt.
  - Handle Gemini rate limits, authentication/configuration errors, service failures, and malformed responses.
  - Bound detailed task history and prompt size to help control input usage.

- **Testing and Reliability**
  - Automated tests for authentication, task services, progress calculations, and AI coaching.
  - Mocked dependencies to test error scenarios without requiring real Gemini API calls.
  - A passing test suite at the current development checkpoint.

### In Progress

- **AI Usage Quotas**
  - MySQL tables and JPA entities for per-user and global daily usage.
  - Repository interfaces and quota exception handling.
  - Configurable daily quota limits.
  - Atomic quota reservations and enforcement under concurrent requests are not yet complete.

### Planned

- React frontend integration.
- Swagger UI / OpenAPI documentation.
- Production deployment and cloud database configuration.
- Additional integration and concurrency tests for AI quotas.

---

## 🛠️ Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 compilation target |
| Framework | Spring Boot 4.1.1 |
| Web | Spring Web, REST APIs |
| Security | Spring Security, JWT, BCrypt |
| Database | MySQL 8.0 |
| Persistence | Spring Data JPA, Hibernate |
| Schema migrations | Flyway |
| Validation | Jakarta Bean Validation |
| JSON processing | Jackson |
| AI integration | Google GenAI Java SDK with Gemini |
| Testing | JUnit, Mockito, Spring Boot Test |
| Build tool | Maven |
| API testing | Postman |

The project has also been tested with Java 24 as the runtime while compiling against the Java 21 target.

---

## 📁 Project Structure

```text
src/main/java/com/example/activitytracker/
├── config/          # Gemini configuration, quote seeding and scheduling
├── controller/      # REST API controllers
├── DTO/             # Request and response data-transfer objects
├── exception/       # Custom exceptions and global exception handling
├── model/           # JPA entities, including AI usage records
├── repository/      # Spring Data JPA repositories
├── security/        # JWT authentication filter and security configuration
├── service/         # Business logic and service interfaces
└── util/            # JWT utilities

src/main/resources/
├── application.properties
└── db/migration/    # Versioned Flyway SQL migrations
```

---

## 🔌 API Endpoints

Base URL: `http://localhost:8080`

| Method | Endpoint | Purpose | Authentication |
|---|---|---|---|
| POST | `/api/auth/signup` | Register a user | No |
| POST | `/api/auth/login` | Authenticate and receive a JWT | No |
| GET | `/api/tasks` | Retrieve the authenticated user's tasks | Yes |
| GET | `/api/tasks/{id}` | Retrieve a task with ownership checks | Yes |
| POST | `/api/tasks` | Create a task | Yes |
| PUT | `/api/tasks/{id}` | Update a task with ownership checks | Yes |
| DELETE | `/api/tasks/{id}` | Delete a task with ownership checks | Yes |
| GET | `/api/progress` | Retrieve productivity statistics | Yes |
| GET | `/api/daily-quote` | Retrieve today's quote | Depends on security configuration |
| GET | `/api/ai-coach` | Generate AI productivity feedback | Yes |

---

## 🚦 HTTP Status Codes

The API uses standard HTTP status codes for successful requests and common error conditions.

| Status | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `201 Created` | Resource created successfully |
| `400 Bad Request` | Invalid request or validation failure |
| `401 Unauthorized` | Missing/invalid authentication or invalid credentials |
| `403 Forbidden` | Authenticated user lacks permission |
| `404 Not Found` | Requested resource was not found |
| `409 Conflict` | Data conflict, such as duplicate email registration |
| `429 Too Many Requests` | AI upstream rate limit or application quota rejection, as applicable |
| `500 Internal Server Error` | Unexpected server-side failure |
| `503 Service Unavailable` | AI service temporarily unavailable |

Error responses use a structured format for supported exceptions. Application-level AI quota enforcement remains in progress.

---

## ⚙️ Local Setup and Installation

### 1. Clone the repository

```bash
git clone https://github.com/ShadowNode12/activity-tracker.git
cd activity-tracker
```

### 2. Create the MySQL database

Create the database in MySQL:

```sql
CREATE DATABASE activitytracker;
```

Flyway manages versioned schema migrations when the application starts. Do not manually recreate tables already managed by migrations.

### 3. Configure environment variables

The application reads database credentials and the JWT secret from environment variables. The Gemini API key is required for AI coaching but should remain optional for starting the rest of the backend.

For Windows Command Prompt:

```bat
set DB_USERNAME=your_mysql_username
set DB_PASSWORD=your_mysql_password
set JWT_SECRET=your_base64_encoded_jwt_secret
set GEMINI_API_KEY=your_gemini_api_key
```

Use your own credentials and a sufficiently strong, Base64-encoded JWT secret. Keep actual secrets out of Git and never commit `.env` files containing credentials.

These commands configure variables for the current terminal session. Start the application from that same session.

### 4. Run the application

```bash
mvn spring-boot:run
```

### 5. Run automated tests