# Testing Guide - Land Management System Backend

## Quick Start Testing

### 1. Build & Run

```bash
# Set environment variables
export JWT_SECRET="test-secret-key-min-32-characters-long"
export UPLOADS_DIR="./data/uploads"

# Build
mvn clean package

# Run
mvn spring-boot:run
```

### 2. Access APIs

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **Health Check**: http://localhost:8080/api/actuator/health
- **Metrics**: http://localhost:8080/api/actuator/metrics

---

## Testing New Features

### A. Pagination Testing

#### 1. List Dossiers with Pagination
```bash
curl -X GET "http://localhost:8080/api/dossiers?page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response**:
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

#### 2. List Users (Admin)
```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=10&role=COMMUNE_OFFICER" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### 3. Test Different Page Numbers
```bash
# Page 1
curl -X GET "http://localhost:8080/api/dossiers?page=1&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Page 2 with size 50
curl -X GET "http://localhost:8080/api/dossiers?page=2&size=50" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### B. Error Response Format Testing

#### 1. Validation Error
```bash
curl -X POST "http://localhost:8080/api/dossiers" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "",
    "citizenName": "",
    "dossierTypeId": null
  }'
```

**Expected Response** (400):
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "status": 400,
  "details": {
    "title": "Title is required",
    "citizenName": "Citizen name is required",
    "dossierTypeId": "Dossier type is required"
  },
  "path": "/api/dossiers",
  "timestamp": "2026-05-18T10:30:00"
}
```

#### 2. Authentication Error
```bash
curl -X GET "http://localhost:8080/api/dossiers" \
  -H "Authorization: Bearer INVALID_TOKEN"
```

**Expected Response** (401):
```json
{
  "code": "AUTHENTICATION_ERROR",
  "message": "Authentication failed: ...",
  "status": 401,
  "path": "/api/dossiers",
  "timestamp": "2026-05-18T10:30:00"
}
```

#### 3. Resource Not Found
```bash
curl -X GET "http://localhost:8080/api/dossiers/99999" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response** (404):
```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Dossier with id 99999 not found",
  "status": 404,
  "path": "/api/dossiers/99999",
  "timestamp": "2026-05-18T10:30:00"
}
```

#### 4. Authorization Error
```bash
curl -X GET "http://localhost:8080/api/admin/audit-logs/user/1" \
  -H "Authorization: Bearer NON_ADMIN_TOKEN"
```

**Expected Response** (403):
```json
{
  "code": "ACCESS_DENIED",
  "message": "You do not have permission to access this resource",
  "status": 403,
  "path": "/api/admin/audit-logs/user/1",
  "timestamp": "2026-05-18T10:30:00"
}
```

---

### C. Audit Logging Testing

#### 1. Create a Dossier (Creates Audit Log)
```bash
curl -X POST "http://localhost:8080/api/dossiers" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Dossier",
    "citizenName": "John Doe",
    "dossierTypeId": 1
  }'
```

#### 2. View Audit Logs for Dossier
```bash
curl -X GET "http://localhost:8080/api/admin/audit-logs/dossier/1?page=0&size=20" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Expected Response**:
```json
{
  "data": [
    {
      "id": 1,
      "userId": 123,
      "username": "user123",
      "actionType": "CREATE",
      "entityType": "DOSSIER",
      "entityId": 1,
      "dossierId": 1,
      "ipAddress": "127.0.0.1",
      "userAgent": "curl/7.68.0",
      "status": "SUCCESS",
      "createdAt": "2026-05-18T10:30:00",
      "description": "Dossier created"
    }
  ],
  "total": 1,
  "page": 0,
  "size": 20,
  "total_pages": 1,
  "has_next": false,
  "has_previous": false
}
```

#### 3. View Audit Logs for User
```bash
curl -X GET "http://localhost:8080/api/admin/audit-logs/user/123?page=0" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### 4. View Audit Logs by Action Type
```bash
curl -X GET "http://localhost:8080/api/admin/audit-logs/by-action/APPROVE?page=0" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

### D. File Upload Validation Testing

#### 1. Upload Valid File
```bash
curl -X POST "http://localhost:8080/api/dossiers/1/attachments" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/document.pdf"
```

#### 2. Upload Invalid File (Blocked Extension)
```bash
curl -X POST "http://localhost:8080/api/dossiers/1/attachments" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/malware.exe"
```

**Expected Response** (400):
```json
{
  "code": "INVALID_REQUEST",
  "message": "File extension .exe is not allowed",
  "status": 400
}
```

#### 3. Upload File Too Large
```bash
# Create a file > 50MB and try to upload
curl -X POST "http://localhost:8080/api/dossiers/1/attachments" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/large-file.zip"
```

**Expected Response** (400):
```json
{
  "code": "INVALID_REQUEST",
  "message": "File size exceeds maximum allowed size of 50MB",
  "status": 400
}
```

---

### E. Database Indexes Performance Testing

#### 1. List Dossiers by Status (Uses Index)
```bash
curl -X GET "http://localhost:8080/api/dossiers?page=0&size=20&status=PENDING" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 2. List Dossiers by Origin Unit (Uses Index)
```bash
curl -X GET "http://localhost:8080/api/dossiers?page=0&size=20&unit_id=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 3. Performance Comparison (Check execution time)
```bash
# Monitor with MySQL EXPLAIN
# Login to MySQL and run:
# EXPLAIN SELECT * FROM dossier WHERE status_code = 'PENDING';
```

