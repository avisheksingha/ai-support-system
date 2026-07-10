import { useState, useEffect } from "react";
import { Badge } from "@/components/ui/badge";
import { BrainCircuit, Target, AlertTriangle, Key } from "lucide-react";
import type { AnalysisModel } from "@/shared/types/workspace";

interface AiInsightsPanelProps {
  analysis: AnalysisModel;
}

export function AiInsightsPanel({ analysis }: AiInsightsPanelProps) {
  const rawScore = analysis.confidenceScore ?? 0;
  const [confidenceScore, setConfidenceScore] = useState(0);

  useEffect(() => {
    // Animate the progress bar after mount
    const timer = setTimeout(() => setConfidenceScore(rawScore * 100), 50);
    return () => clearTimeout(timer);
  }, [rawScore]);
  
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
      
      <div className="p-4 space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <div className="text-xs text-muted-foreground mb-1 flex items-center gap-1"><Target className="h-3 w-3" /> Intent</div>
            <Badge variant="outline" className={`border-border ${getIntentColor(analysis.intent)}`}>
              {formatSemanticString(analysis.intent)}
            </Badge>
          </div>
          <div>
            <div className="text-xs text-muted-foreground mb-1 flex items-center gap-1"><AlertTriangle className="h-3 w-3" /> Urgency</div>
            <Badge variant="outline" className={`border-border ${getUrgencyColor(analysis.urgency)}`}>
              {formatSemanticString(analysis.urgency)}
            </Badge>
          </div>
        </div>

        <div>
          <div className="text-xs text-muted-foreground mb-3 flex items-center justify-between">
            <span>Confidence Score</span>
            <span className="font-medium text-foreground">{getConfidenceString(rawScore)}</span>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex-1 h-2 bg-muted rounded-full overflow-hidden">
              <div 
                className={`h-full rounded-full ${getConfidenceColor(rawScore)} transition-all duration-1000 ease-out`}
                style={{ width: `${Math.max(0, Math.min(100, confidenceScore))}%` }}
              />
            </div>
            <span className="text-xs font-medium font-mono text-muted-foreground whitespace-nowrap min-w-[32px] text-right">
              {rawScore > 0 ? (rawScore * 100).toFixed(0) : 0}%
            </span>
          </div>
        </div>

        <div>
          <div className="text-xs text-muted-foreground mb-2 flex items-center gap-1"><Key className="h-3 w-3" /> Keywords</div>
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

function getIntentColor(intent: string) {
  if (intent.includes("LOGIN") || intent.includes("PASSWORD")) return "text-blue-600 bg-blue-50 border-blue-200";
  if (intent.includes("BILLING") || intent.includes("REFUND")) return "text-emerald-600 bg-emerald-50 border-emerald-200";
  return "text-blue-600 bg-blue-50 border-blue-200";
}

function getUrgencyColor(urgency: string) {
  switch (urgency.toUpperCase()) {
    case "CRITICAL": return "text-red-600 border-red-200 bg-red-50";
    case "HIGH": return "text-orange-600 border-orange-200 bg-orange-50";
    case "MEDIUM": return "text-yellow-600 border-yellow-200 bg-yellow-50";
    case "LOW": return "text-blue-600 border-blue-200 bg-blue-50";
    default: return "text-muted-foreground bg-muted";
  }
}

function getConfidenceString(score: number) {
  if (score >= 0.9) return "High Confidence";
  if (score >= 0.7) return "Medium Confidence";
  return "Low Confidence";
}

function getConfidenceColor(score: number) {
  if (score >= 0.9) return "bg-emerald-500";
  if (score >= 0.7) return "bg-yellow-500";
  return "bg-red-500";
}
