# AI Analysis Service Agent

**Role:** AI Ticket Analysis Processor

**Port:** 8083

**Responsibility:** Consumes `ticket-created` events, runs LLM-based analysis through configured provider (`gemini` or `openai`), stores analysis result, and publishes `TicketAnalyzedEvent` through outbox.

## Quick Commands

### Build
```bash
mvn -pl ai-analysis-service clean install
```

### Run Service
```bash
cd ai-analysis-service && mvn spring-boot:run
```

### Run Tests
```bash
mvn -pl ai-analysis-service test
mvn -pl ai-analysis-service -Dtest=AnalysisControllerTest,AnalysisProcessingServiceTest,AnalysisQueryServiceTest test
```

### Swagger
```text
http://localhost:8083/swagger-ui.html
```

## Key Files

- **Consumer:** `src/main/java/com/aisupport/analysis/consumer/TicketCreatedConsumer.java`
- **Processing Service:** `src/main/java/com/aisupport/analysis/service/AnalysisProcessingService.java`
- **Query Service:** `src/main/java/com/aisupport/analysis/service/AnalysisQueryService.java`
- **Controller (query APIs):** `src/main/java/com/aisupport/analysis/controller/AnalysisController.java`
- **Provider Config:** `src/main/java/com/aisupport/analysis/config/ChatConfig.java`
- **Providers:** `src/main/java/com/aisupport/analysis/chat/GeminiChatProvider.java`, `OpenAiChatProvider.java`
- **Entity:** `src/main/java/com/aisupport/analysis/entity/AnalysisResult.java`
- **Outbox:** `src/main/java/com/aisupport/analysis/outbox/OutboxEventService.java`, `OutboxEventPublisher.java`

## Current API Endpoints

- `GET /api/v1/analysis/ticket/{ticketId}`
- `GET /api/v1/analysis?page=<int>&size=<int>`
- `GET /api/v1/analysis/intent/{intent}`
- `GET /api/v1/analysis/urgency/{urgency}`

Note: analysis creation is event-driven via Kafka consumer, not a public `POST /api/v1/analysis` API.

## AI Provider Notes

- Active provider selected by `chat.provider` (`gemini` default).
- Gemini calls are guarded by `geminiRateLimiter` + `geminiCircuitBreaker`.
- OpenAI provider is available through `OpenAiChatProvider` when enabled.

## Common Tasks

### Fetch Analysis by Ticket ID
```bash
curl "http://localhost:8083/api/v1/analysis/ticket/1"
```

### List Analyses (Paginated)
```bash
curl "http://localhost:8083/api/v1/analysis?page=0&size=20"
```

### Filter by Intent
```bash
curl "http://localhost:8083/api/v1/analysis/intent/GENERAL"
```

## Database Snapshot (from entity)

### analysis_results
- `id` (Long, PK)
- `version` (optimistic lock)
- `ticket_id` (Long, unique)
- `intent`, `sentiment`, `urgency`
- `confidence_score` (BigDecimal)
- `keywords` (TEXT[])
- `suggested_category`
- `raw_response`
- `created_at`

## Important Rules

- Keep ingestion event-driven from `ticket-created`.
- Publish downstream analysis through outbox, not direct cross-service calls.
- Keep provider/model selection configurable through properties.
- Preserve correlation-id headers into MDC in consumer flow.

## Environment Variables / Config Inputs

- `SPRING_PROFILES_ACTIVE`
- `GCP_PROJECT_ID`, `GCP_LOCATION`, `GOOGLE_APPLICATION_CREDENTIALS`
- `OPENAI_API_KEY` (if using OpenAI)
- `chat.provider`
- `spring.ai.vertex.ai.gemini.chat.options.model`
- `spring.ai.openai.chat.options.model`

## Related Services

- Consumes from `ticket-service` (`ticket-created`)
- Produces for `routing-service` and `rag-service` (`ticket-analyzed`)

## Debugging Tips

1. Consumer not firing: verify topic/group config and listener startup logs.
2. Repeated analyses for same ticket: check idempotency guard (`existsByTicketId`).
3. Low-quality output: inspect provider prompt and parsed output converter behavior.
4. Outbox not publishing: inspect outbox table status/retry values and publisher logs.
