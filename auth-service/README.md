# Auth Service

The Auth Service manages user registration, authentication, account status, roles, access-token issuance, and refresh-token rotation.

## Security model

The service uses stateless JWT access tokens and server-side refresh tokens.

- Passwords are hashed using BCrypt with strength 12.
- Access tokens expire after 15 minutes by default.
- Refresh tokens expire after 7 days, are stored as hashes, and are rotated atomically on use.
- Public endpoints are registration, login, and token refresh.
- User and administration endpoints require authentication.
- Administration endpoints require the `ADMIN` role.
- The API Gateway validates access tokens and replaces client-supplied identity headers with verified identity.
- Backend service ports must not be publicly accessible because downstream services trust gateway identity headers.

`JWT_SECRET` is mandatory and must contain a strong Base64-encoded secret shared securely with the API Gateway. Never commit a production secret.

## API endpoints

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Public | Register a customer account |
| POST | `/api/v1/auth/login` | Public | Authenticate and issue access and refresh tokens |
| POST | `/api/v1/auth/refresh` | Public | Rotate a valid refresh token |
| POST | `/api/v1/auth/logout` | Authenticated | Revoke the current user's refresh tokens |
| GET | `/api/v1/auth/me` | Authenticated | Return the current user |
| GET | `/api/v1/auth/admin/users` | ADMIN | List users |
| PATCH | `/api/v1/auth/admin/users/{id}/role` | ADMIN | Update a user's role |
| POST | `/api/v1/auth/admin/users/{id}/lock` | ADMIN | Lock an account and revoke refresh tokens |
| POST | `/api/v1/auth/admin/users/{id}/unlock` | ADMIN | Unlock an account |

The role update endpoint accepts JSON such as:

```json
{"role":"AGENT"}
```

## Local configuration

```properties
server.port=8081
app.jwt.secret=${JWT_SECRET}
app.jwt.access-token-expiration-ms=900000
app.jwt.refresh-token-expiration-ms=604800000
```

Start PostgreSQL and Eureka, set `DB_PASSWORD` and `JWT_SECRET`, and run:

```bash
mvn -f auth-service/pom.xml spring-boot:run
```

OpenAPI UI: `http://localhost:8081/swagger-ui/index.html`
