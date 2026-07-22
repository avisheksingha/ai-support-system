import { useQuery } from "@tanstack/react-query";
import { customerDashboardApi } from "../api/customerDashboardApi";

export const customerDashboardKeys = {
  all: ["customer-dashboard"] as const,
  dashboard: () => [...customerDashboardKeys.all, "overview"] as const,
  detail: (ticketNumber: string) => [...customerDashboardKeys.all, "detail", ticketNumber] as const,
};

export const useCustomerDashboard = () => {
  return useQuery({
    queryKey: customerDashboardKeys.dashboard(),
    queryFn: customerDashboardApi.getDashboard,
    retry: 2,
    refetchInterval: 15000,
  });
};

export const useCustomerTicketDetail = (ticketNumber: string) => {
  return useQuery({
    queryKey: customerDashboardKeys.detail(ticketNumber),
    queryFn: () => customerDashboardApi.getTicketDetail(ticketNumber),
    enabled: !!ticketNumber,
    retry: 2,
    refetchInterval: (query) => {
      const data = query.state.data;
      if (!data) return 10000;
      if (data.ticket.status === "RESOLVED" || data.ticket.status === "CLOSED") return false;
      return 15000;
    },
  });
};
