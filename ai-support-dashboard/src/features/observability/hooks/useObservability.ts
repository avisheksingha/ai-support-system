import { useQuery } from "@tanstack/react-query";
import { workspaceApi } from "@/features/workspace/api/workspaceApi";

export const observabilityKeys = {
  all: ["observability"] as const,
  overview: (params?: Record<string, any>) => [...observabilityKeys.all, "overview", params] as const,
  workflows: (params?: Record<string, any>) => [...observabilityKeys.all, "workflows", params] as const,
};

export const useOperationsOverview = (params?: Record<string, any>) => {
  return useQuery({
    queryKey: observabilityKeys.overview(params),
    queryFn: () => workspaceApi.getOperationsOverview(params),
    refetchInterval: 10000, // Poll every 10s for dashboard
  });
};

export const useWorkflowSearch = (params?: Record<string, any>) => {
  return useQuery({
    queryKey: observabilityKeys.workflows(params),
    queryFn: () => workspaceApi.searchWorkflows(params),
    enabled: Object.keys(params || {}).length > 0,
  });
};
