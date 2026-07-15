import { useQuery } from "@tanstack/react-query";
import type { 
  ApprovalRequest, 
  BlockedRequest, 
  AuditLog 
} from "@/shared/types/workspace";

// Phase 7 Mock Data for Governance Dashboard
const mockApprovalQueue: ApprovalRequest[] = [
  {
    id: "APR-1002",
    workflowId: "WF-88992",
    correlationId: "CORR-7721",
    ticketId: 43516,
    intent: "Process Refund",
    confidence: 0.88,
    triggeredPolicy: "Manual Approval Required",
    reason: "Refund amount exceeds autonomous threshold ($50.00). Requesting $120.00.",
    recommendedAction: "Review customer tenure and approve if within standard grace period.",
    status: "PENDING",
    createdAt: new Date(Date.now() - 15 * 60000).toISOString(),
  },
  {
    id: "APR-1003",
    workflowId: "WF-88995",
    correlationId: "CORR-7728",
    ticketId: 43518,
    intent: "Account Deletion",
    confidence: 0.95,
    triggeredPolicy: "Sensitive Information Protection",
    reason: "High-risk action identified. Requires human verification of identity.",
    recommendedAction: "Verify user identity via secondary channel before approving deletion.",
    status: "PENDING",
    createdAt: new Date(Date.now() - 45 * 60000).toISOString(),
  }
];

const mockBlockedRequests: BlockedRequest[] = [
  {
    id: "BLK-8821",
    workflowId: "WF-88981",
    ticketId: 43501,
    guardrail: "PII Redaction",
    reason: "Detected unredacted credit card number in customer response draft.",
    actor: "AI Orchestrator",
    blockedAt: new Date(Date.now() - 2 * 3600000).toISOString(),
  },
  {
    id: "BLK-8822",
    workflowId: "WF-88989",
    ticketId: 43509,
    guardrail: "Prompt Injection Detection",
    reason: "System prompt override attempt detected in user message: 'Ignore previous instructions...'",
    actor: "Security Agent",
    blockedAt: new Date(Date.now() - 5 * 3600000).toISOString(),
  },
  {
    id: "BLK-8823",
    workflowId: "WF-89001",
    ticketId: 43522,
    guardrail: "Unsafe Tool Invocation",
    reason: "Attempted to call internal tool 'DropDatabase' which is restricted.",
    actor: "Guardrail Node",
    blockedAt: new Date(Date.now() - 24 * 3600000).toISOString(),
  }
];

const mockAuditLogs: AuditLog[] = [
  {
    id: "AUD-99101",
    timestamp: new Date().toISOString(),
    workflowId: "WF-89012",
    policyEvaluated: "Hallucination Risk Review",
    decision: "ALLOWED",
    durationMs: 45,
    actor: "SYSTEM",
  },
  {
    id: "AUD-99102",
    timestamp: new Date(Date.now() - 5000).toISOString(),
    workflowId: "WF-89012",
    policyEvaluated: "PII Redaction",
    decision: "ALLOWED",
    durationMs: 12,
    actor: "SYSTEM",
  },
  {
    id: "AUD-99103",
    timestamp: new Date(Date.now() - 15 * 60000).toISOString(),
    workflowId: "WF-88992",
    policyEvaluated: "Manual Approval Required",
    decision: "APPROVAL_REQUIRED",
    durationMs: 22,
    actor: "SYSTEM",
  },
  {
    id: "AUD-99104",
    timestamp: new Date(Date.now() - 45 * 60000).toISOString(),
    workflowId: "WF-88995",
    policyEvaluated: "Sensitive Information Protection",
    decision: "APPROVAL_REQUIRED",
    durationMs: 34,
    actor: "SYSTEM",
  },
  {
    id: "AUD-99105",
    timestamp: new Date(Date.now() - 2 * 3600000).toISOString(),
    workflowId: "WF-88981",
    policyEvaluated: "Compliance Logging",
    decision: "ALLOWED",
    durationMs: 8,
    actor: "SYSTEM",
  },
  {
    id: "AUD-99106",
    timestamp: new Date(Date.now() - 2 * 3600000 + 100).toISOString(),
    workflowId: "WF-88981",
    policyEvaluated: "PII Redaction",
    decision: "BLOCKED",
    durationMs: 67,
    actor: "SYSTEM",
  }
];

export const governanceKeys = {
  all: ["governance"] as const,
  approvalQueue: () => [...governanceKeys.all, "approvalQueue"] as const,
  blockedRequests: () => [...governanceKeys.all, "blockedRequests"] as const,
  auditLogs: () => [...governanceKeys.all, "auditLogs"] as const,
};

export const useApprovalQueue = () => {
  return useQuery({
    queryKey: governanceKeys.approvalQueue(),
    queryFn: async () => {
      // Simulate network delay
      await new Promise(resolve => setTimeout(resolve, 600));
      return mockApprovalQueue;
    },
  });
};

export const useBlockedRequests = () => {
  return useQuery({
    queryKey: governanceKeys.blockedRequests(),
    queryFn: async () => {
      await new Promise(resolve => setTimeout(resolve, 600));
      return mockBlockedRequests;
    },
  });
};

export const useAuditLogs = () => {
  return useQuery({
    queryKey: governanceKeys.auditLogs(),
    queryFn: async () => {
      await new Promise(resolve => setTimeout(resolve, 600));
      return mockAuditLogs;
    },
  });
};
