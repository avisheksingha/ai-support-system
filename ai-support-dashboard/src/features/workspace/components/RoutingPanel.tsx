import { Network } from "lucide-react";
import type { RoutingModel } from "@/shared/types/workspace";
import type { TicketModel } from "@/shared/types/ticket";

interface RoutingPanelProps {
  routing: RoutingModel;
  ticket: TicketModel;
}

export function RoutingPanel({ routing, ticket }: RoutingPanelProps) {
  const assignee = ticket.assignedTo || routing.assignedAgent;
  
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
              {routing.department}
            </div>
          </div>
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Suggested Agent</span>
            <div className="font-semibold text-slate-800">{assignee || "Any"}</div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Matched Rule</span>
            <div className="font-semibold text-cyan-700 font-mono text-[10px]">
              {routing.ruleName || "GENERAL_ROUTING_RULE"}
            </div>
          </div>
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1 block">Confidence</span>
            <div className="font-bold text-emerald-600">
              {(routing.confidenceScore * 100).toFixed(0)}%
            </div>
          </div>
        </div>

        <div className="pt-2 border-t border-slate-100 mt-2">
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2 block">Routing Factors</span>
          <div className="flex flex-wrap gap-2 mb-3">
            <span className="text-[10px] px-2 py-1 bg-cyan-50 text-cyan-700 border border-cyan-100 rounded-full font-semibold flex items-center gap-1">
              ✓ Skill Match
            </span>
            <span className="text-[10px] px-2 py-1 bg-cyan-50 text-cyan-700 border border-cyan-100 rounded-full font-semibold flex items-center gap-1">
              ✓ Availability
            </span>
            <span className="text-[10px] px-2 py-1 bg-cyan-50 text-cyan-700 border border-cyan-100 rounded-full font-semibold flex items-center gap-1">
              ✓ Low Workload
            </span>
            <span className="text-[10px] px-2 py-1 bg-cyan-50 text-cyan-700 border border-cyan-100 rounded-full font-semibold flex items-center gap-1">
              ✓ High Confidence
            </span>
          </div>
          <div className="text-slate-600 leading-relaxed text-[13px] bg-slate-50 p-3 rounded-lg border border-slate-100">
            {routing.reason}
          </div>
        </div>
      </div>
    </div>
  );
}
