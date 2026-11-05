# Daily Activity Tracker 🚀

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.1-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Security-JWT_%26_BCrypt-red?style=for-the-badge&logo=jsonwebtokens" alt="JWT and BCrypt">
  <img src="https://img.shields.io/badge/AI-Google_Gemini-purple?style=for-the-badge&logo=google" alt="Google Gemini">
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven" alt="Maven">
</p>

Daily Activity Tracker is a productivity application designed to help users manage daily tasks, monitor their progress, maintain consistency, and receive AI-generated productivity coaching.

The backend is built using Java and Spring Boot, with MySQL for persistence and Google Gemini for AI-powered coaching.

The project emphasizes backend engineering practices such as stateless authentication, user-specific data access, database migrations, input validation, centralized exception handling, automated testing, and controlled AI API usage.

---

## 🏛️ Architecture Highlights

- **Layered architecture:** Separates controllers, services, repositories, entities, DTOs, security, and exception handling.
- **JWT authentication:** Stateless authentication using JSON Web Tokens and BCrypt password hashing.
- **User-specific data access:** Task operations enforce ownership based on the authenticated user.
- **Server-side AI integration:** Gemini API calls are handled by the backend; API keys are not exposed to frontend code.
- **Bounded AI context:** Uses recent task history and a prompt-size budget to control AI input.
- **Centralized exception handling:** Returns structured error responses for supported application exceptions.
- **Database migrations:** Flyway manages versioned database schema changes.
- **AI usage protection:** Database-backed daily quotas help control per-user and global Gemini API usage.
- **Automated testing:** JUnit, Mockito, and Spring Boot Test cover service logic, security utilities, and AI quota behavior.

---

## 🚀 Features

### Implemented

#### User Authentication
- User signup and login.
- BCrypt password hashing.
- JWT generation and authentication.
- Duplicate email handling.
- Protected endpoints requiring authentication.

#### Daily Task Management
- Create, retrieve, update, and delete tasks.
- Store task dates, descriptions, and completion status.
- Enforce user-specific task access.
- Validate incoming task requests.

#### Progress Analytics
- Calculate total and completed tasks.
- Calculate completion rate and daily progress.
- Track productivity streaks.

#### Daily Quote Engine
- Store and retrieve motivational quotes.
- Maintain a shared daily quote.
- Generate the daily quote through scheduled processing.
- Ensure today's quote is available when the application starts.

#### AI Productivity Coach
- Integrate Google Gemini through the backend.
- Generate structured coaching responses containing:
  - `summary`
  - `feedback`
  - `suggestion`
  - `motivation`
- Use productivity statistics and recent task history as AI context.
- Handle AI provider errors and malformed responses.
- Keep Gemini configuration optional so the backend can start without an API key.

#### AI Usage Quotas
- Configurable per-user and global daily request limits.
- Atomic database quota reservation.
- Transactional reservation to prevent partial quota consumption when reservation fails.
- Persistent quota tracking.
- Daily quota reset based on the configured `Asia/Kolkata` time zone.

#### Testing and Reliability
- Automated tests for authentication, task management, progress calculations, AI coaching, and quota behavior.
- Mocked AI dependencies to test failure scenarios without making real Gemini API calls.
- Integration tests for database-backed quota behavior and transaction rollback.

---

## 🛡️ AI Coach Quota Policy

The AI Coach uses daily limits to control API usage and help prevent excessive requests.

| Limit | Default |
|---|---:|
| Per-user daily limit | 5 requests |
| Global daily limit | 100 requests |
| Quota time zone | `Asia/Kolkata` |

### Quota reservation behavior

- Quota is reserved before sending a request to Gemini.
- Requests rejected before quota reservation do not consume a reservation.
- Once reserved, a quota is **not automatically refunded** if Gemini fails, times out, or returns an error.
- This policy accounts for cases where Gemini may have received or processed the request even if the backend did not receive a successful response.
- User and global quota reservations are handled transactionally.
- If global quota reservation fails, the preceding user reservation is rolled back.

Quota limits and the time zone are configurable through application properties.

---

## 🛠️ Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 compilation target |
| Framework | Spring Boot 4.1.1 |
| Web | Spring WebMVC, REST APIs |
| Security | Spring Security, JWT, BCrypt |
| Database | MySQL 8 |
| Persistence | Spring Data JPA, Hibernate |
| Database migrations | Flyway |
| Validation | Jakarta Bean Validation |
| JSON processing | Jackson |
| AI integration | Google GenAI Java SDK, Gemini |
| Testing | JUnit, Mockito, Spring Boot Test |
| Build tool | Maven |
| API testing | Postman |

