# Security API - Base project for authentication and authorization using JWT with RSA keys

This project provides a base implementation for authentication and authorization using JSON Web Tokens (JWT) with RSA keys. It is designed to be easily integrated into your applications, allowing you to secure your APIs and manage user access effectively.

## Features
- JWT authentication using RSA keys (RS256)
- User registration and login
- Role-based access control (RBAC)
- Secure password hashing with BCrypt
- Global exception handling
- Input validation with Bean Validation
- Standardized error responses
- Cookie-based token storage

## How Security Works

### Authentication Flow
1. **Registration**: User sends credentials → System validates input → Password hashed with BCrypt → User created with default ROLE_USER
2. **Login**: User sends credentials → System validates → JWT token generated and signed with RSA private key → Token stored in HTTP-only cookie
3. **Authorization**: Client sends request with cookie → Security filter extracts token → Token validated with RSA public key → Authentication set in SecurityContext → Access granted/denied based on roles

### JWT Structure
- **Algorithm**: RS256 (RSA with SHA-256)
- **Issuer**: `security-app`
- **Expiration**: 1 hour (3600 seconds)
- **Claims**: 
  - `sub`: User ID
  - `email`: User email
  - `scope`: Space-separated roles (e.g., `ROLE_USER ROLE_ADMIN`)
  - `iat`: Issued at timestamp
  - `exp`: Expiration timestamp

### Security Layers
1. **Cookie-based Token**: JWT stored in HTTP-only, SameSite=Strict cookie for XSS protection
2. **JWT Validation**: Token signature validated with RSA public key
3. **Role-based Authorization**: Endpoints protected by roles via Spring Security
4. **Password Encoding**: BCrypt with default strength
5. **CSRF Protection**: Disabled for stateless API (relies on cookie SameSite)

## API Endpoints

### Authentication

#### Register New User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Success Response (200 OK):**
```json
"User registered successfully"
```

**Error Response (409 Conflict) - Email Already Exists:**
```json
{
  "code": "EMAIL_ALREADY_EXISTS",
  "message": "Email is already in use",
  "timestamp": "2026-03-08T10:30:45.123Z"
}
```

**Error Response (400 Bad Request) - Validation Failed:**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "timestamp": "2026-03-08T10:30:45.123Z",
  "path": "/api/auth/register",
  "errors": {
    "email": "Email must be valid",
    "password": "Password must contain uppercase, lowercase, and a number",
    "username": "Username must be between 3 and 50 characters"
  }
}
```

**Error Response (500 Internal Server Error) - Role Not Found:**
```json
{
  "code": "ROLE_NOT_FOUND",
  "message": "Default role ROLE_USER was not found",
  "timestamp": "2026-03-08T10:30:45.123Z"
}
```

---

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Success Response (200 OK):**
```
Set-Cookie: token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...; Path=/; HttpOnly; SameSite=Strict; Max-Age=3600
```

**Error Response (401 Unauthorized) - Invalid Credentials:**
```json
{
  "code": "INVALID_CREDENTIALS",
  "message": "Email or password is invalid",
  "timestamp": "2026-03-08T10:30:45.123Z"
}
```

**Error Response (400 Bad Request) - Validation Failed:**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "timestamp": "2026-03-08T10:30:45.123Z",
  "path": "/api/auth/login",
  "errors": {
    "email": "Email is required",
    "password": "Password is required"
  }
}
```

---

#### Get Current User Info
```http
GET /api/auth/me
Cookie: token=<jwt_token>
```

**Success Response (200 OK):**
```json
{
  "id": "1",
  "email": "user@example.com",
  "roles": ["ROLE_USER"]
}
```

**Error Response (401 Unauthorized) - Missing or Invalid Token:**
```json
{
  "code": "UNAUTHORIZED",
  "message": "Authentication is required",
  "timestamp": "2026-03-08T10:30:45.123Z"
}
```

---

#### Logout
```http
POST /api/auth/logout
Cookie: token=<jwt_token>
```

**Success Response (200 OK):**
```
Set-Cookie: token=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0
```

## Validation Rules

### RegisterRequest
- **username**: 
  - Required (not blank)
  - Length: 3-50 characters
- **email**: 
  - Required (not blank)
  - Valid email format
