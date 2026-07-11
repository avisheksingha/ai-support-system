# AI Orchestration Service V1 Release

**Release Name:** AI Orchestration Service
**Version:** 1.0.0
**Architecture:** Frozen
**Compatibility:** Spring Boot 3.x, Spring Cloud 2023.x
**Status:** Backend Feature Complete
**Next Milestone:** Frontend Integration

---

## 1. Overview
The AI Orchestration Service V1 represents a significant evolution from a basic GenAI conceptual architecture into a robust, event-driven orchestration platform. The backend acts as a central control plane for intelligent customer support by composing domain capabilities (Ticketing, Analysis, Retrieval, Routing), executing AI reasoning via agents, providing scalable tool access through MCP, and governing execution through rigid, configuration-driven policies.

## 2. Features Included
- **Modular Workflow Engine:** State-machine based execution (Init → Run → Checkpoint → Resume) enforcing idempotency and preventing duplicate processing.
- **Event-Driven Foundation:** Integrated with Kafka for scalable event processing (`ticket-created`, `ticket-analyzed`, etc.).
- **Agent Layer:** Spring AI integration completely decoupled from the workflow state, acting purely as a reasoning engine.
- **Tool Registry:** Dynamic resolution and invocation of tools, separating agent orchestration from external capability providers.
- **MCP Foundation:** Fully modular integration with the Model Context Protocol, enabling extensible remote tool usage behind standardized interfaces.
- **Context Assembly:** Aggregates metadata from various domain services into a cohesive prompt context.
- **Governance:** Rigid policy enforcement and input/output guardrails governing the AI.
- **Reliability:** Built-in workflow checkpointing, crash resume logic, and exactly-once outbox publishing.
- **Observability:** Comprehensive Runtime Coverage Reporter generating execution metadata.

## 3. Architecture Version
**Version:** 1.0
**Status:** Frozen
**Breaking Changes:** None planned before V2

The following abstractions are officially frozen and entering maintenance mode:
`WorkflowEngine`, `WorkflowDefinition`, `WorkflowStep`, `WorkflowContext`, `ContextProvider`, `Agent`, `AgentSession`, `AgentRequest`, `AgentResponse`, `ToolRegistry`, `ToolExecutor`, `PolicyEngine`, `AiPolicy`, `GuardrailPipeline`, `InputGuardrail`, `OutputGuardrail`.

## 4. Supported Workflows
- **Ticket Orchestration Workflow** 
  - `AssembleContextStep`: Aggregates historical ticket context, knowledge base articles, and user data.
  - `AnalyzeTicketStep`: Evaluates sentiment, determines urgency, and drafts responses via AI reasoning.
  - `RoutingStep`: Assigns the ticket to the appropriate team or autonomous resolution queue.

## 5. MCP Providers
Extensibility is achieved via the `ToolProvider` abstraction backing the `ToolRegistry`. V1 includes the following MCP integrations:
- **Local Provider:** Standard Spring Beans and inline tool definitions.
- **GitHub MCP:** Source code interaction, PR creation, and issue analysis.
- **Filesystem MCP:** Local file system operations for data aggregation.
- **PostgreSQL MCP:** Direct, read-only SQL analysis capabilities for database troubleshooting.

## 6. Governance
Governance is strictly decoupled from runtime business rules to ensure safe execution:
- **Configuration-Driven Rules:** Thresholds and triggers are driven by `@Value` properties.
- **Policies (`AiPolicy`):** Pre-execution rules validating compliance (e.g., `ToolUsagePolicy`, `SensitiveWorkflowPolicy`).
- **Guardrails:** 
  - `PiiRedactionGuardrail` (Input redaction via regex).
  - `PromptSizeValidationGuardrail` (Input size bounding).
  - `JsonSchemaValidationGuardrail` (Output structural type-safety enforcing `AgentResponse.ResponseType`).
- **Audit Trails:** Immutable logging of policy versions, guardrail versions, outcomes, and invoked tools into `AiExecutionRecordEntity`.

## 7. Testing Strategy
V1 emphasizes end-to-end integration and resilience:
- **Testcontainers Integration:** Kafka and PostgreSQL are fully dockerized in integration tests for realistic environment parity.
- **Boundary Mocking:** Only external bounds (LLM API calls, remote MCP servers) are mocked (`MockMcpClient`, `MockChatClient`), preserving full internal runtime execution.
- **E2E Scenarios:**
  - `WorkflowEndToEndIT`: Validates complete happy path orchestration.
  - `WorkflowGovernanceIT`: Asserts that policies properly block and audit non-compliant flows (e.g., malformed JSON).
  - `WorkflowRecoveryIT`: Validates checkpoint resilience, exactly-once Outbox publishing, and duplicate tool execution prevention.

## 8. Known Limitations
The following constraints are intentional and reflect professional engineering triage for the V1 milestone:
- **Mocked Externalities:** `ChatClient` and MCP Providers (GitHub, Postgres, FileSystem) currently rely on mock implementations for local workflow and integration testing.
- **No Human-in-the-loop (HITL):** Asynchronous human approval workflows are unsupported in V1.
- **Basic Load Balancing:** Real-world traffic load balancing and rate limiting across LLMs are missing.

## 9. Deferred V2 Features
To ensure platform stability, the following features have been explicitly deferred to V2:
- Browser MCP, Docker MCP, Kubernetes MCP, Redis MCP
- Semantic Caching layer
- Multi-Agent Orchestration / Swarm capabilities
- Intelligent Model Routing

## 10. Demo Scenarios
V1 architecture allows reviewers to successfully trace the following scenarios from end-to-end:

**Scenario A: Autonomous Resolution Flow**
`Customer Creates Ticket → Ticket Service → Kafka → AI Orchestration → Context Assembly → Prompt Rendering → Spring AI → GitHub MCP → Postgres MCP → Policy Engine → Guardrails → Final Recommendation → Dashboard`

**Scenario B: Crash and Recovery Flow**
`Workflow Starts → Checkpoint Saved → System Crash Simulated → System Restarts → Engine Resumes → Skips Completed Steps → Completes AI Analysis → Exactly-Once Outbox Publish`
