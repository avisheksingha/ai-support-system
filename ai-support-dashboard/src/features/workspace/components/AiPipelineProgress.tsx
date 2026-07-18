import type { TicketModel } from "@/shared/types/ticket";
import type { AnalysisModel, RoutingModel, AiDecisionModel, KnowledgeModel } from "@/shared/types/workspace";
import { Check, Loader2, ArrowRight } from "lucide-react";

interface AiPipelineProgressProps {
  ticket: TicketModel;
  analysis?: AnalysisModel | undefined;
  knowledge?: KnowledgeModel | undefined;
  routing?: RoutingModel | undefined;
  aiDecision?: AiDecisionModel | undefined;
}

export function AiPipelineProgress({ ticket, analysis, knowledge, routing, aiDecision }: AiPipelineProgressProps) {
  // Determine true state of pipeline based on REAL backend data
  const hasAnalysis = !!analysis;
  const hasKnowledge = !!knowledge || !!ticket.ragResponse;
  const hasRouting = !!routing;
  const hasAiDecision = !!aiDecision;
  const isAssigned = !!ticket.assignedTo || ticket.status === 'ASSIGNED' || ticket.status === 'IN_PROGRESS' || ticket.status === 'RESOLVED' || ticket.status === 'CLOSED';
  const isResolved = ticket.status === 'RESOLVED' || ticket.status === 'CLOSED';

  const PrimaryNode = ({ label, isComplete }: { label: string, isComplete: boolean }) => (
    <div className="flex items-center justify-between w-[95px]">
      <span className={`text-[13px] ${isComplete ? 'text-slate-800' : 'text-slate-400'}`}>{label}</span>
      {isComplete ? (
        <div className="h-5 w-5 bg-[#00875A] rounded-full flex items-center justify-center shrink-0 shadow-sm">
          <Check className="h-3 w-3 text-white" strokeWidth={3} />
        </div>
      ) : (
        <div className="h-5 w-5 border-2 border-slate-200 rounded-full shrink-0" />
      )}
    </div>
  );

  const ChildNode1 = ({ label, isComplete, isCurrent }: { label: string, isComplete: boolean, isCurrent: boolean }) => (
    <div className="flex items-center justify-between w-[100px]">
      <span className={`text-[13px] ${isComplete || isCurrent ? 'text-slate-800' : 'text-slate-400'}`}>{label}</span>
      {isComplete ? (
        <div className="h-5 w-5 bg-[#00875A] rounded-full flex items-center justify-center shrink-0 shadow-sm">
          <Check className="h-3 w-3 text-white" strokeWidth={3} />
        </div>
      ) : isCurrent ? (
        <Loader2 className="h-4 w-4 text-[#0C66E4] animate-spin shrink-0" />
      ) : (
        <div className="h-4 w-4 opacity-0 shrink-0" />
      )}
    </div>
  );

  const ChildNode2 = ({ label, isComplete, isCurrent }: { label: string, isComplete: boolean, isCurrent: boolean }) => (
    <div className="flex items-center justify-between w-[95px]">
      <span className={`text-[13px] ${isComplete || isCurrent ? 'text-slate-800' : 'text-slate-400'}`}>{label}</span>
      {isComplete ? (
        <div className="h-5 w-5 bg-[#00875A] rounded-full flex items-center justify-center shrink-0 shadow-sm">
          <Check className="h-3 w-3 text-white" strokeWidth={3} />
        </div>
      ) : isCurrent ? (
        <Loader2 className="h-4 w-4 text-[#0C66E4] animate-spin shrink-0" />
      ) : (
        <div className="h-4 w-4 opacity-0 shrink-0" />
      )}
    </div>
  );

  const Arrow = ({ active }: { active: boolean }) => (
    <div className="flex items-center justify-center w-6">
      <ArrowRight className={`h-[11px] w-[11px] ${active ? 'text-[#00875A]' : 'text-slate-300'}`} />
    </div>
  );

  return (
    <div className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm">
      <h2 className="text-[10px] font-bold text-slate-400 mb-5 uppercase tracking-widest">AI Pipeline Status</h2>
      
      <div className="flex flex-col gap-4 overflow-x-auto pb-2">
        {/* Row 1: Analysis Pipeline */}
        <div className="flex items-center gap-1.5 min-w-max">
          <PrimaryNode label="Customer" isComplete={!!ticket} />
          <Arrow active={hasAnalysis} />
          
          <ChildNode1 label="AI Analysis" isComplete={hasAnalysis} isCurrent={!!ticket && !hasAnalysis} />
          <Arrow active={hasKnowledge} />
          
          <ChildNode2 label="Knowledge" isComplete={hasKnowledge} isCurrent={hasAnalysis && !hasKnowledge} />
        </div>

        {/* Row 2: Action Pipeline */}
        <div className="flex items-center gap-1.5 min-w-max">
          <PrimaryNode label="AI Decision" isComplete={hasAiDecision} />
          <Arrow active={hasRouting} />
          
          <ChildNode1 label="Routing" isComplete={hasRouting} isCurrent={hasAiDecision && !hasRouting} />
          <Arrow active={isAssigned} />
          
          <ChildNode2 label="Assigned" isComplete={isAssigned} isCurrent={hasRouting && !isAssigned} />
          <Arrow active={isResolved} />
          
          <ChildNode1 label="Resolution" isComplete={isResolved} isCurrent={isAssigned && !isResolved} />
        </div>
      </div>
    </div>
  );
}
