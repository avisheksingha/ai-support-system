import { useQuery } from "@tanstack/react-query";
import type { 
  ApprovalRequest, 
  BlockedRequest, 
  AuditLog,
  ActiveGuardrail,
  GovernanceOverview
} from "@/shared/types/workspace";
import { apiClient } from "@/lib/api-client";

export const governanceKeys = {
  all: ["governance"] as const,
  overview: () => [...governanceKeys.all, "overview"] as const,
  approvalQueue: () => [...governanceKeys.all, "approvalQueue"] as const,
  blockedRequests: () => [...governanceKeys.all, "blockedRequests"] as const,
  auditLogs: () => [...governanceKeys.all, "auditLogs"] as const,
  activeGuardrails: () => [...governanceKeys.all, "activeGuardrails"] as const,
};

export const useGovernanceOverview = () => {
  return useQuery({
    queryKey: governanceKeys.overview(),
    queryFn: async () => {
      const response = await apiClient.get<GovernanceOverview>("/orchestration/governance/overview");
      return response.data;
    },
  });
};

export const useApprovalQueue = () => {
  return useQuery({
    queryKey: governanceKeys.approvalQueue(),
    queryFn: async () => {
      const response = await apiClient.get<ApprovalRequest[]>("/orchestration/governance/approval-queue");
      return response.data;
    },
  });
};

export const useBlockedRequests = () => {
  return useQuery({
    queryKey: governanceKeys.blockedRequests(),
    queryFn: async () => {
      const response = await apiClient.get<BlockedRequest[]>("/orchestration/governance/blocked-requests");
      return response.data;
    },
  });
};

export const useAuditLogs = () => {
  return useQuery({
    queryKey: governanceKeys.auditLogs(),
    queryFn: async () => {
      const response = await apiClient.get<AuditLog[]>("/orchestration/governance/audit-logs");
      return response.data;
    },
  });
};

export const useActiveGuardrails = () => {
  return useQuery({
    queryKey: governanceKeys.activeGuardrails(),
    queryFn: async () => {
      const response = await apiClient.get<ActiveGuardrail[]>("/orchestration/governance/active-guardrails");
      return response.data;
    },
  });
};
