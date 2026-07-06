# 4. Stateless Authentication and CSRF Protection

Date: 2026-07-06

## Status

Accepted

## Context

Our microservices expose stateless REST APIs using Bearer tokens (JWT) for authentication via the `HeaderAuthenticationFilter`. Because we do not rely on session cookies (no `JSESSIONID`), Cross-Site Request Forgery (CSRF) is naturally mitigated, as there are no cookies for the browser to automatically attach to cross-origin requests.

However, security scanning tools (like GitHub Advanced Security / CodeQL) flag instances where Spring Security's CSRF module is disabled (`csrf.disable()`) as a high-severity vulnerability. Relying solely on developers to maintain statelessness provides no physical guarantee against a future change accidentally introducing cookie-based authentication, which would silently open a CSRF surface.

## Decision

1. **Stateless First**: All internal services will strictly use Bearer-token authentication. Cookie authentication is forbidden for the REST API.
2. **Explicit Disablement**: We explicitly disable the CSRF module in Spring Security (`.csrf(csrf -> csrf.disable())`) and suppress false-positive static analysis alerts with `// NOSONAR` and `// codeql[java/spring-disabled-csrf-protection]`.
3. **Runtime Cookie Guard**: To enforce this invariant at runtime, we implemented a global `CookieGuardFilter` (located in `common-library`). This filter inspects incoming requests *before* authentication happens and rejects any request containing a `Cookie` header (unless explicitly whitelisted).

## Consequences

- **Positive**: We completely eliminate CSRF vulnerabilities through physical runtime constraints rather than configuration assumptions.
- **Positive**: We appease security scanners gracefully with documented mitigations.
- **Negative**: If a future feature legitimately requires a browser-based cookie flow (e.g., OAuth2 login), the `CookieGuardFilter`'s whitelist must be carefully updated via configuration properties (`security.cookie-guard.allowed-paths`), and CSRF protection must be re-evaluated for those paths.

## Addendum (2026-07-06)

- **CodeQL Syntax Fix**: Corrected the CodeQL suppression comment to use the exact case-sensitive keyword `codeql` and the correct rule ID `java/spring-disabled-csrf-protection`.
- **Filter vs Interceptor Execution**: The original implementation used a Spring MVC `HandlerInterceptor`. However, since MVC interceptors execute *after* the Spring Security filter chain (including authentication), an accidentally introduced session cookie would be fully processed by the security layer before the interceptor could reject it. To guarantee true defense-in-depth, the guard was refactored into a `OncePerRequestFilter` (`CookieGuardFilter`) and wired securely at the front of the chain via `.addFilterBefore(cookieGuardFilter, HeaderAuthenticationFilter.class)`.
