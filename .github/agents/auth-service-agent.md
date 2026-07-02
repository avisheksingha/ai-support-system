# Auth Service Agent

**Role:** Authentication & Authorization Management

**Port:** 8081

**Responsibility:** Exposes authentication APIs, validates user credentials, issues JSON Web Tokens (JWT), manages users and roles, and provides security configurations.

## Quick Commands

### Build
```bash
mvn -pl auth-service clean install
```

### Run Service
```bash
cd auth-service && mvn spring-boot:run
```

### Run Tests
```bash
mvn -pl auth-service test
```

### Swagger
```text
http://localhost:8081/swagger-ui/index.html
```

## Key Files

- **Controller:** `src/main/java/com/aisupport/auth/controller/AuthController.java`
- **Service:** `src/main/java/com/aisupport/auth/service/AuthService.java`
- **Entity:** `src/main/java/com/aisupport/auth/entity/User.java`
- **Repository:** `src/main/java/com/aisupport/auth/repository/UserRepository.java`
- **Security Config:** `src/main/java/com/aisupport/auth/config/SecurityConfig.java`

## Current API Endpoints

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/refresh`

## Database Snapshot (from entity)

### users
- `id` (Long, PK)
- `username` (String, unique)
- `password` (String, hashed)
- `email` (String, unique)
- `roles` (Enum / String collection)
- `created_at`, `updated_at`

## Important Rules

- Passwords MUST be hashed using `PasswordEncoder` before saving to the database.
- Always use the shared `JwtUtil` (from `common-library`) for token generation to maintain consistency across services.
- Protect endpoints with `SecurityConfig`.
- Do NOT expose sensitive user details in API responses.

## Related Services

- Accessed primarily by `api-gateway` or client applications for login/registration.
- Tokens issued here are validated across all other microservices (`ticket-service`, `ai-analysis-service`, etc.) using the shared `HeaderAuthenticationFilter`.

## Debugging Tips

1. Token invalid or expired: Verify JWT secret key and expiration settings.
2. Login fails: Ensure password hash matches the database value.
3. Access Denied: Verify the `roles` assigned to the user match the requirements of the downstream service.
