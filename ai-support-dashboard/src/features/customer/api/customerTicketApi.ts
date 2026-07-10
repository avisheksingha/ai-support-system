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
};
