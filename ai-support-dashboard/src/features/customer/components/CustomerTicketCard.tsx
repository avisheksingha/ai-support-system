import { formatTimeAgo } from "@/shared/utils/date";
import type { TicketSummaryDTO } from "../api/customerDashboardApi";
import { ChevronRight } from "lucide-react";
import { getCustomerStatusMapping } from "../utils/customer-status";

interface CustomerTicketCardProps {
  ticket: TicketSummaryDTO;
  onClick: () => void;
}

export function CustomerTicketCard({ ticket, onClick }: CustomerTicketCardProps) {
  return (
    <button
      onClick={onClick}
      className="w-full text-left p-6 rounded-xl border border-border bg-card hover:bg-muted/30 hover:border-border transition-all duration-200 group flex items-start gap-4 shadow-sm"
    >
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-3 mb-2">
          <span className="text-[11px] font-mono text-muted-foreground">Ticket #{ticket.ticketNumber}</span>
          <StatusBadge status={ticket.status} />
        </div>
        
        <h3 className="text-base font-medium text-foreground truncate mb-1 group-hover:text-blue-600 transition-colors">
          {ticket.subject}
        </h3>
        
        <p className="text-sm text-muted-foreground line-clamp-1 mb-4">
          Status: {ticket.status}
        </p>
        
        <MiniPipeline status={ticket.status} />

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-5 pt-5 border-t border-border/60">
          <div className="flex flex-col">
             <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Priority</span>
             <div className="font-medium text-foreground text-xs">{ticket.priority}</div>
          </div>
          <div className="flex flex-col">
             <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Assigned Team</span>
             <div className="font-medium text-foreground text-xs">{ticket.assignedSupportStatus}</div>
          </div>
          <div className="flex flex-col">
             <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Updated</span>
             <div className="font-medium text-foreground text-xs">{ticket.lastUpdated ? formatTimeAgo(ticket.lastUpdated) : 'just now'}</div>
          </div>
          <div className="flex flex-col">
             <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Estimated Response</span>
             <div className="font-medium text-foreground text-xs">{ticket.estimatedResponse}</div>
          </div>
        </div>
      </div>
      
      <div className="h-full flex items-center pt-8 opacity-0 group-hover:opacity-100 transition-opacity">
        <ChevronRight className="h-5 w-5 text-muted-foreground" />
      </div>
    </button>
  );
}

function StatusBadge({ status }: { status: string }) {
  const mapping = getCustomerStatusMapping(status);

  return (
    <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded border ${mapping.style}`}>
      {mapping.label}
    </span>
  );
}

function MiniPipeline({ status }: { status: string }) {
  const mapping = getCustomerStatusMapping(status);
  const { isReviewed, isAssigned, isResolved } = mapping.progress;
  
  const icon = (done: boolean, active: boolean) => {
    if (active && !done) return <span className="text-blue-600 font-bold animate-pulse">●</span>;
    if (done) return <span className="text-blue-600 font-bold">✓</span>;
    return <span className="text-muted-foreground opacity-50 font-bold">○</span>;
  };
  const arrow = <span className="text-muted-foreground/30 mx-1.5 font-bold">→</span>;

  return (
    <div className="flex items-center text-[11px] font-medium text-muted-foreground gap-1 bg-muted/50 w-fit px-3 py-1.5 rounded-lg border border-border/50">
      {icon(true, true)} <span className="text-foreground">Submitted</span> {arrow}
      {icon(isReviewed, !isAssigned)} <span className={isReviewed ? "text-foreground" : (!isAssigned ? "text-foreground opacity-80" : "")}>Reviewed</span> {arrow}
      {icon(isAssigned, !isResolved && isAssigned)} <span className={isAssigned ? "text-foreground" : (!isResolved && isAssigned ? "text-foreground opacity-80" : "")}>Assigned</span> {arrow}
      {icon(isResolved, false)} <span className={isResolved ? "text-foreground" : ""}>Resolved</span>
    </div>
  );
}