The project has been tested locally with Java 24 as the runtime while targeting Java 21 for compilation.

---

## 📁 Project Structure

```text
src/main/java/com/example/activitytracker/
├── config/          # Application configuration and scheduling
├── controller/      # REST API controllers
├── DTO/             # Request and response DTOs
├── exception/       # Custom exceptions and global exception handling
├── model/           # JPA entities
├── repository/      # Spring Data JPA repositories
├── security/        # JWT authentication and security configuration
├── service/         # Business logic and service interfaces
└── util/            # JWT utilities

src/main/resources/
├── application.properties
└── db/migration/    # Flyway SQL migrations
```

---

## 🔌 API Endpoints

Base URL:

```text
http://localhost:8080
```

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/signup` | Register a user |
| POST | `/api/auth/login` | Login and receive a JWT |
| GET | `/api/tasks` | Retrieve the authenticated user's tasks |
| GET | `/api/tasks/{id}` | Retrieve a task |
| POST | `/api/tasks` | Create a task |
| PUT | `/api/tasks/{id}` | Update a task |
| DELETE | `/api/tasks/{id}` | Delete a task |
| GET | `/api/progress` | Retrieve productivity statistics |
| GET | `/api/daily-quote` | Retrieve today's motivational quote |
| GET | `/api/ai-coach` | Generate AI productivity coaching |

Protected endpoints require a valid JWT in the Authorization header:

```http
Authorization: Bearer <your_jwt_token>
```

---

## 🚦 HTTP Status Codes

| Status | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `201 Created` | Resource created successfully |
| `204 No Content` | Resource deleted successfully |
| `400 Bad Request` | Invalid request or validation failure |
| `401 Unauthorized` | Missing or invalid authentication |
| `403 Forbidden` | Access denied |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Conflict, such as duplicate email registration |
| `429 Too Many Requests` | AI quota exceeded or upstream rate limit |
| `500 Internal Server Error` | Unexpected server-side failure |
| `502 Bad Gateway` | Invalid or unsuccessful upstream response |
| `503 Service Unavailable` | AI service temporarily unavailable |

Supported errors use a structured JSON response.

---

## ⚙️ Local Setup and Installation

### Prerequisites

- Java 21 or compatible JDK
- Maven
- MySQL 8
- Google Gemini API key (optional; required for AI Coach functionality)

### 1. Clone the repository

```bash
git clone https://github.com/ShadowNode12/activity-tracker.git
cd activity-tracker
```

### 2. Create the MySQL database

```sql
CREATE DATABASE activitytracker;
```

Flyway manages the schema migrations when the application starts.

### 3. Configure environment variables

The application uses environment variables for database credentials and JWT configuration.

For Windows Command Prompt:

```bat
set DB_USERNAME=your_mysql_username
set DB_PASSWORD=your_mysql_password
set JWT_SECRET=your_base64_encoded_jwt_secret
set GEMINI_API_KEY=your_gemini_api_key
```

- Use your own MySQL credentials.
- Configure a strong Base64-encoded JWT secret.
- `GEMINI_API_KEY` is optional for application startup.
- Keep real credentials and API keys out of source control.

These commands configure variables for the current terminal session. Start the application from that same terminal.

### 4. Run the application

```bash
mvn spring-boot:run
```

The backend starts on:

```text
http://localhost:8080
```

### 5. Run automated tests

```bash
mvn clean test
```

The test suite includes unit tests and database-backed integration tests. Configure the test database as required by the test profile.

### 6. Package the application

```bash
mvn package
```

The executable Spring Boot JAR is generated under:

```text
target/activitytracker-0.0.1-SNAPSHOT.jar
```

Run the packaged application:

```bash
java -jar target/activitytracker-0.0.1-SNAPSHOT.jar
```

---

## 🧪 Current Verification

The backend has been locally verified with:

- **41 automated tests passing**
- Successful Maven packaging
- Successful executable JAR startup
- Spring Boot running on port `8080`
- Flyway migrations applied successfully during application startup

---

## 📌 Project Status

The backend currently includes authentication, task management, progress analytics, daily quotes, AI coaching, AI quota enforcement, and automated testing.

### Next milestones
- React frontend integration.
- Swagger / OpenAPI documentation.
- Production deployment.
- Further integration and concurrency testing.

---

## 👤 Author

**ShadowNode12**

- GitHub: [@ShadowNode12](https://github.com/ShadowNode12)
- Repository: [Daily Activity Tracker](https://github.com/ShadowNode12/activity-tracker)

---

*This project is under active development.*
