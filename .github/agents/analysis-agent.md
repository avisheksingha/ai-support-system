# AI Analysis Service Agent

**Role:** AI Ticket Analysis Domain Capability

**Port:** 8083

**Responsibility:** Provides AI-powered sentiment, urgency, and intent analysis as a domain capability. Exposes internal REST endpoints consumed by the `ai-orchestration-service` via Tool Calling. Also retains a Kafka consumer for direct `ticket-created` events as a legacy/fallback path.

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

- **Internal Controller:** `src/main/java/com/aisupport/analysis/controller/InternalAnalysisController.java`
- **Admin Stats Controller:** `src/main/java/com/aisupport/analysis/controller/AdminAnalysisStatsController.java`
- **Writing Assistant Controller:** `src/main/java/com/aisupport/analysis/controller/WritingAssistantController.java`
- **Consumer (legacy):** `src/main/java/com/aisupport/analysis/consumer/TicketCreatedConsumer.java`
- **Processing Service:** `src/main/java/com/aisupport/analysis/service/AnalysisProcessingService.java`
- **Query Service:** `src/main/java/com/aisupport/analysis/service/AnalysisQueryService.java`
- **Controller (query APIs):** `src/main/java/com/aisupport/analysis/controller/AnalysisController.java`
- **Provider Config:** `src/main/java/com/aisupport/analysis/config/ChatConfig.java`
- **LLMs:** `src/main/java/com/aisupport/analysis/chat/GeminiChatProvider.java`, `OpenAiChatProvider.java`
- **Entity:** `src/main/java/com/aisupport/analysis/entity/AnalysisResult.java`

## Key Responsibilities & Flow

- Primary invocation path: **Orchestrator calls `POST /api/internal/analysis/analyze`** via Tool Calling.
- Active provider selected by `chat.provider` (`gemini` default).
- Gemini calls are guarded by `geminiRateLimiter` + `geminiCircuitBreaker`.
- OpenAI provider is available through `OpenAiChatProvider` when enabled.

## Current API Endpoints

### Public (Query) Endpoints

- `GET /api/v1/analysis/ticket/{ticketId}`
- `GET /api/v1/analysis?page=<int>&size=<int>`
- `GET /api/v1/analysis/intent/{intent}`
- `GET /api/v1/analysis/urgency/{urgency}`

### Internal Endpoints (Orchestrator Only)

- `POST /api/internal/analysis/analyze` — Invoked by `ai-orchestration-service` as an AI Tool.
- `GET /api/internal/analysis/stats` — Admin dashboard statistics.

## Database Snapshot

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

## Important Rules

- Primary invocation is via orchestrator REST Tool Calling, not direct Kafka consumption.
- Keep provider/model selection configurable through properties.
- Preserve correlation-id headers into MDC in all flows.

## Environment Variables

- `SPRING_PROFILES_ACTIVE`
- `GOOGLE_CLOUD_PROJECT`, `GOOGLE_CLOUD_LOCATION`, `GOOGLE_APPLICATION_CREDENTIALS`
- `OPENAI_API_KEY` (if using OpenAI)
- `chat.provider`
- `spring.ai.google.genai.chat.model`
- `spring.ai.openai.chat.model`

## Related Services

- Invoked by `ai-orchestration-service` via internal REST API (Tool Calling).
- Query APIs consumed by `ticket-service` and the dashboard frontend.

## Debugging Tips

1. Orchestrator not receiving analysis: verify internal endpoint availability and service discovery.
2. Repeated analyses for same ticket: check idempotency guard (`existsByTicketId`).
3. Low-quality output: inspect provider prompt and parsed output converter behavior.
4. Consumer not firing (legacy path): verify topic/group config and listener startup logs.
