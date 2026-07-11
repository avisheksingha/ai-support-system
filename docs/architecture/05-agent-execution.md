# Agent Execution

Version: 1.0
Status: Current
Last Updated: 2026-07-11
Related Documents:
- [06-tool-execution.md](06-tool-execution.md)

The **Agent Execution Layer** handles the literal interaction with AI models. It completely abstracts away specific providers (e.g., OpenAI, Gemini) from the workflow layer.

## Agent Session
Instead of treating LLM calls as single atomic requests, the runtime manages an `AgentSession`. 
The session captures the entire operational multi-turn trace:
- Initial Request
- Intermediate Tool Invocations
- Intermediate Tool Responses
- Final Response
- Total Token Usage

## Token Budgeting
The `TokenBudgetManager` sits inside the Agent layer. It inspects the `ModelProfile` (which contains max context tokens) and intelligently truncates the context (e.g., conversation history) *before* sending the payload to the LLM. 

## Model Profiles
Models are not defined by hardcoded strings. Instead, the runtime uses immutable `ModelProfile` objects that specify capabilities:
- `supportsToolCalling`
- `supportsStreaming`
- `supportsVision`
- `supportsStructuredOutput`
