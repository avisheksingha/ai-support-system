# Auth Service

Microservice responsible for managing authentication and authorization for the AI Support System.

## Features

- **Authentication**: Validates user credentials and issues JSON Web Tokens (JWT).
- **Authorization**: Manages role-based access control across the microservice ecosystem.
- **JWT Management**: Generates, validates, and parses JWTs for secure stateless authentication.
- **Service Discovery**: Registers with Eureka for dynamic invocation.
- **Security**: Localized `SecurityConfig` to protect authentication endpoints and internal APIs.

## API Endpoints

- `POST /api/v1/auth/login`: Authenticate a user and receive a JWT.
- `POST /api/v1/auth/register`: Register a new user account.
- `POST /api/v1/auth/refresh`: Refresh an expired JWT.

## Configuration

| Property | Value | Description |
|----------|-------|-------------|
| Server Port | 8081 | Port where service runs |
| Database | PostgreSQL | `auth_db` |
| Service Discovery | Enabled | Registers with Eureka |
| Security | JWT | Stateless Authentication |

## Running Locally

```bash
mvn spring-boot:run
```
