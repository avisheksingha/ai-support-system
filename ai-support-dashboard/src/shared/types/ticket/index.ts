export type TicketStatus = "NEW" | "ANALYZING" | "ANALYZED" | "ASSIGNED" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";
export type TicketPriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface TicketModel {
  id: number;
  ticketNumber: string;
  customerUserId: number;
  customerEmail: string;
  customerName: string;
  subject: string;
  message: string;
  status: TicketStatus;
  priority: TicketPriority;
  assignedTo?: string;
  ragResponse?: string;
  ragGeneratedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTicketRequest {
  subject: string;
  message: string;
}
