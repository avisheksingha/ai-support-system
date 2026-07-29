import { Sparkles, Copy, Check, ExternalLink, ChevronDown, ChevronUp, AlertCircle } from "lucide-react";
import { useState } from "react";
import type { KnowledgeModel } from "@/shared/types/workspace";

interface RagResponsePanelProps {
  knowledge: KnowledgeModel;
}

export function RagResponsePanel({ knowledge }: RagResponsePanelProps) {
  const [copied, setCopied] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);
  
  const knowledgeSummary = knowledge.knowledgeSummary;

  const handleCopy = () => {
    navigator.clipboard.writeText(knowledgeSummary);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const parseKnowledgeSummary = (summary: string) => {
    try {
      const parsed = JSON.parse(summary);
      if (parsed && typeof parsed === 'object') {
        return parsed.response || parsed.generatedReply || summary;
      }
    } catch {
      // Ignore parse error, return raw string
    }
    return summary;
  };

  const getArticleCategory = (category?: string) => {
    if (!category || category === "General" || category === "Unknown" || category === "None") {
      return { label: "Knowledge Article", style: "bg-slate-100 text-slate-600 border-slate-200" };
    }
    
    const c = category.toLowerCase();
    let style = "bg-indigo-50 text-indigo-700 border-indigo-200";
    
    if (c.includes("identi") || c.includes("access") || c.includes("iam") || c.includes("auth") || c.includes("login")) {
      style = "bg-blue-50 text-blue-700 border-blue-200";
    } else if (c.includes("bill") || c.includes("account") || c.includes("pay") || c.includes("subscrip")) {
      style = "bg-emerald-50 text-emerald-700 border-emerald-200";
    } else if (c.includes("network") || c.includes("infrastruc") || c.includes("connect") || c.includes("latenc") || c.includes("core")) {
      style = "bg-purple-50 text-purple-700 border-purple-200";
    } else if (c.includes("defect") || c.includes("software") || c.includes("engineer") || c.includes("bug") || c.includes("crash")) {
      style = "bg-rose-50 text-rose-700 border-rose-200";
    }
    
    return { label: category, style };
  };

  return (
    <div className="text-xs flex flex-col gap-3">
      {/* Matched Articles */}
      {((knowledge.sources && knowledge.sources.length > 0) || (knowledge.matchedArticleTitles && knowledge.matchedArticleTitles.length > 0)) ? (
        <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-2">
            Matched Articles ({knowledge.sources?.length || knowledge.matchedArticleTitles?.length || 0})
          </span>
          <div className="space-y-1.5">
            {(knowledge.sources && knowledge.sources.length > 0 ? knowledge.sources : (knowledge.matchedArticleTitles || []).map((title, id) => ({ id: String(id), title, category: undefined, similarityScore: undefined, hybridScore: undefined, vectorScore: undefined }))).map((source, i) => {
              const cat = getArticleCategory(source.category);
              const score = source.hybridScore ?? source.similarityScore ?? source.vectorScore;
              return (
                <div key={i} className="flex items-center justify-between p-2 bg-white border border-slate-200 rounded text-xs gap-2">
                  <div className="flex items-center gap-2 min-w-0 flex-1">
                    <span className="truncate font-medium text-slate-800" title={source.title}>{source.title}</span>
                    <span className={`px-1.5 py-0.5 rounded text-[9px] font-bold uppercase border shrink-0 ${cat.style}`}>
                      {cat.label}
                    </span>
                    {score !== undefined && score !== null && score > 0 && (
                      <span className="text-[10px] text-slate-500 font-semibold shrink-0" title="Retrieval match score">
                        {(score * 100).toFixed(0)}%
                      </span>
                    )}
                  </div>
                  <button 
                    disabled
                    title="Article URL unavailable"
                    className="text-[10px] font-semibold text-slate-400 bg-slate-50 px-2 py-0.5 rounded border border-slate-200 flex items-center gap-1 shrink-0 cursor-not-allowed"
                  >
                    <ExternalLink className="h-3 w-3" /> Open
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      ) : (
        /* Empty State */
        <div className="bg-slate-50 p-3 rounded-lg border border-dashed border-slate-200 text-center text-slate-500">
          <AlertCircle className="h-4 w-4 text-slate-400 mx-auto mb-1" />
          <span className="text-[11px] font-medium block">No matching knowledge articles retrieved.</span>
          <span className="text-[10px] text-slate-400">General fallback resolution rules applied.</span>
        </div>
      )}

      {/* Knowledge Summary */}
      {knowledge.knowledgeFound && knowledgeSummary && (
        <div>
          <div className="flex items-center justify-between mb-2">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1.5">
              <Sparkles className="h-3 w-3 text-emerald-500" /> 
              Knowledge Summary
            </span>

            <div className="flex items-center gap-1">
              <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="text-[10px] font-medium text-slate-500 hover:text-slate-800 bg-white border border-slate-200 px-2 py-0.5 rounded flex items-center gap-1 transition-colors"
              >
                {isExpanded ? (
                  <>
                    <ChevronUp className="h-3 w-3" /> Collapse
                  </>
                ) : (
                  <>
                    <ChevronDown className="h-3 w-3" /> Expand
                  </>
                )}
              </button>

              <button 
                onClick={handleCopy}
                className="text-slate-400 hover:text-slate-700 transition-colors bg-white border border-slate-200 p-1 rounded-md shadow-sm"
                title="Copy summary"
              >
                {copied ? <Check className="h-3 w-3 text-emerald-500" /> : <Copy className="h-3 w-3" />}
              </button>
            </div>
          </div>

          <div className={`bg-slate-50 border border-slate-100 rounded-lg p-3 text-[11px] text-slate-700 leading-relaxed whitespace-pre-wrap font-medium shadow-inner transition-all ${
            isExpanded ? "max-h-96 overflow-y-auto" : "line-clamp-4"
          }`}>
            {parseKnowledgeSummary(knowledgeSummary)}
          </div>
        </div>
      )}
    </div>
  );
}
