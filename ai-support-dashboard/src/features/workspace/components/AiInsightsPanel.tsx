import { Badge } from "@/components/ui/badge";
import { BrainCircuit, Target, AlertTriangle, Key } from "lucide-react";
import type { AnalysisModel } from "@/shared/types/workspace";

interface AiInsightsPanelProps {
  analysis: AnalysisModel;
}

export function AiInsightsPanel({ analysis }: AiInsightsPanelProps) {
  const confidenceScore = analysis.confidenceScore * 100;
  
  return (
    <div className="bg-zinc-900/50 border border-zinc-800 rounded-xl overflow-hidden">
      <div className="bg-zinc-900 border-b border-zinc-800 p-4 flex items-center gap-2">
        <BrainCircuit className="h-5 w-5 text-indigo-400" />
        <h3 className="font-semibold text-zinc-100">AI Insights</h3>
      </div>
      
      <div className="p-4 space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <div className="text-xs text-zinc-500 mb-1 flex items-center gap-1"><Target className="h-3 w-3" /> Intent</div>
            <Badge variant="outline" className={`border-zinc-700 ${getIntentColor(analysis.intent)}`}>
              {formatSemanticString(analysis.intent)}
            </Badge>
          </div>
          <div>
            <div className="text-xs text-zinc-500 mb-1 flex items-center gap-1"><AlertTriangle className="h-3 w-3" /> Urgency</div>
            <Badge variant="outline" className={`border-zinc-700 ${getUrgencyColor(analysis.urgency)}`}>
              {formatSemanticString(analysis.urgency)}
            </Badge>
          </div>
        </div>

        <div>
          <div className="flex justify-between items-center mb-2">
             <div className="text-xs text-zinc-500">Confidence Score</div>
             <div className="text-xs font-medium text-zinc-300">{getConfidenceString(analysis.confidenceScore)}</div>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex-1 h-2 bg-zinc-800 rounded-full overflow-hidden">
              <div 
                className={`h-full rounded-full ${getConfidenceColor(analysis.confidenceScore)}`}
                style={{ width: `${Math.max(0, Math.min(100, confidenceScore))}%` }}
              />
            </div>
            <span className="text-xs font-medium font-mono text-zinc-400">
              {confidenceScore.toFixed(0)}%
            </span>
          </div>
        </div>

        {analysis.keywords && analysis.keywords.length > 0 && (
          <div>
            <div className="text-xs text-zinc-500 mb-2 flex items-center gap-1"><Key className="h-3 w-3" /> Keywords</div>
            <div className="flex flex-wrap gap-1">
              {analysis.keywords.map((kw, i) => (
                <span key={i} className="text-[11px] px-2 py-1 bg-zinc-800 border border-zinc-700 text-zinc-300 rounded-md">
                  {kw}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function formatSemanticString(val: string) {
  return val.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase());
}

function getIntentColor(intent: string) {
  if (intent.includes("LOGIN") || intent.includes("PASSWORD")) return "text-purple-400 bg-purple-400/10 border-purple-400/30";
  if (intent.includes("BILLING") || intent.includes("REFUND")) return "text-emerald-400 bg-emerald-400/10 border-emerald-400/30";
  return "text-blue-400 bg-blue-400/10 border-blue-400/30";
}

function getUrgencyColor(urgency: string) {
  switch (urgency.toUpperCase()) {
    case "CRITICAL": return "text-red-400 border-red-400/30 bg-red-400/10";
    case "HIGH": return "text-orange-400 border-orange-400/30 bg-orange-400/10";
    case "MEDIUM": return "text-yellow-400 border-yellow-400/30 bg-yellow-400/10";
    case "LOW": return "text-blue-400 border-blue-400/30 bg-blue-400/10";
    default: return "text-zinc-400 bg-zinc-800/50";
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
