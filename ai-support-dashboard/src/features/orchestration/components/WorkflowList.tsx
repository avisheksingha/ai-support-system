import { Search, Filter, Play, CheckCircle2, AlertCircle, RefreshCw, Clock, X } from "lucide-react";
import { useState } from "react";
import { formatTimeAgo, formatDuration } from "@/shared/utils/date";
import { useWorkflows } from "../hooks/useWorkflows";
import { Skeleton } from "@/components/ui/skeleton";

export function WorkflowList({ selectedId, onSelect }: { selectedId: string | null; onSelect: (id: string) => void }) {
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [showFilters, setShowFilters] = useState(false);
  const { data: workflows, isLoading, error } = useWorkflows();

  const filtered = workflows?.filter((ex) => {
    const matchesSearch =
      !searchTerm ||
      ex.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      ex.entityId.toLowerCase().includes(searchTerm.toLowerCase()) ||
      ex.triggerEvent.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === "ALL" || ex.status.toUpperCase() === statusFilter;

    return matchesSearch && matchesStatus;
  }) || [];

  if (isLoading) {
    return (
      <div className="flex flex-col h-full overflow-hidden bg-white">
        <div className="shrink-0 bg-slate-50/50">
          <div className="h-16 flex items-center px-6 border-b border-slate-200 justify-between">
            <span className="font-bold text-sm tracking-wide text-slate-800 uppercase flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-purple-500"></span>
              Orchestration Log
            </span>
          </div>
          <div className="p-4 border-b border-slate-200 bg-white">
            <Skeleton className="h-9 w-full" />
          </div>
        </div>
        
        <div className="flex-1 overflow-y-auto p-3 space-y-2 bg-slate-50/30">
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="w-full p-4 rounded-xl border border-slate-200 bg-white">
              <Skeleton className="h-4 w-24 mb-2" />
              <Skeleton className="h-5 w-full mb-2" />
              <Skeleton className="h-4 w-32 mb-3" />
              <Skeleton className="h-6 w-24" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col h-full overflow-hidden bg-white">
        <div className="shrink-0 bg-slate-50/50">
          <div className="h-16 flex items-center px-6 border-b border-slate-200 justify-between">
            <span className="font-bold text-sm tracking-wide text-slate-800 uppercase flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-purple-500"></span>
              Orchestration Log
            </span>
          </div>
        </div>
        <div className="flex-1 flex items-center justify-center p-8 text-center">
          <div className="text-red-500 text-sm">Failed to load workflows</div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full overflow-hidden bg-white">
      <div className="shrink-0 bg-slate-50/50">
        <div className="h-16 flex items-center px-6 border-b border-slate-200 justify-between">
          <span className="font-bold text-sm tracking-wide text-slate-800 uppercase flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-purple-500"></span>
            Orchestration Log
          </span>
          <button
            onClick={() => setShowFilters(!showFilters)}
            className={`p-1.5 rounded-lg border transition-colors ${
              showFilters || statusFilter !== "ALL"
                ? "bg-purple-50 border-purple-200 text-purple-700"
                : "text-slate-400 hover:text-slate-700 border-slate-200"
            }`}
            title="Toggle Operational Filters"
          >
            <Filter className="h-4 w-4" />
          </button>
        </div>

        <div className="p-4 border-b border-slate-200 bg-white space-y-3">
          <div className="relative">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
            <input
              placeholder="Search by Trace ID, Ticket ID, or Event..."
              className="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 text-sm rounded-md text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-purple-500/50 shadow-sm transition-all"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          {showFilters && (
            <div className="pt-2 flex items-center gap-2 flex-wrap border-t border-slate-100 animate-in fade-in slide-in-from-top-1">
              <span className="text-[10px] font-bold uppercase text-slate-400">Status:</span>
              {["ALL", "COMPLETED", "FAILED", "RUNNING", "RETRYING"].map((st) => (
                <button
                  key={st}
                  onClick={() => setStatusFilter(st)}
                  className={`text-[10px] font-bold px-2 py-0.5 rounded border transition-all ${
                    statusFilter === st
                      ? "bg-purple-600 border-purple-600 text-white shadow-xs"
                      : "bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100"
                  }`}
                >
                  {st}
                </button>
              ))}
              {statusFilter !== "ALL" && (
                <button
                  onClick={() => setStatusFilter("ALL")}
                  className="text-[10px] text-slate-400 hover:text-red-500 flex items-center gap-0.5 ml-auto"
                >
                  <X className="h-3 w-3" /> Reset
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-3 space-y-2 bg-slate-50/30">
        {filtered.length === 0 && (
          <div className="text-slate-400 text-center text-xs py-8">
            No workflow executions match the criteria.
          </div>
        )}

        {filtered.map((ex) => {
          const isSelected = selectedId === ex.id;
          const shortId = ex.id.length > 18 ? ex.id.substring(0, 18) + "..." : ex.id;

          return (
            <button
              key={ex.id}
              onClick={() => onSelect(ex.id)}
              className={`w-full text-left p-4 rounded-xl border transition-all duration-200 ease-in-out group ${
                isSelected
                  ? "bg-purple-50/90 border-purple-200 ring-1 ring-purple-500/20 shadow-sm"
                  : "bg-white border-slate-200 hover:border-slate-300 hover:shadow-sm"
              }`}
            >
              <div className="flex items-center justify-between mb-2">
                <span
                  className={`text-[10px] font-mono font-bold tracking-widest ${
                    isSelected ? "text-purple-700" : "text-slate-500"
                  }`}
                  title={ex.id}
                >
                  {shortId}
                </span>
                <span className="text-[10px] font-medium text-slate-400">
                  {formatTimeAgo(ex.startedAt)}
                </span>
              </div>

              <h3
                className={`text-sm font-bold line-clamp-1 mb-2 ${
                  isSelected ? "text-purple-950" : "text-slate-800 group-hover:text-slate-900"
                }`}
              >
                {ex.triggerEvent}
              </h3>

              <div className="flex items-center justify-between text-xs font-medium text-slate-500 mb-3">
                <span className="flex items-center gap-1.5 bg-slate-100 px-2 py-0.5 rounded border border-slate-200 text-slate-700 font-mono text-[11px]">
                  {ex.entityId}
                </span>
                <span className="flex items-center gap-1 font-mono text-slate-600 text-xs">
                  {formatDuration(ex.durationMs)}
                </span>
              </div>

              <div className="flex items-center justify-between pt-2.5 border-t border-slate-100/80">
                {getStatusBadge(ex.status)}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}

function getStatusBadge(status: string) {
  const upperStatus = status?.toUpperCase() || "COMPLETED";
  switch (upperStatus) {
    case "COMPLETED":
    case "SUCCESS":
      return (
        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded uppercase">
          <CheckCircle2 className="h-3 w-3 text-emerald-600" /> Completed
        </span>
      );
    case "FAILED":
      return (
        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-red-700 bg-red-50 border border-red-200 px-2 py-0.5 rounded uppercase">
          <AlertCircle className="h-3 w-3 text-red-600" /> Failed
        </span>
      );
    case "RUNNING":
    case "IN_PROGRESS":
      return (
        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-blue-700 bg-blue-50 border border-blue-200 px-2 py-0.5 rounded uppercase">
          <Play className="h-3 w-3 text-blue-600" /> Running
        </span>
      );
    case "RETRYING":
    case "RECOVERED":
      return (
        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-orange-700 bg-orange-50 border border-orange-200 px-2 py-0.5 rounded uppercase">
          <RefreshCw className="h-3 w-3 text-orange-600 animate-spin" /> Retrying
        </span>
      );
    case "WAITING":
      return (
        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-amber-700 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded uppercase">
          <Clock className="h-3 w-3 text-amber-600" /> Waiting
        </span>
      );
    case "SKIPPED":
    default:
      return (
        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-slate-600 bg-slate-100 border border-slate-200 px-2 py-0.5 rounded uppercase">
          Skipped
        </span>
      );
  }
}
