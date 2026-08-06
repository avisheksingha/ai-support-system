# AI Support System Microservices Platform

<!-- Project Status & Quality -->
[![GitHub release](https://img.shields.io/github/v/release/avisheksingha/ai-support-system)](https://github.com/avisheksingha/ai-support-system/releases/latest)
[![GitHub Repo stars](https://img.shields.io/github/stars/avisheksingha/ai-support-system)](https://github.com/avisheksingha/ai-support-system/stargazers)
[![CI/CD](https://github.com/avisheksingha/ai-support-system/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/avisheksingha/ai-support-system/actions/workflows/ci-cd.yml)
[![CodeQL](https://github.com/avisheksingha/ai-support-system/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/avisheksingha/ai-support-system/actions/workflows/github-code-scanning/codeql)
[![Dependabot Updates](https://github.com/avisheksingha/ai-support-system/actions/workflows/dependabot/dependabot-updates/badge.svg)](https://github.com/avisheksingha/ai-support-system/actions/workflows/dependabot/dependabot-updates)
[![Deploy to AWS EC2](https://github.com/avisheksingha/ai-support-system/actions/workflows/deploy-aws.yml/badge.svg)](https://github.com/avisheksingha/ai-support-system/actions/workflows/deploy-aws.yml)

<!-- Tech Stack -->
[![Java 21](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.1.2-0ea5e9)](https://spring.io/projects/spring-cloud)
[![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Spring AI](https://img.shields.io/badge/Spring_AI-2.0.0-6DB33F?logo=spring&logoColor=white)](https://docs.spring.io/spring-ai/reference/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![React](https://img.shields.io/badge/React-TypeScript-61DAFB?logo=react&logoColor=white)](https://react.dev/)

<!-- Docs & Tools -->
[![Architecture](https://img.shields.io/badge/Architecture-Microservices-orange)](OVERVIEW.md)
[![GitHub Copilot](https://img.shields.io/badge/GitHub%20Copilot-Ready-0F66D9?logo=github&logoColor=white)](.github/copilot-instructions.md)

## Table of Contents

| Section | Purpose |
| --------- | ------------- |
| [Business Problem](#business-problem) | The customer support challenges this platform is designed to solve. |
| [Solution](#solution) | How the platform leverages AI and microservices to address those challenges. |
| [Why This Project](#why-this-project) | Highlights the enterprise engineering practices demonstrated in this repository. |
| [Feature Matrix](#feature-matrix) | Overview of the platform's implemented capabilities. |
| [Overview](#overview) | High-level introduction and links to detailed architecture documentation. |
| [Architecture](#architecture) | System architecture, technology stack, and microservice responsibilities. |
| [Engineering Decisions](#engineering-decisions) | Design choices, trade-offs, and technology selection rationale. |
| [Local Development](#local-development) | Prerequisites, runtime profiles, and local setup instructions. |
| [API Documentation](#api-documentation) | Available service endpoints, Swagger locations, and local access URLs. |
| [Authentication Architecture](#authentication-architecture) | JWT authentication model, security flow, and authorization strategy. |
| [Sample API Flow](#sample-api-flow) | End-to-end example demonstrating request processing through the platform. |
| [Project Structure](#project-structure) | Repository organization and purpose of each major module. |
| [Contributing](#contributing) | Contribution workflow, coding standards, and pull request process. |
| [Security](#security) | Security policy and responsible vulnerability disclosure process. |
| [Community Health](#community-health) | Community guidelines, issue templates, and project governance. |
| [License](#license) | Project licensing information. |

## Business Problem

Modern customer support teams often handle hundreds or thousands of tickets across multiple channels. As ticket volumes grow, manual triage becomes increasingly difficult and introduces several operational challenges:  

- Urgent customer issues may not be identified quickly enough.
- Tickets can be assigned to the wrong team or support queue.
- Support agents spend valuable time performing repetitive classification tasks.
- Knowledge retrieval becomes slower as historical ticket data grows.
- Inconsistent prioritization can negatively impact customer satisfaction and response times.

Organizations need a scalable solution that can automatically analyze incoming requests, identify urgency, route tickets intelligently, and provide contextual assistance to support teams.

## Solution

The AI Support System is a microservices-based platform designed to automate support ticket processing using Artificial Intelligence and event-driven architecture.

The platform combines AI-powered sentiment analysis, urgency detection, intelligent routing, and Retrieval-Augmented Generation (RAG) to streamline support operations and reduce manual intervention.

Key capabilities include:

- AI-driven sentiment and urgency analysis using Google GenAI (Gemini/Vertex AI).
- Event-driven asynchronous processing using Apache Kafka.
- Intelligent ticket routing based on business rules and AI insights.
- Semantic search and contextual knowledge retrieval using PostgreSQL pgvector.
- Distributed microservices architecture built with Spring Boot and Spring Cloud.
- Correlation ID–based request tracing across services.
- Scalable and cloud-ready deployment architecture.

The result is a system that demonstrates how modern AI services, vector search, and event-driven microservices can work together to improve customer support workflows while maintaining scalability, observability, and maintainability.

## Why This Project?

This project demonstrates enterprise backend engineering practices, including:

- Event-driven microservices
- AI-assisted ticket analysis
- Retrieval-Augmented Generation (RAG)
- JWT authentication with token rotation
- Distributed tracing using Correlation IDs
- Outbox Pattern for reliable messaging
- Circuit breakers with Resilience4j
- Cloud-ready deployment using Docker and Kubernetes

## Feature Matrix

| Capability             | Description                                                                  |
| ---------------------- | ---------------------------------------------------------------------------- |
| Ticket Management      | Role-aware ticket lifecycle, assignment, priority, and messaging             |
| AI Sentiment Analysis  | Google GenAI-powered sentiment classification                                |
| Intelligent Routing    | Rule-based routing using AI insights                                         |
| Workflow Orchestration | Event-driven AI workflow runtime that composes analysis, routing, and RAG    |
| Knowledge Base         | Article management, embedding synchronization, and contextual retrieval      |
| Support Dashboard      | Role-specific customer, agent, and administrator workspaces                  |
| Event Processing       | Kafka-based asynchronous workflows                                           |
| Observability          | Correlation ID tracing, workflow timelines, operations, and governance views |
| Reliability            | Outbox Pattern and Resilience4j                                              |
| Deployment             | Docker Compose infrastructure                                                |
| CI/CD                  | Automated GitHub Actions pipeline                                            |

## Overview

The AI Support System is a leading-edge, microservices-based ticket management platform designed to automate and augment traditional support workflows. It leverages AI for analyzing ticket sentiment, urgency, and intent, employs event-driven communication via Apache Kafka, and utilizes rule-based orchestration to intelligently route tickets. Finally, it integrates a Retrieval-Augmented Generation (RAG) service to provide contextual AI responses.

> For a comprehensive mapping of the system flow, module interactions, and diagrams, please refer to the **[System Overview](OVERVIEW.md)** document.
> For a detailed explanation of design decisions, technology stack rationale, and scalability considerations, see the **[Architecture](ARCHITECTURE.md)** document.
> For test execution commands (including controller/service test pack), see **[Testing Guide](TESTING.md)**.

## Architecture

### Architecture Diagram

![AI Support System Architecture](docs/architecture/architecture.png)

### Demo Video

![Platform Walkthrough](docs/video/demo-video.gif)

> **Full walkthrough:** Customer ticket submission → Kafka event ingestion → AI sentiment analysis → RAG-powered draft → Smart routing → Admin governance dashboard.

### Platform Screenshots

> Detailed screenshots of all workspaces (Customer, Agent, Admin) and infrastructure are available in [docs/screenshots](docs/screenshots/).

### Technology Stack

| Category          | Technology                             |
| ----------------- | -------------------------------------- |
| Language          | **Java 21**                            |
| Framework         | **Spring Boot** 4.1.0                  |
| Cloud             | **Spring Cloud** 2025.1.2              |
| AI                | **Spring AI** 2.0.0 + **Google GenAI** |
| Messaging         | **Apache Kafka**                       |
| Database          | **PostgreSQL** + **pgvector**          |
| Security          | **JWT** + **Spring Security**          |
| Build             | **Maven**                              |
| Containers        | **Docker Compose**                     |
| CI/CD             | **GitHub Actions**                     |
| API Documentation | **SpringDoc OpenAPI**                  |

### Architecture & Key Components

- **[discovery-service](discovery-service/README.md)**: Eureka Service Discovery Server.
- **[api-gateway](api-gateway/README.md)**: Centralized entry point and request routing.
- **[auth-service](auth-service/README.md)**: Authentication, authorization, and JWT management.
- **[ticket-service](ticket-service/README.md)**: Core ticket management and lifecycle operations.
- **[ai-orchestration-service](ai-orchestration-service/README.md)**: Central workflow runtime that coordinates AI executions by composing domain capabilities.
- **[ai-analysis-service](ai-analysis-service/README.md)**: Domain capability service for AI-powered analysis (sentiment and urgency).
- **[routing-service](routing-service/README.md)**: Domain capability service for deterministic ticket routing decisions.
- **[rag-service](rag-service/README.md)**: Domain capability service providing vector embedding and contextual knowledge retrieval.
- **[ai-support-dashboard](ai-support-dashboard/README.md)**: React dashboard for customer, agent, and administrator workflows.
- **[common-library](common-library/README.md)**: Shared models, DTOs, events, and utilities.

- **[aisupport-parent](aisupport-parent/README.md)**: Central Maven POM for uniform dependency management.
- **[infra](infra/README.md)**: Docker Compose setup for infrastructure (PostgreSQL, Kafka, pgvector, Redpanda Console).

## Engineering Decisions

### Why Microservices?

The platform is intentionally designed as a microservices architecture to demonstrate service isolation, independent scalability, and clear separation of responsibilities across ticket management, AI analysis, routing, and knowledge retrieval domains.

### Why Apache Kafka?

Ticket processing involves multiple asynchronous operations such as AI analysis, routing decisions, and knowledge enrichment. Apache Kafka enables event-driven communication between services while reducing direct service-to-service coupling and improving scalability.

### Why PostgreSQL with pgvector?

Traditional relational data is stored in PostgreSQL while pgvector enables semantic similarity search for Retrieval-Augmented Generation (RAG) workflows. This combination allows structured transactional storage and AI-powered contextual retrieval within the same database platform.

### Why Google GenAI (Gemini / Vertex AI)?

Google GenAI (via Vertex AI or Gemini API) provides managed access to modern foundation models for sentiment analysis, urgency detection, and intent classification while reducing operational overhead associated with hosting and maintaining custom AI models.

### Why Spring Boot and Spring Cloud?

Spring Boot accelerates microservice development through convention-based configuration, while Spring Cloud provides service discovery, gateway routing, and cloud-native integration patterns commonly used in enterprise environments.

### Why Event-Driven Processing?

Support tickets do not require synchronous AI processing before being created. Event-driven processing allows tickets to be accepted immediately while downstream services perform analysis and routing asynchronously, improving responsiveness and user experience.

## Local Development

### Prerequisites

- Java 21+
- Maven 3.9+ (or use included wrapper)
- Docker & Docker Compose (for spinning up Kafka, ZooKeeper, PostgreSQL, etc.)

### Runtime Profiles

Each service supports profile-driven startup:

`spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}`

- `local`: IDE/local development
- `docker`: Docker network runtime
- `k8s`: Kubernetes runtime using explicit service URLs

Discovery strategy:

- `local`/`docker`: Eureka-based service discovery
- `k8s`: Eureka clients disabled, environment-based service URLs

### AI-Assisted Development

This repository uses AI coding assistants (including GitHub Copilot) as productivity tools for scaffolding, refactoring suggestions, and test drafting.

Engineering policy:

- AI-generated code is reviewed and validated before merge.
- Build/test checks must pass before PR approval.
- Security-sensitive decisions (credentials, logging, deployment config) are manually reviewed by the maintainer.

### 1. Configure Environment Variables

Create a `.env` file in the project root by copying `.env.example` and updating the required values.

```bash
cp .env.example .env
```

> **Note:** Windows users can manually copy `.env.example` to `.env`.

The current local profiles use `GOOGLE_CLOUD_PROJECT` and `GOOGLE_CLOUD_LOCATION` for Google GenAI. Authenticate with Google Application Default Credentials (`gcloud auth application-default login`) or provide `GOOGLE_APPLICATION_CREDENTIALS` in your local environment. `OPENAI_API_KEY` is only required when switching to the optional OpenAI provider.

---

### 2. Start Infrastructure

Start the local infrastructure required by the microservices.

This launches:

- PostgreSQL + PGVector
- Apache Kafka
- Apache ZooKeeper
- Redpanda Console (Kafka UI at <http://localhost:9090>)

```bash
docker compose --env-file .env -f infra/docker-compose.yml up -d
```

Verify the containers are running:

```bash
docker ps
```

---

### 3. Stop Infrastructure

Stop all infrastructure containers while preserving database data.

```bash
docker compose -f infra/docker-compose.yml down
```

---

### 4. Reset Local Database (Optional)

Stop the infrastructure and remove all Docker volumes.

> **Warning**
> This permanently deletes all local PostgreSQL data and recreates the database on the next startup.

```bash
docker compose -f infra/docker-compose.yml down -v
```

---

### 5. Start the Microservices

Once the infrastructure is running, start the Spring Boot microservices from your IDE (recommended for local development) or using Maven.

Recommended startup order:

1. discovery-service
2. auth-service
3. ai-analysis-service
4. routing-service
5. rag-service
6. ai-orchestration-service
7. ticket-service
8. api-gateway

Run each service from its own directory, for example:

```bash
cd discovery-service
./mvnw spring-boot:run
```

The workflow runtime must be running before tickets are created; it consumes `TicketCreatedEvent` and coordinates the downstream AI capabilities.

### 6. Start the Support Dashboard (Optional)

The dashboard uses the gateway at `http://localhost:8080/api/v1` by default. After the backend services are running:

```bash
cd ai-support-dashboard
npm ci
npm run dev
```

Open the URL printed by Vite (normally `http://localhost:5173`). See the [dashboard README](ai-support-dashboard/README.md) for environment variables, roles, and available workspaces.

## API Documentation

Each service provides its own OpenAPI documentation. Available locally at:

| Service           | Port | Swagger / UI                               |
| ----------------- | ---: | ------------------------------------------ |
| Auth              | 8081 | `/swagger-ui/index.html`                   |
| Ticket            | 8082 | `/swagger-ui/index.html`                   |
| AI Analysis       | 8083 | `/swagger-ui/index.html`                   |
| Routing           | 8084 | `/swagger-ui/index.html`                   |
| RAG               | 8085 | `/swagger-ui/index.html`                   |
| AI Orchestration  | 8086 | `/swagger-ui/index.html`                   |
| Gateway           | 8080 | `/swagger-ui/index.html` (aggregated docs) |
| Eureka            | 8761 | `/` (dashboard)                            |
| Redpanda          | 9090 | `/overview` (Kafka UI)                     |
| Support Dashboard | 5173 | Vite development server                    |

## Authentication Architecture

> The AI Support System uses a stateless JWT authentication model with rotating refresh tokens. Authentication is centralized in the API Gateway, ensuring that backend microservices never receive unverified requests. The gateway validates every access token, strips client-supplied identity headers, and forwards trusted user information to downstream services.

### Security Model

| Component        | Responsibility                                                                    |
| ---------------- | --------------------------------------------------------------------------------- |
| Frontend         | Stores tokens securely and refreshes access tokens when required                  |
| API Gateway      | Validates JWTs, removes spoofed identity headers, propagates trusted user context |
| Auth Service     | Handles registration, login, logout, token refresh, and user management           |
| Backend Services | Trust the gateway and focus exclusively on business logic                         |

### Token Lifecycle

| Token         |   Lifetime | Purpose                                                        |
| ------------- | ---------: | -------------------------------------------------------------- |
| Access Token  | 15 minutes | Authenticate API requests                                      |
| Refresh Token |     7 days | Obtain new access and refresh tokens without re-authentication |

### Protected Endpoints

| Endpoint        | Authentication         | Authorization      |
| --------------- | ---------------------- | ------------------ |
| Register        | Public                 | —                  |
| Login           | Public                 | —                  |
| Refresh         | Public (Refresh Token) | —                  |
| Logout          | Required               | Authenticated User |
| `/me`           | Required               | Authenticated User |
| User Management | Required               | `ADMIN`            |

### Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Gateway
    participant Auth
    participant Service

    User->>Frontend: Login
    Frontend->>Auth: POST /login
    Auth-->>Frontend: Access Token + Refresh Token

    Frontend->>Gateway: API Request (Access Token)
    Gateway->>Gateway: Validate JWT

    alt Token valid
        Gateway->>Service: Forward trusted user context
        Service-->>Gateway: Response
        Gateway-->>Frontend: Response
    else Near expiry
        Gateway-->>Frontend: X-Access-Token-Refresh: true
        Frontend->>Auth: POST /refresh
        Auth-->>Frontend: New Access + Refresh Tokens
    end
```

### Authentication Principles

- Backend services are never exposed directly to external clients.
- All client traffic enters through the API Gateway.
- Identity is established only after JWT validation.
- Client-supplied identity headers are discarded to prevent spoofing.
- Refresh tokens are rotated on every successful refresh.
- Business services remain stateless and do not perform authentication.

## Sample API Flow

Run this end-to-end flow to demonstrate the project quickly:

**1. Register and log in as a customer.**

```bash
curl -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"ChangeMe123!","fullName":"Demo Customer"}'

curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"ChangeMe123!"}'
```

Copy `accessToken` from the login response into an environment variable, for example `export ACCESS_TOKEN="<accessToken>"`.

**2. Create a ticket through the gateway.**

```bash
curl -X POST "http://localhost:8080/api/v1/tickets" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{"subject":"Payment failed","message":"Card charged twice and order missing."}'
```

**3. Retrieve the customer's tickets (or inspect the returned ticket number).**

```bash
curl "http://localhost:8080/api/v1/tickets/my" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

**4. Check a ticket's details by ticket number.**

```bash
curl "http://localhost:8080/api/v1/tickets/my/{ticketNumber}" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### What Happens Next?

1. Client submits a ticket through the API Gateway.
2. The `ticket-service` persists the ticket and immediately returns a response.
3. A `TicketCreatedEvent` is published to Apache Kafka.
4. The `ai-orchestration-service` consumes the event and starts a workflow.
5. The orchestrator composes the `ai-analysis-service` (via synchronous REST tools) for sentiment/urgency, and the `rag-service` for knowledge retrieval.
6. Based on the analysis, it coordinates with the `routing-service` to assign the ticket to the appropriate queue.
7. A `TicketOrchestratedEvent` is published asynchronously upon workflow completion, carrying the final analysis, routing decision, and knowledge context back to the `ticket-service`.
8. Correlation IDs enable end-to-end request tracing across all services.

### Processing Flow

```mermaid
flowchart LR
    A[Client]
    --> B[API Gateway]
    --> C[Ticket Service]

    C --> D[(PostgreSQL)]
    C --> E[TicketCreatedEvent]

    E --> F[Kafka]

    F --> G[AI Orchestration Service]
    G -.REST.-> H[AI Analysis Service]
    G -.REST.-> I[Routing Service]
    G -.REST.-> J[RAG Service]
    
    G --> K[Business Events]
```

> **Hybrid Communication Model:** The platform intentionally uses Kafka for asynchronous business events (like `TicketCreatedEvent`), while the AI Orchestration Service uses synchronous REST/internal clients to execute Tool Calling and compose the domain capabilities.

## Project Structure

```plaintext
ai-support-system/
├── .github/                # GitHub Actions, issue templates, and Copilot guidance
├── discovery-service/      # Eureka Server (Port: 8761)
├── api-gateway/            # Spring Cloud Gateway (Port: 8080)
├── auth-service/           # Authentication & Authorization (Port: 8081)
├── ticket-service/         # Ticket Management (Port: 8082)
├── ai-analysis-service/    # AI Analysis via Google GenAI (OpenAI optional) (Port: 8083)
├── routing-service/        # Deterministic ticket routing (Port: 8084)
├── rag-service/            # Knowledge retrieval and embeddings (Port: 8085)
├── ai-orchestration-service/ # AI workflow runtime (Port: 8086)
├── ai-support-dashboard/   # React support dashboard
├── common-library/         # Shared DTOs, Logic, and Common Configuration
├── aisupport-parent/       # Maven Parent POM
├── infra/                  # Docker Config for DB/Kafka/Redpanda Console
├── docs/                   # Architecture, screenshots, demo video
├── ARCHITECTURE.md         # Design decisions and scalability
├── CONTRIBUTING.md         # Contribution workflow and PR expectations
├── OVERVIEW.md             # Architectural end-to-end details & diagrams
├── SECURITY.md             # Vulnerability reporting policy
├── TESTING.md              # Test execution and troubleshooting guide
└── README.md               # This file
```

### Internal Package Structure

The microservices follow a **Dual Package Philosophy**:

1. **Standard Domain Services** (e.g., `ticket-service`, `auth-service`, `rag-service`): Strictly adhere to a flat structure (`config`, `controller`, `service`, `repository`, `dto/request`, `dto/response`) avoiding abstract layers to maximize Spring Boot discoverability.
2. **Orchestrator** (`ai-orchestration-service`): Uses a strict feature-first Hexagonal Architecture (`config`, `application`, `domain`, `infrastructure`) due to its role in coordinating complex multi-agent AI workflows.

> **Note:** The backend package structure is structurally frozen for V1. Please see [12. Package and Naming Convention](docs/architecture/12-package-and-naming-convention.md) for detailed guidelines.

## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for contribution workflow, PR expectations, and contribution guidelines.

## Security

Please see [SECURITY.md](SECURITY.md) for vulnerability reporting and security response policy.

## Community Health

- Contribution guide: [CONTRIBUTING.md](CONTRIBUTING.md)
- Security policy: [SECURITY.md](SECURITY.md)
- Issue templates: `.github/ISSUE_TEMPLATE/`
- PR template: `.github/pull_request_template.md`

## License

MIT License — see [LICENSE](LICENSE) for details.
