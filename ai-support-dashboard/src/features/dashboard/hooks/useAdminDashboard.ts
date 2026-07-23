import { useQuery } from "@tanstack/react-query";
import { adminDashboardApi } from "../api/adminDashboardApi";

export const useAdminDashboard = () => {
  return useQuery({
    queryKey: ["admin-dashboard"],
    queryFn: adminDashboardApi.getAdminDashboard,
    refetchInterval: 30000, // Refresh every 30 seconds
  });
};

export const useOrchestrationHealth = () => {
  return useQuery({
    queryKey: ["orchestration-health"],
    queryFn: adminDashboardApi.getOrchestrationHealth,
    refetchInterval: 15000, // Refresh every 15 seconds for health checks
    retry: 1,
  });
};
