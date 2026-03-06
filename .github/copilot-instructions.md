# Copilot Instructions for AI Support System

This repository is a Spring Boot 4.0.3 microservices platform for AI-powered ticket management, using Spring AI with Google Gemini and OpenAI integration, service discovery, and event-driven automation via Kafka.

## Build, Test, and Lint Commands

- **Build all services:**
  ```bash
  mvn clean install
  ```

- **Build a single module:**
  ```bash
  mvn clean install -pl <module-name>
  ```

- **Run a service:**
  ```bash
  cd <service-dir> && mvn spring-boot:run
  ```

- **Run tests (all):**
  ```bash
  mvn test
  ```

- **SonarQube analysis:**
  ```bash
  mvn clean verify sonar:sonar
  ```

## High-Level Architecture

- **discovery-service:** Eureka registry for all microservices.
- **api-gateway:** Central entry point, routes requests (Port: 8081).
- **ticket-service:** Core ticket management (Port: 8082).
- **ai-analysis-service:** AI analysis via Spring AI (Gemini & OpenAI) (Port: 8083).
- **routing-service:** Orchestrates ticket routing (Port: 8085).
- **common-library:** Shared DTOs, events, exceptions, utilities.
- **aisupport-parent:** Shared Maven parent configuration.

### Service Startup Order
1. `discovery-service` (Eureka)
2. `api-gateway`
3. Core services: `ticket-service`, `ai-analysis-service`, `routing-service` (any order)

### API Documentation
Each core service exposes OpenAPI docs at `/swagger-ui.html` (e.g., http://localhost:8082/swagger-ui.html).

## Key Conventions

- **Architecture:** Layered (Controller → Service → Repository).
- **DI:** Constructor-based dependency injection.
- **Shared Code:** Use `common-library` for models, events, and exceptions.
- **Testing:** JUnit 5 + Mockito.
- **Communication:** Synchronous via Feign/LoadBalancer (where applicable) and Asynchronous via Kafka.
- **Discovery:** Eureka (no hardcoded URLs).

## Environment Variables

### AI Analysis Service
- **Google Cloud:** `GCP_PROJECT_ID`, `GCP_LOCATION`, `GOOGLE_APPLICATION_CREDENTIALS`.
- **OpenAI:** `SPRING_AI_OPENAI_API_KEY`.

---

Summary: Updated .github/copilot-instructions.md with the latest tech stack (Java 21, Spring Boot 4.x), OpenAI/Gemini integration details, and Kafka mention.
