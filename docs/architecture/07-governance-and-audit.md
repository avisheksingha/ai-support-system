# Governance and Audit

Version: 1.0
Status: Current
Last Updated: 2026-07-11
Related ADRs:
- [ADR-005 Policy vs Guardrails](../adr/ADR-005-policy-vs-guardrails.md)

AI Governance ensures that executions are safe, authorized, and fully audited.

## 1. Policies (Is this allowed?)
The `PolicyEngine` executes *before* any AI inference begins. It evaluates standard rules (`AiPolicy`) such as:
- *VIP tickets require human approval.*
- *Finance tickets cannot use external models.*

Policies output a `PolicyDecision` (`ALLOW`, `DENY`, `REQUIRE_APPROVAL`, `REROUTE`).

## 2. Guardrails (Is this safe?)
The `GuardrailPipeline` executes around the Agent loop.
- **Input Guardrails**: Evaluates the `AgentRequest` before it hits the LLM.
- **Output Guardrails**: Evaluates the `AgentResponse` before it's returned to the Workflow.

Guardrails output a `GuardrailResult` (`ALLOW`, `MODIFY`, `BLOCK`, `WARN`). For instance, a PII Redaction guardrail might `MODIFY` the prompt to mask credit card numbers.

## 3. Execution Provenance (Audit)
When an `AgentSession` completes, the `AiAuditService` persists an `AiExecutionRecordEntity`. This is NOT a conversation log, but an execution trace.
It guarantees provenance by storing:
- `workflowVersion`
- `agentVersion`
- `promptHash`
- `latencyMs`
- `outcome`
- `toolsInvoked`
