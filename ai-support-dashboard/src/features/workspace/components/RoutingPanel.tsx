import { Network } from "lucide-react";
import type { RoutingModel } from "@/shared/types/workspace";
import type { TicketModel } from "@/shared/types/ticket";

interface RoutingPanelProps {
  routing: RoutingModel;
  ticket: TicketModel;
}

export function RoutingPanel({ routing, ticket }: RoutingPanelProps) {
  const assignee = ticket.assignedTo || "Unassigned";
  
  return (
    <div className="bg-white shadow-sm border border-slate-200 rounded-xl overflow-hidden relative">
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-cyan-400 to-blue-500"></div>
      <div className="bg-slate-50 border-b border-slate-100 p-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="bg-white p-1.5 rounded-md shadow-sm border border-slate-100">
            <Network className="h-4 w-4 text-cyan-600" />
          </div>
          <h3 className="font-bold text-slate-800 text-sm">Smart Routing</h3>
        </div>
      </div>

      <div className="p-5 space-y-4 text-sm">
        <div className="grid grid-cols-2 gap-4">
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Assigned Team</span>
            <div className="font-semibold text-slate-800 flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-cyan-500"></span>
              {routing.assignedTeam}
            </div>
          </div>
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Assigned To</span>
            <div className="font-semibold text-slate-800">{assignee}</div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Priority</span>
            <div className="font-semibold text-cyan-700 font-mono text-[10px]">
              {routing.priority}
            </div>
          </div>
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">SLA Hours</span>
            <div className="font-bold text-emerald-600">
              {routing.slaHours}h
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
