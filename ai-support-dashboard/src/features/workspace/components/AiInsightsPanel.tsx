import { BrainCircuit, Key } from "lucide-react";
import type { AnalysisModel } from "@/shared/types/workspace";

interface AiInsightsPanelProps {
  analysis: AnalysisModel;
}

export function AiInsightsPanel({ analysis }: AiInsightsPanelProps) {
  const rawScore = analysis.confidenceScore ?? 0;
  let keywords: string[] = [];
  if (Array.isArray(analysis.keywords)) {
    keywords = analysis.keywords;
  } else if (typeof analysis.keywords === 'string') {
    try {
      keywords = JSON.parse(analysis.keywords);
    } catch {
      keywords = (analysis.keywords as string).replace(/^\[|\]$/g, '').split(',').map((k: string) => k.trim()).filter(Boolean);
    }
  }
  
  return (
    <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg overflow-hidden">
      <div className="bg-card border-b border-border p-4 flex items-center gap-2">
        <BrainCircuit className="h-5 w-5 text-blue-400" />
        <h3 className="font-semibold text-foreground">AI Insights</h3>
      </div>
      
      <div className="p-4 space-y-3 text-[13px]">
        <div className="grid grid-cols-2 gap-3">
          <div>
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">Model</span>
            <div className="font-medium text-foreground">Gemini 2.5 Flash</div>
          </div>
          <div>
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">Confidence</span>
            <div className="font-medium text-emerald-600">{(rawScore * 100).toFixed(0)}%</div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">Latency</span>
            <div className="font-medium text-foreground font-mono">842 ms</div>
          </div>
          <div>
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">Intent</span>
            <div className="font-medium text-foreground">{formatSemanticString(analysis.intent)}</div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <span className="text-[11px] font-semibold text-muted-foreground uppercase tracking-wider block">Urgency</span>
            <div className="font-medium text-foreground">{formatSemanticString(analysis.urgency)}</div>
          </div>
        </div>

        <div>
          <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2 flex items-center gap-1"><Key className="h-3 w-3" /> Extracted Keywords</span>
          <div className="flex flex-wrap gap-1">
            {keywords.length > 0 ? (
              keywords.map((kw, i) => (
                <span key={i} className="text-[11px] px-2 py-1 bg-muted border border-border text-foreground rounded-md">
                  {kw}
                </span>
              ))
            ) : (
              <span className="text-[11px] text-muted-foreground italic">No keywords detected</span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function formatSemanticString(val: string) {
  return val.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase());
}

