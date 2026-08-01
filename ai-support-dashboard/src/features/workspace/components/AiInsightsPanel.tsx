import type { AnalysisModel } from "@/shared/types/workspace";
import { formatIntent } from "@/shared/utils/format";

interface AiInsightsPanelProps {
  analysis: AnalysisModel;
}

export function AiInsightsPanel({ analysis }: AiInsightsPanelProps) {


  const getUrgencyColor = (urgency: string) => {
    const upper = urgency.toUpperCase();
    if (upper.includes('CRITICAL') || upper.includes('HIGH')) return 'text-rose-600';
    if (upper.includes('MEDIUM')) return 'text-amber-600';
    return 'text-blue-600';
  };


  let displayIntent = formatIntent(analysis.intent || "General Inquiry");
  let displayCategory = analysis.suggestedCategory || "General Support";

  if (displayIntent.toLowerCase() === displayCategory.toLowerCase() || displayIntent.toLowerCase().includes(displayCategory.toLowerCase()) || displayCategory.toLowerCase().includes(displayIntent.toLowerCase())) {
    const lower = displayIntent.toLowerCase();
    if (lower.includes("auth") || lower.includes("login") || lower.includes("oauth") || lower.includes("security")) {
      displayIntent = "Account Access & Login Verification Failure";
      displayCategory = "Identity & Access Management (IAM)";
    } else if (lower.includes("bill") || lower.includes("pay") || lower.includes("invoice")) {
      displayIntent = "Billing Discrepancy & Payment Verification";
      displayCategory = "Billing & Account Services";
    } else if (lower.includes("network") || lower.includes("connect") || lower.includes("latency")) {
      displayIntent = "Network Latency & Connectivity Troubleshooting";
      displayCategory = "Core Network & Infrastructure";
    } else if (lower.includes("bug") || lower.includes("crash") || lower.includes("error")) {
      displayIntent = "Software Defect & Crash Investigation";
      displayCategory = "Software Engineering & Defect Resolution";
    } else {
      displayIntent = displayIntent + " Troubleshooting";
      displayCategory = displayCategory + " Operations";
    }
  }

  return (
    <div className="text-xs space-y-3">
      {/* Analysis Grid */}
      <div className="grid grid-cols-2 gap-2">
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Intent</span>
          <div className="font-semibold text-indigo-700 break-words leading-snug" title={displayIntent}>{displayIntent}</div>
        </div>
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Sentiment</span>
          <div className="font-semibold text-slate-700">{formatSemanticString(analysis.sentiment || "NEUTRAL")}</div>
        </div>
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Urgency</span>
          <div className={`font-semibold ${getUrgencyColor(analysis.urgency)}`}>{formatSemanticString(analysis.urgency)}</div>
        </div>
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Category</span>
          <div className="font-semibold text-slate-700 break-words leading-snug" title={displayCategory}>{displayCategory}</div>
        </div>
      </div>

      {/* Keywords as Chips */}
      {analysis.keywords && analysis.keywords.length > 0 && (
        <div>
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-2">Keywords</span>
          <div className="flex flex-wrap gap-1.5">
            {analysis.keywords.map((kw, i) => (
              <span key={i} className="px-2 py-0.5 bg-white border border-slate-200 text-slate-600 rounded-md text-[10px] font-medium shadow-sm">
                {kw}
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function formatSemanticString(val: string) {
  return val.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase());
}

