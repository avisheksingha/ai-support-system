import { apiClient } from "@/lib/api-client";
import type {
  TimelinePageResponse,
  OperationsDashboardResponse,
  WorkspaceDataResponse,
  AgentDashboardResponse
} from "@/shared/types/workspace";
import type {
  TicketModel,
  TicketStatus,
  TicketPriority
} from "@/shared/types/ticket";

export const workspaceApi = {
  // Agent Dashboard API
  getAgentDashboard: async (): Promise<AgentDashboardResponse> => {
    const response = await apiClient.get<AgentDashboardResponse>("/orchestration/dashboard/agent");
    return response.data;
  },

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

  // Workspace Aggregation (Orchestrator Central Brain)
  getWorkspaceAggregation: async (ticketId: number): Promise<WorkspaceDataResponse | null> => {
    try {
      const response = await apiClient.get<WorkspaceDataResponse>(`/orchestration/tickets/${ticketId}/workspace`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },
  // Trigger AI Action
  triggerAction: async (ticketId: number, actionType: string, instructions?: string): Promise<any> => {
    const response = await apiClient.post(`/orchestration/tickets/${ticketId}/actions`, {
      actionType,
      instructions
    });
    return response.data;
  },

  // Ticket Messaging
  getMessages: async (ticketNumber: string): Promise<any[]> => {
    const response = await apiClient.get(`/tickets/${ticketNumber}/messages`);
    return response.data;
  },
  
  addMessage: async (ticketNumber: string, content: string, isInternal: boolean): Promise<any> => {
    const response = await apiClient.post(`/tickets/${ticketNumber}/messages`, {
      content,
      isInternal
    });
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
  },

  // Get timeline for a specific workflow execution
  getWorkflowTimeline: async (workflowId: string, page: number = 0, size: number = 100): Promise<TimelinePageResponse> => {
    const response = await apiClient.get<TimelinePageResponse>(`/orchestration/workflows/${workflowId}/timeline`, {
      params: { page, size }
    });
    return response.data;
  }
};
