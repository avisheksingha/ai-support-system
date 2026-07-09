import { formatDistanceToNow } from "date-fns";
import { Badge } from "@/components/ui/badge";
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
      className="w-full text-left p-5 rounded-xl border border-border bg-card hover:bg-card hover:border-border transition-all duration-200 group flex items-start gap-4"
    >
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-3 mb-2">
          <span className="text-xs font-mono text-muted-foreground">{ticket.ticketNumber}</span>
          <StatusBadge status={ticket.status} />
        </div>
        
        <h3 className="text-base font-medium text-foreground truncate mb-1.5 group-hover:text-blue-400 transition-colors">
          {ticket.subject}
        </h3>
        
        <p className="text-sm text-muted-foreground line-clamp-1 mb-3">
          {ticket.message}
        </p>
        
        <div className="text-[11px] text-muted-foreground font-medium tracking-wide uppercase">
          Last updated {ticket.updatedAt ? formatDistanceToNow(new Date(ticket.updatedAt), { addSuffix: true }) : 'just now'}
        </div>
      </div>
      
      <div className="h-full flex items-center pt-6 opacity-0 group-hover:opacity-100 transition-opacity">
        <ChevronRight className="h-5 w-5 text-muted-foreground" />
      </div>
    </button>
  );
}

function StatusBadge({ status }: { status: string }) {
  let style = "text-muted-foreground border-border";
  let label = status;

  switch (status) {
    case "NEW":
    case "ANALYZING":
    case "ANALYZED":
      style = "text-blue-400 border-blue-400/30 bg-blue-400/10";
      label = "Submitted";
      break;
    case "ASSIGNED":
    case "IN_PROGRESS":
      style = "text-amber-400 border-amber-400/30 bg-amber-400/10";
      label = "In Progress";
      break;
    case "RESOLVED":
      style = "text-emerald-400 border-emerald-400/30 bg-emerald-400/10";
      label = "Resolved";
      break;
    case "CLOSED":
      style = "text-muted-foreground border-zinc-400/30 bg-zinc-400/10";
      label = "Closed";
      break;
  }

  return (
    <Badge variant="outline" className={`text-[10px] uppercase font-semibold ${style}`}>
      {label}
    </Badge>
  );
}
