import { BookOpen, Sparkles, Copy, Check } from "lucide-react";
import { useState } from "react";
import type { KnowledgeModel } from "@/shared/types/workspace";

interface RagResponsePanelProps {
  knowledge: KnowledgeModel;
  onUseReply?: (text: string) => void;
}

export function RagResponsePanel({ knowledge, onUseReply }: RagResponsePanelProps) {
  const [copied, setCopied] = useState(false);
  
  const ragResponse = knowledge.knowledgeSummary;

  const handleCopy = () => {
    navigator.clipboard.writeText(ragResponse);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="bg-white shadow-sm border border-slate-200 rounded-xl overflow-hidden relative">
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-emerald-400 to-teal-500"></div>
      <div className="bg-slate-50 border-b border-slate-100 p-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="bg-white p-1.5 rounded-md shadow-sm border border-slate-100">
            <BookOpen className="h-4 w-4 text-emerald-500" />
          </div>
          <h3 className="font-bold text-slate-800 text-sm">Knowledge Base</h3>
        </div>
      </div>

      <div className="p-5 flex flex-col gap-5">

        {/* Generated Reply */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest flex items-center gap-1.5">
              <Sparkles className="h-3.5 w-3.5 text-emerald-500" /> 
              RAG Summary
            </span>
            <button 
              onClick={handleCopy}
              className="text-slate-400 hover:text-slate-700 transition-colors bg-white border border-slate-200 p-1.5 rounded-md shadow-sm"
              title="Copy summary"
            >
              {copied ? <Check className="h-3.5 w-3.5 text-emerald-500" /> : <Copy className="h-3.5 w-3.5" />}
            </button>
          </div>
          <div className="bg-slate-50 border border-slate-100 rounded-lg p-4 text-[13px] text-slate-700 leading-relaxed relative group whitespace-pre-wrap max-h-64 overflow-y-auto shadow-inner font-medium">
            {(() => {
              try {
                const parsed = JSON.parse(ragResponse);
                if (parsed && typeof parsed === 'object') {
                  return parsed.response || parsed.generatedReply || ragResponse;
                }
              } catch {
                // Ignore parse error, return raw string
              }
              return ragResponse;
            })()}
          </div>
          {onUseReply && (
            <div className="mt-3">
              <button 
                className="w-full py-2 bg-emerald-500 hover:bg-emerald-600 text-white font-semibold text-xs rounded-lg shadow-sm transition-colors flex items-center justify-center gap-2"
                onClick={() => onUseReply(ragResponse)}
              >
                <Check className="h-3.5 w-3.5" /> Use as Reply
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
