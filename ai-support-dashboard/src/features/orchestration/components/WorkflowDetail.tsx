import { useState } from "react";
import { formatTimeAgo } from "@/shared/utils/date";
import { CheckCircle2, AlertCircle, Bot, Shield, Wrench, ChevronDown, ChevronUp, Network, Zap, BookOpen, Send, Radio } from "lucide-react";

export interface WorkflowStep {
  id: string;
  type: "BUSINESS_EVENT" | "GUARDRAIL" | "AI_PLANNING" | "TOOL_INVOCATION" | "KNOWLEDGE_RETRIEVAL" | "WORKFLOW_DECISION" | "EVENT_PUBLICATION";
  name: string;
  status: "SUCCESS" | "FAILED" | "SKIPPED" | "RETRYING";
  durationMs: number;
  payload: any;
  summary?: string;
  error?: string;
  timestamp: Date;
}

const MOCK_STEPS: Record<string, WorkflowStep[]> = {
  "wf-exc-9042": [
    { id: "s-1", type: "BUSINESS_EVENT", name: "ticket-created", status: "SUCCESS", durationMs: 4, timestamp: new Date(Date.now() - 1000 * 60 * 2), payload: { source: "ticket-service", entityId: "INC-48291", payload: { subject: "Can't access billing portal" } } },
    { id: "s-2", type: "GUARDRAIL", name: "pii-redaction-policy", status: "SUCCESS", durationMs: 12, timestamp: new Date(Date.now() - 1000 * 60 * 2 + 10), payload: { redactedFields: ["credit_card"], action: "PASS" } },
    { id: "s-3", type: "BUSINESS_EVENT", name: "context-assembly", status: "SUCCESS", durationMs: 15, timestamp: new Date(Date.now() - 1000 * 60 * 2 + 22), payload: { loaded: ["customer_profile", "recent_tickets"] } },
    { id: "s-4", type: "AI_PLANNING", name: "orchestrator-plan", status: "SUCCESS", durationMs: 840, timestamp: new Date(Date.now() - 1000 * 60 * 2 + 37), summary: "Intent: BILLING_ISSUE (92%). Selected workflow: Billing Resolution Flow.", payload: { model: "gemini-2.5-flash", plan: ["analyze-intent", "retrieve-knowledge", "generate-reply", "assign-agent"] } },
    { id: "s-5", type: "TOOL_INVOCATION", name: "ai-analysis-service", status: "SUCCESS", durationMs: 450, timestamp: new Date(Date.now() - 1000 * 60 * 2 + 877), summary: "Analyzed sentiment (FRUSTRATED) and urgency (HIGH)", payload: { intent: "BILLING_ISSUE", urgency: "HIGH", confidence: 0.92 } },
    { id: "s-6", type: "KNOWLEDGE_RETRIEVAL", name: "rag-service", status: "SUCCESS", durationMs: 310, timestamp: new Date(Date.now() - 1000 * 60 * 2 + 1327), summary: "Matched 2 articles. Top match: 'Billing Policy' (Distance: 0.12)", payload: { matchedDocuments: 2, vectorDistance: 0.12, generatedDraft: "Hi, I understand you are having issues with billing..." } },
    { id: "s-7", type: "WORKFLOW_DECISION", name: "routing-evaluation", status: "SUCCESS", durationMs: 45, timestamp: new Date(Date.now() - 1000 * 60 * 2 + 1637), summary: "Assigned to Billing Team due to high confidence intent match.", payload: { assignedTo: "Billing Team", reason: "Intent matched BILLING_ISSUE with high confidence." } },
    { id: "s-8", type: "EVENT_PUBLICATION", name: "workflow-completed", status: "SUCCESS", durationMs: 5, timestamp: new Date(Date.now() - 1000 * 60 * 2 + 1682), payload: { topic: "ai-support.workflow.completed", partition: 2 } }
  ],
  "wf-exc-9041": [
    { id: "s-1", type: "BUSINESS_EVENT", name: "ticket-updated", status: "SUCCESS", durationMs: 5, timestamp: new Date(Date.now() - 1000 * 60 * 15), payload: { entityId: "INC-48290" } },
    { id: "s-2", type: "KNOWLEDGE_RETRIEVAL", name: "rag-service", status: "FAILED", durationMs: 2000, timestamp: new Date(Date.now() - 1000 * 60 * 15 + 10), error: "Timeout waiting for vector DB", payload: {} },
    { id: "s-3", type: "KNOWLEDGE_RETRIEVAL", name: "rag-service-retry", status: "SUCCESS", durationMs: 340, timestamp: new Date(Date.now() - 1000 * 60 * 15 + 2010), summary: "Recovered. Matched 1 article.", payload: { recovered: true, matchedDocuments: 1 } },
    { id: "s-4", type: "WORKFLOW_DECISION", name: "auto-close-evaluation", status: "SKIPPED", durationMs: 1, timestamp: new Date(Date.now() - 1000 * 60 * 15 + 2350), summary: "Skipped: Confidence too low for auto-close.", payload: { reason: "Confidence too low for auto-close." } },
    { id: "s-5", type: "EVENT_PUBLICATION", name: "workflow-completed", status: "SUCCESS", durationMs: 3, timestamp: new Date(Date.now() - 1000 * 60 * 15 + 2351), payload: { topic: "ai-support.workflow.completed", partition: 0 } }
  ]
};

