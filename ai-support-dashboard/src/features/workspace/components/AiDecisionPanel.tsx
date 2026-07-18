import { Sparkles, Check, Copy } from "lucide-react";
import { useState } from "react";
import type { AiDecisionModel } from "@/shared/types/workspace";
import { Button } from "@/components/ui/button";

interface AiDecisionPanelProps {
  decision: AiDecisionModel;
  onUseReply?: (text: string) => void;
}

export function AiDecisionPanel({ decision, onUseReply }: AiDecisionPanelProps) {
  const [copied, setCopied] = useState(false);
  
  const suggestedReply = decision.suggestedReply;

  const handleCopy = () => {
    navigator.clipboard.writeText(suggestedReply);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="bg-white shadow-sm border border-slate-200 rounded-xl overflow-hidden relative mt-6">
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-purple-400 to-pink-500"></div>
      <div className="bg-slate-50 border-b border-slate-100 p-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="bg-white p-1.5 rounded-md shadow-sm border border-slate-100">
            <Sparkles className="h-4 w-4 text-purple-500" />
          </div>
          <h3 className="font-bold text-slate-800 text-sm">AI Suggested Reply</h3>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">Confidence</span>
          <div className="font-bold text-purple-600 flex items-center gap-1.5 text-xs">
            <span className="w-1.5 h-1.5 rounded-full bg-purple-500"></span>
            {(decision.confidence * 100).toFixed(0)}%
          </div>
        </div>
      </div>

      <div className="p-5 flex flex-col gap-5">
        <div>
          <div className="flex items-center justify-between mb-3">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">
              AI Summary
            </span>
          </div>
          <div className="text-[13px] text-slate-700 leading-relaxed bg-slate-50 border border-slate-100 rounded-lg p-4 mb-4 font-medium italic">
            {decision.aiSummary}
          </div>

          <div className="flex items-center justify-between mb-3 mt-2">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest">
              Suggested Reply
            </span>
            <button 
              onClick={handleCopy}
              className="text-slate-400 hover:text-slate-700 transition-colors bg-white border border-slate-200 p-1.5 rounded-md shadow-sm"
              title="Copy suggestion"
            >
              {copied ? <Check className="h-3.5 w-3.5 text-purple-500" /> : <Copy className="h-3.5 w-3.5" />}
            </button>
          </div>
          <div className="bg-purple-50/50 border border-purple-100/50 rounded-lg p-4 text-[13px] text-slate-800 leading-relaxed relative group whitespace-pre-wrap max-h-64 overflow-y-auto shadow-inner font-medium">
            {suggestedReply}
          </div>
          
          {onUseReply && (
            <div className="mt-4">
              <Button 
                className="w-full font-semibold"
                onClick={() => onUseReply(suggestedReply)}
              >
                <Check className="h-3.5 w-3.5" /> Use as Reply
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