- **password**: 
  - Required (not blank)
  - Length: 8-72 characters (BCrypt limit)
  - Must contain: uppercase letter, lowercase letter, and a number
  - Pattern: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$`

### LoginRequest
- **email**: 
  - Required (not blank)
  - Valid email format
- **password**: 
  - Required (not blank)

## Error Handling

### HTTP Status Codes
- `200 OK`: Successful operation
- `400 Bad Request`: Input validation failed
- `401 Unauthorized`: Authentication required, invalid credentials, or invalid/expired token
- `403 Forbidden`: Authenticated but insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists (e.g., email already registered)
- `500 Internal Server Error`: Unexpected server error

### Standard Error Response Format
```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable error description",
  "timestamp": "2026-03-08T10:30:45.123Z"
}
```

### Validation Error Response Format
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "timestamp": "2026-03-08T10:30:45.123Z",
  "path": "/api/endpoint",
  "errors": {
    "fieldName1": "error message 1",
    "fieldName2": "error message 2"
  }
}
```

### Exception Types
- `EmailAlreadyExistsException` → 409 Conflict
- `UserNotFoundException` → 404 Not Found
- `RoleNotFoundException` → 500 Internal Server Error
- `BadCredentialsException` → 401 Unauthorized
- `AuthenticationException` → 401 Unauthorized
- `AccessDeniedException` → 403 Forbidden
- `MethodArgumentNotValidException` → 400 Bad Request

## Project Architecture

### Package Structure
```
dev.john.security
├── config/              # Security configuration
│   └── SecurityConfig.java
├── controllers/         # REST API endpoints
│   └── AuthController.java
├── dto/                 # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RegisterRequest.java
│   ├── TokenVerificationResponse.java
│   ├── ApiErrorResponse.java
│   └── ValidationErrorResponse.java
├── entity/              # JPA entities
│   ├── User.java
│   └── Role.java
├── exception/           # Custom exceptions and handlers
│   ├── GlobalExceptionHandler.java
│   ├── EmailAlreadyExistsException.java
│   ├── UserNotFoundException.java
│   └── RoleNotFoundException.java
├── repository/          # Spring Data JPA repositories
│   ├── UserRepository.java
│   └── RoleRepository.java
└── services/            # Business logic layer
    ├── AuthService.java
    ├── TokenService.java
    └── CustomUserDetailsService.java
```

### Key Components

#### SecurityConfig
- Configures HTTP security with stateless session management
- Defines public endpoints (`/api/auth/**`)
- Configures JWT decoder with RSA public key
- Configures JWT encoder with RSA private key
- Custom bearer token resolver to extract JWT from cookies
- JWT authentication converter to map `scope` claim to Spring Security authorities

#### AuthController
- `POST /register`: Delegates to `AuthService.registerUser()`
- `POST /login`: Delegates to `AuthService.login()`, sets cookie
- `GET /me`: Returns authenticated user info from JWT
- `POST /logout`: Clears authentication cookie

#### AuthService
- `registerUser()`: Validates email uniqueness, hashes password, assigns default role
- `login()`: Authenticates user, generates token, sets HTTP-only cookie
- `logout()`: Expires authentication cookie

#### TokenService
- `generateToken()`: Creates JWT with user claims and RSA signature
- Embeds user ID, email, and roles in token
- Sets 1-hour expiration

#### CustomUserDetailsService
- Implements Spring Security's `UserDetailsService`
- Loads user by email for authentication
- Maps user roles to Spring Security authorities

#### GlobalExceptionHandler
- Centralized exception handling with `@RestControllerAdvice`
- Maps domain exceptions to appropriate HTTP responses
- Handles validation errors with field-level details
- Provides consistent error response format

### Database Schema

#### users
```sql
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### roles
```sql
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
```

#### user_roles (Many-to-Many)
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id)
);
```

#### Default Roles
- `ROLE_USER`: Default role assigned to all new users
- `ROLE_ADMIN`: Admin role (must be manually assigned)

## Prerequisites
- Java 21 or higher
- Maven 3.8+
- PostgreSQL 15+ (or Docker for containerized setup)
- OpenSSL (for generating RSA keys)

## Getting Started

### 1. Generate RSA Keys
Generate RSA key pair for JWT signing:
```bash
# Navigate to resources/keys directory
cd src/main/resources/keys

# Generate private key
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048

# Generate public key from private key
openssl rsa -pubout -in private.pem -out public.pem
```

