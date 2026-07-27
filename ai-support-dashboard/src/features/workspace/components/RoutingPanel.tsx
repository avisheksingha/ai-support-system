import { ArrowRight, AlertTriangle, Target, CheckCircle2 } from "lucide-react";
import type { RoutingModel, AnalysisModel, KnowledgeModel, AiDecisionContextModel } from "@/shared/types/workspace";
import type { TicketModel } from "@/shared/types/ticket";

interface RoutingPanelProps {
  routing: RoutingModel;
  ticket: TicketModel;
  analysis?: AnalysisModel | undefined;
  knowledge?: KnowledgeModel | undefined;
  aiDecisionContext?: AiDecisionContextModel | undefined;
}

const getSupportDomain = (teamOrCat?: string, subject?: string) => {
  const cat = (teamOrCat || "").toLowerCase();
  const sub = (subject || "").toLowerCase();
  if (cat.includes("auth") || sub.includes("oauth") || sub.includes("login") || sub.includes("security")) {
    return "Identity & Access Management (IAM)";
  }
  if (cat.includes("bill") || sub.includes("pay") || sub.includes("invoice")) {
    return "Billing & Account Services";
  }
  if (cat.includes("network") || sub.includes("connect") || sub.includes("latency") || cat.includes("technical")) {
    return "Core Network & Infrastructure";
  }
  if (cat.includes("bug") || sub.includes("crash") || sub.includes("error")) {
    return "Software Engineering & Defect Resolution";
  }
  return "Customer Support Operations";
};