---

### F. Actuator/Monitoring Testing

#### 1. Health Check
```bash
curl -X GET "http://localhost:8080/api/actuator/health"
```

**Expected Response**:
```json
{
  "status": "UP"
}
```

#### 2. Application Info
```bash
curl -X GET "http://localhost:8080/api/actuator/info"
```

**Expected Response**:
```json
{
  "app": {
    "name": "Land Management System",
    "version": "1.0.0-SNAPSHOT"
  }
}
```

#### 3. Metrics
```bash
curl -X GET "http://localhost:8080/api/actuator/metrics"
```

#### 4. Specific Metric (e.g., HTTP requests)
```bash
curl -X GET "http://localhost:8080/api/actuator/metrics/http.server.requests"
```

#### 5. Prometheus Metrics
```bash
curl -X GET "http://localhost:8080/api/actuator/prometheus"
```

---

### G. Security Headers Testing

```bash
# Check security headers
curl -I "http://localhost:8080/api/dossiers" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected headers:
# X-XSS-Protection: 1; mode=block
# Content-Security-Policy: default-src 'self'
# X-Frame-Options: DENY
```

---

### H. CORS Testing

#### 1. Browser Origin Request
```bash
curl -X OPTIONS "http://localhost:8080/api/dossiers" \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization"
```

**Expected Response Headers**:
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD
Access-Control-Allow-Headers: *
Access-Control-Max-Age: 3600
```

---

## Test Scenarios

### Scenario 1: Complete Dossier Workflow with Pagination

```bash
# 1. Login
TOKEN=$(curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}' | jq -r '.access_token')

# 2. Create multiple dossiers
for i in {1..25}; do
  curl -X POST "http://localhost:8080/api/dossiers" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"title\":\"Dossier $i\",\"citizenName\":\"Citizen $i\",\"dossierTypeId\":1}"
done

# 3. List first page
curl -X GET "http://localhost:8080/api/dossiers?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" | jq '.total, .page, .size, .total_pages'

# 4. List second page
curl -X GET "http://localhost:8080/api/dossiers?page=1&size=20" \
  -H "Authorization: Bearer $TOKEN"

# 5. Check audit logs
curl -X GET "http://localhost:8080/api/admin/audit-logs/by-action/CREATE?page=0&size=50" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### Scenario 2: Admin User Management

```bash
# 1. Get admin token
ADMIN_TOKEN=$(curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' | jq -r '.access_token')

# 2. Create users
for i in {1..15}; do
  curl -X POST "http://localhost:8080/api/admin/users" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"user$i\",\"password\":\"password\",\"fullName\":\"User $i\",\"email\":\"user$i@example.com\",\"role\":\"COMMUNE_OFFICER\",\"unitId\":1}"
done

# 3. List users with pagination
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 4. Filter by role
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=10&role=COMMUNE_OFFICER" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 5. View audit logs for user creation
curl -X GET "http://localhost:8080/api/admin/audit-logs/by-action/CREATE?page=0" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## Performance Testing

### Load Test with Pagination

```bash
#!/bin/bash
# Load test script

TOKEN="your_token"
ITERATIONS=100

echo "Running $ITERATIONS pagination requests..."

for i in $(seq 1 $ITERATIONS); do
  PAGE=$((RANDOM % 5))
  curl -s -X GET "http://localhost:8080/api/dossiers?page=$PAGE&size=20" \
    -H "Authorization: Bearer $TOKEN" > /dev/null
  
  if [ $((i % 10)) -eq 0 ]; then
    echo "Completed $i/$ITERATIONS requests"
  fi
done

echo "Load test completed"
```

---

## Troubleshooting

### Issue: JWT Token Expired
**Solution**: Generate a new token using login endpoint

### Issue: File Upload Fails
**Solution**: Check that `UPLOADS_DIR` exists and is writable:
```bash
mkdir -p ./data/uploads
chmod 755 ./data/uploads
```

### Issue: Database Indexes Not Used
**Solution**: Run ANALYZE table:
```sql
ANALYZE TABLE dossier;
ANALYZE TABLE approval_history;
ANALYZE TABLE dossier_attachment;
ANALYZE TABLE user_account;
ANALYZE TABLE audit_log;
```

### Issue: Audit Logs Not Recording
**Solution**: Check that audit_log table exists:
```sql
SHOW TABLES LIKE 'audit_log';
```

---

## Checklist for Feature Validation

- [ ] Pagination returns correct total count
- [ ] Pagination respects page size limits
- [ ] Error responses use standardized format
- [ ] Validation errors include field details
- [ ] Audit logs are created for dossier operations
- [ ] Audit logs are created for user operations
- [ ] File uploads validate MIME type
- [ ] File uploads validate size
- [ ] File uploads block dangerous extensions
- [ ] Security headers are present
- [ ] CORS works for frontend domain
- [ ] Actuator endpoints are accessible
- [ ] Health check returns UP status
- [ ] Metrics are collecting data
- [ ] Database indexes improve query performance

---

**Last Updated**: 2026-05-18
