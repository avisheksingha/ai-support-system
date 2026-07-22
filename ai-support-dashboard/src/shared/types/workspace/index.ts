

export interface AnalysisModel {
  intent: string;
  sentiment: string;
  urgency: string;
  confidenceScore: number;
  analysisProvider: string;
  keywords?: string[];
  suggestedCategory?: string;
}

export interface KnowledgeSource {
  id: string;
  title: string;
  similarityScore?: number;
}

export interface KnowledgeModel {
  knowledgeSummary: string;
  confidence?: number;
  sources: KnowledgeSource[];
  knowledgeFound: boolean;
  model?: string;
  retrievedDocumentCount?: number;
  matchedArticleTitles?: string[];
}

export interface RoutingModel {
  assignedTeam: string;
  priority: string;
  slaHours: number;
}

export interface AiDecisionModel {
  aiSummary: string;
  suggestedReply: string;
  confidence: number;
  decisionReason?: string;
}

export interface WorkflowMetadata {
  workflowExecutionId: string;
  workflowState: string;
  workflowDurationMs: number;
}

export interface PipelineProgress {
  analysisCompleted: boolean;
  knowledgeCompleted: boolean;
  routingCompleted: boolean;
  decisionCompleted: boolean;
}

export interface WorkspaceDataResponse {
  analysis?: AnalysisModel;
  knowledge?: KnowledgeModel;
  routing?: RoutingModel;
  aiDecision?: AiDecisionModel;
  workflowMetadata?: WorkflowMetadata;
  pipelineProgress?: PipelineProgress;
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
  ticketNumber?: string;
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

export interface AiRecommendationDTO {
  ticketNumber: string;
  subject: string;
  confidence: number;
  intent: string;
  suggestedAction: string;
  businessReason: string;
}

export interface AgentProfileDTO {
  name: string;
  team: string;
  status: string;
  avatarUrl: string;
}

export interface TopSummaryDTO {
  currentDate: string;
  shift: string;
  assignedToday: number;
  currentSla: string;
}

export interface MyQueueSummaryDTO {
  assignedTickets: number;
  critical: number;
  high: number;
  medium: number;
  low: number;
  averageWaitTime: string;
  oldestTicketAge: string;
}

export interface SlaAtRiskSummaryDTO {
  ticketsNearBreach: number;
  nextSlaBreach: string;
  averageRemainingTime: string;
}

export interface ResolvedTodaySummaryDTO {
  ticketsResolvedToday: number;
  averageHandleTime: string;
  averageFirstResponseTime: string;
}

export interface PerformanceSummaryDTO {
  csat: number;
  resolutionRate: number;
  qualityScore: number;
}

export interface AiActivityTodaySummaryDTO {
  aiDecisionsGenerated: number;
  suggestedRepliesGenerated: number;
  knowledgeSearches: number;
  averageAiConfidence: number | null;
}

export interface AiProcessingSummaryDTO {
  workflowsExecuted: number;
  averageProcessingDurationMs: number;
  successRate: number;
  primaryModel: string;
}

export interface DashboardTimelineEvent {
  id: string;
  eventType: string;
  ticketNumber: string;
  timestamp: string;
  description: string;
  source: string;
}

export interface AgentDashboardResponse {
  generatedAt: string;
  refreshIntervalMs: number;
  version: string;
  profile: AgentProfileDTO;
  topSummary: TopSummaryDTO;
  myQueue: MyQueueSummaryDTO;
  slaAtRisk: SlaAtRiskSummaryDTO;
  resolvedToday: ResolvedTodaySummaryDTO;
  myPerformance: PerformanceSummaryDTO | null;
  aiRecommendations: AiRecommendationDTO[];
  aiActivityToday: AiActivityTodaySummaryDTO;
  aiProcessing: AiProcessingSummaryDTO | null;
  recentActivity: DashboardTimelineEvent[];
}