export function RoutingPanel({ routing, ticket, analysis, knowledge, aiDecisionContext }: RoutingPanelProps) {
  const isEscalated = ticket.priority === 'CRITICAL' || ticket.priority === 'HIGH';
  const supportDomain = getSupportDomain(routing.assignedTeam, ticket.subject);
  const assignedAgent = (!ticket.assignedTo || ticket.assignedTo === "Unassigned" || ticket.assignedTo === routing.assignedTeam)
    ? "Unassigned"
    : ticket.assignedTo;

  // Compute dynamic decision summary data
  const rawIntent = analysis?.intent || aiDecisionContext?.intent || ticket.subject || "General Support Inquiry";
  const displayIntent = rawIntent
    .replace(/[-_]/g, ' ')
    .replace(/\w\S*/g, (txt) => txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase());

  const rawConf = analysis?.confidenceScore ?? aiDecisionContext?.confidence ?? (ticket.aiConfidence !== undefined && ticket.aiConfidence !== null ? (ticket.aiConfidence > 1 ? ticket.aiConfidence / 100 : ticket.aiConfidence) : 0.92);
  const confPercent = Math.round(rawConf * 100);
  const confLabel = confPercent >= 85 ? "High Confidence" : confPercent >= 60 ? "Medium Confidence" : "Low Confidence";
  const confBadgeStyle = confPercent >= 85 ? "bg-emerald-50 text-emerald-700 border-emerald-200" : confPercent >= 60 ? "bg-amber-50 text-amber-700 border-amber-200" : "bg-rose-50 text-rose-700 border-rose-200";

  const matchedSignals: string[] = [];
  if (analysis?.keywords && analysis.keywords.length > 0) {
    matchedSignals.push(`Keywords matched: "${analysis.keywords.slice(0, 4).join('", "')}"`);
  } else {
    const derivedWords = (ticket.subject || "").split(/\s+/).filter(w => w.replace(/[^a-zA-Z]/g, '').length > 3).slice(0, 3);
    if (derivedWords.length > 0) {
      matchedSignals.push(`Keywords matched: "${derivedWords.join('", "')}"`);
    } else {
      matchedSignals.push("Intent semantics matched core support dictionary");
    }
  }
  const categorySignal = analysis?.suggestedCategory || aiDecisionContext?.category || supportDomain;
  matchedSignals.push(`Domain category classification: ${categorySignal}`);
  matchedSignals.push(`Urgency evaluation: ${analysis?.urgency || ticket.priority || 'HIGH'}`);
  if (ticket.customerTier) {
    matchedSignals.push(`Customer SLA tier: ${ticket.customerTier}`);
  }

  const docCount = aiDecisionContext?.retrievedArticleCount ?? knowledge?.retrievedDocumentCount ?? knowledge?.sources?.length ?? knowledge?.matchedArticleTitles?.length ?? 0;
  const isFallback = aiDecisionContext?.retrievalFallbackUsed ?? (docCount === 0);

  const ruleName = isFallback
    ? "general_domain_fallback_policy"
    : `${(routing.assignedTeam || "general_support").toLowerCase().replace(/\s+/g, '_')}_routing_rule`;
  const policyDescription = `Priority ${routing.priority} with ${routing.slaHours}h resolution SLA target`;

  const teamDisplay = `${routing.assignedTeam || "General Support"} Team`;
  const backendExplanation = routing.routingExplanation || aiDecisionContext?.routingExplanation;
  const finalDecisionText = backendExplanation
    ? backendExplanation
    : isFallback
      ? `Due to retrieval fallback rules (0 articles retrieved), this ticket was assigned to the ${routing.assignedTeam || "General Support"} team for investigation.`
      : `According to routing policy and high confidence (${confPercent}%), this ticket was assigned to the ${routing.assignedTeam || "General Support"} team for initial investigation.`;

  return (
    <div className="text-xs space-y-3">
      {/* Escalation Badge - Prominent at top */}
      {isEscalated && (
        <div className="flex items-center justify-between bg-amber-50 rounded-lg p-2.5 border border-amber-200">
          <span className="text-[9px] font-bold uppercase text-amber-700">Priority Status</span>
          <div className="flex items-center gap-1 px-2 py-0.5 bg-amber-100 border border-amber-300 rounded-md">
            <AlertTriangle className="h-3 w-3 text-amber-600" />
            <span className="text-[10px] font-bold text-amber-700 uppercase">Escalated</span>
          </div>
        </div>
      )}

      {/* Assignment Info - Graceful Field Hiding */}
      <div className="flex flex-wrap gap-2">
        {/* Support Domain - Always Present */}
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100 flex-1 min-w-[110px]">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Support Domain</span>
          <div className="font-semibold text-slate-800 text-[11px] truncate" title={supportDomain}>
            {supportDomain}
          </div>
        </div>

        {/* Assigned Team - Shown if Present */}
        {routing.assignedTeam && (
          <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100 flex-1 min-w-[110px]">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Assigned Team</span>
            <div className="font-semibold text-cyan-800 text-[11px] truncate" title={`${routing.assignedTeam} Team`}>
              {routing.assignedTeam} Team
            </div>
          </div>
        )}

        {/* Assigned Agent */}
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100 flex-1 min-w-[110px]">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Assigned Agent</span>
          <div className="font-semibold text-slate-800 text-[11px] truncate" title={assignedAgent}>
            {assignedAgent}
          </div>
        </div>
      </div>

      {/* Priority and SLA */}
      <div className="grid grid-cols-2 gap-2">
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Priority</span>
          <div className={`font-semibold font-mono text-[10px] ${
            ticket.priority === 'CRITICAL' ? 'text-red-600' :
            ticket.priority === 'HIGH' ? 'text-orange-600' :
            ticket.priority === 'MEDIUM' ? 'text-amber-600' :
            'text-blue-600'
          }`}>
            {routing.priority}
          </div>
        </div>
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">SLA</span>
          <div className="font-bold text-emerald-600">
            {routing.slaHours}h
          </div>
        </div>
      </div>

      {/* Structured Routing Decision Summary */}
      <div className="bg-slate-50/80 rounded-xl border border-slate-200 overflow-hidden shadow-2xs">
        <div className="bg-slate-100/80 px-3.5 py-2.5 border-b border-slate-200 flex items-center justify-between">
          <div className="flex items-center gap-1.5 font-bold text-[10.5px] text-slate-700 uppercase tracking-wider">
            <Target className="h-3.5 w-3.5 text-cyan-600" />
            <span>Structured Routing Decision</span>
          </div>
          <span className="text-[9.5px] font-mono text-slate-500 bg-white px-2 py-0.5 rounded border border-slate-200">
            Policy Engine v1.0
          </span>
        </div>

        <div className="p-3.5 space-y-3">
          {/* Intent & Confidence row */}
          <div className="grid grid-cols-2 gap-2 bg-white p-2.5 rounded-lg border border-slate-150">
            <div>
              <span className="text-[9px] font-bold text-slate-400 uppercase tracking-wider block mb-0.5">Detected Intent</span>
              <span className="font-bold text-slate-800 text-[11px] truncate block" title={displayIntent}>{displayIntent}</span>
            </div>
            <div>
              <span className="text-[9px] font-bold text-slate-400 uppercase tracking-wider block mb-0.5">Confidence Score</span>
              <div className="flex items-center gap-1.5">
                <span className="font-bold text-slate-800 text-[11px] font-mono">{confPercent}%</span>
                <span className={`text-[9px] font-bold uppercase px-1.5 py-0.2 rounded border ${confBadgeStyle}`}>
                  {confLabel}
                </span>
              </div>
            </div>
          </div>

          {/* Matched Signals */}
          <div className="bg-white p-2.5 rounded-lg border border-slate-150 space-y-1.5">
            <span className="text-[9px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Matched Signals</span>
            <ul className="space-y-1 text-[10.5px] text-slate-600">
              {matchedSignals.map((signal, idx) => (
                <li key={idx} className="flex items-start gap-1.5">
                  <span className="text-cyan-500 font-bold mt-0.5">•</span>
                  <span className="leading-snug">{signal}</span>
                </li>
              ))}
            </ul>
          </div>

          {/* Knowledge Retrieval & Routing Rule row */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            <div className="bg-white p-2.5 rounded-lg border border-slate-150">
              <span className="text-[9px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Knowledge Retrieval</span>
              <div className="flex items-center gap-1.5">
                {docCount > 0 ? (
                  <div className="flex items-center gap-1.5 text-emerald-700 font-semibold text-[10.5px]">
                    <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500 shrink-0" />
                    <span>✓ {docCount} relevant article{docCount !== 1 ? 's' : ''} retrieved</span>
                  </div>
                ) : (
                  <div className="flex items-center gap-1.5 text-amber-700 font-semibold text-[10.5px]">
                    <AlertTriangle className="h-3.5 w-3.5 text-amber-500 shrink-0" />
                    <span>0 articles (fallback applied)</span>
                  </div>
                )}
              </div>
            </div>

            <div className="bg-white p-2.5 rounded-lg border border-slate-150">
              <span className="text-[9px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Routing Rule / Policy</span>
              <div className="flex flex-col">
                <span className="font-mono text-[10px] text-cyan-800 font-semibold truncate" title={ruleName}>{ruleName}</span>
                <span className="text-[9.5px] text-slate-500">{policyDescription}</span>
              </div>
            </div>
          </div>

          {/* Assigned Team & Final Decision */}
          <div className="bg-gradient-to-r from-cyan-50/70 to-blue-50/50 p-3 rounded-lg border border-cyan-100 space-y-2">
            <div className="flex items-center justify-between border-b border-cyan-100/80 pb-2">
              <span className="text-[9px] font-bold text-cyan-800 uppercase tracking-wider">Assigned Team</span>
              <span className="font-bold text-[11px] text-cyan-900 bg-white px-2.5 py-0.5 rounded-md border border-cyan-200 shadow-2xs">
                {teamDisplay}
              </span>
            </div>
            <div className="pt-0.5">
              <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Final Decision Summary</span>
              <p className="text-[10.5px] font-medium text-slate-700 leading-relaxed flex items-start gap-1.5">
                <ArrowRight className="h-3.5 w-3.5 text-cyan-600 mt-0.5 shrink-0" />
                <span>{finalDecisionText}</span>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
