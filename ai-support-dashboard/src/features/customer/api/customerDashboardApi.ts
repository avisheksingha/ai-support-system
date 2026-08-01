import { apiClient } from "@/lib/api-client";

export interface CustomerAssistanceDTO {
  title: string;
  summary: string;
  resourceLinks: string[];
  suggestedActions: string[];
}

export interface TicketSummaryDTO {
  ticketNumber: string;
  subject: string;
  status: string;
  priority: string;
  assignedSupportStatus: string;
  estimatedResponse: string;
  lastUpdated: string;
}

export interface TicketDetailDTO {
  ticketNumber: string;
  subject: string;
  message: string;
  status: string;
  priority: string;
  assignedSupportStatus: string;
  estimatedResponse: string;
  createdAt: string;
  lastUpdated: string;
}

export interface CustomerProfileDTO {
  name: string;
  email: string;
}

export interface CustomerSummaryDTO {
  openRequests: number;
  waitingForSupport: number;
  resolved: number;
  latestTicketStatus: string;
  assignedSupportStatus: string;
  averageResponseTime: string;
}

export interface CustomerDashboardResponse {
  profile: CustomerProfileDTO;
  summary: CustomerSummaryDTO;
  tickets: TicketSummaryDTO[];
  recommendedResources: CustomerAssistanceDTO | null;
}

export interface MessageDTO {
  id: number;
  content: string;
  senderName: string;
  type: string;
  createdAt: string;
}

export interface CustomerTicketDetailResponse {
  ticket: TicketDetailDTO;
  messages: MessageDTO[];
  customerAssistance: CustomerAssistanceDTO | null;
}

export const customerDashboardApi = {
  getDashboard: async (): Promise<CustomerDashboardResponse> => {
    const response = await apiClient.get<CustomerDashboardResponse>("/orchestration/dashboard/customer");
    return response.data;
  },

  getTicketDetail: async (ticketNumber: string): Promise<CustomerTicketDetailResponse> => {
    const response = await apiClient.get<CustomerTicketDetailResponse>(`/orchestration/dashboard/customer/tickets/${ticketNumber}`);
    return response.data;
  }
};
