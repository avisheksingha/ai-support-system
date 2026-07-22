import { Sparkles, Check, Copy, Edit, RefreshCw, ThumbsUp, ThumbsDown, History, Info } from "lucide-react";
import { useState } from "react";
import type { AiDecisionModel } from "@/shared/types/workspace";
import { Button } from "@/components/ui/button";

interface AiDecisionPanelProps {
  decision: AiDecisionModel;
  onUseReply?: ((text: string) => void) | undefined;
}

export function AiDecisionPanel({ decision, onUseReply }: AiDecisionPanelProps) {
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

  const rawConfidence = decision.confidence;
  const confidencePct = (rawConfidence * 100).toFixed(0);
  const confidenceBadgeLabel = rawConfidence >= 0.8 ? `High Confidence (${confidencePct}%)` : rawConfidence >= 0.6 ? `Medium Confidence (${confidencePct}%)` : `Low Confidence (${confidencePct}%)`;

  const getConfidenceColor = (confidence: number) => {
    if (confidence >= 0.8) return "text-emerald-700 bg-emerald-50 border-emerald-200";
    if (confidence >= 0.6) return "text-amber-700 bg-amber-50 border-amber-200";
    return "text-red-700 bg-red-50 border-red-200";
  };

  const decisionExplanation = decision.decisionReason || "Decision explanation unavailable.";

  return (
    <div className="bg-white shadow-sm border border-slate-200 rounded-xl overflow-hidden relative">
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-purple-400 to-pink-500"></div>
      
      {/* Header */}
      <div className="bg-slate-50 border-b border-slate-100 p-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="bg-white p-1.5 rounded-md shadow-sm border border-slate-100">
            <Sparkles className="h-4 w-4 text-purple-500" />
          </div>
          <h3 className="font-bold text-slate-800 text-sm">AI Recommendation</h3>
        </div>
        <div className={`px-2 py-0.5 rounded-md border text-[10px] font-bold ${getConfidenceColor(decision.confidence)}`}>
          {confidenceBadgeLabel}
        </div>
      </div>

      <div className="p-4 flex flex-col gap-3.5 text-xs">
        
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

          <div className="flex items-start gap-1.5 text-[10.5px] text-slate-600 font-medium pt-1.5 border-t border-purple-100/60">
            <Info className="h-3.5 w-3.5 text-purple-600 shrink-0 mt-0.5" />
            <p>
              <strong className="text-purple-950">Decision Reason:</strong> {decisionExplanation}
            </p>
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
    </div>
  );
}
