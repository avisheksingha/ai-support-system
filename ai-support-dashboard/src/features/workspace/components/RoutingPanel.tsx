import { ArrowRight, AlertTriangle } from "lucide-react";
import type { RoutingModel } from "@/shared/types/workspace";
import type { TicketModel } from "@/shared/types/ticket";

interface RoutingPanelProps {
  routing: RoutingModel;
  ticket: TicketModel;
}

export function RoutingPanel({ routing, ticket }: RoutingPanelProps) {
  const isEscalated = ticket.priority === 'CRITICAL' || ticket.priority === 'HIGH';
  
  return (
    <div className="text-xs space-y-3">
      {/* Escalation Badge - Prominent at top */}
      {isEscalated && (
        <div className="flex items-center justify-between bg-amber-50 rounded-lg p-2.5 border border-amber-200">
          <span className="text-[9px] font-bold uppercase text-amber-700">Priority Status</span>
          <div className="flex items-center gap-1 px-2 py-0.5 bg-amber-100 border border-amber-300 rounded-md">
            <AlertTriangle className="h-3 w-3 text-amber-600" />
            <span className="text-[10px] font-bold text-amber-700 uppercase">Escalated</span>
          </div>
        </div>
      )}

      {/* Assignment Info - Graceful Field Hiding */}
      <div className="flex flex-wrap gap-2">
        {/* Support Domain - Always Present */}
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100 flex-1 min-w-[110px]">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Support Domain</span>
          <div className="font-semibold text-slate-800 text-[11px] truncate">
            {routing.assignedTeam || "Account & Access Support"}
          </div>
        </div>

        {/* Assigned Team - Shown if Present */}
        {routing.assignedTeam && (
          <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100 flex-1 min-w-[110px]">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Assigned Team</span>
            <div className="font-semibold text-cyan-800 text-[11px] truncate" title={`${routing.assignedTeam} Team`}>
              {routing.assignedTeam} Team
            </div>
          </div>
        )}

        {/* Assigned Agent - Shown Only If Assigned */}
        {ticket.assignedTo && ticket.assignedTo !== "Unassigned" && (
          <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100 flex-1 min-w-[110px]">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Assigned Agent</span>
            <div className="font-semibold text-slate-800 text-[11px] truncate" title={ticket.assignedTo}>
              {ticket.assignedTo}
            </div>
          </div>
        )}
      </div>

      {/* Priority and SLA */}
      <div className="grid grid-cols-2 gap-2">
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Priority</span>
          <div className={`font-semibold font-mono text-[10px] ${
            ticket.priority === 'CRITICAL' ? 'text-red-600' :
            ticket.priority === 'HIGH' ? 'text-orange-600' :
            ticket.priority === 'MEDIUM' ? 'text-amber-600' :
            'text-blue-600'
          }`}>
            {routing.priority}
          </div>
        </div>
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">SLA</span>
          <div className="font-bold text-emerald-600">
            {routing.slaHours}h
          </div>
        </div>
      </div>

      {/* Routing Reason */}
      <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
        <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Routing Reason</span>
        <div className="font-medium text-slate-700 flex items-center gap-1.5">
          <ArrowRight className="h-3 w-3 text-cyan-500" />
          AI-based routing based on intent and urgency analysis
        </div>
      </div>

      {/* Matched Rule */}
      <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
        <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Matched Rule</span>
        <div className="font-mono text-[10px] text-slate-600">
          {routing.assignedTeam.toLowerCase()}_routing_rule
        </div>
      </div>
    </div>
  );
}
