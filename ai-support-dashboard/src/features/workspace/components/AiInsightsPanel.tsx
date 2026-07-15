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
    <div className="bg-white shadow-sm border border-slate-200 rounded-xl overflow-hidden relative">
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-400 to-indigo-500"></div>
      <div className="bg-slate-50 border-b border-slate-100 p-4 flex items-center gap-2">
        <div className="bg-white p-1.5 rounded-md shadow-sm border border-slate-100">
          <BrainCircuit className="h-4 w-4 text-blue-600" />
        </div>
        <h3 className="font-bold text-slate-800 text-sm">AI Command Center</h3>
      </div>
      
      <div className="p-5 space-y-4 text-[13px]">
        <div className="grid grid-cols-2 gap-4">
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest block mb-1">Model</span>
            <div className="font-semibold text-slate-800">Gemini 2.5 Flash</div>
          </div>
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest block mb-1">Confidence</span>
            <div className="font-bold text-emerald-600 flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
              {(rawScore * 100).toFixed(0)}%
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest block mb-1">Intent</span>
            <div className="font-semibold text-indigo-700">{formatSemanticString(analysis.intent)}</div>
          </div>
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest block mb-1">Sentiment</span>
            <div className="font-semibold text-slate-700">{formatSemanticString(analysis.sentiment || "NEUTRAL")}</div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest block mb-1">Urgency</span>
            <div className="font-semibold text-rose-600">{formatSemanticString(analysis.urgency)}</div>
          </div>
          <div className="bg-slate-50 rounded-lg p-3 border border-slate-100">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest block mb-1">Suggested Category</span>
            <div className="font-semibold text-slate-700 truncate">{formatSemanticString(analysis.suggestedCategory || "General")}</div>
          </div>
        </div>

        <div className="pt-2 border-t border-slate-100 mt-2">
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2 block">Suggested Resolution</span>
          <div className="text-slate-600 leading-relaxed text-[13px] bg-indigo-50/50 p-3 rounded-lg border border-indigo-100/50 font-medium">
            Verify user identity and process the password reset manually using the admin portal. Share knowledge article #KB-904 with the user.
          </div>
        </div>

        <div className="pt-2 border-t border-slate-100 mt-2">
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-2 block text-indigo-600">What should I do next?</span>
          <div className="flex flex-col gap-2">
            <button className="text-left px-3 py-2.5 bg-indigo-600 border border-transparent rounded-lg text-xs font-semibold text-white shadow-sm hover:bg-indigo-700 transition-colors flex justify-between items-center group">
              <span>Run Diagnostics Workflow</span>
              <span className="opacity-70 group-hover:opacity-100">→</span>
            </button>
            <button className="text-left px-3 py-2 bg-white border border-slate-200 rounded-lg text-xs font-semibold text-slate-700 shadow-sm hover:bg-slate-50 transition-colors flex justify-between items-center group">
              <span>Send Identity Verification Email</span>
              <span className="opacity-50 group-hover:opacity-100">→</span>
            </button>
          </div>
        </div>

        <div className="pt-2 border-t border-slate-100 mt-2">
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-3 flex items-center gap-1.5">
            <Key className="h-3 w-3" /> Extracted Keywords
          </span>
          <div className="flex flex-wrap gap-1.5 mt-2">
            {keywords.length > 0 ? (
              keywords.map((kw, i) => (
                <span key={i} className="text-[11px] font-medium px-2.5 py-1 bg-white border border-slate-200 text-slate-700 rounded-md shadow-sm">
                  {kw}
                </span>
              ))
            ) : (
              <span className="text-[11px] text-slate-400 italic">No keywords detected</span>
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