export function WorkflowDetail({ executionId }: { executionId: string }) {
  const steps = MOCK_STEPS[executionId] || [];
  
  if (steps.length === 0) {
    return <div className="p-8 text-slate-500">No trace data available for this execution.</div>;
  }

  const duration = steps.reduce((acc, step) => acc + step.durationMs, 0);
  const lastStep = steps[steps.length - 1];
  const status = steps.some(s => s.status === 'FAILED') 
    ? (lastStep?.status === 'SUCCESS' ? 'RECOVERED' : 'FAILED') 
    : 'COMPLETED';

  return (
    <div className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 flex flex-col items-start bg-[#F8FAFC]">
      
      {/* Header Panel */}
      <div className="w-full bg-white shadow-sm border border-slate-200 rounded-xl p-6 mb-8 relative overflow-hidden">
        <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-purple-500 to-indigo-500"></div>
        <div className="flex flex-col md:flex-row justify-between items-start gap-4">
          <div>
            <div className="flex items-center gap-3 mb-2">
              <h1 className="text-2xl font-bold text-slate-900 leading-tight font-mono">{executionId}</h1>
              {status === 'COMPLETED' && <span className="bg-emerald-100 text-emerald-800 text-xs font-bold px-2.5 py-0.5 rounded border border-emerald-200 uppercase">Completed</span>}
              {status === 'FAILED' && <span className="bg-red-100 text-red-800 text-xs font-bold px-2.5 py-0.5 rounded border border-red-200 uppercase">Failed</span>}
              {status === 'RECOVERED' && <span className="bg-amber-100 text-amber-800 text-xs font-bold px-2.5 py-0.5 rounded border border-amber-200 uppercase">Recovered</span>}
            </div>
            <p className="text-sm text-slate-500 mb-4">Orchestration trace for entity <span className="font-mono bg-slate-100 px-1.5 rounded">{steps[0]?.payload?.entityId || "N/A"}</span></p>
            
            <div className="flex flex-wrap gap-6 text-sm">
              <div>
                <span className="text-slate-400 text-[10px] block uppercase font-bold tracking-wider mb-1">Correlation ID</span>
                <span className="text-slate-700 font-mono text-xs font-medium">corr-8f92a1b</span>
              </div>
              <div>
                <span className="text-slate-400 text-[10px] block uppercase font-bold tracking-wider mb-1">Model</span>
                <span className="text-slate-700 font-mono text-xs font-medium">gemini-2.5-flash</span>
              </div>
              <div>
                <span className="text-slate-400 text-[10px] block uppercase font-bold tracking-wider mb-1">Total Duration</span>
                <span className="text-slate-800 font-mono text-xs font-medium">{duration} ms</span>
              </div>
              <div>
                <span className="text-slate-400 text-[10px] block uppercase font-bold tracking-wider mb-1">Steps</span>
                <span className="text-slate-800 font-medium text-xs">{steps.length} ops</span>
              </div>
              <div>
                <span className="text-slate-400 text-[10px] block uppercase font-bold tracking-wider mb-1">Started</span>
                <span className="text-slate-800 font-medium text-xs">{steps[0]?.timestamp ? formatTimeAgo(steps[0].timestamp) : 'N/A'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Timeline */}
      <div className="w-full max-w-4xl mx-auto">
        <h2 className="text-xs font-bold text-slate-500 mb-6 uppercase tracking-widest px-2 flex items-center gap-2">
          <Network className="h-4 w-4" /> Execution Trace
        </h2>
        
        <div className="space-y-4 relative before:absolute before:top-0 before:bottom-0 before:left-5 before:-translate-x-1/2 md:before:left-1/2 before:w-0.5 before:bg-slate-200">
          {steps.map((step, idx) => (
            <WorkflowStepCard key={step.id} step={step} index={idx} isLast={idx === steps.length - 1} />
          ))}
        </div>
      </div>

    </div>
  );
}

function WorkflowStepCard({ step, index, isLast }: { step: WorkflowStep, index: number, isLast: boolean }) {
  const [expanded, setExpanded] = useState(false);
  const isEven = index % 2 === 0;
  
  return (
    <div className={`relative flex items-center justify-between md:justify-center group ${isLast ? 'mb-0' : 'mb-8'}`}>
      
      {/* Icon */}
      <div className={`flex items-center justify-center w-10 h-10 rounded-full border-4 border-[#F8FAFC] shrink-0 md:absolute md:left-1/2 md:-translate-x-1/2 shadow-sm z-10 ${getStepIconBg(step.type, step.status)}`}>
        {getStepIcon(step.type)}
      </div>

      {/* Card Wrapper */}
      <div className={`w-[calc(100%-3.5rem)] md:w-full flex ${isEven ? 'md:justify-start' : 'md:justify-end'}`}>
        <div className="w-full md:w-[calc(50%-2.5rem)] bg-white rounded-xl border border-slate-200 shadow-sm p-4 hover:shadow-md transition-all">
        <div className="flex justify-between items-start mb-2 cursor-pointer" onClick={() => setExpanded(!expanded)}>
          <div className="flex items-center gap-2">
            <span className={`text-[10px] font-bold uppercase tracking-widest px-1.5 py-0.5 rounded border ${getTypeColor(step.type)}`}>
              {step.type.replace("_", " ")}
            </span>
            <h3 className="font-bold text-slate-800 text-sm font-mono">{step.name}</h3>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-mono text-slate-500 bg-slate-50 px-1.5 py-0.5 rounded border border-slate-100">{step.durationMs}ms</span>
            {expanded ? <ChevronUp className="h-4 w-4 text-slate-400" /> : <ChevronDown className="h-4 w-4 text-slate-400" />}
          </div>
        </div>

        <div className="text-xs text-slate-500 mb-3 flex items-center justify-between border-b border-slate-100 pb-2">
          <span>Step {index + 1}</span>
          {getStepStatus(step.status)}
        </div>

        {step.summary && (
          <div className="mb-2 text-[13px] text-slate-700 leading-relaxed font-medium">
            {step.summary}
          </div>
        )}

        {step.error && (
          <div className="mt-2 mb-2 text-[13px] bg-red-50 text-red-700 p-2.5 rounded-lg border border-red-100 font-mono flex items-start gap-2">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{step.error}</span>
          </div>
        )}

        {expanded && (
          <div className="mt-3 pt-3 border-t border-slate-100 animate-in fade-in slide-in-from-top-2 duration-200">
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-2">Raw JSON Payload</span>
            <pre className="text-[11px] font-mono text-slate-700 bg-slate-50 p-3.5 rounded-lg border border-slate-200 overflow-x-auto shadow-inner leading-relaxed">
              {JSON.stringify(step.payload, null, 2)}
            </pre>
          </div>
        )}
        </div>
      </div>
    </div>
  );
}

function getStepIconBg(type: string, status: string) {
  if (status === "FAILED") return "bg-red-500 text-white";
  if (status === "SKIPPED") return "bg-slate-300 text-slate-500";
  
  switch (type) {
    case "BUSINESS_EVENT": return "bg-blue-500 text-white";
    case "GUARDRAIL": return "bg-amber-500 text-white";
    case "AI_PLANNING": return "bg-purple-500 text-white";
    case "TOOL_INVOCATION": return "bg-indigo-500 text-white";
    case "KNOWLEDGE_RETRIEVAL": return "bg-teal-500 text-white";
    case "WORKFLOW_DECISION": return "bg-emerald-500 text-white";
    case "EVENT_PUBLICATION": return "bg-slate-800 text-white";
    default: return "bg-slate-500 text-white";
  }
}

function getStepIcon(type: string) {
  switch (type) {
    case "BUSINESS_EVENT": return <Radio className="h-4 w-4" />;
    case "GUARDRAIL": return <Shield className="h-4 w-4" />;
    case "AI_PLANNING": return <Bot className="h-4 w-4" />;
    case "TOOL_INVOCATION": return <Wrench className="h-4 w-4" />;
    case "KNOWLEDGE_RETRIEVAL": return <BookOpen className="h-4 w-4" />;
    case "WORKFLOW_DECISION": return <Zap className="h-4 w-4" />;
    case "EVENT_PUBLICATION": return <Send className="h-4 w-4" />;
    default: return <CheckCircle2 className="h-4 w-4" />;
  }
}

function getTypeColor(type: string) {
  switch (type) {
    case "BUSINESS_EVENT": return "text-blue-700 bg-blue-50 border-blue-200";
    case "GUARDRAIL": return "text-amber-700 bg-amber-50 border-amber-200";
    case "AI_PLANNING": return "text-purple-700 bg-purple-50 border-purple-200";
    case "TOOL_INVOCATION": return "text-indigo-700 bg-indigo-50 border-indigo-200";
    case "KNOWLEDGE_RETRIEVAL": return "text-teal-700 bg-teal-50 border-teal-200";
    case "WORKFLOW_DECISION": return "text-emerald-700 bg-emerald-50 border-emerald-200";
    case "EVENT_PUBLICATION": return "text-slate-700 bg-slate-100 border-slate-300";
    default: return "text-slate-700 bg-slate-50 border-slate-200";
  }
}

function getStepStatus(status: string) {
  switch (status) {
    case "SUCCESS": return <span className="text-emerald-600 font-bold flex items-center gap-1"><CheckCircle2 className="h-3 w-3" /> SUCCESS</span>;
    case "FAILED": return <span className="text-red-600 font-bold flex items-center gap-1"><AlertCircle className="h-3 w-3" /> FAILED</span>;
    case "SKIPPED": return <span className="text-slate-500 font-bold flex items-center gap-1"> SKIPPED</span>;
    case "RETRYING": return <span className="text-amber-600 font-bold flex items-center gap-1"> RETRYING</span>;
    default: return null;
  }
}
