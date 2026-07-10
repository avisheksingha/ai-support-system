import { Network, ShieldAlert, Cpu } from "lucide-react";
import type { RoutingModel } from "@/shared/types/workspace";
import type { TicketModel } from "@/shared/types/ticket";
import { Badge } from "@/components/ui/badge";

interface RoutingPanelProps {
  routing: RoutingModel;
  ticket: TicketModel;
}

export function RoutingPanel({ routing, ticket }: RoutingPanelProps) {
  const assignee = ticket.assignedTo || routing.assignedAgent;
  
  return (
    <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg overflow-hidden">
      <div className="bg-card border-b border-border p-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Network className="h-5 w-5 text-[#0C66E4]" />
          <h3 className="font-semibold text-foreground">Routing</h3>
        </div>
      </div>

      <div className="p-4 space-y-4 text-sm">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1 block">Assigned Team</span>
            <div className="font-medium text-foreground">{routing.department}</div>
          </div>
          <div>
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1 block">Suggested Agent</span>
            <div className="font-medium text-foreground">{assignee || "Any"}</div>
          </div>
        </div>

        <div>
          <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1 block">Matched Rule</span>
          <div className="font-medium text-foreground font-mono text-[11px] bg-muted inline-flex px-1.5 py-0.5 rounded border border-border">
            {routing.ruleName || "GENERAL_ROUTING_RULE"}
          </div>
        </div>

        <div>
          <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1 block">Confidence</span>
          <div className="font-medium text-emerald-600">
            {(routing.confidenceScore * 100).toFixed(0)}%
          </div>
        </div>

        <div>
          <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1 block">Reason</span>
          <div className="text-foreground/80 leading-relaxed text-[13px]">
            {routing.reason}
          </div>
        </div>
      </div>
    </div>
  );
}
