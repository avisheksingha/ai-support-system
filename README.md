# AI Support System Microservices Platform

## Overview
AI Support System is a microservices-based ticket management platform with AI-powered assistance and rule-based automation.

## Architecture
- **ticket-service**: Core ticket management functionality
- **ai-service**: AI-powered ticket analysis and suggestions
- **rule-based-service**: Automated ticket routing and processing

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
- PostgreSQL 16+
- Docker (optional, for containerization)

## Getting Started

### Build All Services
```bash
mvn clean install
```

### Run Specific Service
```bash
cd ticket-service
mvn spring-boot:run
```

### Run Tests
```bash
mvn test
```

### Run with Testcontainers
```bash
mvn verify
```

## Project Structure
```
aisupport-project/
├── pom.xml                    # Parent POM
├── ticket-service/            # Ticket management microservice
├── ai-service/                # AI-powered analysis microservice
└── rule-based-service/        # Rule-based automation microservice
```

## Configuration
Each microservice has its own `application.yml` in `src/main/resources/`.

## API Documentation
Once running, access Swagger UI:
- Ticket Service: http://localhost:8081/swagger-ui.html
- AI Service: http://localhost:8082/swagger-ui.html
- Rule-based Service: http://localhost:8083/swagger-ui.html

## Code Quality
```bash
# Run SonarQube analysis
mvn clean verify sonar:sonar
```

## Contributing
1. Follow layered architecture (Controller → Service → Repository)
2. Use constructor-based dependency injection
3. Write unit tests (JUnit 5 + Mockito)
4. Write integration tests (Testcontainers)
5. Ensure SonarQube compliance

## License
MIT License