### 2. Setup Database

**Option A: Using Docker Compose (Recommended)**
```bash
# Start PostgreSQL container
docker-compose up -d

# Database will be available at:
# Host: localhost
# Port: 5432
# Database: security_db
# Username: postgres
# Password: postgres
```

**Option B: Local PostgreSQL Installation**
```sql
-- Create database
CREATE DATABASE security_db;

-- Connect to database
\c security_db

-- Flyway will automatically create tables on first run
```

### 3. Configure Application
Edit `src/main/resources/application.properties` if needed:
```properties
# Update these if using different database credentials
spring.datasource.url=jdbc:postgresql://localhost:5432/security_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 4. Build and Run

**Using Maven:**
```bash
# Clean and build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

**Using Maven Wrapper (Windows):**
```bash
# Build
.\mvnw.cmd clean install

# Run
.\mvnw.cmd spring-boot:run
```

**Using Maven Wrapper (Linux/Mac):**
```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

### 5. Verify Installation
Test the health of the application:
```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "SecurePass123"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePass123"
  }' \
  -c cookies.txt

# Get current user info (using saved cookie)
curl -X GET http://localhost:8080/api/auth/me \
  -b cookies.txt
```


## Spring Boot Dependencies
- **Spring Boot Starter Web**: REST API endpoints
- **Spring Boot Starter Security**: Authentication and authorization
- **Spring Boot Starter Data JPA**: Database persistence layer
- **Spring Boot Starter Validation**: Bean validation for request DTOs
- **Spring Security OAuth2 Resource Server**: JWT token validation
- **PostgreSQL Driver**: Database connectivity
- **Flyway**: Database migrations
- **Lombok**: Boilerplate code reduction
- **Spring Boot DevTools**: Hot reload during development

## Configuration

### application.properties
```properties
# Security Configuration
spring.security.oauth2.resourceserver.jwt.public-key-location=classpath:keys/public.pem
app.security.jwt.private-key-location=classpath:keys/private.pem

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/security_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Configuration
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.enabled=true

# DevTools Configuration for Hot Reload
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
```

## Testing
Run tests with Maven:
```bash
mvn test
```

Run specific test class:
```bash
mvn -Dtest=SecurityApplicationTests test
```

## Future Improvements

### High Priority
- [ ] **Refresh Token Flow**: Implement refresh tokens to allow token renewal without re-authentication
  - Store refresh tokens in database with expiration
  - Add `/api/auth/refresh` endpoint
  - Rotate refresh tokens on use
- [ ] **Token Revocation/Blacklist**: Implement token revocation mechanism
  - Redis-based blacklist for revoked tokens
  - Check blacklist on every authenticated request
- [ ] **Password Reset Flow**: Add forgot/reset password functionality
  - Generate secure reset tokens
  - Send reset link via email
  - Validate token expiration

### Medium Priority
- [ ] **Email Verification**: Require email verification on registration
  - Send verification email with token
  - Block login until email verified
- [ ] **Rate Limiting**: Protect against brute-force attacks
  - Use Bucket4j or Spring Security filter
  - Limit login attempts per IP/email
- [ ] **OAuth2 Social Login**: Add Google, GitHub authentication
  - Spring Security OAuth2 Client
  - Link social accounts to existing users

### Low Priority
- [ ] **Audit Logging**: Track user actions and security events
  - Log all authentication attempts
  - Track role changes and permission escalations
- [ ] **Metrics & Health Checks**: Add Spring Boot Actuator
  - Monitor authentication success/failure rates
  - Track token generation/validation metrics
- [ ] **API Documentation**: Add Swagger/OpenAPI
  - Auto-generate API docs from controllers
  - Provide interactive API testing UI
- [ ] **Multi-factor Authentication (MFA)**: Add TOTP-based 2FA
  - Google Authenticator integration
  - Backup codes generation

### Security Enhancements
- [ ] **CORS Configuration**: Fine-tune CORS for production
- [ ] **HTTPS Enforcement**: Require HTTPS in production (set `cookie.secure=true`)
- [ ] **Password Policy**: Add password strength meter and history
- [ ] **Account Lockout**: Lock account after N failed login attempts
- [ ] **Session Management**: Track active sessions per user

