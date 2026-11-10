# Land Management System - Backend Java - Enhancement Guide

## Latest Enhancements (v1.1.0)

This document outlines all the improvements made to the backend Java application.

### 1. **Standardized Error Response Format** ✅

- **New Class**: `ApiErrorResponse.java`
- **Features**:
  - Consistent error response structure across all endpoints
  - Machine-readable error codes (e.g., `VALIDATION_ERROR`, `AUTHENTICATION_ERROR`, `RESOURCE_NOT_FOUND`)
  - HTTP status codes
  - User-friendly error messages
  - Detailed error information for validation errors
  - Request tracing with timestamps and paths

**Example Error Response**:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "status": 400,
  "details": {
    "username": "Username is required",
    "email": "Email format is invalid"
  },
  "path": "/admin/users",
  "timestamp": "2026-05-18T10:30:00"
}
```

### 2. **Pagination Support** ✅

- **New Classes**:
  - `PaginationRequest.java` - Pagination parameters
  - `PaginatedResponse.java` - Generic paginated response wrapper
- **Updated Endpoints**:
  - `GET /dossiers` - List dossiers with pagination
  - `GET /dossiers/inbox` - User's inbox with pagination
  - `GET /dossiers/central/decisions` - Central decisions with pagination
  - `GET /admin/users` - List users with pagination

**Query Parameters**:

- `page` (default: 0) - Page number starting from 0
- `size` (default: 20) - Items per page (max: 100)

**Example Request**:

```
GET /dossiers?page=0&size=20&status=PENDING
```

**Example Response**:

```json
{
  "data": [...],
  "total": 100,
  "page": 0,
  "size": 20,
  "total_pages": 5,
  "has_next": true,
  "has_previous": false
}
```

### 3. **Audit Logging System** ✅

- **New Entity**: `AuditLog.java`
- **New Service**: `AuditLogService.java`
- **New Controller**: `AuditLogController.java`
- **New Repository**: `AuditLogRepository.java`
- **Features**:
  - Track sensitive operations (create, update, delete, approve, reject, etc.)
  - Record user, IP address, user agent, timestamp
  - Store old and new values for auditing
  - Query audit logs by dossier, user, action type, or date range
  - Admin endpoints to view audit history

**Tracked Operations**:

- User account creation/updates
- Dossier creation/status changes
- Dossier approvals, rejections, returns, escalations
- Attachment uploads/downloads
- Admin operations

**Audit Log Migration**: `V4__Create_audit_log_table.sql`

### 4. **Input Validation & File Upload Security** ✅

- **New Class**: `FileUploadValidationRequest.java`
- **Features**:
  - File size validation (1 byte - 50 MB)
  - MIME type whitelist validation
  - Dangerous file extension blocking
  - Filename length validation
  - Comprehensive validation method

**Allowed MIME Types**:

- `application/pdf`
- `image/jpeg`, `image/png`, `image/tiff`
- `application/msword`, Office documents
- `text/plain`
- `application/zip`

**Blocked Extensions**:

- `.exe`, `.bat`, `.cmd`, `.com`, `.pif`, `.scr`
- `.vbs`, `.js`, `.jar`, `.zip`, `.rar`

### 5. **Database Indexes for Performance** ✅

- **Added Indexes to**:
  - `Dossier`: status, origin_unit_id, assigned_to_unit_id, created_by_user_id, sent_to_central, created_at
  - `ApprovalHistory`: dossier_id, actor_user_id, actor_unit_id, action, created_at
  - `DossierAttachment`: dossier_id, created_at
  - `UserAccount`: unit_id, role_code, is_active, username
  - `AuditLog`: user_id, dossier_id, created_at, action_type

**Benefits**:

- Faster queries for list operations
- Improved filtering performance
- Better database statistics

### 6. **Enhanced Security Configuration** ✅

- **Updated**: `SecurityConfig.java`
- **New Features**:
  - Security headers (X-XSS-Protection, Content-Security-Policy, X-Frame-Options)
  - Improved error handling with `ApiErrorResponse`
  - Better CORS configuration
  - Actuator endpoints restricted to ADMIN
  - Additional security context
  - Health check endpoint public access

### 7. **Spring Actuator & Monitoring** ✅

- **Added Dependency**: `spring-boot-starter-actuator`
- **Exposed Endpoints**:
  - `/actuator/health` - Application health (public)
  - `/actuator/health/readiness` - Readiness probe
  - `/actuator/health/liveness` - Liveness probe
  - `/actuator/info` - Application info
  - `/actuator/metrics` - Metrics data
  - `/actuator/prometheus` - Prometheus metrics

**Configuration**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### 8. **Configuration Management** ✅

- **Environment Variables**:
  - `JWT_SECRET` - JWT signing secret (default: dev-secret-change-me-in-production-very-important-key-for-security)
  - `UPLOADS_DIR` - Upload directory path (default: ./data/uploads)

**Updated `application.yml`**:

```yaml
app:
  jwt:
    secret: ${JWT_SECRET:default-secret}
    expiration: 86400000
  uploads:
    dir: ${UPLOADS_DIR:./data/uploads}
    max-file-size: 52428800
