import { apiClient } from "@/lib/api-client";
import type { 
  RoutingRule, 
  RoutingRuleRequest, 
  RoutingRuleStats, 
  RoutingRuleFilterParams, 
  Page 
} from "../types";

const API_BASE_URL = "/routing/rules";

export const routingRulesApi = {
  getRules: async (params?: RoutingRuleFilterParams): Promise<Page<RoutingRule>> => {
    const queryParams: Record<string, string | number | boolean> = {};
    if (params?.page !== undefined) queryParams.page = params.page;
    if (params?.size !== undefined) queryParams.size = params.size;
    if (params?.search) queryParams.search = params.search;
    if (params?.active !== undefined) queryParams.active = params.active;
    if (params?.team && params.team !== "ALL") queryParams.team = params.team;

    const response = await apiClient.get(API_BASE_URL, { params: queryParams });
    return response.data;
  },

  getRule: async (id: number): Promise<RoutingRule> => {
    const response = await apiClient.get(`${API_BASE_URL}/${id}`);
    return response.data;
  },

  createRule: async (request: RoutingRuleRequest): Promise<RoutingRule> => {
    const response = await apiClient.post(API_BASE_URL, request);
    return response.data;
  },

  updateRule: async (id: number, request: RoutingRuleRequest): Promise<RoutingRule> => {
    const response = await apiClient.put(`${API_BASE_URL}/${id}`, request);
    return response.data;
  },

  toggleRuleActive: async (id: number): Promise<RoutingRule> => {
    const response = await apiClient.patch(`${API_BASE_URL}/${id}/toggle`);
    return response.data;
  },

  deleteRule: async (id: number): Promise<void> => {
    await apiClient.delete(`${API_BASE_URL}/${id}`);
  },

  getStats: async (): Promise<RoutingRuleStats> => {
    const response = await apiClient.get(`${API_BASE_URL}/stats`);
    return response.data;
  }
};
