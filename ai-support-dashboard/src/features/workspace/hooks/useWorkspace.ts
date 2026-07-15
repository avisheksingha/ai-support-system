import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { workspaceApi } from "../api/workspaceApi";
import { workspaceKeys } from "./workspaceKeys";
import type { TicketStatus, TicketPriority } from "@/shared/types/ticket";
import { toast } from "sonner";

import { parseDate } from "@/shared/utils/date";

export const useTicketList = (status?: string) => {
  return useQuery({
    queryKey: workspaceKeys.ticketList(status),
    queryFn: () => workspaceApi.getTickets(status),
    select: (tickets) => {
      return [...tickets].sort((a, b) => parseDate(b.createdAt).getTime() - parseDate(a.createdAt).getTime());
    },
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


export const useRouting = (ticketId?: number) => {
  return useQuery({
    queryKey: ticketId ? workspaceKeys.routing(ticketId) : [],
    queryFn: () => workspaceApi.getRouting(ticketId!),
    enabled: !!ticketId,
    retry: 2,
    refetchInterval: (query) => query.state.data ? false : 5000,
  });
};

export const useMessages = (ticketNumber?: string) => {
  return useQuery({
    queryKey: ticketNumber ? ["ticket-messages", ticketNumber] : [],
    queryFn: () => workspaceApi.getMessages(ticketNumber!),
    enabled: !!ticketNumber,
    retry: 2,
  });
};

export const useAddMessage = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ ticketNumber, content, isInternal }: { ticketNumber: string, content: string, isInternal: boolean }) => 
      workspaceApi.addMessage(ticketNumber, content, isInternal),
    onSuccess: (newMessage, { ticketNumber }) => {
      queryClient.invalidateQueries({ queryKey: ["ticket-messages", ticketNumber] });
      toast.success(newMessage.isInternal ? "Internal note added" : "Reply sent");
    },
    onError: (error: any) => {
      toast.error("Failed to send message", {
        description: error?.response?.data?.message || "An error occurred"
      });
    }
  });
};

export const useTriggerAction = () => {
  return useMutation({
    mutationFn: ({ ticketId, actionType, instructions }: { ticketId: number, actionType: string, instructions?: string }) => 
      workspaceApi.triggerAction(ticketId, actionType, instructions),
    onSuccess: (_, { actionType }) => {
      toast.success(`Action Triggered`, {
        description: `${actionType} has been successfully submitted.`
      });
    }
  });
};

export const useTimeline = (ticketId?: number) => {
  return useQuery({
    queryKey: ticketId ? workspaceKeys.timeline(ticketId) : [],
    queryFn: () => workspaceApi.getTimeline(ticketId!),
    select: (data) => data.content,
    enabled: !!ticketId,
    retry: 2,
    refetchInterval: (query) => {
      const data = query.state.data as unknown as any;
      const events = Array.isArray(data) ? data : data?.content;
      
      if (events && Array.isArray(events)) {
        // Stop polling if a terminal outcome is reached
        const hasTerminal = events.some((e: any) => 
          e.outcome === "COMPLETED" || 
          e.outcome === "FAILED" || 
          e.outcome === "PARTIAL_SUCCESS" || 
          e.outcome === "WAITING_APPROVAL"
        );
        if (hasTerminal) return false;
      }
      return 5000;
    },
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
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || "Failed to update ticket status. Invalid transition.";
      toast.error(`Update Failed`, {
        description: message
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
