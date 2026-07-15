import { apiClient } from "@/lib/api-client";
import type { TicketModel, CreateTicketRequest } from "@/shared/types/ticket";

export const customerTicketApi = {
  getMyTickets: async (): Promise<TicketModel[]> => {
    const response = await apiClient.get<TicketModel[]>("/tickets/my");
    return response.data;
  },

  getMyTicket: async (ticketNumber: string): Promise<TicketModel> => {
    const response = await apiClient.get<TicketModel>(`/tickets/my/${ticketNumber}`);
    return response.data;
  },

  createTicket: async (request: CreateTicketRequest): Promise<TicketModel> => {
    const response = await apiClient.post<TicketModel>("/tickets", request);
    return response.data;
  },

  getMessages: async (ticketNumber: string) => {
    const response = await apiClient.get(`/tickets/my/${ticketNumber}/messages`);
    return response.data;
  },

  addMessage: async ({ ticketNumber, content }: { ticketNumber: string; content: string }) => {
    const response = await apiClient.post(`/tickets/my/${ticketNumber}/messages`, {
      content,
      isInternal: false,
    });
    return response.data;
  },
};
