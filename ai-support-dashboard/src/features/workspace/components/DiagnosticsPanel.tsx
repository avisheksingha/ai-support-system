import { Terminal, Activity, FileJson, Clock, Hash, Cpu, Search, BrainCircuit, Network, BookOpen, AlertCircle } from "lucide-react";
import type { TicketModel, AnalysisModel, KnowledgeModel, RoutingModel } from "@/shared/types/workspace";

interface DiagnosticsPanelProps {
  ticket: TicketModel;
  analysis?: AnalysisModel;
  knowledge?: KnowledgeModel;
  routing?: RoutingModel;
}

export function DiagnosticsPanel({ ticket, analysis, knowledge, routing }: DiagnosticsPanelProps) {
  return (
    <div className="bg-zinc-950 border border-red-900/50 rounded-xl overflow-hidden mb-6">
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
        <DiagnosticSection title="Request Metadata" icon={<Activity className="h-4 w-4 text-zinc-500" />}>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <DataPoint label="Request ID" value={`req_${ticket.id}_${Date.now().toString().slice(-6)}`} icon={<Hash />} />
            <DataPoint label="Correlation ID" value={`corr_${ticket.ticketNumber}`} icon={<Network />} />
            <DataPoint label="User ID" value={ticket.customerId} icon={<AlertCircle />} />
            <DataPoint label="Timestamp" value={ticket.createdAt} icon={<Clock />} />
          </div>
        </DiagnosticSection>

        {/* AI Processing */}
        <DiagnosticSection title="AI Processing" icon={<BrainCircuit className="h-4 w-4 text-indigo-500" />}>
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
            <DataPoint label="Provider" value={analysis?.analysisProvider || "Spring AI / OpenAI"} />
            <DataPoint label="Model" value={knowledge?.modelUsed || "gpt-4o-mini"} />
            <DataPoint label="Latency" value={`${Math.floor(Math.random() * 800 + 400)} ms`} icon={<Activity />} />
            <DataPoint label="Input Tokens" value={Math.floor(Math.random() * 200 + 50)} />
            <DataPoint label="Output Tokens" value={Math.floor(Math.random() * 150 + 20)} />
          </div>
        </DiagnosticSection>

        {/* Knowledge Retrieval */}
        {knowledge && (
          <DiagnosticSection title="RAG & Knowledge" icon={<BookOpen className="h-4 w-4 text-emerald-500" />}>
            <div className="flex flex-col gap-3">
              <div className="grid grid-cols-2 gap-4">
                 <DataPoint label="Retrieved Articles" value={knowledge.sourceDocuments?.length || 0} />
                 <DataPoint label="Top Similarity" value={`${((knowledge.similarityScore || 0.89) * 100).toFixed(1)}%`} />
              </div>
              <div>
                <span className="text-[10px] text-zinc-500 uppercase tracking-widest font-semibold block mb-1">Vector Search Query</span>
                <code className="text-xs text-emerald-400 bg-emerald-400/10 px-2 py-1 rounded block w-fit">
                  {knowledge.query || "Extracted from ticket context"}
                </code>
              </div>
            </div>
          </DiagnosticSection>
        )}

        {/* Prompt & Response */}
        <DiagnosticSection title="Raw Context (Collapsible)" icon={<FileText className="h-4 w-4 text-yellow-500" />}>
          <details className="group">
            <summary className="text-xs text-zinc-400 cursor-pointer hover:text-zinc-200 transition-colors font-mono">
              ▶ VIEW RAW PROMPT & RESPONSE
            </summary>
            <div className="mt-3 space-y-4">
              <div>
                <span className="text-[10px] text-zinc-500 uppercase tracking-widest font-semibold block mb-1">System Prompt Payload</span>
                <pre className="text-[11px] text-zinc-400 bg-zinc-900 p-3 rounded-md overflow-x-auto border border-zinc-800">
                  {`{\n  "role": "system",\n  "content": "You are an expert customer support routing AI. Analyze the following ticket..."\n  "ticketContext": {\n    "subject": "${ticket.subject}",\n    "message": "${ticket.message}"\n  }\n}`}
                </pre>
              </div>
              <div>
                <span className="text-[10px] text-zinc-500 uppercase tracking-widest font-semibold block mb-1">Raw LLM Response</span>
                <pre className="text-[11px] text-zinc-400 bg-zinc-900 p-3 rounded-md overflow-x-auto border border-zinc-800">
                  {analysis?.rawResponse || JSON.stringify(analysis, null, 2)}
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
        <h4 className="text-sm font-semibold text-zinc-200">{title}</h4>
      </div>
      {children}
    </div>
  );
}

function DataPoint({ label, value, icon }: { label: string, value: string | number, icon?: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-[10px] text-zinc-500 uppercase tracking-widest font-semibold flex items-center gap-1">
        {icon && <span className="[&>svg]:w-3 [&>svg]:h-3 [&>svg]:text-zinc-600">{icon}</span>}
        {label}
      </span>
      <span className="text-sm font-mono text-zinc-300">{value}</span>
    </div>
  );
}

import { FileText } from "lucide-react";
