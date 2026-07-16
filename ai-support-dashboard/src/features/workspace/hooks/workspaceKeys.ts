export const workspaceKeys = {
  all: ["workspace"] as const,
  tickets: () => [...workspaceKeys.all, "tickets"] as const,
  ticketList: (status?: string) => [...workspaceKeys.tickets(), "list", { status }] as const,
  
  ticketDetail: (ticketNumber: string) => [...workspaceKeys.tickets(), "detail", ticketNumber] as const,
  
  workspaceAggregation: (ticketId: number) => [...workspaceKeys.all, "workspaceAggregation", ticketId] as const,
  timeline: (ticketId: number) => [...workspaceKeys.all, "timeline", ticketId] as const,
};
