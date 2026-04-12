# RAG Service Agent

**Role:** Retrieval-Augmented Response Generator

**Port:** 8085

**Responsibility:** Consumes analyzed ticket events, generates grounded responses using Spring AI (`ChatClient` + `QuestionAnswerAdvisor` + PGVector), persists responses, and publishes `TicketRagResponseEvent` through outbox.

## Quick Commands

### Build
```bash
mvn -pl rag-service clean install
```

### Run Service
```bash
cd rag-service && mvn spring-boot:run
```

### Run Tests
```bash
mvn -pl rag-service test
mvn -pl rag-service -Dtest=RagServiceTest test
```

## Key Files

- **Consumer:** `src/main/java/com/aisupport/rag/consumer/TicketAnalyzedConsumer.java`
- **Core Service:** `src/main/java/com/aisupport/rag/service/RagService.java`
- **Startup Loader:** `src/main/java/com/aisupport/rag/runner/DataLoaderRunner.java`
- **Entities:** `src/main/java/com/aisupport/rag/entity/KnowledgeArticle.java`, `RagResponse.java`
- **Repositories:** `src/main/java/com/aisupport/rag/repository/KnowledgeArticleRepository.java`, `RagResponseRepository.java`
- **Outbox:** `src/main/java/com/aisupport/rag/outbox/OutboxEventService.java`, `OutboxEventPublisher.java`
- **RAG Config:** `src/main/java/com/aisupport/rag/config/RagConfig.java`, `ChatConfig.java`

## Runtime Flow

1. Consume `ticket-analyzed` event.
2. Build query text from intent/sentiment/urgency/keywords.
3. Call `RagService.generateResponse(...)`.
4. `ChatClient` with `QuestionAnswerAdvisor` retrieves context from vector store.
5. Persist generated response in `rag_responses`.
6. Publish `TicketRagResponseEvent` via outbox.

## Data Loading Notes

`DataLoaderRunner` behavior on startup:
- Checks `KnowledgeArticleRepository.countEmbeddedArticles()`.
- Skips load when already embedded.
- Converts DB rows to `Document` and writes to vector store.
- Marks `embedded=true` and persists.

## Common Tasks

### Verify Knowledge Articles
```sql
SELECT id, title, embedded
FROM knowledge_articles
ORDER BY id;
```

### Verify Generated RAG Responses
```sql
SELECT ticket_id, model, created_at
FROM rag_responses
ORDER BY created_at DESC
LIMIT 50;
```

### Verify Outbox Events
```sql
SELECT aggregate_id, event_type, status, retry_count, processed_at
FROM outbox_events
WHERE event_type = 'TicketRagResponseEvent'
ORDER BY created_at DESC;
```

## Schema Snapshot (from entities)

### knowledge_articles
- `id` (Long, PK)
- `title`
- `content` (TEXT)
- `embedded` (boolean)

### rag_responses
- `id` (Long, PK)
- `ticket_id`
- `query` (TEXT)
- `response` (TEXT)
- `model`
- `created_at`

## Important Rules

- Keep response generation RAG-grounded using advisor context.
- Keep cross-service publication via outbox.
- Preserve correlation-id from consumer through logs/outbox.
- Keep startup loader idempotent (skip if already embedded).

## Environment Variables / Config Inputs

- `SPRING_PROFILES_ACTIVE`
- `GCP_PROJECT_ID`, `GCP_LOCATION`, `GOOGLE_APPLICATION_CREDENTIALS`
- `spring.ai.vertex.ai.gemini.chat.options.model`
- `spring.ai.vertex.ai.embedding.options.model`
- `spring.ai.vectorstore.pgvector.dimensions`

## Related Services

- Consumes `ticket-analyzed` from `ai-analysis-service`
- Produces `ticket-rag-response` for `ticket-service`

## Debugging Tips

1. No useful response: confirm vector store has embedded articles.
2. Startup reload loops: verify `embedded` flag and count query behavior.
3. Event not delivered: inspect outbox status/retries and Kafka topic logs.
