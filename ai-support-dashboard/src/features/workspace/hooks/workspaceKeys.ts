export const workspaceKeys = {
  all: ["workspace"] as const,
  tickets: () => [...workspaceKeys.all, "tickets"] as const,
  ticketList: (status?: string) => [...workspaceKeys.tickets(), "list", { status }] as const,
  
  ticketDetail: (ticketNumber: string) => [...workspaceKeys.tickets(), "detail", ticketNumber] as const,
  
  analysis: (ticketId: number) => [...workspaceKeys.all, "analysis", ticketId] as const,
  knowledge: (ticketId: number) => [...workspaceKeys.all, "knowledge", ticketId] as const,
  routing: (ticketId: number) => [...workspaceKeys.all, "routing", ticketId] as const,
  timeline: (ticketId: number) => [...workspaceKeys.all, "timeline", ticketId] as const,
};
