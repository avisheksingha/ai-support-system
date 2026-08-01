import { useQuery } from "@tanstack/react-query";
import { workspaceApi } from "@/features/workspace/api/workspaceApi";

export interface WorkflowStep {
  id: string;
  type: "BUSINESS_EVENT" | "GUARDRAIL" | "AI_PLANNING" | "TOOL_INVOCATION" | "KNOWLEDGE_RETRIEVAL" | "WORKFLOW_DECISION" | "EVENT_PUBLICATION";
  name: string;
  stageName: string;
  category: "BUSINESS" | "TECHNICAL";
  status: "SUCCESS" | "FAILED" | "SKIPPED" | "RETRYING" | "RUNNING" | "WAITING";
  durationMs: number;
  percentage: number;
  payload: any;
  summary?: string;
  error?: string;
  timestamp: Date;
}

function mapEventType(type: string, subType: string): WorkflowStep["type"] {
  const upperType = type?.toUpperCase() || "";
  const upperSub = subType?.toUpperCase() || "";

  if (upperType === "WORKFLOW") {
    return "WORKFLOW_DECISION";
  }
  if (upperType === "AI" || upperSub.includes("AI") || upperSub.includes("ANALYSIS")) {
    return "AI_PLANNING";
  }
  if (upperType === "TOOL" || upperSub.includes("TOOL")) {
    return "TOOL_INVOCATION";
  }
  if (upperType === "GOVERNANCE" || upperSub.includes("GUARDRAIL")) {
    return "GUARDRAIL";
  }
  if (upperSub.includes("KNOWLEDGE") || upperSub.includes("RAG")) {
    return "KNOWLEDGE_RETRIEVAL";
  }
  if (upperSub.includes("ROUTING") || upperSub.includes("DECISION")) {
    return "WORKFLOW_DECISION";
  }
  if (upperSub.includes("PUBLISH") || upperSub.includes("KAFKA") || upperSub.includes("EVENT_PUBLISHED")) {
    return "EVENT_PUBLICATION";
  }

  return "BUSINESS_EVENT";
}

function mapOutcomeToStatus(outcome: string, severity: string): WorkflowStep["status"] {
  if (severity?.toUpperCase() === "ERROR") return "FAILED";
  if (severity?.toUpperCase() === "WARNING") return "RETRYING";

  const upper = outcome?.toUpperCase();
  if (upper === "COMPLETED" || upper === "SUCCESS") return "SUCCESS";
  if (upper === "FAILED") return "FAILED";
  if (upper === "PARTIAL_SUCCESS") return "RETRYING";
  if (upper === "SKIPPED") return "SKIPPED";
  return "SUCCESS";
}

function deriveStageName(title: string, subType: string, stepName?: string): { stageName: string; category: WorkflowStep["category"] } {
  const combined = (title + " " + subType + " " + (stepName || "")).toUpperCase();

  if (combined.includes("ANALYZE") || combined.includes("ANALYSIS")) {
    return { stageName: "AI Analysis", category: "BUSINESS" };
  }
  if (combined.includes("KNOWLEDGE") || combined.includes("RAG") || combined.includes("SEARCH")) {
    return { stageName: "Knowledge Retrieval", category: "BUSINESS" };
  }
  if (combined.includes("DECISION") || combined.includes("FINAL_AI")) {
    return { stageName: "AI Decision", category: "BUSINESS" };
  }
  if (combined.includes("ROUTE") || combined.includes("ROUTING")) {
    return { stageName: "Routing", category: "BUSINESS" };
  }
  if (combined.includes("ASSIGN") || combined.includes("ASSIGNMENT")) {
    return { stageName: "Assignment", category: "BUSINESS" };
  }
  if (combined.includes("CONTEXT") || combined.includes("ASSEMBLE")) {
    return { stageName: "Context Assembly", category: "TECHNICAL" };
  }
  if (combined.includes("PROMPT")) {
    return { stageName: "Prompt Assembly", category: "TECHNICAL" };
  }
  if (combined.includes("CHECKPOINT")) {
    return { stageName: "Checkpoint Save", category: "TECHNICAL" };
  }
  if (combined.includes("PUBLISH") || combined.includes("KAFKA")) {
    return { stageName: "Kafka Event Publication", category: "TECHNICAL" };
  }

  return { stageName: title || subType || "Workflow Step", category: "TECHNICAL" };
}

export function useWorkflowDetail(executionId?: string) {
  return useQuery({
    queryKey: ["workflowDetail", executionId],
    queryFn: async () => {
      if (!executionId) return [];

      const response = await workspaceApi.getWorkflowTimeline(executionId);
      const events = response.content || [];

      // Calculate total duration for percentage computation
      const totalMs = events.reduce((acc: number, ev: any) => acc + (ev.latencyMs || 0), 0);

      let hasFailed = false;

      return events.map((event: any) => {
        const rawStatus = mapOutcomeToStatus(event.outcome, event.severity);
        let status = rawStatus;

        if (hasFailed && status !== "FAILED") {
          status = "SKIPPED";
        }
        if (rawStatus === "FAILED") {
          hasFailed = true;
        }

        const durationMs = event.latencyMs || 0;
        const percentage = totalMs > 0 ? Math.round((durationMs / totalMs) * 100) : 0;
        const { stageName, category } = deriveStageName(event.title, event.subType, event.stepName);

        return {
          id: event.eventId || event.id,
          type: mapEventType(event.type, event.subType),
          name: event.title || event.subType || "Workflow Event",
          stageName,
          category,
          status,
          durationMs,
          percentage,
          payload: event,
          summary: event.description,
          error: event.severity === "ERROR" ? event.description || "Stage execution failed" : undefined,
          timestamp: new Date(event.timestamp),
        };
      });
    },
    enabled: !!executionId,
    staleTime: 30000,
  });
}
