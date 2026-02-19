# AI Support System Microservices Platform

## Overview

AI Support System is a microservices-based ticket management platform with AI-powered assistance and rule-based automation.

## Architecture

- **discovery-service**: Eureka Service Discovery Server
- **ticket-service**: Core ticket management functionality
- **ai-analysis-service**: AI-powered ticket analysis and suggestions
- **common-library**: Shared DTOs, exceptions, and utilities

## Technology Stack

- **Java**: 21
- **Spring Boot**: 4.0.2
- **Spring Cloud**: 2025.1.0
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Service Discovery**: Eureka
- **API Documentation**: SpringDoc OpenAPI

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

### Run Specific Service

1. **Discovery Service** (Start first):
   ```bash
   cd discovery-service
   mvn spring-boot:run
   ```
2. **Ticket Service**:
   ```bash
   cd ticket-service
   mvn spring-boot:run
   ```
3. **AI Analysis Service**:
   ```bash
   cd ai-analysis-service
   mvn spring-boot:run
   ```

## Project Structure

```
aisupport-project/
├── pom.xml                    # Parent POM
├── discovery-service/         # Service Discovery (Eureka)
├── ticket-service/            # Ticket management microservice
├── ai-analysis-service/       # AI-powered analysis microservice
└── common-library/            # Shared components
```

## Configuration

Each microservice has its own `application.properties` or `application.yml` in `src/main/resources/`.

## API Documentation

Once running, access Swagger UI:

- Ticket Service: http://localhost:8082/swagger-ui.html
- AI Analysis Service: http://localhost:8083/swagger-ui.html

## Code Quality

```bash
# Run SonarQube analysis
mvn clean verify sonar:sonar
```

## Contributing

1. Follow layered architecture (Controller → Service → Repository)
2. Use constructor-based dependency injection
3. Write unit tests (JUnit 5 + Mockito)
4. Write integration tests
5. Ensure SonarQube compliance

## License

MIT License
