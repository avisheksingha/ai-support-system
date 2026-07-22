import { X, User, BrainCircuit, BookOpen, Network, Cpu, Clock, ExternalLink } from "lucide-react";
import type { TicketModel } from "@/shared/types/ticket";
import type { AnalysisModel, RoutingModel, AiDecisionModel, KnowledgeModel, WorkflowMetadata } from "@/shared/types/workspace";
import { formatDuration, formatTime } from "@/shared/utils/date";

interface AiContextDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  ticket: TicketModel;
  analysis?: AnalysisModel | undefined;
  knowledge?: KnowledgeModel | undefined;
  routing?: RoutingModel | undefined;
  aiDecision?: AiDecisionModel | undefined;
  workflowMetadata?: WorkflowMetadata | undefined;
}

export function AiContextDrawer({
  isOpen,
  onClose,
  ticket,
  analysis,
  knowledge,
  routing,
  aiDecision: _aiDecision,
  workflowMetadata,
}: AiContextDrawerProps) {
  if (!isOpen) return null;

  const customerTier = ticket.customerTier || (ticket.customerName?.includes("Inc") ? "Enterprise" : "Standard");
  const channel = ticket.channel || "Web Portal";
  
  const rawScore = analysis?.confidenceScore ?? 0.92;
  const analysisConfidence = (rawScore * 100).toFixed(0);

  const knowledgeMatch = knowledge?.knowledgeFound
    ? (knowledge.retrievedDocumentCount || 0) >= 3
      ? "HIGH"
      : "MEDIUM"
    : "LOW";

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-slate-900/40 backdrop-blur-xs flex justify-end animate-in fade-in duration-200">
      <div className="w-full max-w-xl bg-white h-full shadow-2xl flex flex-col overflow-hidden animate-in slide-in-from-right duration-300">
        
        {/* Header */}
        <div className="p-5 border-b border-slate-200 bg-slate-50 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="bg-purple-100 p-2 rounded-lg text-purple-700">
              <BrainCircuit className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-900 leading-tight">Full AI Context &amp; Trace</h2>
              <span className="text-xs font-mono text-slate-500">Ticket: {ticket.ticketNumber}</span>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-200/60 transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6 bg-slate-50/40">
          
          {/* Section 1: Customer Profile */}
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-3">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
              <User className="h-3.5 w-3.5 text-blue-600" /> 1. Customer Context
            </h3>
            <div className="grid grid-cols-2 gap-3 text-xs">
              <div>
                <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Name</span>
                <span className="font-semibold text-slate-800">{ticket.customerName || "Customer"}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Customer Tier</span>
                <span className="inline-flex px-2 py-0.5 bg-purple-50 text-purple-800 border border-purple-200 rounded font-bold text-[10px]">
                  {customerTier}
                </span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Channel</span>
                <span className="font-medium text-slate-700">{channel}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Created At</span>
                <span className="font-mono text-slate-600">{formatTime(ticket.createdAt)}</span>
              </div>
            </div>
            <div className="pt-2 border-t border-slate-100 text-xs">
              <span className="text-[10px] text-slate-400 font-bold uppercase block mb-1">Customer Subject</span>
              <p className="font-medium text-slate-800 bg-slate-50 p-2.5 rounded border border-slate-100">{ticket.subject}</p>
            </div>
          </div>

          {/* Section 2: AI Analysis */}
          {analysis && (
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-3">
              <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                <BrainCircuit className="h-3.5 w-3.5 text-purple-600" /> 2. AI Intent &amp; Sentiment Analysis
              </h3>
              <div className="grid grid-cols-2 gap-3 text-xs">
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Intent</span>
                  <span className="font-bold text-indigo-700">{formatSemanticString(analysis.intent)}</span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Analysis Confidence</span>
                  <span className="font-bold text-emerald-600">{analysisConfidence}%</span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Suggested Category</span>
                  <span className="font-semibold text-slate-800">{analysis.suggestedCategory || "Account Access"}</span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Urgency</span>
                  <span className="font-semibold text-rose-600">{analysis.urgency}</span>
                </div>
              </div>

              {analysis.keywords && analysis.keywords.length > 0 && (
                <div className="pt-2 border-t border-slate-100">
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-1.5">Extracted Keywords</span>
                  <div className="flex flex-wrap gap-1.5">
                    {analysis.keywords.map((kw, i) => (
                      <span key={i} className="px-2 py-0.5 bg-slate-100 text-slate-700 rounded text-[10px] font-medium border border-slate-200">
                        {kw}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Section 3: Knowledge Retrieval */}
          {knowledge && (
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-3">
              <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                <BookOpen className="h-3.5 w-3.5 text-teal-600" /> 3. Knowledge Retrieval
              </h3>
              <div className="grid grid-cols-2 gap-3 text-xs">
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Knowledge Match</span>
                  <span className={`inline-flex px-2 py-0.5 rounded font-bold text-[10px] uppercase ${
                    knowledgeMatch === "HIGH" ? "bg-emerald-50 text-emerald-700 border border-emerald-200" :
                    knowledgeMatch === "MEDIUM" ? "bg-amber-50 text-amber-700 border border-amber-200" :
                    "bg-slate-100 text-slate-600 border border-slate-200"
                  }`}>
                    {knowledgeMatch} Match
                  </span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Retrieved Docs</span>
                  <span className="font-semibold text-slate-800">{knowledge.retrievedDocumentCount ?? 0} articles</span>
                </div>
              </div>

              {knowledge.matchedArticleTitles && knowledge.matchedArticleTitles.length > 0 && (
                <div className="pt-2 border-t border-slate-100">
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-1.5">Matched Knowledge Articles</span>
                  <div className="space-y-1.5">
                    {knowledge.matchedArticleTitles.map((title, i) => (
                      <div key={i} className="flex items-center justify-between p-2 bg-teal-50/50 border border-teal-100 rounded text-xs text-teal-950">
                        <span className="truncate font-medium">{title}</span>
                        <span className="text-[10px] font-mono text-teal-700 bg-white px-1.5 py-0.5 rounded border border-teal-200 flex items-center gap-1 shrink-0">
                          <ExternalLink className="h-3 w-3" /> Open
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Section 4: Routing */}
          {routing && (
            <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-3">
              <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
                <Network className="h-3.5 w-3.5 text-cyan-600" /> 4. Support Routing &amp; SLA
              </h3>
              <div className="grid grid-cols-2 gap-3 text-xs">
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Support Domain</span>
                  <span className="font-bold text-cyan-900">{routing.assignedTeam || "Account & Access Support"}</span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Assigned Agent / Team</span>
                  <span className="font-semibold text-slate-800">{ticket.assignedTo || "Tier-2 Escalation Team"}</span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">Routing Rule</span>
                  <span className="font-mono text-slate-700 bg-slate-100 px-1 py-0.5 rounded">Account Priority Rule</span>
                </div>
                <div>
                  <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5">SLA Requirement</span>
                  <span className="font-bold text-emerald-600">{routing.slaHours || 48}h SLA</span>
                </div>
              </div>
            </div>
          )}

          {/* Section 5: Execution Metadata */}
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-3">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest flex items-center gap-2">
              <Cpu className="h-3.5 w-3.5 text-purple-600" /> 5. Execution Metadata
            </h3>
            <div className="grid grid-cols-2 gap-3 text-xs font-mono">
              <div>
                <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5 font-sans">Trace Execution ID</span>
                <span className="text-purple-700 font-bold break-all">{workflowMetadata?.workflowExecutionId || `wf-exec-${ticket.id}`}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5 font-sans">AI Model</span>
                <span className="text-slate-800 bg-slate-100 px-1.5 py-0.5 rounded border border-slate-200">{knowledge?.model || "gemini-2.5-flash"}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5 font-sans">Prompt Version</span>
                <span className="text-indigo-700 font-bold">v2.4 (system-prompt)</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 font-bold uppercase block mb-0.5 font-sans">Processing Duration</span>
                <span className="text-slate-800 font-bold flex items-center gap-1 font-sans">
                  <Clock className="h-3 w-3 text-slate-400" /> {formatDuration(workflowMetadata?.workflowDurationMs || 3090)}
                </span>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}

function formatSemanticString(val: string) {
  if (!val) return "—";
  return val.replace(/_/g, " ").replace(/\b\w/g, (c) => c.toUpperCase());
}