```

**Production Recommendations**:

1. Set `JWT_SECRET` to a strong random value in production
2. Use vault/secret management for sensitive data
3. Disable Swagger in production: `springdoc.swagger-ui.enabled=false`
4. Set logging level to `INFO` in production

### 9. **Global Exception Handler** ✅

- **Updated**: `GlobalExceptionHandler.java`
- **Handles**:
  - Validation errors (400 with field details)
  - Authentication failures (401)
  - Authorization failures (403)
  - Resource not found (404)
  - Business logic errors (400)
  - Generic exceptions (500)

### 10. **API Documentation** ✅

- **Swagger/OpenAPI Available at**:
  - `GET /api/v3/api-docs` - OpenAPI specification
  - `GET /api/swagger-ui.html` - Swagger UI

**Updated Controllers with Annotations**:

- `@Tag` - Controller grouping
- `@Operation` - Endpoint documentation
- `@SecurityRequirement` - Security requirements

---

## Environment Setup

### Development Environment Variables

```bash
# JWT Configuration
export JWT_SECRET="your-secure-random-key-min-32-chars"

# Upload Configuration
export UPLOADS_DIR="./data/uploads"

# Database Configuration (optional, defaults in application.yml)
export DB_URL="jdbc:mysql://localhost:3306/land_mgmt"
export DB_USERNAME="root"
export DB_PASSWORD="root"

# Logging Level
export LOGGING_LEVEL="DEBUG"
```

### Running the Application

```bash
# Build
mvn clean package

# Run with environment variables
export JWT_SECRET="your-secret-key"
mvn spring-boot:run

# Or with jar
java -jar target/land-management-system-1.0.0-SNAPSHOT.jar
```

---

## Database Migrations

### Applied Migrations:

1. **V1\_\_Create_tables.sql** - Initial schema
2. **V2\_\_Seed_data.sql** - Initial data
3. **V3\_\_Allow_submit_approval_action.sql** - Allow SUBMIT action
4. **V4\_\_Create_audit_log_table.sql** - Audit logging table

Flyway automatically applies migrations on application startup.

---

## API Endpoints

### Authentication

- `POST /auth/login` - Login
- `GET /me` - Get current user
- `POST /auth/change-password` - Change password

### Dossiers

- `POST /dossiers` - Create dossier
- `GET /dossiers?page=0&size=20` - List dossiers (paginated)
- `GET /dossiers/{id}` - Get dossier detail
- `POST /dossiers/{id}/actions` - Perform action
- `GET /dossiers/inbox?page=0&size=20` - User's inbox
- `GET /dossiers/central/decisions?page=0&size=20` - Central decisions

### Admin Users

- `POST /admin/users` - Create user
- `GET /admin/users?page=0&size=20` - List users (paginated)
- `PUT /admin/users/{id}` - Update user

### Admin Audit Logs

- `GET /admin/audit-logs/dossier/{dossierId}?page=0` - Dossier audit logs
- `GET /admin/audit-logs/user/{userId}?page=0` - User audit logs
- `GET /admin/audit-logs/by-action/{actionType}?page=0` - Logs by action

### Monitoring

- `GET /actuator/health` - Health status
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Metrics

---

## Security Best Practices

1. **JWT Secret Management**:
   - Always set `JWT_SECRET` in production
   - Use strong random values (min 32 characters)
   - Rotate keys periodically

2. **Audit Logging**:
   - Review audit logs regularly for suspicious activities
   - Archive old logs for compliance

3. **Database**:
   - Enable SSL for database connections
   - Use strong database credentials
   - Regular backups

4. **API Security**:
   - Enable HTTPS in production
   - Use rate limiting (to be implemented)
   - Validate all inputs
   - Sanitize error messages for production

---

## Performance Considerations

1. **Database Indexes**: All critical columns are indexed
2. **Pagination**: All list endpoints support pagination (default 20 items/page, max 100)
3. **Lazy Loading**: Relationships use appropriate fetch strategies
4. **Connection Pooling**: HikariCP with pool size 10, minimum idle 2

---

## Next Steps (To Do)

1. **Unit & Integration Tests**:
   - Auth service tests
   - Dossier workflow tests
   - Attachment security tests
   - Audit logging tests

2. **Rate Limiting**:
   - Implement Spring Security rate limiting
   - Protect login endpoint (e.g., 5 attempts per 15 minutes)

3. **Virus Scanning**:
   - Integrate ClamAV or similar for file uploads
   - Quarantine suspicious files

4. **Additional Features**:
   - Refresh tokens for JWT
   - Account lockout after repeated failures
   - Password strength requirements
   - Request tracing with sleuth

5. **Production Deployment**:
   - Setup Kubernetes manifests
   - Configure health checks
   - Setup monitoring dashboard
   - Setup log aggregation

---

## Troubleshooting

### Migration Issues

```
Error: Flyway validation failed
Solution: Ensure application.yml has correct DB connection
```

### JWT Secret Not Set

```
Error: JwtUtil requires JWT_SECRET
Solution: Set JWT_SECRET environment variable or update application.yml
```

### Upload Directory Not Found

```
Error: Failed to create upload directory
Solution: Ensure UPLOADS_DIR path exists and is writable
```

---

## Support & Documentation

- API Docs: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/api/v3/api-docs`
- Health: `http://localhost:8080/api/actuator/health`

---

**Last Updated**: 2026-05-18  
**Version**: 1.1.0  
**Status**: Ready for Integration Testing
