# AI Support System Microservices Platform

## Overview

AI Support System is a microservices-based ticket management platform with AI-powered assistance and rule-based automation.

## Architecture

- **discovery-service**: Eureka Service Discovery Server
- **api-gateway**: Centralized entry point and request routing
- **ticket-service**: Core ticket management functionality
- **ai-analysis-service**: AI-powered ticket analysis using Google Gemini
- **rule-engine-service**: Flexible rule-based logic for ticket processing
- **routing-service**: Orchestrates ticket routing between agents and teams
- **common-library**: Shared DTOs, exceptions, and utilities

## Technology Stack

- **Java**: 21
- **Spring Boot**: 4.0.2
- **Spring Cloud**: 2025.1.0
- **Database**: PostgreSQL
- **Build Tool**: Maven
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
   - Rule Engine Service (`cd rule-engine-service && mvn spring-boot:run`)
   - Routing Service (`cd routing-service && mvn spring-boot:run`)

## Project Structure

```
ai-support-system/
├── discovery-service/    # Eureka Server (Port: 8761)
├── api-gateway/          # Spring Cloud Gateway (Port: 8081)
├── ticket-service/       # Ticket Management (Port: 8082)
├── ai-analysis-service/  # AI Powered Analysis (Port: 8083)
├── rule-engine-service/  # Rule Management (Port: 8084)
├── routing-service/      # Routing Orchestrator (Port: 8085)
├── common-library/       # Shared Components & DTOs
└── aisupport-parent/     # Maven Parent POM
```

## API Documentation

Each service provides its own OpenAPI documentation. Once running, access via Swagger UI:

- Ticket Service: http://localhost:8082/swagger-ui.html
- AI Analysis Service: http://localhost:8083/swagger-ui.html
- Rule Engine Service: http://localhost:8084/swagger-ui.html
- Routing Service: http://localhost:8085/swagger-ui.html

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
