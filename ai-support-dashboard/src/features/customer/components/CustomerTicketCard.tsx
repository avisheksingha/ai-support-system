import { formatTimeAgo } from "@/shared/utils/date";
import type { TicketModel } from "@/shared/types/ticket";
import { ChevronRight } from "lucide-react";

interface CustomerTicketCardProps {
  ticket: TicketModel;
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
          {ticket.message}
        </p>
        
        <MiniPipeline status={ticket.status} />

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-5 pt-5 border-t border-border/60">
          <div className="flex flex-col">
             <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Support Category</span>
             <div className="font-medium text-foreground text-xs">🖥 Technical</div>
          </div>
          <div className="flex flex-col">
             <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Assigned Team</span>
             <div className="font-medium text-foreground text-xs">Technical Support</div>
          </div>
          <div className="flex flex-col">
             <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Updated</span>
             <div className="font-medium text-foreground text-xs">{ticket.updatedAt ? formatTimeAgo(ticket.updatedAt) : 'just now'}</div>
          </div>
          <div className="flex flex-col">
             <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Estimated Response</span>
             <div className="font-medium text-foreground text-xs">Within 4 hours</div>
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
  let style = "text-muted-foreground border-border bg-muted";
  let label = status;

  switch (status) {
    case "NEW":
    case "ANALYZING":
    case "ANALYZED":
      style = "text-blue-600 border-blue-200 bg-blue-50";
      label = "Submitted";
      break;
    case "ASSIGNED":
      style = "text-purple-600 border-purple-200 bg-purple-50";
      label = "In Review";
      break;
    case "IN_PROGRESS":
      style = "text-orange-600 border-orange-200 bg-orange-50";
      label = "In Progress";
      break;
    case "RESOLVED":
      style = "text-emerald-600 border-emerald-200 bg-emerald-50";
      label = "Resolved";
      break;
    case "CLOSED":
      style = "text-gray-600 border-gray-200 bg-gray-50";
      label = "Closed";
      break;
  }

  return (
    <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded border ${style}`}>
      {label}
    </span>
  );
}

function MiniPipeline({ status }: { status: string }) {
  const s = status;
  const isReviewed = ["ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"].includes(s);
  const isAssigned = ["ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"].includes(s);
  const isResolved = ["RESOLVED", "CLOSED"].includes(s);
  
  const icon = (done: boolean) => done ? <span className="text-blue-600 font-bold">✓</span> : <span className="text-muted-foreground opacity-50 font-bold">○</span>;
  const arrow = <span className="text-muted-foreground/30 mx-1.5 font-bold">→</span>;

  return (
    <div className="flex items-center text-[11px] font-medium text-muted-foreground gap-1 bg-muted/50 w-fit px-3 py-1.5 rounded-lg border border-border/50">
      {icon(true)} <span className={true ? "text-foreground" : ""}>Submitted</span> {arrow}
      {icon(isReviewed)} <span className={isReviewed ? "text-foreground" : ""}>Reviewed</span> {arrow}
      {icon(isAssigned)} <span className={isAssigned ? "text-foreground" : ""}>Assigned</span> {arrow}
      {icon(isResolved)} <span className={isResolved ? "text-foreground" : ""}>Resolved</span>
    </div>
  );
}
