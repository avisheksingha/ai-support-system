import { apiClient } from "@/lib/api-client";
import type { 
  AnalysisModel, 
  KnowledgeModel, 
  RoutingModel,
  TimelinePageResponse,
  OperationsDashboardResponse
} from "@/shared/types/workspace";
import type {
  TicketModel,
  TicketStatus,
  TicketPriority
} from "@/shared/types/ticket";

export const workspaceApi = {
  // Ticket Service
  getTickets: async (status?: string): Promise<TicketModel[]> => {
    const response = await apiClient.get<TicketModel[]>("/tickets", { params: { status } });
    return response.data;
  },

  getTicket: async (ticketNumber: string): Promise<TicketModel> => {
    const response = await apiClient.get<TicketModel>(`/tickets/${ticketNumber}`);
    return response.data;
  },

  updateTicketStatus: async (ticketNumber: string, status: TicketStatus, slaHours?: number): Promise<TicketModel> => {
    const response = await apiClient.patch<TicketModel>(`/tickets/${ticketNumber}/status`, null, {
      params: { status, slaHours }
    });
    return response.data;
  },

  assignTicket: async (ticketNumber: string, assignedTo: string, slaHours?: number): Promise<TicketModel> => {
    const response = await apiClient.patch<TicketModel>(`/tickets/${ticketNumber}/assign`, null, {
      params: { assignedTo, slaHours }
    });
    return response.data;
  },

  updateTicketPriority: async (ticketNumber: string, priority: TicketPriority, slaHours?: number): Promise<TicketModel> => {
    const response = await apiClient.patch<TicketModel>(`/tickets/${ticketNumber}/priority`, null, {
      params: { priority, slaHours }
    });
    return response.data;
  },

  // Analysis Service
  getAnalysis: async (ticketId: number): Promise<AnalysisModel> => {
    const response = await apiClient.get<AnalysisModel>(`/analysis/ticket/${ticketId}`);
    return response.data;
  },

  // Unmocked Knowledge Service (reads from TicketModel)
  getKnowledge: async (ticketId: number): Promise<KnowledgeModel> => {
    const response = await apiClient.get<TicketModel>(`/tickets/${ticketId}`);
    const ticket = response.data;
    
    return {
      ticketId,
      query: ticket.subject,
      generatedReply: ticket.ragResponse || "No knowledge response available yet.",
      similarityScore: 1.0,
      sourceDocuments: [], // Sources not currently persisted on ticket entity
      modelUsed: "AI Orchestrator",
      generatedAt: ticket.updatedAt
    };
  },

  // Routing Service API call (Temporary internal endpoint. Will later be consumed by orchestration-service.)
  getRouting: async (ticketId: number): Promise<RoutingModel> => {
    const response = await apiClient.get<RoutingModel>(`/routing/ticket/${ticketId}`);
    return response.data;
  },

  // Timeline API (Routed to Orchestration Service via Gateway)
  getTimeline: async (ticketId: number, page: number = 0, size: number = 50): Promise<TimelinePageResponse> => {
    const response = await apiClient.get<TimelinePageResponse>(`/orchestration/tickets/${ticketId}/timeline`, {
      params: { page, size }
    });
    return response.data;
  },

  // Operations Dashboard API
  getOperationsOverview: async (params?: Record<string, any>): Promise<OperationsDashboardResponse> => {
    const response = await apiClient.get<OperationsDashboardResponse>(`/orchestration/operations/overview`, { params });
    return response.data;
  },

  // Workflow Explorer API
  searchWorkflows: async (params?: Record<string, any>): Promise<TimelinePageResponse> => {
    const response = await apiClient.get<TimelinePageResponse>(`/orchestration/workflows/search`, { params });
    return response.data;
  }
};
