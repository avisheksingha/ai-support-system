import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { customerTicketApi } from "../api/customerTicketApi";
import type { CreateTicketRequest } from "@/shared/types/ticket";
import { toast } from "sonner";

export const customerKeys = {
  all: ["customer-tickets"] as const,
  lists: () => [...customerKeys.all, "list"] as const,
  detail: (ticketNumber: string) => [...customerKeys.all, "detail", ticketNumber] as const,
};

import { parseDate } from "@/shared/utils/date";

export const useCustomerTickets = () => {
  return useQuery({
    queryKey: customerKeys.lists(),
    queryFn: customerTicketApi.getMyTickets,
    select: (tickets) => {
      return [...tickets].sort((a, b) => parseDate(b.createdAt).getTime() - parseDate(a.createdAt).getTime());
    },
    retry: 2,
    refetchInterval: 15000,
  });
};

export const useCustomerTicket = (ticketNumber: string) => {
  return useQuery({
    queryKey: customerKeys.detail(ticketNumber),
    queryFn: () => customerTicketApi.getMyTicket(ticketNumber),
    enabled: !!ticketNumber,
    retry: 2,
    refetchInterval: (query) => {
      const ticket = query.state.data;
      if (!ticket) return 10000;
      if (ticket.status === "RESOLVED" || ticket.status === "CLOSED") return false;
      return 15000;
    },
  });
};

export const useCreateTicket = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateTicketRequest) => customerTicketApi.createTicket(request),
    onSuccess: (newTicket) => {
      queryClient.invalidateQueries({ queryKey: customerKeys.lists() });
      toast.success("Ticket Submitted", {
        description: `Your ticket ${newTicket.ticketNumber} has been created.`,
      });
    },
    onError: () => {
      toast.error("Failed to create ticket", {
        description: "Please try again later.",
      });
    }
  });
};
