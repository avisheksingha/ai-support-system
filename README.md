# AI Support System Microservices Platform

## Overview

The AI Support System is a leading-edge, microservices-based ticket management platform designed to automate and augment traditional support workflows. It leverages AI for analyzing ticket sentiment, urgency, and intent, employs event-driven communication via Apache Kafka, and utilizes rule-based orchestration to intelligently route tickets. Finally, it integrates a Retrieval-Augmented Generation (RAG) service to provide contextual AI responses.

> For a comprehensive mapping of the system flow, module interactions, and diagrams, please refer to the **[System Overview](OVERVIEW.md)** document.
> For a detailed explanation of design decisions, technology stack rationale, and scalability considerations, see the **[Architecture](ARCHITECTURE.md)** document.

## Architecture & Key Components

- **[discovery-service](discovery-service/README.md)**: Eureka Service Discovery Server.
- **[api-gateway](api-gateway/README.md)**: Centralized entry point and request routing.
- **[ticket-service](ticket-service/README.md)**: Core ticket management and lifecycle operations.
- **[ai-analysis-service](ai-analysis-service/README.md)**: AI-powered analysis for sentiment and urgency (Gemini & OpenAI).
- **[routing-service](routing-service/README.md)**: Orchestrator for intelligent ticket assignment based on analysis.
- **[rag-service](rag-service/README.md)**: Vector embedding and RAG capabilities for automated contextual responses.
- **[common-library](common-library/README.md)**: Shared models, DTOs, events, and utilities.
- **[aisupport-parent](aisupport-parent/README.md)**: Central Maven POM for uniform dependency management.
- **[infra](infra/README.md)**: Docker Compose setup for infrastructure (PostgreSQL, Kafka, pgvector).

## Technology Stack

- **Java**: 21
- **Spring Boot**: 4.0.3
- **Spring Cloud**: 2025.1.0
- **Spring AI**: 2.0.0-M1
- **Messaging**: Apache Kafka
- **Database**: PostgreSQL (with `pgvector` extension)
- **Service Discovery**: Eureka
- **API Documentation**: SpringDoc OpenAPI

## Prerequisites

- Java 21+
- Maven 3.9+ (or use included wrapper)
- Docker & Docker Compose (for spinning up Kafka, PostgreSQL, etc.)

## Getting Started

### 1. Start Infrastructure

Start the underlying database and messaging infrastructure:
```bash
cd infra
docker-compose up -d
cd ..
```

### 2. Build All Services

```bash
mvn clean install
```

### 3. Run Services (In Order)

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

3. **Core Services** (Start in parallel or sequentially):
   - Ticket Service: `cd ticket-service && mvn spring-boot:run`
   - AI Analysis Service: `cd ai-analysis-service && mvn spring-boot:run`
   - Routing Service: `cd routing-service && mvn spring-boot:run`
   - RAG Service: `cd rag-service && mvn spring-boot:run`

## Project Structure

```plaintext
ai-support-system/
├── discovery-service/    # Eureka Server (Port: 8761)
├── api-gateway/          # Spring Cloud Gateway (Port: 8081)
├── ticket-service/       # Ticket Management (Port: 8082)
├── ai-analysis-service/  # AI Analysis via Gemini/OpenAI (Port: 8083)
├── routing-service/      # Intelligent Routing Orchestrator (Port: 8084)
├── rag-service/          # Contextual Knowledge Response (Port: 8085)
├── common-library/       # Shared DTOs and Logic
├── aisupport-parent/     # Maven Parent POM
├── infra/                # Docker Config for DB/Kafka
├── ARCHITECTURE.md       # Design decisions and scalability
├── OVERVIEW.md           # Architectural end-to-end details & diagrams
└── README.md             # This file
```

## API Documentation

Each service provides its own OpenAPI documentation. Available locally at:
- Ticket Service: `http://localhost:8082/swagger-ui.html`
- AI Analysis Service: `http://localhost:8083/swagger-ui.html`
- Routing Service: `http://localhost:8084/swagger-ui.html`
- RAG Service: `http://localhost:8085/swagger-ui.html`

## License
MIT License
