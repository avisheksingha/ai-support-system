import { apiClient } from "@/lib/api-client";
import axios from "axios";

// Create separate axios instance for actuator health check on port 8671
const actuatorClient = axios.create({
  baseURL: import.meta.env.VITE_ORCHESTRATION_BASE_URL || "http://localhost:8671",
  timeout: 5000,
  headers: {
    "Content-Type": "application/json",
  },
});

export interface SystemHealthDTO {
  serviceName: string;
  status: string; // HEALTHY, DEGRADED, DOWN
}

export interface PlatformOverviewDTO {
  ticketsToday: number;
  totalCustomers: number;
  totalAgents: number;
  totalAdmins: number;
  activeUsers: string;
}

export interface AiGovernanceDTO {
  analysisSuccess: string;
  routingSuccess: string;
  ragCoverage: string;
  averageLatency: string;
}

export interface RagKnowledgeDTO {
  totalArticles: number;
  vectorizedDocuments: number;
  mostAccessedArticle: string;
}

export interface EventDTO {
  label: string;
  sublabel: string;
  time: string;
}

export interface ActivityDTO {
  label: string;
  time: string;
  color: string;
}

export interface AdminDashboardResponse {
  platformOverview: PlatformOverviewDTO;
  aiGovernance: AiGovernanceDTO;
  departmentWorkload: Record<string, number>;
  routingOverview: Record<string, number>;
  systemHealth: SystemHealthDTO[];
  ragKnowledge: RagKnowledgeDTO;
  recentEvents: EventDTO[];
  myActivity: ActivityDTO[];
}

export const adminDashboardApi = {
  getAdminDashboard: async (): Promise<AdminDashboardResponse> => {
    const response = await apiClient.get<AdminDashboardResponse>("/orchestration/dashboard/admin");
    return response.data;
  },

  // Fetch AI Orchestration Service health from actuator endpoint on port 8671
  getOrchestrationHealth: async (): Promise<{ status: string; components?: any }> => {
    try {
      const response = await actuatorClient.get("/actuator/health");
      return response.data;
    } catch (error) {
      // Return degraded status if actuator is unavailable
      return { status: "DOWN" };
    }
  },
};
