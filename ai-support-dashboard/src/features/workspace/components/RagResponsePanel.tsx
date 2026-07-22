import { BookOpen, Sparkles, Copy, Check, ExternalLink, ChevronDown, ChevronUp, AlertCircle } from "lucide-react";
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

  const knowledgeMatch = knowledge.knowledgeFound
    ? (knowledge.retrievedDocumentCount || 0) >= 3
      ? "HIGH"
      : "MEDIUM"
    : "LOW";

  const getKnowledgeMatchBadge = (match: string) => {
    switch (match) {
      case "HIGH":
        return <span className="px-2 py-0.5 rounded border text-[10px] font-bold uppercase bg-emerald-50 text-emerald-700 border-emerald-200">High Match</span>;
      case "MEDIUM":
        return <span className="px-2 py-0.5 rounded border text-[10px] font-bold uppercase bg-amber-50 text-amber-700 border-amber-200">Medium Match</span>;
      case "LOW":
      default:
        return <span className="px-2 py-0.5 rounded border text-[10px] font-bold uppercase bg-slate-100 text-slate-600 border-slate-200">Low Match</span>;
    }
  };

  return (
    <div className="bg-white shadow-sm border border-slate-200 rounded-xl overflow-hidden relative">
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-emerald-400 to-teal-500"></div>
      
      {/* Header */}
      <div className="bg-slate-50 border-b border-slate-100 p-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="bg-white p-1.5 rounded-md shadow-sm border border-slate-100">
            <BookOpen className="h-4 w-4 text-emerald-500" />
          </div>
          <div>
            <h3 className="font-bold text-slate-800 text-sm">Knowledge Base</h3>
            <span className="text-[10px] text-slate-400 font-medium">{knowledge.retrievedDocumentCount ?? 1} document(s) retrieved</span>
          </div>
        </div>
        
        <div className="flex items-center gap-1.5">
          <span className="px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 font-mono text-[10px] font-bold">
            {knowledge.retrievedDocumentCount ?? 1} Article{(knowledge.retrievedDocumentCount ?? 1) > 1 ? "s" : ""}
          </span>
          {getKnowledgeMatchBadge(knowledgeMatch)}
        </div>
      </div>

      <div className="p-4 flex flex-col gap-3 text-xs">
        {/* Matched Articles */}
        {knowledge.matchedArticleTitles && knowledge.matchedArticleTitles.length > 0 ? (
          <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider block mb-2">Matched Articles</span>
            <div className="space-y-1.5">
              {knowledge.matchedArticleTitles.map((title, i) => (
                <div key={i} className="flex items-center justify-between p-2 bg-white border border-slate-200 rounded text-xs">
                  <span className="truncate font-medium text-slate-800 max-w-[200px]" title={title}>{title}</span>
                  <button 
                    disabled
                    title="Article URL unavailable"
                    className="text-[10px] font-semibold text-slate-400 bg-slate-50 px-2 py-0.5 rounded border border-slate-200 flex items-center gap-1 shrink-0 cursor-not-allowed"
                  >
                    <ExternalLink className="h-3 w-3" /> Open Article
                  </button>
                </div>
              ))}
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
    </div>
  );
}
