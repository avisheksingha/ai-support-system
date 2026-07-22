import { useState } from "react";
import type { TicketModel } from "@/shared/types/ticket";
import type { AnalysisModel, RoutingModel, AiDecisionModel, KnowledgeModel, WorkflowMetadata } from "@/shared/types/workspace";
import { Check, Loader2, ChevronDown, ChevronRight, Clock, Activity } from "lucide-react";

interface AiPipelineProgressProps {
  ticket: TicketModel;
  analysis?: AnalysisModel | undefined;
  knowledge?: KnowledgeModel | undefined;
  routing?: RoutingModel | undefined;
  aiDecision?: AiDecisionModel | undefined;
  workflowMetadata?: WorkflowMetadata | undefined;
}

interface WorkflowStage {
  id: string;
  label: string;
  isComplete: boolean;
  isCurrent: boolean;
  details?: React.ReactNode;
}

export function AiPipelineProgress({ ticket, analysis, knowledge, routing, workflowMetadata }: AiPipelineProgressProps) {
  const [expandedStage, setExpandedStage] = useState<string | null>(null);

  // Determine true state of pipeline based on REAL backend data
  const hasAnalysis = !!analysis;
  const hasKnowledge = !!knowledge || !!ticket.ragResponse;
  const hasRouting = !!routing;
  const isAssigned = !!ticket.assignedTo || ticket.status === 'ASSIGNED' || ticket.status === 'IN_PROGRESS' || ticket.status === 'RESOLVED' || ticket.status === 'CLOSED';

  const customerTier = ticket.customerTier || (ticket.customerName?.includes("Inc") ? "Enterprise" : "Standard");
  const channel = ticket.channel || "Web Portal";

  const knowledgeMatch = knowledge?.knowledgeFound
    ? (knowledge.retrievedDocumentCount || 0) >= 3
      ? "HIGH"
      : "MEDIUM"
    : "LOW";

  const isResolved = ticket.status === 'RESOLVED' || ticket.status === 'CLOSED';

  const stages: WorkflowStage[] = [
    {
      id: 'customer',
      label: 'Submitted',
      isComplete: !!ticket,
      isCurrent: false,
      details: (
        <div className="grid grid-cols-2 gap-2.5 text-xs">
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Customer Name</span>
            <span className="font-semibold text-slate-800">{ticket.customerName || "Customer"}</span>
          </div>
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Customer Tier</span>
            <span className="font-bold text-purple-700 bg-purple-50 px-1.5 py-0.2 rounded border border-purple-200 text-[10px]">
              {customerTier}
            </span>
          </div>
          <div className="bg-slate-50 rounded p-2 border border-slate-100 col-span-2">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Channel</span>
            <span className="font-medium text-slate-700">{channel}</span>
          </div>
        </div>
      ),
    },
    {
      id: 'analysis',
      label: 'AI Analysis',
      isComplete: hasAnalysis,
      isCurrent: !!ticket && !hasAnalysis,
      details: (
        <div className="grid grid-cols-2 gap-2.5 text-xs">
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Intent</span>
            <span className="font-bold text-indigo-700">{analysis ? formatSemanticString(analysis.intent) : "Password Reset"}</span>
          </div>
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Analysis Confidence</span>
            <span className="font-bold text-emerald-600">
              {analysis?.confidenceScore ? `${((analysis.confidenceScore ?? 0) * 100).toFixed(0)}%` : "95%"}
            </span>
          </div>
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Category</span>
            <span className="font-semibold text-slate-700">{analysis?.suggestedCategory || "Account Access"}</span>
          </div>
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Urgency</span>
            <span className="font-semibold text-rose-600">{analysis?.urgency || "HIGH"}</span>
          </div>
        </div>
      ),
    },
    {
      id: 'knowledge',
      label: 'Knowledge Retrieval',
      isComplete: hasKnowledge,
      isCurrent: hasAnalysis && !hasKnowledge,
      details: (
        <div className="grid grid-cols-2 gap-2.5 text-xs">
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Knowledge Match</span>
            <span className={`font-bold text-[10px] uppercase px-1.5 py-0.2 rounded border ${
              knowledgeMatch === "HIGH" ? "bg-emerald-50 text-emerald-700 border-emerald-200" :
              knowledgeMatch === "MEDIUM" ? "bg-amber-50 text-amber-700 border-amber-200" :
              "bg-slate-100 text-slate-600 border-slate-200"
            }`}>
              {knowledgeMatch} Match
            </span>
          </div>
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Documents Retrieved</span>
            <span className="font-semibold text-teal-700 font-mono">{knowledge?.retrievedDocumentCount ?? 1} articles</span>
          </div>
        </div>
      ),
    },
    {
      id: 'routing',
      label: 'Routing',
      isComplete: hasRouting,
      isCurrent: hasKnowledge && !hasRouting,
      details: (
        <div className="grid grid-cols-2 gap-2 text-xs">
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Support Domain</span>
            <span className="font-semibold text-slate-800">{routing?.assignedTeam || "Account & Access Support"}</span>
          </div>
          <div className="bg-slate-50 rounded p-2 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">SLA Target</span>
            <span className="font-bold text-emerald-600">{routing?.slaHours || 48}h</span>
          </div>
        </div>
      ),
    },
    {
      id: 'assigned',
      label: 'Assigned',
      isComplete: isAssigned,
      isCurrent: hasRouting && !isAssigned,
      details: (
        <div className="text-xs bg-slate-50 rounded p-2 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Assigned Agent</span>
          <span className="font-semibold text-slate-800">{ticket.assignedTo || 'Tier-2 Support Agent'}</span>
        </div>
      ),
    },
    {
      id: 'resolved',
      label: 'Resolved',
      isComplete: isResolved,
      isCurrent: isAssigned && !isResolved,
      details: (
        <div className="text-xs bg-slate-50 rounded p-2 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Resolution Status</span>
          <span className={`font-bold ${isResolved ? 'text-emerald-600' : 'text-slate-500'}`}>
            {isResolved ? 'Resolved' : 'Pending Resolution'}
          </span>
        </div>
      ),
    },
  ];

  const formatDuration = (ms: number) => {
    if (ms < 1000) return `${ms}ms`;
    if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
    return `${(ms / 60000).toFixed(1)}m`;
  };

  return (
    <div className="bg-white border border-slate-200 rounded-xl p-4 sm:p-5 shadow-sm">
      <div className="flex items-center justify-between mb-3 sm:mb-4">
        <h2 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">AI Pipeline Status</h2>
        {workflowMetadata && (
          <div className="flex items-center gap-2 sm:gap-3 text-[9px] sm:text-[10px] text-slate-500">
            <div className="hidden sm:flex items-center gap-1">
              <Activity className="h-3 w-3" />
              <span className="font-mono">{workflowMetadata.workflowExecutionId.slice(-8)}</span>
            </div>
            <div className="flex items-center gap-1">
              <Clock className="h-3 w-3" />
              <span className="font-mono">{formatDuration(workflowMetadata.workflowDurationMs)}</span>
            </div>
          </div>
        )}
      </div>
      
      <div className="flex flex-col gap-2">
        {stages.map((stage, index) => {
          const isExpanded = expandedStage === stage.id;
          const showConnector = index < stages.length - 1;
          
          return (
            <div key={stage.id} className="relative">
              {/* Connector Line */}
              {showConnector && (
                <div className="absolute left-[11px] top-6 bottom-0 w-0.5 bg-slate-200" />
              )}
              
              {/* Stage Row */}
              <div 
                className={`flex items-center gap-2 sm:gap-3 p-2 rounded-lg cursor-pointer transition-colors ${
                  isExpanded ? 'bg-slate-50' : 'hover:bg-slate-50'
                } ${stage.isCurrent ? 'bg-blue-50/50' : ''}`}
                onClick={() => setExpandedStage(isExpanded ? null : stage.id)}
              >
                {/* Status Icon */}
                <div className="relative z-10">
                  {stage.isComplete ? (
                    <div className="h-5 w-5 bg-emerald-500 rounded-full flex items-center justify-center shadow-sm">
                      <Check className="h-3 w-3 text-white" strokeWidth={3} />
                    </div>
                  ) : stage.isCurrent ? (
                    <div className="h-5 w-5 bg-blue-500 rounded-full flex items-center justify-center shadow-sm">
                      <Loader2 className="h-3 w-3 text-white animate-spin" />
                    </div>
                  ) : (
                    <div className="h-5 w-5 border-2 border-slate-300 rounded-full bg-white" />
                  )}
                </div>
                
                {/* Label */}
                <span className={`text-sm font-medium ${
                  stage.isComplete ? 'text-slate-800' : stage.isCurrent ? 'text-blue-700' : 'text-slate-400'
                }`}>
                  {stage.label}
                </span>
                
                {/* Expand/Collapse Icon */}
                {stage.details && (
                  <div className="ml-auto">
                    {isExpanded ? (
                      <ChevronDown className="h-4 w-4 text-slate-400" />
                    ) : (
                      <ChevronRight className="h-4 w-4 text-slate-400" />
                    )}
                  </div>
                )}
              </div>
              
              {/* Expanded Details */}
              {isExpanded && stage.details && (
                <div className="ml-6 sm:ml-8 mt-2 p-2.5 sm:p-3 bg-slate-50 rounded-lg border border-slate-100">
                  {stage.details}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

function formatSemanticString(val: string) {
  return val.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase());
}
