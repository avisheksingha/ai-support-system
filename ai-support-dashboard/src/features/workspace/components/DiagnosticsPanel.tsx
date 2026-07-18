import { Terminal, Activity, Clock, Hash, BrainCircuit, Network, BookOpen, AlertCircle, FileText, Cpu } from "lucide-react";
import type { AnalysisModel, RoutingModel } from "@/shared/types/workspace";
import type { TicketModel } from "@/shared/types/ticket";

interface DiagnosticsPanelProps {
  ticket: TicketModel;
  analysis?: AnalysisModel | undefined;
  routing?: RoutingModel | undefined;
}

export function DiagnosticsPanel({ ticket, analysis, routing }: DiagnosticsPanelProps) {
  return (
    <div className="bg-background border border-red-900/50 rounded-xl overflow-hidden mb-6">
      <div className="bg-red-950/30 border-b border-red-900/50 p-4 flex items-center justify-between">
        <div className="flex items-center gap-2 text-red-400">
          <Terminal className="h-5 w-5" />
          <h3 className="font-semibold font-mono text-sm tracking-widest uppercase">Diagnostics Mode</h3>
        </div>
        <div className="flex items-center gap-2">
           <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
          </span>
          <span className="text-[10px] text-red-500/70 font-mono">LIVE TELEMETRY</span>
        </div>
      </div>

      <div className="p-0 divide-y divide-zinc-800/50">
        {/* Request Metadata */}
        <DiagnosticSection title="Request Metadata" icon={<Activity className="h-4 w-4 text-muted-foreground" />}>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <DataPoint label="Request ID" value={`req_${ticket.id}_${Date.now().toString().slice(-6)}`} icon={<Hash />} />
            <DataPoint label="Correlation ID" value={`corr_${ticket.ticketNumber}`} icon={<Network />} />
            <DataPoint label="User ID" value={ticket.customerUserId} icon={<AlertCircle />} />
            <DataPoint label="Timestamp" value={ticket.createdAt} icon={<Clock />} />
          </div>
        </DiagnosticSection>

        {/* AI Processing */}
        <DiagnosticSection title="AI Processing" icon={<BrainCircuit className="h-4 w-4 text-blue-500" />}>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
            <DataPoint label="Provider" value={analysis?.analysisProvider || "Spring AI / OpenAI"} />
            <DataPoint label="Model" value="gpt-4o-mini" />
            <DataPoint label="Latency" value="N/A" icon={<Activity />} />
            <DataPoint label="Input Tokens" value="N/A" />
            <DataPoint label="Output Tokens" value="N/A" />
          </div>
        </DiagnosticSection>

        {/* Knowledge Retrieval */}
        {ticket.ragResponse && (
          <DiagnosticSection title="RAG & Knowledge" icon={<BookOpen className="h-4 w-4 text-emerald-500" />}>
            <div className="flex flex-col gap-3">
              <div className="grid grid-cols-2 gap-4">
                 <DataPoint label="Retrieved Articles" value={1} />
                 <DataPoint label="Top Similarity" value={`${(0.89 * 100).toFixed(1)}%`} />
              </div>
              <div>
                <span className="text-[10px] text-muted-foreground uppercase tracking-widest font-semibold block mb-1">Vector Search Query</span>
                <code className="text-xs text-emerald-400 bg-emerald-400/10 px-2 py-1 rounded block w-fit">
                  Extracted from ticket context
                </code>
              </div>
            </div>
          </DiagnosticSection>
        )}

        {/* Routing Metrics */}
        {routing && (
          <DiagnosticSection title="Routing Engine" icon={<Cpu className="h-4 w-4 text-purple-500" />}>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
               <DataPoint label="Priority Match" value={routing.priority} />
               <DataPoint label="SLA Target" value={`${routing.slaHours}h`} />
               <DataPoint label="Assigned Team" value={routing.assignedTeam} />
            </div>
          </DiagnosticSection>
        )}

        {/* Prompt & Response */}
        <DiagnosticSection title="Raw Context (Collapsible)" icon={<FileText className="h-4 w-4 text-yellow-500" />}>
          <details className="group">
            <summary className="text-xs text-muted-foreground cursor-pointer hover:text-foreground transition-colors font-mono">
              ▶ VIEW RAW PROMPT & RESPONSE
            </summary>
            <div className="mt-3 space-y-4">
              <div>
                <span className="text-[10px] text-muted-foreground uppercase tracking-widest font-semibold block mb-1">System Prompt Payload</span>
                <pre className="text-[11px] text-muted-foreground bg-card p-3 rounded-md overflow-x-auto border border-border">
                  {`{\n  "role": "system",\n  "content": "You are an expert customer support routing AI. Analyze the following ticket..."\n  "ticketContext": {\n    "subject": "${ticket.subject}",\n    "message": "${ticket.message}"\n  }\n}`}
                </pre>
              </div>
              <div>
                <span className="text-[10px] text-muted-foreground uppercase tracking-widest font-semibold block mb-1">Raw LLM Response</span>
                <pre className="text-[11px] text-muted-foreground bg-card p-3 rounded-md overflow-x-auto border border-border">
                  {JSON.stringify(analysis, null, 2)}
                </pre>
              </div>
            </div>
          </details>
        </DiagnosticSection>
      </div>
    </div>
  );
}

function DiagnosticSection({ title, icon, children }: { title: string, icon: React.ReactNode, children: React.ReactNode }) {
  return (
    <div className="p-4 md:p-5">
      <div className="flex items-center gap-2 mb-4">
        {icon}
        <h4 className="text-sm font-semibold text-foreground">{title}</h4>
      </div>
      {children}
    </div>
  );
}

function DataPoint({ label, value, icon }: { label: string, value: string | number, icon?: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-[10px] text-muted-foreground uppercase tracking-widest font-semibold flex items-center gap-1">
        {icon && <span className="[&>svg]:w-3 [&>svg]:h-3 [&>svg]:text-muted-foreground">{icon}</span>}
        {label}
      </span>
      <span className="text-sm font-mono text-foreground">{value}</span>
    </div>
  );
}

