import type { AnalysisModel } from "@/shared/types/workspace";

interface AiInsightsPanelProps {
  analysis: AnalysisModel;
}

export function AiInsightsPanel({ analysis }: AiInsightsPanelProps) {
  const rawScore = analysis.confidenceScore ?? 0;
  const confidencePercent = (rawScore * 100).toFixed(0);
  
  const getConfidenceColor = (score: number) => {
    if (score >= 0.8) return 'text-emerald-600 bg-emerald-50 border-emerald-200';
    if (score >= 0.6) return 'text-amber-600 bg-amber-50 border-amber-200';
    return 'text-red-600 bg-red-50 border-red-200';
  };
  
  const getUrgencyColor = (urgency: string) => {
    const upper = urgency.toUpperCase();
    if (upper.includes('CRITICAL') || upper.includes('HIGH')) return 'text-rose-600';
    if (upper.includes('MEDIUM')) return 'text-amber-600';
    return 'text-blue-600';
  };

  const confidenceLabel = rawScore >= 0.8 ? `High (${confidencePercent}%)` : rawScore >= 0.6 ? `Medium (${confidencePercent}%)` : `Low (${confidencePercent}%)`;

  return (
    <div className="text-xs space-y-3">
      {/* Confidence Badge - Prominent at top */}
      <div className="flex items-center justify-between bg-slate-50 rounded-lg p-2.5 border border-slate-100">
        <span className="text-[9px] font-bold uppercase text-slate-500">Analysis Confidence</span>
        <div className={`px-2.5 py-0.5 rounded-md border text-[11px] font-bold ${getConfidenceColor(rawScore)}`}>
          {confidenceLabel}
        </div>
      </div>

      {/* Analysis Grid */}
      <div className="grid grid-cols-2 gap-2">
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-1">Intent</span>
          <div className="font-semibold text-indigo-700 truncate">{formatSemanticString(analysis.intent)}</div>
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
          <div className="font-semibold text-slate-700 truncate">{analysis.suggestedCategory || "Uncategorized"}</div>
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

