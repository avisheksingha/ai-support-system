import { apiClient } from "@/lib/api-client";
import type { 
  AnalysisModel, 
  KnowledgeModel, 
  RoutingModel,
  TimelineEvent,
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

  // Routing Service API call (Temporary internal endpoint. Will later be consumed by orchestration-service.)
  getRouting: async (ticketId: number): Promise<RoutingModel> => {
    const response = await apiClient.get<RoutingModel>(`/routing/ticket/${ticketId}`);
    return response.data;
  },

  // Mocked Timeline Events (Will be powered by Orchestrator later)
  getTimeline: async (ticketId: number, baseDate?: any): Promise<TimelineEvent[]> => {
    await new Promise(resolve => setTimeout(resolve, 400));
    
    // We import parseDate inline to avoid circular dependencies if any, but since it's just a util it's fine.
    // Actually we can just dynamically import or use the provided logic, since I can't easily add import to the top of this file in one replace chunk.
    let baseTime = new Date().getTime();
    if (baseDate) {
      // Just manually do the parseDate logic to be safe without imports
      if (Array.isArray(baseDate)) {
        const [y, m, d, h=0, min=0, s=0, ms=0] = baseDate;
        baseTime = Date.UTC(y, m - 1, d, h, min, s, ms / 1000000);
      } else if (typeof baseDate === 'string') {
        let str = baseDate;
        if (str.includes('T') && !str.endsWith('Z') && !str.match(/[+-]\d{2}:?\d{2}$/)) {
          str += 'Z';
        }
        baseTime = new Date(str).getTime();
      }
    } else {
      try {
        const response = await apiClient.get<TicketModel>(`/tickets/id/${ticketId}`);
        if (response.data && response.data.createdAt) {
           const backendDate = response.data.createdAt;
           if (Array.isArray(backendDate)) {
             const [y, m, d, h=0, min=0, s=0, ms=0] = backendDate;
             baseTime = Date.UTC(y, m - 1, d, h, min, s, ms / 1000000);
           } else if (typeof backendDate === 'string') {
             let str = backendDate;
             if (str.includes('T') && !str.endsWith('Z') && !str.match(/[+-]\d{2}:?\d{2}$/)) {
               str += 'Z';
             }
             baseTime = new Date(str).getTime();
           }
        }
      } catch (e) {
        // Ignore and fallback to current time
      }
    }

    return [
      {
        id: "1",
        type: "CREATED",
        timestamp: new Date(baseTime).toISOString(),
        title: "Ticket Created",
        description: "Customer submitted support request",
        status: "completed"
      },
      {
        id: "2",
        type: "AI_ANALYSIS",
        timestamp: new Date(baseTime + 420).toISOString(),
        title: "AI Analysis",
        description: "Google GenAI categorized intent and urgency",
        status: "completed"
      },
      {
        id: "3",
        type: "KNOWLEDGE_RETRIEVED",
        timestamp: new Date(baseTime + 600).toISOString(),
        title: "Knowledge Base Search",
        description: "RAG agent retrieved similar documentation",
        status: "completed"
      },
      {
        id: "4",
        type: "ROUTING_DECISION",
        timestamp: new Date(baseTime + 670).toISOString(),
        title: "Rule-based Routing",
        description: "Ticket routed to L2 Technical Support",
        status: "completed"
      }
    ];
  }
};
