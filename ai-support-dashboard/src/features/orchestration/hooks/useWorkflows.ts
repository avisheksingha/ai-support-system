import { useQuery } from "@tanstack/react-query";
import { workspaceApi } from "@/features/workspace/api/workspaceApi";

export interface WorkflowExecution {
  id: string;
  triggerEvent: string;
  entityId: string;
  status: "COMPLETED" | "FAILED" | "IN_PROGRESS" | "RECOVERED";
  durationMs: number;
  startedAt: Date;
}

export function useWorkflows(params?: Record<string, any>) {
  return useQuery({
    queryKey: ["workflows", params],
    queryFn: async () => {
      const response = await workspaceApi.getOperationsOverview(params);
      // Transform backend recentExecutions to match our interface
      return response.recentExecutions?.map((exec: any) => ({
        id: exec.workflowId || exec.id,
        triggerEvent: exec.definitionId || "workflow-execution",
        entityId: exec.ticketNumber || `TKT-${exec.ticketId}`,
        status: exec.state === "COMPLETED" ? "COMPLETED" : 
               exec.state === "FAILED" ? "FAILED" : 
               exec.state === "RECOVERED" ? "RECOVERED" : "IN_PROGRESS",
        durationMs: exec.durationMs || 0,
        startedAt: new Date(exec.startedAt),
      })) || [];
    },
    staleTime: 30000, // 30 seconds
  });
}
