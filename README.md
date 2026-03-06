# AI Support System Microservices Platform

## Overview

AI Support System is a microservices-based ticket management platform with AI-powered assistance, event-driven communication (Kafka), and rule-based automation.

## Architecture

- **discovery-service**: Eureka Service Discovery Server.
- **api-gateway**: Centralized entry point and request routing.
- **ticket-service**: Core ticket management and lifecycle handling.
- **ai-analysis-service**: AI-powered analysis using Spring AI (Google Gemini & OpenAI).
- **routing-service**: Orchestrates ticket routing and intelligent assignment.
- **common-library**: Shared models, events, and utilities.
- **aisupport-parent**: Central Maven parent for dependency management.

## Technology Stack

- **Java**: 21
- **Spring Boot**: 4.0.3
- **Spring Cloud**: 2025.1.0
- **Spring AI**: 2.0.0-M1 (Gemini & OpenAI)
- **Messaging**: Apache Kafka
- **Database**: PostgreSQL
- **Service Discovery**: Eureka
- **API Documentation**: SpringDoc OpenAPI
- **Resilience**: Resilience4j (Circuit Breaker)

## Prerequisites

- Java 21 or higher
- Maven 3.9+ or use included Maven wrapper
- PostgreSQL 18+
- Docker (optional, for containerization)

## Getting Started

### Build All Services

```bash
mvn clean install
```

### Run Services (Order Matters)

1. **Discovery Service**:

   ```bash
   cd discovery-service
   mvn spring-boot:run
   ```

2. **API Gateway**:

   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

3. **Core Services** (Start any order):
   - Ticket Service (`cd ticket-service && mvn spring-boot:run`)
   - AI Analysis Service (`cd ai-analysis-service && mvn spring-boot:run`)
   - Routing Service (`cd routing-service && mvn spring-boot:run`)

## Project Structure

```plaintext
ai-support-system/
├── discovery-service/    # Eureka Server (Port: 8761)
├── api-gateway/          # Spring Cloud Gateway (Port: 8081)
├── ticket-service/       # Ticket Management (Port: 8082)
├── ai-analysis-service/  # AI Powered Analysis (Port: 8083)
├── routing-service/      # Routing Orchestrator (Port: 8084)
├── common-library/       # Shared Components & DTOs
├── aisupport-parent/     # Maven Parent POM
└── .github/              # GitHub Config & Copilot Instructions
```

## API Documentation

Each service provides its own OpenAPI documentation. Once running, access via Swagger UI:

- Ticket Service: <http://localhost:8082/swagger-ui.html>
- AI Analysis Service: <http://localhost:8083/swagger-ui.html>
- Routing Service: <http://localhost:8085/swagger-ui.html>

## Code Quality

```bash
# Run SonarQube analysis
mvn clean verify sonar:sonar
```

## Contributing

1. Follow layered architecture (Controller → Service → Repository)
2. Use constructor-based dependency injection
3. Write unit tests (JUnit 5 + Mockito)
4. Use `common-library` for shared models and exceptions
5. Ensure SonarQube compliance

## License

MIT License
