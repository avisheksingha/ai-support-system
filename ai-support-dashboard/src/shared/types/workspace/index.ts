export type TicketStatus = "NEW" | "ANALYZING" | "ANALYZED" | "ASSIGNED" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";
export type TicketPriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface TicketModel {
  id: number;
  ticketNumber: string;
  customerId: number;
  customerEmail: string;
  customerName: string;
  subject: string;
  message: string;
  status: TicketStatus;
  priority: TicketPriority;
  assignedTo?: string;
  createdAt: string;
  updatedAt: string;
}

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
  suggestedDepartment: string;
  suggestedAgent?: string;
  confidenceScore: number;
  reasoning: string;
  routedAt?: string;
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
