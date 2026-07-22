import { useState } from "react";
import { ChevronDown, ChevronUp, CheckCircle2, AlertCircle, Clock, Play, RefreshCw, ArrowRight, Activity } from "lucide-react";
import { formatDuration } from "@/shared/utils/date";

export interface ExecutionGraphNode {
  id: string;
  name: string;
  type: string;
  status: "SUCCESS" | "FAILED" | "SKIPPED" | "RETRYING" | "RUNNING" | "WAITING";
  durationMs: number;
  percentage: number;
  category?: "BUSINESS" | "TECHNICAL";
}

interface ExecutionGraphProps {
  nodes: ExecutionGraphNode[];
  totalDurationMs?: number;
}

export function ExecutionGraph({ nodes, totalDurationMs: _totalDurationMs }: ExecutionGraphProps) {
  const [isOpen, setIsOpen] = useState(true);

  if (!nodes || nodes.length === 0) return null;

  return (
    <div className="w-full bg-white border border-slate-200 rounded-xl shadow-sm mb-6 overflow-hidden transition-all">
      {/* Header Bar */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="w-full px-5 py-3.5 bg-slate-50/70 border-b border-slate-200/80 flex items-center justify-between hover:bg-slate-100/60 transition-colors text-left"
      >
        <div className="flex items-center gap-2.5">
          <Activity className="h-4 w-4 text-purple-600" />
          <span className="text-xs font-bold text-slate-800 uppercase tracking-widest">
            Execution Dependency Graph
          </span>
          <span className="text-[11px] font-medium text-slate-500 bg-slate-200/60 px-2 py-0.5 rounded-full">
            {nodes.length} stages
          </span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-[11px] text-slate-400 font-mono">Data-Driven Flow</span>
          {isOpen ? (
            <ChevronUp className="h-4 w-4 text-slate-400" />
          ) : (
            <ChevronDown className="h-4 w-4 text-slate-400" />
          )}
        </div>
      </button>

      {/* Graph Node Flow Container */}
      {isOpen && (
        <div className="p-5 overflow-x-auto bg-gradient-to-r from-slate-50/30 via-white to-slate-50/30">
          <div className="flex items-center gap-3 min-w-max pb-2">
            {/* Start Node */}
            <div className="flex items-center gap-2">
              <div className="flex flex-col items-center justify-center p-3 rounded-lg border border-purple-200 bg-purple-50/60 text-purple-900 shadow-xs min-w-[110px]">
                <span className="text-[10px] font-bold uppercase tracking-wider text-purple-600 mb-0.5">Trigger</span>
                <span className="text-xs font-bold font-mono">Workflow Start</span>
              </div>
              <ArrowRight className="h-4 w-4 text-slate-300 shrink-0" />
            </div>

            {/* Dynamic Stage Nodes */}
            {nodes.map((node, index) => {
              const isLast = index === nodes.length - 1;
              const statusStyle = getNodeStatusStyle(node.status);
              
              return (
                <div key={node.id || index} className="flex items-center gap-3">
                  <div
                    className={`flex flex-col p-3 rounded-lg border shadow-xs min-w-[130px] max-w-[180px] transition-all hover:shadow-md ${statusStyle.bg} ${statusStyle.border} ${statusStyle.text}`}
                  >
                    <div className="flex items-center justify-between gap-1 mb-1">
                      <span className="text-[9px] font-bold uppercase tracking-wider opacity-75 truncate">
                        {node.type.replace("_", " ")}
                      </span>
                      {statusStyle.icon}
                    </div>

                    <h4 className="text-xs font-bold truncate mb-2 leading-tight" title={node.name}>
                      {node.name}
                    </h4>

                    <div className="flex items-center justify-between pt-1.5 border-t border-current/10 text-[10px] font-mono font-medium">
                      <span>{formatDuration(node.durationMs)}</span>
                      {node.percentage > 0 && (
                        <span className="opacity-80 bg-white/50 px-1 rounded">
                          {node.percentage}%
                        </span>
                      )}
                    </div>
                  </div>

                  {!isLast && <ArrowRight className="h-4 w-4 text-slate-300 shrink-0" />}
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

function getNodeStatusStyle(status: string) {
  const upper = status?.toUpperCase() || "COMPLETED";
  switch (upper) {
    case "SUCCESS":
    case "COMPLETED":
      return {
        bg: "bg-emerald-50/80",
        border: "border-emerald-200",
        text: "text-emerald-900",
        icon: <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600 shrink-0" />,
      };
    case "FAILED":
      return {
        bg: "bg-red-50/80",
        border: "border-red-200",
        text: "text-red-900",
        icon: <AlertCircle className="h-3.5 w-3.5 text-red-600 shrink-0" />,
      };
    case "RUNNING":
    case "IN_PROGRESS":
      return {
        bg: "bg-blue-50/80",
        border: "border-blue-200",
        text: "text-blue-900",
        icon: <Play className="h-3.5 w-3.5 text-blue-600 shrink-0" />,
      };
    case "RETRYING":
      return {
        bg: "bg-orange-50/80",
        border: "border-orange-200",
        text: "text-orange-900",
        icon: <RefreshCw className="h-3.5 w-3.5 text-orange-600 shrink-0 animate-spin" />,
      };
    case "WAITING":
      return {
        bg: "bg-amber-50/80",
        border: "border-amber-200",
        text: "text-amber-900",
        icon: <Clock className="h-3.5 w-3.5 text-amber-600 shrink-0" />,
      };
    case "SKIPPED":
    default:
      return {
        bg: "bg-slate-50/80",
        border: "border-slate-200",
        text: "text-slate-500 opacity-65",
        icon: <span className="text-[9px] font-bold text-slate-400">SKIPPED</span>,
      };
  }
}
