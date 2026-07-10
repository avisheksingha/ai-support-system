import type { TicketModel, TicketPriority } from "@/shared/types/ticket";

export class BusinessMetricsCalculator {
  static calculateOpenTickets(tickets: TicketModel[]): number {
    return tickets.filter(t => !["RESOLVED", "CLOSED"].includes(t.status)).length;
  }

  static calculateResolvedToday(tickets: TicketModel[]): number {
    const today = new Date().toISOString().split('T')[0] || '';
    return tickets.filter(t => 
      (t.status === "RESOLVED" || t.status === "CLOSED") && 
      t.updatedAt && t.updatedAt.startsWith(today)
    ).length;
  }

  static calculatePriorityDistribution(tickets: TicketModel[]) {
    const dist: Record<TicketPriority, number> = {
      CRITICAL: 0, HIGH: 0, MEDIUM: 0, LOW: 0
    };
    tickets.forEach(t => {
      if (dist[t.priority] !== undefined) {
        dist[t.priority]++;
      }
    });
    return Object.entries(dist).map(([name, value]) => ({ name, value }));
  }

  static calculateStatusDistribution(tickets: TicketModel[]) {
    const dist: Record<string, number> = {};
    tickets.forEach(t => {
      dist[t.status] = (dist[t.status] || 0) + 1;
    });
    return Object.entries(dist).map(([name, value]) => ({ name, value }));
  }
}
