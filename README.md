# Hyperativa Challenge - Card Management API

A secure, production-ready REST API for credit/debit card number storage and retrieval, built with Spring Boot 3, featuring end-to-end encryption, comprehensive testing, and enterprise-grade security practices.

## Table of Contents
- [Key Improvements](#key-improvements)
- [Architecture Overview](#architecture-overview)
- [Security Features](#security-features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Comparison with README.md](#comparison-with-readmemd)

---

## Key Improvements

This implementation goes beyond the basic requirements with the following enhancements:

### Security Enhancements
- **AES-256-GCM Encryption at Rest**: Card numbers are automatically encrypted before storage using industry-standard AES-256-GCM
- **JWT Authentication**: Stateless authentication with configurable token expiration
- **BCrypt Password Hashing**: User passwords are securely hashed with BCrypt
- **Input Validation**: Comprehensive validation at DTO and entity levels
- **Secure Configuration**: Externalized secrets via environment variables

### Code Quality
- **Comprehensive Testing**:
  - Unit tests for services (CardService, UserService)
  - Unit tests for file processor
  - Integration tests for controllers
  - Test coverage for success and failure scenarios
- **OpenAPI/Swagger Documentation**: Interactive API documentation at `/swagger-ui.html`
- **Transaction Management**: Proper @Transactional annotations for data consistency
- **JavaDoc Comments**: Well-documented public APIs

### Robustness
- **Enhanced File Processing**:
  - File size validation (configurable, default 10MB)
  - Content type validation
  - Detailed error reporting with line numbers
  - Invalid record handling and logging
- **Global Exception Handling**: Centralized error handling with meaningful error messages
- **Logging Aspect**: AOP-based request/response logging for all REST endpoints

### Production Readiness
- **Environment Configuration**: Separate configurations for dev/test/prod
- **Database Migrations**: Flyway for version-controlled schema changes
- **Connection Pooling**: Optimized HikariCP configuration
- **Graceful Shutdown**: Proper application lifecycle management
- **HTTP/2 Support**: Enabled for improved performance

---

## Architecture Overview

```
┌─────────────────┐
│  REST Client    │
└────────┬────────┘
         │ JWT Token
         ▼
┌─────────────────────────────────────┐
│    Spring Security Filter Chain     │
│  - JWT Validation                   │
│  - Authentication                   │
└────────┬───────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│       REST Controllers              │
│  - CardController (CRUD + Upload)   │
│  - AuthController (Login/Register)  │
└────────┬───────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│         Service Layer               │
│  - CardService (Business Logic)     │
│  - UserService (User Management)    │
└────────┬───────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│    JPA Repositories                 │
│  - CardRepository                   │
│  - UserRepository                   │
└────────┬───────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│      Database (MySQL)               │
│  - Encrypted card numbers           │
│  - Hashed passwords                 │
│  - Optimized indexes                │
└─────────────────────────────────────┘
```

### Data Flow for Card Storage

1. **Client Request** → Card number in plain text
2. **Controller** → Validation (16 digits, not blank)
3. **Service Layer** → Business logic
4. **JPA Entity** → AttributeConverter intercepts
5. **Encryption** → AES-256-GCM encryption applied
6. **Database** → Encrypted value stored
7. **Retrieval** → Automatic decryption on read

---

## Security Features

### 1. Card Number Encryption (AES-256-GCM)

Card numbers are **never** stored in plain text. The system uses:
- **Algorithm**: AES-256-GCM (Galois/Counter Mode)
- **Key Derivation**: SHA-256 for key generation from passphrase
- **IV**: Unique 12-byte initialization vector per encryption
- **Authentication**: 128-bit authentication tag for integrity

**Implementation**: `CardNumberEncryptor.java` - JPA AttributeConverter that transparently encrypts/decrypts

### 2. JWT Authentication

- **Algorithm**: HS256 (HMAC with SHA-256)
- **Claims**: Username, issued at, expiration
- **Expiration**: Configurable (default 24 hours)
- **Stateless**: No server-side session storage

### 3. Password Security

- **Algorithm**: BCrypt with salt
- **Work Factor**: Default 10 rounds
- **Validation**: Constant-time comparison

### 4. API Security Best Practices

- CSRF disabled (stateless JWT)
- CORS can be configured as needed
- Sensitive endpoints require authentication
- Public endpoints: `/v1/auth/**`, Swagger UI

---

## Prerequisites

- **Java**: 21 or higher
- **Docker**: For MySQL database (or use local MySQL)
- **Gradle**: Included via wrapper (./gradlew)

---

## Quick Start

### 1. Clone and Navigate
```bash
cd hyperativa-challenge-001
```

### 2. Set Up Environment Variables

Create a `.env` file or set environment variables:
```bash
# Copy example
cp .env.example .env

# Edit with your values
export DATABASE_URL=jdbc:mysql://localhost:3306/hyperativa
export DATASOURCE_USERNAME=hyperativa
export DATASOURCE_PASSWORD=hyperativa123
export JWT_SECRET=your-jwt-secret-key-minimum-256-bits-long
export ENCRYPTION_KEY=your-32-character-encryption-key!
```

### 3. Start MySQL Database

```bash
docker compose -f docker/mysql.yml up -d
```

### 4. Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### 5. Access API Documentation

Open your browser to:
```
http://localhost:8080/swagger-ui.html
```

---

## API Documentation

### Authentication Endpoints

#### Register User
```http
POST /v1/auth/register
Content-Type: application/json

{
  "username": "user123",
  "password": "securePass123"
}

Response: 201 Created
{
  "username": "user123"
}
```

#### Login
```http
POST /v1/auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "securePass123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Card Management Endpoints

**All card endpoints require JWT authentication via `Authorization: Bearer <token>` header**

#### Create Single Card
```http
POST /v1/card/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "cardNumber": "1234567890123456"
}

Response: 201 Created
{
  "id": 1,
  "cardNumberIdentifier": "01HQZX9Y8Z7W6V5U4T3S2R1Q0P"
}
```

#### Upload Cards from File
```http
POST /v1/card/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: cards.txt

Response: 202 Accepted
```

**File Format** (see `challenge-requirements/cards_upload_file.txt`):
```
DESAFIO-HYPERATIVA           20180524LOTE0001000010
C1     4456897922969999
C2     1234567890123456
LOTE0001000002
```

#### Get Card by Number
```http
GET /v1/card/{cardNumber}
Authorization: Bearer <token>

Response: 200 OK
{
  "id": 1,
  "cardNumberIdentifier": "01HQZX9Y8Z7W6V5U4T3S2R1Q0P"
}
```

#### Get All Cards (Paginated)
```http
GET /v1/card?page=0&size=20
Authorization: Bearer <token>

Response: 200 OK
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

---

## Testing

### Run All Tests
```bash
./gradlew test
```

### Test Coverage

- **Unit Tests**:
  - `CardServiceImplTest`: Service layer logic
  - `UserServiceImplTest`: User management
  - `CardTxtProcessorTest`: File parsing and validation

- **Integration Tests**:
  - `CardControllerIntegrationTest`: Full request/response cycle with security

### Test Database

Tests use H2 in-memory database for isolation and speed. Configuration in `src/test/resources/application-test.yml`

---

## Project Structure

```
src/
├── main/
│   ├── java/br/com/hyperativa/service/
│   │   ├── application/
│   │   │   ├── config/           # Configuration classes
│   │   │   │   ├── security/     # JWT, SecurityConfig
│   │   │   │   ├── aspect/       # Logging AOP
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── EncryptionConfig.java
│   │   │   │   └── CardNumberEncryptor.java
│   │   │   ├── util/             # Encryption utilities
│   │   │   └── web/
│   │   │       ├── controller/   # REST endpoints
│   │   │       └── errors/       # Exception handlers
│   │   ├── domain/
│   │   │   ├── entity/           # JPA entities + DTOs
│   │   │   ├── exceptions/       # Custom exceptions
│   │   │   ├── processor/        # File processors
│   │   │   └── services/         # Business logic
│   │   └── resources/
│   │       └── repository/       # JPA repositories
│   └── resources/
│       ├── application.yml       # Main configuration
│       └── db/migration/         # Flyway migrations
└── test/
    ├── java/                     # Test classes
    └── resources/
        └── application-test.yml  # Test configuration
```

---

## Comparison with README.md

| Aspect | Original README.md | This Implementation (README_2.md) |
|--------|-------------------|-----------------------------------|
| **Security** | Mentions Spring Security + JWT | ✅ AES-256-GCM encryption at rest<br>✅ Comprehensive input validation<br>✅ Externalized secrets |
| **Documentation** | Basic setup instructions | ✅ Interactive Swagger UI<br>✅ Detailed API examples<br>✅ Architecture diagrams<br>✅ JavaDoc comments |
| **Testing** | "Not intended to be production-ready" | ✅ Comprehensive unit tests<br>✅ Integration tests<br>✅ H2 test database setup |
| **Code Quality** | Mentions future improvements | ✅ @Transactional management<br>✅ Global exception handling<br>✅ Request/response logging<br>✅ File validation |
| **Production Readiness** | Development-focused | ✅ Environment variables<br>✅ Database connection pooling<br>✅ Graceful shutdown<br>✅ HTTP/2 support |
| **File Processing** | Basic implementation | ✅ File size validation<br>✅ Detailed error messages<br>✅ Line-by-line validation<br>✅ Statistics logging |
| **Database** | MySQL with Flyway | ✅ Optimized indexes<br>✅ Encrypted storage<br>✅ Migration versioning |

### What Was Improved

#### Critical Security Issues Fixed
1. **Card Numbers in Plain Text** → Now encrypted with AES-256-GCM
2. **Weak Validation** → Added @Size, @NotBlank with proper messages
3. **Missing Transaction Management** → Added @Transactional to service methods

#### Code Quality Enhancements
1. **No Tests** → 3 unit test classes + integration tests
2. **No API Documentation** → Full OpenAPI/Swagger integration
3. **Basic File Processing** → Robust validation with detailed logging
4. **Generic Error Messages** → Specific, actionable error responses

#### Production-Ready Features Added
1. **Environment Configuration** → .env.example with all required variables
2. **Test Configuration** → Separate test profile with H2 database
3. **OpenAPI Documentation** → Interactive API explorer
4. **Enhanced Logging** → AOP-based request/response logging

---

## Environment Variables Reference

| Variable | Description | Example | Required |
|----------|-------------|---------|----------|
| `DATABASE_URL` | JDBC connection string | `jdbc:mysql://localhost:3306/hyperativa` | Yes |
| `DATASOURCE_USERNAME` | Database username | `hyperativa` | Yes |
| `DATASOURCE_PASSWORD` | Database password | `hyperativa123` | Yes |
| `JWT_SECRET` | Secret key for JWT signing (min 256 bits) | `your-secret-key...` | Yes |
| `ENCRYPTION_KEY` | AES encryption key (32 chars recommended) | `your-32-char-key!` | Yes |

---

## Performance Considerations

### Database Optimization
- **Indexes**: Created on `username` and `card_number` for fast lookups
- **Connection Pool**: HikariCP configured with 50 max connections
- **Batch Processing**: Hibernate batching enabled for bulk inserts

### Application Performance
- **HTTP/2**: Enabled for multiplexing
- **Compression**: Gzip enabled for responses > 1KB
- **Open-in-View**: Disabled to prevent lazy loading issues

### Scalability Notes
- **Stateless Design**: JWT enables horizontal scaling
- **Connection Pooling**: Handles concurrent requests efficiently
- **Async Support**: Task executor configured for background processing

---

## Future Enhancements (Beyond Assessment Scope)

The original README.md mentioned these as "Future Improvements". This implementation already includes many, but here are additional recommendations:

1. **Role-Based Access Control (RBAC)**
   - Add `ROLE_USER`, `ROLE_ADMIN` authorities
   - Restrict certain endpoints by role

2. **WebFlux for Reactive Streams**
   - Replace Spring MVC with WebFlux for non-blocking I/O
   - Better for high-concurrency scenarios

3. **Distributed Caching**
   - Redis for session management (if needed)
   - Hibernate 2nd-level cache for frequently accessed data

4. **Cloud-Native Features**
   - Spring Cloud Config for centralized configuration
   - Kubernetes-ready with health checks and metrics

5. **Observability**
   - Spring Boot Actuator + Prometheus/Grafana
   - Distributed tracing with Zipkin/Jaeger

6. **Advanced Security**
   - Rate limiting per user/IP
   - API key support for service-to-service auth
   - Certificate-based mutual TLS

---

## Running Tests with Coverage

To see test coverage report:

```bash
./gradlew test jacocoTestReport
```

Report will be available at: `build/reports/jacoco/test/html/index.html`

---

## Troubleshooting

### Issue: "Table 'card' doesn't exist"
**Solution**: Flyway migrations didn't run. Ensure `spring.flyway.enabled=true` in application.yml

### Issue: "Encryption failed"
**Solution**: Check that `ENCRYPTION_KEY` environment variable is set and is at least 32 characters

### Issue: "JWT token invalid"
**Solution**: Ensure `JWT_SECRET` is configured and is at least 256 bits (32 characters for HS256)

### Issue: Tests fail with "No qualifying bean"
**Solution**: Ensure `@SpringBootTest` annotation is present and test profile is active

---

## License

This project is developed as part of a technical assessment for Hyperativa.

---

## Author

**Challenge Submission**
- Built with Spring Boot 3.4.4
- Java 21
- Follows PCI-DSS principles for card data security
- Production-ready architecture with comprehensive testing

---

## Acknowledgments

- Spring Boot team for excellent framework
- Hyperativa for the challenging assessment
- Security best practices from OWASP and PCI-DSS guidelines
