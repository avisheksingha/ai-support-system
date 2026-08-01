# Runtime Verification

This document outlines the testing strategy for the AI Orchestration Service and clarifies the boundaries between genuine execution and mocked infrastructure during our integration test suite.

## The Strategy

The AI Orchestration Service is validated using a **Layered Integration Suite** backed by **Testcontainers**. We prioritize testing the real Spring application context without isolating individual domain classes via `@MockBean`.

Instead, we mock only at the absolute perimeter: the network calls to external LLM providers and 3rd party APIs.

### Real Components Exercised

The following components are fully instantiated and execute their genuine production logic during testing:

* ✓ **Kafka**: Embedded broker via Testcontainers.
* ✓ **PostgreSQL**: Real database instance via Testcontainers.
* ✓ **Workflow Runtime**: Full state machine and context propagation.
* ✓ **Prompt Rendering**: Genuine prompt template compilation.
* ✓ **Tool Registry**: Real component scanning and parameter injection.
* ✓ **Persistence**: All JPA entities and database transactions.
* ✓ **Audit**: Complete AI execution audit trail generation.
* ✓ **Outbox**: Transactional outbox event creation.

### Mocked Boundaries

The following external dependencies are mocked to ensure fast, deterministic, and cost-free CI runs:

* ✓ **Spring AI `ChatClient`**: Mocked via `TestAiConfiguration` to prevent OpenAI/Anthropic API calls.
* ✓ **External MCP APIs**: GitHub, Postgres, and Filesystem providers are forced into `mode=mock` to safely simulate their behavior locally.

### Justification

By maintaining this boundary, we ensure:

1. **High Confidence**: Our integration tests exercise the exact same bean wiring, transaction boundaries, and serialization logic as the production artifact.
2. **Deterministic Execution**: Tests never fail due to intermittent LLM latencies, hallucinated tool choices, or external API rate limits.
3. **CI Friendly**: The entire suite runs successfully offline in standard GitHub Actions runners without requiring API keys.
