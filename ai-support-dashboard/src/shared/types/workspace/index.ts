import type { TicketPriority } from "../ticket";

export interface AnalysisModel {
  id: number;
  ticketId: number;
  intent: string;
  sentiment: string;
  urgency: TicketPriority | string;
  confidenceScore: number;
  keywords: string[];
  suggestedCategory: string;
  rawResponse?: string;
  analysisProvider?: string;
  analyzedAt: string;
}

export interface KnowledgeModel {
  id?: number;
  ticketId?: number;
  query?: string;
  generatedReply: string;
  similarityScore?: number;
  sourceDocuments?: Array<{ title: string; url: string }>;
  modelUsed?: string;
  generatedAt?: string;
}

export interface RoutingModel {
  id?: number;
  ticketId?: number;
  department: string;
  assignedAgent?: string;
  confidenceScore: number;
  reason: string;
  ruleName?: string;
  ruleVersion?: number;
  executedAt?: string;
}

export type TimelineEventType = "SYSTEM" | "WORKFLOW" | "AI" | "TOOL" | "GOVERNANCE" | "USER" | "STATUS" | "NOTIFICATION";

export interface TimelineEvent {
  eventId: string;
  parentEventId?: string;
  type: TimelineEventType;
  subType: string;
  title: string;
  description: string;
  severity: "INFO" | "WARNING" | "ERROR" | "SUCCESS";
  processingStage: string;
  timestamp: string;
  
  // Execution Details
  tools?: string[];
  latencyMs?: number;
  tokens?: number;
  workflowVersion?: string;
  promptVersion?: string;
  model?: string;
  outcome?: "COMPLETED" | "PARTIAL_SUCCESS" | "FAILED" | "WAITING_APPROVAL";
}

export interface TimelinePageResponse {
  content: TimelineEvent[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface TrendData {
  label: string;
  value: number;
}

export interface RuntimeMetrics {
  activeWorkflows: number;
  completedToday: number;
  failedToday: number;
  averageDurationMs: number;
  hourlyThroughput: TrendData[];
}

export interface AiMetrics {
  averageTotalTokens: number;
  averagePromptTokens: number;
  averageCompletionTokens: number;
  averageLatencyMs: number;
  hourlyTokenTrend: TrendData[];
}

export interface GovernanceMetrics {
  policyViolations: number;
  guardrailBlocks: number;
  approvalRequests: number;
  hourlyViolations: TrendData[];
}

export interface ProviderMetrics {
  providerId: string;
  invocations: number;
  successes: number;
  failures: number;
  avgLatencyMs: number;
  lastInvocation: string;
}

export interface HealthMetrics {
  circuitBreakersOpen: number;
  retries: number;
  timeouts: number;
  outboxQueueSize: number;
  avgQueueDelayMs: number;
  kafkaConnected: boolean;
  databaseConnected: boolean;
}

export interface SystemInfo {
  serviceVersion: string;
  gitCommit: string;
  buildNumber: string;
  environment: string;
}

export interface OperationsOverviewDTO {
  runtime: RuntimeMetrics;
  ai: AiMetrics;
  governance: GovernanceMetrics;
  tools: ProviderMetrics[];
  health: HealthMetrics;
  systemInfo: SystemInfo;
}

export interface WorkflowSummaryDTO {
  workflowId: string;
  ticketId: number;
  correlationId: string;
  definitionId: string;
  state: string;
  startedAt: string;
  completedAt: string;
  durationMs: number;
}

export interface OperationsDashboardResponse {
  overview: OperationsOverviewDTO;
  recentExecutions: WorkflowSummaryDTO[];
}

export interface ApprovalRequest {
  id: string;
  workflowId: string;
  correlationId: string;
  ticketId: number;
  intent: string;
  confidence: number;
  triggeredPolicy: string;
  reason: string;
  recommendedAction: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
  createdAt: string;
}

export interface BlockedRequest {
  id: string;
  workflowId: string;
  ticketId: number;
  guardrail: string;
  reason: string;
  actor: string;
  blockedAt: string;
}

export interface AuditLog {
  id: string;
  timestamp: string;
  workflowId: string;
  policyEvaluated: string;
  decision: "ALLOWED" | "BLOCKED" | "APPROVAL_REQUIRED";
  durationMs: number;
  actor: "AI" | "SYSTEM" | "AGENT";
}
