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

export type TimelineEventType = "CREATED" | "AI_ANALYSIS" | "KNOWLEDGE_RETRIEVED" | "ROUTING_DECISION" | "ASSIGNMENT" | "STATUS_CHANGE" | "RESOLVED";

export interface TimelineEvent {
  id: string;
  type: TimelineEventType;
  timestamp: string;
  title: string;
  description: string;
  status: "pending" | "in_progress" | "completed" | "failed";
}
