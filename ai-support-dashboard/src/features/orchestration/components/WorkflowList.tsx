import { Search, Filter, Play, CheckCircle2, AlertCircle, RefreshCw } from "lucide-react";
import { useState } from "react";
import { formatTimeAgo } from "@/shared/utils/date";

export interface WorkflowExecution {
  id: string;
  triggerEvent: string;
  entityId: string;
  status: "COMPLETED" | "FAILED" | "IN_PROGRESS" | "RECOVERED";
  durationMs: number;
  startedAt: Date;
}

const MOCK_EXECUTIONS: WorkflowExecution[] = [
  { id: "wf-exc-9042", triggerEvent: "ticket-created", entityId: "INC-48291", status: "COMPLETED", durationMs: 1240, startedAt: new Date(Date.now() - 1000 * 60 * 2) },
  { id: "wf-exc-9041", triggerEvent: "ticket-updated", entityId: "INC-48290", status: "RECOVERED", durationMs: 3150, startedAt: new Date(Date.now() - 1000 * 60 * 15) },
  { id: "wf-exc-9040", triggerEvent: "agent-reply", entityId: "INC-48285", status: "COMPLETED", durationMs: 850, startedAt: new Date(Date.now() - 1000 * 60 * 45) },
  { id: "wf-exc-9039", triggerEvent: "ticket-created", entityId: "INC-48288", status: "FAILED", durationMs: 420, startedAt: new Date(Date.now() - 1000 * 60 * 60) },
  { id: "wf-exc-9038", triggerEvent: "system-sync", entityId: "SYS-001", status: "IN_PROGRESS", durationMs: 15000, startedAt: new Date(Date.now() - 1000 * 30) },
];

export function WorkflowList({ selectedId, onSelect }: { selectedId: string | null, onSelect: (id: string) => void }) {
  const [searchTerm, setSearchTerm] = useState("");

  const filtered = MOCK_EXECUTIONS.filter(ex => 
    ex.id.includes(searchTerm) || ex.entityId.includes(searchTerm) || ex.triggerEvent.includes(searchTerm)
  );

  return (
    <div className="flex flex-col h-full overflow-hidden bg-white">
      <div className="shrink-0 bg-slate-50/50">
        <div className="h-16 flex items-center px-6 border-b border-slate-200 justify-between">
          <span className="font-bold text-sm tracking-wide text-slate-800 uppercase flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-purple-500"></span>
            Orchestration Log
          </span>
          <button className="text-slate-400 hover:text-slate-700 transition-colors">
            <Filter className="h-4 w-4" />
          </button>
        </div>
        <div className="p-4 border-b border-slate-200 bg-white">
          <div className="relative">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
            <input 
              placeholder="Search by Trace ID or Event..." 
              className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 text-sm rounded-md text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-purple-500/50 shadow-sm transition-all"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
      </div>
      
      <div className="flex-1 overflow-y-auto p-3 space-y-2 bg-slate-50/30">
        {filtered.map((ex) => (
          <button
            key={ex.id}
            onClick={() => onSelect(ex.id)}
            className={`w-full text-left p-4 rounded-xl border transition-all duration-200 ease-in-out group ${
              selectedId === ex.id
                ? "bg-purple-50 border-purple-200 ring-1 ring-purple-500/20 shadow-sm"
                : "bg-white border-slate-200 hover:border-slate-300 hover:shadow-sm"
            }`}
          >
            <div className="flex items-center justify-between mb-2">
              <span className={`text-[10px] font-mono font-bold tracking-widest ${
                selectedId === ex.id ? "text-purple-700" : "text-slate-500"
              }`}>{ex.id}</span>
              <span className="text-[10px] font-medium text-slate-400">{formatTimeAgo(ex.startedAt)}</span>
            </div>
            
            <h3 className={`text-sm font-bold line-clamp-1 mb-2 ${
              selectedId === ex.id ? "text-purple-950" : "text-slate-800 group-hover:text-slate-900"
            }`}>
              {ex.triggerEvent}
            </h3>
            
            <div className="flex items-center gap-4 text-xs font-medium text-slate-500 mb-3">
              <span className="flex items-center gap-1.5 bg-slate-100 px-2 py-0.5 rounded border border-slate-200 text-slate-700">{ex.entityId}</span>
              <span className="flex items-center gap-1 font-mono">{ex.durationMs}ms</span>
            </div>

            <div className="flex items-center gap-1.5 pt-3 border-t border-slate-100/80">
              {getStatusBadge(ex.status)}
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}

function getStatusBadge(status: WorkflowExecution["status"]) {
  switch (status) {
    case "COMPLETED":
      return <span className="inline-flex items-center gap-1 text-[10px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded uppercase"><CheckCircle2 className="h-3 w-3" /> Completed</span>;
    case "FAILED":
      return <span className="inline-flex items-center gap-1 text-[10px] font-bold text-red-700 bg-red-50 border border-red-200 px-2 py-0.5 rounded uppercase"><AlertCircle className="h-3 w-3" /> Failed</span>;
    case "IN_PROGRESS":
      return <span className="inline-flex items-center gap-1 text-[10px] font-bold text-blue-700 bg-blue-50 border border-blue-200 px-2 py-0.5 rounded uppercase"><Play className="h-3 w-3" /> In Progress</span>;
    case "RECOVERED":
      return <span className="inline-flex items-center gap-1 text-[10px] font-bold text-amber-700 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded uppercase"><RefreshCw className="h-3 w-3" /> Recovered</span>;
  }
}
