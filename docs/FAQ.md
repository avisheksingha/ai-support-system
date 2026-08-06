# AI Support System - FAQ

## Table of Contents

- **General**
  - [What is the AI Support System?](#what-is-the-ai-support-system)
  - [How does the system process incoming tickets?](#how-does-the-system-process-incoming-tickets)
- **AI and RAG**
  - [What exactly does the AI pipeline do?](#what-exactly-does-the-ai-pipeline-do)
  - [How does Retrieval-Augmented Generation (RAG) work in this platform?](#how-does-retrieval-augmented-generation-rag-work-in-this-platform)
  - [How do you prevent the AI from hallucinating a response to the customer?](#how-do-you-prevent-the-ai-from-hallucinating-a-response-to-the-customer)
  - [Is ticket routing controlled directly by the AI?](#is-ticket-routing-controlled-directly-by-the-ai)
  - [How are LLM prompts managed?](#how-are-llm-prompts-managed)
- **Architecture and Design Decisions**
  - [Why use Apache Kafka instead of synchronous REST APIs for ticket ingestion?](#why-use-apache-kafka-instead-of-synchronous-rest-apis-for-ticket-ingestion)
  - [Why use a central Orchestrator instead of event choreography?](#why-use-a-central-orchestrator-instead-of-event-choreography)
  - [Why choose Spring AI?](#why-choose-spring-ai)
  - [Why use PGVector instead of a dedicated vector database?](#why-use-pgvector-instead-of-a-dedicated-vector-database)
  - [How do you monitor AI performance and costs?](#how-do-you-monitor-ai-performance-and-costs)
  - [What happens if the LLM API goes down completely?](#what-happens-if-the-llm-api-goes-down-completely)
- **Security & Data Integrity**
  - [How is authentication and authorization handled?](#how-is-authentication-and-authorization-handled)
  - [How do you guarantee reliable messaging with Kafka?](#how-do-you-guarantee-reliable-messaging-with-kafka)

---

## General

### What is the AI Support System?

The AI Support System is an enterprise-grade, event-driven microservices platform that automates customer support triage. It uses Apache Kafka to ingest tickets asynchronously, and a central Orchestrator that coordinates Google GenAI for sentiment analysis, intelligent routing, and pgvector-backed Retrieval-Augmented Generation (RAG) to draft highly contextual agent responses.

### How does the system process incoming tickets?

The system uses a **hybrid communication model**:

1. **Asynchronous Ingestion:** Tickets are submitted via an API Gateway to the `ticket-service`. The service immediately persists the data and fires a `TicketCreatedEvent` to Kafka. This guarantees low latency for the customer.
2. **Synchronous Orchestration:** The `ai-orchestration-service` consumes the Kafka event. It acts as a central workflow runtime, making synchronous REST calls to distinct domain services (`ai-analysis-service`, `routing-service`, `rag-service`).

## AI and RAG

### What exactly does the AI pipeline do?

The AI pipeline is strictly coordinated by the Orchestrator in four steps:

1. **Analysis:** The LLM evaluates the ticket text, extracting sentiment (e.g., ANGRY) and an urgency score (0-100).
2. **RAG Context:** The orchestrator retrieves relevant knowledge base articles.
3. **Decision Draft:** The LLM generates a draft response for the agent, explicitly citing the retrieved knowledge.
4. **Routing:** A deterministic rules engine assigns the ticket based on the AI's urgency and intent classification.

### How does Retrieval-Augmented Generation (RAG) work in this platform?

RAG is implemented using PostgreSQL with the `pgvector` extension. Before performing the expensive vector similarity search (cosine distance), the system filters the relational data (e.g., tags, categories) using standard SQL. This "Metadata-aware RAG" drastically improves the accuracy of the vector search by narrowing the search space.

### How do you prevent the AI from hallucinating a response to the customer?

We use a "Human-in-the-Loop" (HITL) design. The AI only generates a *draft* response and saves it as an internal note. A human agent reviews, edits, and approves the response before it is ever sent to the customer.

### Is ticket routing controlled directly by the AI?

No. Routing is **deterministic**. While AI is used to *extract* insights (sentiment, urgency), the actual assignment (e.g., routing to "Tier 2 Billing") is handled by a traditional rules engine within the `routing-service`. This ensures that routing logic remains explainable, auditable, and unaffected by LLM hallucinations.

### How are LLM prompts managed?

Prompts are treated as code. They are externalized and versioned, strictly instructing the LLM to output structured JSON. We utilize few-shot prompting techniques to improve the consistency of intent classification.

## Architecture and Design Decisions

### Why use Apache Kafka instead of synchronous REST APIs for ticket ingestion?

We chose Kafka to decouple the high-throughput ticket ingestion from the high-latency AI processing. It prevents the customer from waiting 3-5 seconds for an LLM to reply just to submit a ticket. If the AI provider (Google GenAI) goes down, the `ai-orchestration-service` will fail to process the event, but the event remains in Kafka to be retried later. Ticket creation is entirely unaffected.

### Why use a central Orchestrator instead of event choreography?

In a distributed saga (choreography), tracking the state of an AI workflow is notoriously difficult. A central Orchestrator (`ai-orchestration-service`) makes the AI pipeline explicit, easier to observe, and easier to debug, preventing the system from becoming a tangled web of events.

### Why choose Spring AI?

Spring AI provides a unified, vendor-agnostic abstraction over LLMs. This allows us to swap between Google Vertex AI, Gemini, or OpenAI by simply changing configuration properties, avoiding vendor lock-in.

### Why use PGVector instead of a dedicated vector database?

While dedicated vector databases (like Pinecone or Milvus) offer advanced indexing, `pgvector` allows us to keep our transactional relational data (ticket histories, article metadata) and our vector embeddings in the exact same database. This avoids the massive operational headache of keeping a relational database and a separate vector database in sync.

### How do you monitor AI performance and costs?

The Admin Dashboard provides real-time observability into the AI's performance. The dedicated `AiAuditService` tracks:

- **Latency:** Average time taken for LLM inference.
- **Token Usage:** Monitoring cost metrics per model to estimate spending.
- **Guardrails:** Tracking how often the AI produces blocked or inappropriate content.

### What happens if the LLM API goes down completely?

The `ai-orchestration-service` will fail to process the event. Because it's consuming from Kafka, the offset won't be committed, and the event remains safely in the queue. Meanwhile, customers can still create tickets because the `ticket-service` only depends on the local database and Kafka. Once the LLM API recovers, the Orchestrator resumes processing the backlog.

## Security & Data Integrity

### How is authentication and authorization handled?

We use a **stateless JWT authentication** model centralized at the API Gateway. The frontend (React) receives an Access Token and a Refresh Token upon login. The API Gateway validates every incoming JWT, intentionally strips any client-supplied identity headers (to prevent spoofing), and forwards the cryptographically trusted user context to downstream backend services. This ensures backend services remain completely stateless and never receive unverified external requests.

### How do you guarantee reliable messaging with Kafka?

To prevent data inconsistency where a database transaction commits but the Kafka message fails to send (or vice-versa), we utilize the **Transactional Outbox Pattern**. The `ticket-service` writes both the new ticket record and the pending `TicketCreatedEvent` to the PostgreSQL database within a single local transaction. A separate background process then safely reads the outbox table and publishes the events to Kafka, guaranteeing *at-least-once* delivery without distributed transaction locks.
