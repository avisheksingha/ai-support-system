import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { workspaceApi } from "../api/workspaceApi";
import { workspaceKeys } from "./workspaceKeys";
import type { TicketStatus, TicketPriority } from "@/shared/types/ticket";
import { toast } from "sonner";

export const useTicketList = (status?: string) => {
  return useQuery({
    queryKey: workspaceKeys.ticketList(status),
    queryFn: () => workspaceApi.getTickets(status),
    retry: 2,
    refetchInterval: 10000,
  });
};

export const useTicket = (ticketNumber: string) => {
  return useQuery({
    queryKey: workspaceKeys.ticketDetail(ticketNumber),
    queryFn: () => workspaceApi.getTicket(ticketNumber),
    enabled: !!ticketNumber,
    retry: 2,
    refetchInterval: (query) => {
      const ticket = query.state.data;
      if (!ticket) return 10000;
      if (ticket.status === "RESOLVED" || ticket.status === "CLOSED") return false;
      return 10000;
    },
  });
};

export const useAnalysis = (ticketId?: number) => {
  return useQuery({
    queryKey: ticketId ? workspaceKeys.analysis(ticketId) : [],
    queryFn: () => workspaceApi.getAnalysis(ticketId!),
    enabled: !!ticketId,
    retry: 2,
    refetchInterval: (query) => {
      // If we already have the data, we don't need to poll anymore
      if (query.state.data) return false;
      return 5000; // Poll faster for AI results
    }
  });
};

export const useKnowledge = (ticketId?: number) => {
  return useQuery({
    queryKey: ticketId ? workspaceKeys.knowledge(ticketId) : [],
    queryFn: () => workspaceApi.getKnowledge(ticketId!),
    enabled: !!ticketId,
    retry: 2,
    refetchInterval: (query) => query.state.data ? false : 5000,
  });
};

export const useRouting = (ticketId?: number) => {
  return useQuery({
    queryKey: ticketId ? workspaceKeys.routing(ticketId) : [],
    queryFn: () => workspaceApi.getRouting(ticketId!),
    enabled: !!ticketId,
    retry: 2,
    refetchInterval: (query) => query.state.data ? false : 5000,
  });
};

export const useTimeline = (ticketId?: number) => {
  return useQuery({
    queryKey: ticketId ? workspaceKeys.timeline(ticketId) : [],
    queryFn: () => workspaceApi.getTimeline(ticketId!),
    enabled: !!ticketId,
    retry: 2,
    refetchInterval: 10000,
  });
};

export const useUpdateTicketStatus = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ ticketNumber, status, slaHours }: { ticketNumber: string, status: TicketStatus, slaHours?: number }) => 
      workspaceApi.updateTicketStatus(ticketNumber, status, slaHours),
    onSuccess: (updatedTicket) => {
      queryClient.invalidateQueries({ queryKey: workspaceKeys.tickets() });
      queryClient.setQueryData(workspaceKeys.ticketDetail(updatedTicket.ticketNumber), updatedTicket);
      toast.success(`Ticket Status Updated`, {
        description: `${updatedTicket.ticketNumber} is now ${updatedTicket.status}`
      });
    }
  });
};

export const useAssignTicket = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ ticketNumber, assignedTo, slaHours }: { ticketNumber: string, assignedTo: string, slaHours?: number }) => 
      workspaceApi.assignTicket(ticketNumber, assignedTo, slaHours),
    onSuccess: (updatedTicket) => {
      queryClient.invalidateQueries({ queryKey: workspaceKeys.tickets() });
      queryClient.setQueryData(workspaceKeys.ticketDetail(updatedTicket.ticketNumber), updatedTicket);
      toast.success(`Ticket Assigned`, {
        description: `${updatedTicket.ticketNumber} assigned to @${updatedTicket.assignedTo}`
      });
    }
  });
};

export const useUpdateTicketPriority = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ ticketNumber, priority, slaHours }: { ticketNumber: string, priority: TicketPriority, slaHours?: number }) => 
      workspaceApi.updateTicketPriority(ticketNumber, priority, slaHours),
    onSuccess: (updatedTicket) => {
      queryClient.invalidateQueries({ queryKey: workspaceKeys.tickets() });
      queryClient.setQueryData(workspaceKeys.ticketDetail(updatedTicket.ticketNumber), updatedTicket);
      toast.success(`Priority Updated`, {
        description: `${updatedTicket.ticketNumber} is now ${updatedTicket.priority} priority`
      });
    }
  });
};
