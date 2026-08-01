# Runtime Architecture

Version: 1.0
Status: Current
Last Updated: 2026-07-11
Related Documents:

- [03-workflow-runtime.md](03-workflow-runtime.md)

The **AI Orchestration Service** is designed as a reusable platform runtime rather than a single monolithic application.

It is constructed through highly decoupled layers, ensuring that no single component understands the entire process.

## Architectural Separation

The most significant achievement of this architecture is the boundary of responsibilities:

```text
Workflow Runtime
↓
Policy Engine
↓
Agent
↓
Guardrail Pipeline
↓
Model
↓
Audit
```

- **Workflow Runtime**: Knows nothing about LLMs or prompts. It just runs steps and manages state.
- **Policy Engine**: Determines if a workflow is allowed to run.
- **Context Providers**: Fetch data without knowing how it will be formatted.
- **Prompt Renderer**: Formats data without knowing what model will execute it.
- **Agent**: Executes the inference and tool loops, knowing nothing about the business domain.
- **Guardrails**: Intercept payloads to modify or block unsafe content.
- **Audit**: Persists execution history.
