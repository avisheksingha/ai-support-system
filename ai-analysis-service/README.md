# AI Analysis Service

Domain Capability Service that uses Spring AI to analyze support tickets for sentiment, urgency, and intent. The active provider is Google GenAI (supporting both Gemini API and Vertex AI), with OpenAI available as an optional provider.

## Features

- Analyzes ticket content (Subject + Message)
- Detects Sentiment (POSITIVE, NEUTRAL, NEGATIVE)
- Determines Urgency (LOW, MEDIUM, HIGH, CRITICAL)
- Identifies User Intent (TECHNICAL, BILLING, etc.)
- Auto-tagging with keywords
- Built on **Spring Boot 4.1.0** and **Spring AI**
- Active AI provider: **Google GenAI (Gemini/Vertex AI)**
- Optional provider support: **OpenAI**

## Configuration

| Property | Value | Description |
| ---------- | ------- | ------------- |
| Server Port | 8083 | Port where service runs |
| AI Providers | Google GenAI (active), OpenAI (optional) | Supported provider options via Spring AI |
| Database | PostgreSQL | `analysis_db` |
| Service Discovery | Enabled | Registers with Eureka |

> [!IMPORTANT]
> Set Google GenAI credentials (API Key or GCP Auth) for standard runs. OpenAI credentials are only needed if you switch provider.

### Environment Variables

#### Google GenAI (Gemini / Vertex AI)

- `GCP_PROJECT_ID`: Your Google Cloud Project ID.
- `GCP_LOCATION`: Your Google Cloud Project location (e.g., `us-central1`).
- `GOOGLE_APPLICATION_CREDENTIALS`: Path to your GCP service account JSON key file.

#### OpenAI (Optional)

- `SPRING_AI_OPENAI_API_KEY`: Your OpenAI API key.

## Interfaces & Endpoints

### Interactions (REST & Kafka)

- **Synchronous**: Provides analysis capabilities to the `ai-orchestration-service` via REST endpoints (often consumed as Tools).
- **Asynchronous**: Can optionally consume/produce Kafka events for fallback or specialized asynchronous workflows. Employs a **Resilient Outbox Pattern** with timeout/retry mechanisms to guarantee delivery of any generated events. Extracts `X-Correlation-Id` for distributed tracing.

### Observability

- All REST and Kafka interactions are traced via a `CorrelationIdFilter` binding to Logback's `MDC`.

### REST API

- `POST /api/v1/analysis/analyze`: Analyze a ticket manually
- `GET /api/v1/analysis/{ticketId}`: Get existing analysis

## Running Locally

1. Configure Google GenAI credentials (OpenAI is optional).
2. Run the application:

```bash
mvn spring-boot:run
```
