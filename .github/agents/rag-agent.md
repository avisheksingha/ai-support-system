# RAG Service Agent

**Role:** Retrieval-Augmented Response Generator (Domain Capability)

**Port:** 8085

**Responsibility:** Provides context-aware knowledge retrieval as a domain capability. Manages the knowledge article lifecycle, vector embeddings, and RAG-grounded response generation. Exposes internal REST endpoints consumed by the `ai-orchestration-service` via Tool Calling.

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
mvn -pl rag-service -Dtest=RagServiceTest,KnowledgeEmbeddingServiceTest,MetadataAwareRetrievalServiceTest test
```

## Key Files

- **Internal Controller:** `src/main/java/com/aisupport/rag/controller/InternalRagController.java`
- **Admin Stats Controller:** `src/main/java/com/aisupport/rag/controller/AdminRagStatsController.java`
- **Knowledge Article Controller:** `src/main/java/com/aisupport/rag/controller/KnowledgeArticleController.java`
- **Consumer (legacy):** `src/main/java/com/aisupport/rag/consumer/TicketAnalyzedConsumer.java`
- **Core Service:** `src/main/java/com/aisupport/rag/service/RagService.java`
- **Embedding Service:** `src/main/java/com/aisupport/rag/service/KnowledgeEmbeddingService.java`
- **Retrieval Service:** `src/main/java/com/aisupport/rag/service/retrieval/MetadataAwareRetrievalService.java`
- **Entities:** `src/main/java/com/aisupport/rag/entity/KnowledgeArticle.java`, `RagResponse.java`
- **Repositories:** `src/main/java/com/aisupport/rag/repository/KnowledgeArticleRepository.java`, `RagResponseRepository.java`
- **RAG Config:** `src/main/java/com/aisupport/rag/config/RagConfig.java`, `ChatConfig.java`

## Key Responsibilities & Flow

1. Orchestrator calls `POST /api/internal/rag/search` with ticket context.
2. Build query text from intent/sentiment/urgency/keywords.
3. Execute metadata-aware hybrid retrieval from pgvector.
4. Generate grounded response via `ChatClient`.
5. Persist generated response in `rag_responses`.
6. Return knowledge context to orchestrator.

## Current API Endpoints

### Internal Endpoints (Orchestrator Only)

- `POST /api/internal/rag/search` — Invoked by `ai-orchestration-service` as an AI Tool.
- `GET /api/internal/rag/stats` — Admin dashboard statistics.

### Internal Endpoints (Knowledge Management)

- `GET /api/internal/rag/articles` — List knowledge articles (paginated).
- `POST /api/internal/rag/articles` — Create a knowledge article.
- `PUT /api/internal/rag/articles/{id}` — Update a knowledge article.
- `DELETE /api/internal/rag/articles/{id}` — Delete a knowledge article.
- `PATCH /api/internal/rag/articles/{id}/status` — Update article status (DRAFT/PUBLISHED/ARCHIVED).

## Database Snapshot

### knowledge_articles
- `id` (Long, PK)
- `title`, `content` (TEXT), `category`, `tags`
- `status` (DRAFT/PUBLISHED/ARCHIVED)
- `embedding_status` (PENDING/PROCESSING/READY/FAILED)
- `version` (optimistic lock)
- `created_at`, `updated_at`

### rag_responses
- `id` (Long, PK)
- `ticket_id`
- `query` (TEXT)
- `response` (TEXT)
- `model`
- `created_at`

## Common Tasks

### Verify Knowledge Articles
```sql
SELECT id, title, status, embedding_status
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

## Important Rules

- Primary invocation is via orchestrator REST Tool Calling, not direct Kafka consumption.
- Keep response generation RAG-grounded using metadata-aware retrieval.
- Keep embedding lifecycle idempotent (skip if already embedded).
- Preserve correlation-id from all flows through logs.

## Environment Variables

- `SPRING_PROFILES_ACTIVE`
- `GCP_PROJECT_ID`, `GCP_LOCATION`, `GOOGLE_APPLICATION_CREDENTIALS`
- `spring.ai.vertex.ai.gemini.chat.options.model`
- `spring.ai.vertex.ai.embedding.options.model`
- `spring.ai.vectorstore.pgvector.dimensions`

## Related Services

- Invoked by `ai-orchestration-service` via internal REST API (Tool Calling).
- Knowledge context is included in the `ticket-orchestrated` event published by the orchestrator.

## Debugging Tips

1. No useful response: confirm vector store has embedded articles with `embedding_status = READY`.
2. Embeddings stuck in PENDING: check `KnowledgeEmbeddingService` scheduled task logs.
3. Orchestrator not receiving RAG results: verify internal endpoint availability and service discovery.
4. Low relevance results: inspect retrieval strategy and hybrid ranking weights in properties.
