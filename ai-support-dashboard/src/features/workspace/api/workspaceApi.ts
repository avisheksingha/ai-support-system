import { apiClient } from "@/lib/api-client";
import type { 
  TicketModel, 
  AnalysisModel, 
  KnowledgeModel, 
  RoutingModel,
  TimelineEvent,
  TicketStatus,
  TicketPriority
} from "@/shared/types/workspace";

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

  // Mocked Knowledge Service (since it uses Kafka and lacks endpoints currently)
  getKnowledge: async (ticketId: number): Promise<KnowledgeModel> => {
    await new Promise(resolve => setTimeout(resolve, 600)); // simulate network delay
    // In reality, this would fetch from RAG service via Gateway
    return {
      ticketId,
      query: "Extracted from ticket analysis keywords",
      generatedReply: "This is a placeholder RAG response. RAG generation is fully event-driven via Kafka right now. Once the Orchestration API is built, real documents will show here.",
      similarityScore: 0.89,
      sourceDocuments: [
        { title: "Troubleshooting Guide", url: "#" },
        { title: "Account Recovery", url: "#" }
      ],
      modelUsed: "Google GenAI text-bison",
      generatedAt: new Date().toISOString()
    };
  },

  // Mocked Routing Service
  getRouting: async (ticketId: number): Promise<RoutingModel> => {
    await new Promise(resolve => setTimeout(resolve, 800)); // simulate network delay
    return {
      ticketId,
      suggestedDepartment: "L2 Technical Support",
      suggestedAgent: "agent123",
      confidenceScore: 0.95,
      reasoning: "Ticket intent matched technical issue with high urgency.",
      routedAt: new Date().toISOString()
    };
  },

  // Mocked Timeline Events (Will be powered by Orchestrator later)
  getTimeline: async (_ticketId: number): Promise<TimelineEvent[]> => {
    await new Promise(resolve => setTimeout(resolve, 400));
    const now = new Date();
    return [
      {
        id: "1",
        type: "CREATED",
        timestamp: new Date(now.getTime() - 600000).toISOString(), // 10 mins ago
        title: "Ticket Created",
        description: "Customer submitted support request",
        status: "completed"
      },
      {
        id: "2",
        type: "AI_ANALYSIS",
        timestamp: new Date(now.getTime() - 590000).toISOString(),
        title: "AI Analysis",
        description: "Google GenAI categorized intent and urgency",
        status: "completed"
      },
      {
        id: "3",
        type: "KNOWLEDGE_RETRIEVED",
        timestamp: new Date(now.getTime() - 580000).toISOString(),
        title: "Knowledge Base Search",
        description: "RAG agent retrieved similar documentation",
        status: "completed"
      },
      {
        id: "4",
        type: "ROUTING_DECISION",
        timestamp: new Date(now.getTime() - 570000).toISOString(),
        title: "Rule-based Routing",
        description: "Ticket routed to L2 Technical Support",
        status: "completed"
      }
    ];
  }
};
