# Discovery Service Agent

**Role:** Eureka Service Registry

**Port:** 8761

**Responsibility:** Hosts Eureka registry where services register and discover each other through service names.

## Quick Commands

### Build
```bash
mvn -pl discovery-service clean install
```

### Run Service
```bash
cd discovery-service && mvn spring-boot:run
```

### Open Dashboard
```text
http://localhost:8761
```

## Key Files

- **Main App:** `src/main/java/com/aisupport/eureka/EurekaDiscoveryServiceApplication.java`
- **Main Config:** `src/main/resources/application.properties`
- **GCP Profile:** `src/main/resources/application-gcp.properties`

## Key Responsibilities

1. Accept service registrations from gateway and core services.
2. Serve registry lookups for `lb://SERVICE-NAME` routing.
3. Track availability through Eureka heartbeat/eviction behavior.
4. Provide a simple dashboard for local diagnostics.

## Current Config Notes

From `application.properties`:

- `eureka.client.register-with-eureka=false`
- `eureka.client.fetch-registry=false`
- `eureka.server.enable-self-preservation=true`
- `eureka.server.eviction-interval-timer-in-ms=5000`

## Common Tasks

### Verify Apps in Registry
```bash
curl "http://localhost:8761/eureka/apps"
```

### Inspect One Service
```bash
curl "http://localhost:8761/eureka/apps/TICKET-SERVICE"
```

### Check Registry UI
- Visit `http://localhost:8761`
- Confirm all expected services are `UP`

## Important Rules

- Keep discovery service lightweight and infra-focused.
- Do not add business/domain logic here.
- Keep service names consistent with gateway route URIs.

## Related Services

- `api-gateway`
- `ticket-service`
- `ai-analysis-service`
- `routing-service`
- `rag-service`

## Debugging Tips

1. Service missing: verify that service has Eureka client enabled in its active profile.
2. Registry empty: confirm discovery-service started before other services.
3. Flapping instances: check network reachability and profile-specific Eureka URLs.
