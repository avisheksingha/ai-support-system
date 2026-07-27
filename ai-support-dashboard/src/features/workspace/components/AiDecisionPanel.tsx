import { Sparkles, Check, Copy, Edit, RefreshCw, ThumbsUp, ThumbsDown, History, Info } from "lucide-react";
import { useState } from "react";
import type { AiDecisionModel, AiDecisionContextModel } from "@/shared/types/workspace";
import { Button } from "@/components/ui/button";

interface AiDecisionPanelProps {
  decision: AiDecisionModel;
  context?: AiDecisionContextModel | undefined;
  onUseReply?: ((text: string) => void) | undefined;
}

export function AiDecisionPanel({ decision, context, onUseReply }: AiDecisionPanelProps) {
  const [copied, setCopied] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editedReply, setEditedReply] = useState(decision.suggestedReply);
  
  // Historical versions array for "Compare Previous Reply" feature
  const [replyHistory, setReplyHistory] = useState<string[]>([decision.suggestedReply]);
  const [activeVersionIndex, setActiveVersionIndex] = useState(0);
  const [showHistoryModal, setShowHistoryModal] = useState(false);

  // Agent feedback state
  const [feedback, setFeedback] = useState<"HELPFUL" | "NOT_HELPFUL" | null>(null);
  const [actionTag, setActionTag] = useState<"ACCEPTED" | "MODIFIED" | "REJECTED" | null>(null);

  const currentReply = isEditing ? editedReply : (replyHistory[activeVersionIndex] || decision.suggestedReply);

  const handleCopy = () => {
    navigator.clipboard.writeText(currentReply);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleUseReply = () => {
    setActionTag("ACCEPTED");
    onUseReply?.(currentReply);
    setIsEditing(false);
  };

  const handleRegenerate = () => {
    const regenerated = `${decision.suggestedReply}\n\n[Regenerated Option ${replyHistory.length + 1}]: Additional details provided regarding step-by-step account recovery instructions.`;
    setReplyHistory((prev) => [...prev, regenerated]);
    setActiveVersionIndex(replyHistory.length);
    setEditedReply(regenerated);
  };

  const decisionExplanation = decision.decisionReason || "Decision explanation unavailable.";

  let displayIntent = context?.intent || "General Inquiry";
  let displayCategory = context?.category || "General Support";

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
    <div className="text-xs flex flex-col gap-3.5">
      {/* Compact AI Recommendation Box */}
      <div className="p-3 bg-gradient-to-r from-purple-50 to-indigo-50/60 border border-purple-100/80 rounded-lg space-y-2 text-purple-950">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5 font-bold text-[10px] text-purple-700 uppercase tracking-wider">
            <Sparkles className="h-3.5 w-3.5 text-purple-600" />
            <span>Suggested Action</span>
          </div>
          <span className="text-[10px] font-semibold text-indigo-700 bg-white px-2 py-0.5 rounded border border-indigo-100 shadow-2xs">
            Est. Resolution: ~5 mins
          </span>
        </div>

        <div className="text-[11px] font-semibold text-slate-800">
          Send resolution guide &amp; verification steps
        </div>

        <div className="space-y-2 pt-2 border-t border-purple-100/60">
          {context ? (
            <div className="grid grid-cols-2 gap-1.5 text-[10.5px] bg-white/70 p-2.5 rounded border border-purple-100/60 shadow-2xs font-medium">
              <div><span className="text-slate-400 font-bold uppercase text-[9px] block">Intent</span> <span className="capitalize text-slate-700 font-semibold" title={displayIntent}>{displayIntent}</span></div>
              <div><span className="text-slate-400 font-bold uppercase text-[9px] block">Category</span> <span className="text-slate-700 font-semibold" title={displayCategory}>{displayCategory}</span></div>
              <div><span className="text-slate-400 font-bold uppercase text-[9px] block">Confidence</span> <span className="text-purple-700 font-bold">{(context.confidence * 100).toFixed(0)}%</span></div>
              <div><span className="text-slate-400 font-bold uppercase text-[9px] block">Knowledge Retrieval</span> <span className="text-emerald-700 font-semibold">{context.retrievedArticleCount} matching article{context.retrievedArticleCount !== 1 ? 's' : ''}</span></div>
              <div className="col-span-2 pt-1 border-t border-purple-50/80"><span className="text-slate-400 font-bold uppercase text-[9px] block">Routing Decision</span> <span className="text-cyan-800 font-semibold">{context.routingDecision}</span></div>
            </div>
          ) : null}

          <div className="flex items-start gap-1.5 text-[10.5px] text-slate-600 font-medium">
            <Info className="h-3.5 w-3.5 text-purple-600 shrink-0 mt-0.5" />
            <p className="leading-relaxed">
              <strong className="text-purple-950 font-bold">Decision Reason:</strong> {context?.decisionReason || decisionExplanation}
            </p>
          </div>
        </div>
      </div>

      {/* AI Assessment Section */}
      <div>
        <div className="flex items-center justify-between mb-1.5">
          <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider">
            AI Assessment Summary
          </span>
        </div>
        <div className="text-[11px] text-slate-700 leading-relaxed bg-slate-50 border border-slate-100 rounded-lg p-3 font-medium">
          {decision.aiSummary}
        </div>
      </div>

      {/* Suggested Reply Section */}
      <div>
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2">
            <span className="text-[9px] font-bold text-slate-500 uppercase tracking-wider">
              Suggested Reply
            </span>
            <span className="text-[10px] font-mono text-purple-700 bg-purple-50 px-1.5 py-0.2 rounded border border-purple-200">
              v{activeVersionIndex + 1} of {replyHistory.length}
            </span>
          </div>

          <div className="flex items-center gap-1">
            {replyHistory.length > 1 && (
              <button
                onClick={() => setShowHistoryModal(!showHistoryModal)}
                className="text-slate-500 hover:text-slate-800 transition-colors bg-white border border-slate-200 px-2 py-0.5 rounded text-[10px] font-semibold flex items-center gap-1"
                title="Compare reply history"
              >
                <History className="h-3 w-3 text-purple-600" /> Compare Previous
              </button>
            )}
            
            <button 
              onClick={handleCopy}
              className="text-slate-400 hover:text-slate-700 transition-colors bg-white border border-slate-200 p-1 rounded-md shadow-sm"
              title="Copy suggestion"
            >
              {copied ? <Check className="h-3 w-3 text-purple-500" /> : <Copy className="h-3 w-3" />}
            </button>

            <button 
              onClick={() => setIsEditing(!isEditing)}
              className="text-slate-400 hover:text-slate-700 transition-colors bg-white border border-slate-200 p-1 rounded-md shadow-sm"
              title="Edit suggestion"
            >
              <Edit className="h-3 w-3" />
            </button>
          </div>
        </div>

        {/* History Comparison Modal Dropdown */}
        {showHistoryModal && (
          <div className="mb-3 p-3 bg-slate-900 text-slate-100 rounded-lg text-xs space-y-2 font-mono">
            <div className="flex justify-between items-center pb-2 border-b border-slate-800">
              <span className="text-[10px] font-bold text-purple-300 uppercase">Version History Comparison</span>
              <button onClick={() => setShowHistoryModal(false)} className="text-slate-400 hover:text-white text-[10px]">Close</button>
            </div>
            <div className="space-y-2 max-h-40 overflow-y-auto">
              {replyHistory.map((h, i) => (
                <button
                  key={i}
                  onClick={() => {
                    setActiveVersionIndex(i);
                    setEditedReply(h);
                    setShowHistoryModal(false);
                  }}
                  className={`w-full text-left p-2 rounded text-[11px] border transition-colors ${
                    activeVersionIndex === i
                      ? "bg-purple-900/60 border-purple-500 text-white"
                      : "bg-slate-800 border-slate-700 text-slate-300 hover:bg-slate-700"
                  }`}
                >
                  <div className="font-bold text-[10px] mb-1">Version {i + 1} {i === replyHistory.length - 1 ? "(Latest)" : ""}</div>
                  <p className="line-clamp-2 text-[10px] font-sans text-slate-300">{h}</p>
                </button>
              ))}
            </div>
          </div>
        )}

        {isEditing ? (
          <textarea
            className="w-full bg-purple-50/50 border border-purple-100/50 rounded-lg p-3 text-[11px] text-slate-800 leading-relaxed min-h-[100px] resize-y focus:outline-none focus:ring-2 focus:ring-purple-500/50"
            value={editedReply}
            onChange={(e) => setEditedReply(e.target.value)}
          />
        ) : (
          <div className="bg-purple-50/50 border border-purple-100/50 rounded-lg p-3 text-[11px] text-slate-800 leading-relaxed whitespace-pre-wrap max-h-48 overflow-y-auto shadow-inner font-medium">
            {currentReply}
          </div>
        )}
      </div>

      {/* Action Buttons */}
      <div className="flex gap-2 pt-2 border-t border-slate-100">
        <Button 
          size="sm"
          variant="outline"
          className="h-7 text-xs font-medium gap-1.5 flex-1"
          onClick={handleRegenerate}
        >
          <RefreshCw className="h-3 w-3" />
          Regenerate
        </Button>
        <Button 
          size="sm"
          className="h-7 text-xs font-medium gap-1.5 flex-1 bg-purple-600 hover:bg-purple-700 text-white"
          onClick={handleUseReply}
        >
          <Check className="h-3 w-3" /> Insert into Reply Editor
        </Button>
      </div>

      {/* Agent Feedback & Quality Controls */}
      <div className="pt-2 border-t border-slate-100 flex items-center justify-between text-[11px] text-slate-500">
        <span className="font-medium text-[10px] uppercase text-slate-400 font-bold">Agent AI Feedback:</span>
        
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1 bg-slate-100 p-0.5 rounded border border-slate-200">
            <button
              onClick={() => setFeedback(feedback === "HELPFUL" ? null : "HELPFUL")}
              className={`p-1 rounded transition-colors ${
                feedback === "HELPFUL" ? "bg-emerald-500 text-white" : "text-slate-500 hover:text-slate-900"
              }`}
              title="Helpful suggestion"
            >
              <ThumbsUp className="h-3 w-3" />
            </button>
            <button
              onClick={() => setFeedback(feedback === "NOT_HELPFUL" ? null : "NOT_HELPFUL")}
              className={`p-1 rounded transition-colors ${
                feedback === "NOT_HELPFUL" ? "bg-rose-500 text-white" : "text-slate-500 hover:text-slate-900"
              }`}
              title="Not helpful suggestion"
            >
              <ThumbsDown className="h-3 w-3" />
            </button>
          </div>

          {actionTag && (
            <span className="px-2 py-0.5 rounded bg-emerald-50 text-emerald-800 border border-emerald-200 font-bold text-[10px] uppercase">
              {actionTag}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
