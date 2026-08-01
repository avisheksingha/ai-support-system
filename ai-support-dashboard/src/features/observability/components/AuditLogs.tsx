import { useAuditLogs } from "../hooks/useGovernance";
import { formatTimeAgo } from "@/shared/utils/date";
import { Loader2, ShieldCheck, CheckCircle2, XCircle, AlertCircle, Clock, Fingerprint, Cpu, User, Server } from "lucide-react";
import { Badge } from "@/components/ui/badge";

export function AuditLogs() {
  const { data: logs, isLoading } = useAuditLogs();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64 bg-white rounded-xl shadow-sm border border-slate-200">
        <Loader2 className="w-8 h-8 animate-spin text-slate-300" />
      </div>
    );
  }

  if (!logs || logs.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 bg-white rounded-xl shadow-sm border border-slate-200">
        <ShieldCheck className="w-12 h-12 text-emerald-400 mb-3" />
        <h3 className="text-lg font-medium text-slate-900">No Audit Logs</h3>
        <p className="text-sm text-slate-500">No governance policy evaluations found.</p>
      </div>
    );
  }

  const getDecisionBadge = (decision: string) => {
    switch (decision) {
      case "ALLOWED":
        return <Badge variant="outline" className="bg-emerald-50 text-emerald-700 border-emerald-200"><CheckCircle2 className="w-3 h-3 mr-1" /> ALLOWED</Badge>;
      case "BLOCKED":
        return <Badge variant="outline" className="bg-rose-50 text-rose-700 border-rose-200"><XCircle className="w-3 h-3 mr-1" /> BLOCKED</Badge>;
      case "APPROVAL_REQUIRED":
        return <Badge variant="outline" className="bg-amber-50 text-amber-700 border-amber-200"><AlertCircle className="w-3 h-3 mr-1" /> APPROVAL REQUIRED</Badge>;
      default:
        return <Badge variant="outline" className="bg-slate-50 text-slate-700 border-slate-200">{decision}</Badge>;
    }
  };

  const getActorIcon = (actor: string) => {
    switch (actor.toUpperCase()) {
      case "AI":
        return <Cpu className="w-4 h-4 text-purple-500" />;
      case "AGENT":
        return <User className="w-4 h-4 text-blue-500" />;
      case "SYSTEM":
      default:
        return <Server className="w-4 h-4 text-slate-500" />;
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
      <div className="p-4 border-b border-slate-200 bg-slate-50 flex items-center justify-between">
        <div>
          <h3 className="text-base font-semibold text-slate-900 flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-emerald-500" />
            Policy Execution Timeline
          </h3>
          <p className="text-sm text-slate-500 mt-1">
            Chronological log of all guardrail and policy evaluations across workflows.
          </p>
        </div>
      </div>
      
      <div className="p-6 relative">
        <div className="absolute top-0 bottom-0 left-10 w-px bg-slate-200"></div>
        <div className="space-y-8 relative">
          {logs.map((log) => (
            <div key={log.id} className="flex gap-6 relative group">
              {/* Timeline marker */}
              <div className="w-8 h-8 rounded-full bg-white border-2 border-slate-200 flex items-center justify-center shrink-0 z-10 group-hover:border-blue-400 transition-colors">
                {getActorIcon(log.actor)}
              </div>
              
              {/* Log content */}
              <div className="flex-1 bg-white border border-slate-100 rounded-lg p-4 shadow-sm group-hover:border-slate-300 transition-colors">
                <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
                  
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-slate-900">{log.policyEvaluated}</span>
                      {getDecisionBadge(log.decision)}
                    </div>
                    
                    <div className="flex flex-wrap items-center gap-4 text-xs font-mono text-slate-500">
                      <div className="flex items-center gap-1.5" title="Workflow ID">
                        <Fingerprint className="w-3.5 h-3.5" />
                        {log.workflowId}
                      </div>
                      <div className="flex items-center gap-1.5" title="Evaluation Duration">
                        <Clock className="w-3.5 h-3.5" />
                        {log.durationMs}ms
                      </div>
                      <div className="flex items-center gap-1.5 uppercase font-sans font-semibold tracking-wider bg-slate-100 px-2 py-0.5 rounded text-slate-600">
                        {log.actor}
                      </div>
                    </div>
                  </div>

                  <div className="text-right shrink-0">
                    <div className="text-sm font-medium text-slate-700">
                      {new Date(log.timestamp).toLocaleTimeString()}
                    </div>
                    <div className="text-xs text-slate-400 mt-0.5">
                      {formatTimeAgo(log.timestamp)}
                    </div>
                    <div className="text-[10px] text-slate-300 font-mono mt-1">
                      {log.id}
                    </div>
                  </div>
                  
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
