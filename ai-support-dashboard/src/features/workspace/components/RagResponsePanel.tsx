import { BookOpen, Sparkles, Copy, Check } from "lucide-react";
import { useState } from "react";

interface RagResponsePanelProps {
  ragResponse: string;
}

export function RagResponsePanel({ ragResponse }: RagResponsePanelProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(ragResponse);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg overflow-hidden">
      <div className="bg-card border-b border-border p-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <BookOpen className="h-5 w-5 text-emerald-400" />
          <h3 className="font-semibold text-foreground">Knowledge</h3>
        </div>
      </div>

      <div className="p-4 flex flex-col gap-5">
        {/* Mocked Knowledge Articles Used */}
        <div>
          <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3 block flex items-center gap-1">
            <BookOpen className="h-3.5 w-3.5" /> Knowledge Used
          </span>
          <ul className="space-y-2 text-[13px] text-foreground/80 pl-1">
            <li className="flex items-center justify-between">
              <span className="flex items-center gap-2"><Check className="h-3.5 w-3.5 text-emerald-500" /> Subscription Upgrades</span>
              <span className="text-[11px] font-mono text-muted-foreground bg-muted px-1.5 py-0.5 rounded">94%</span>
            </li>
            <li className="flex items-center justify-between">
              <span className="flex items-center gap-2"><Check className="h-3.5 w-3.5 text-emerald-500" /> Billing Policy</span>
              <span className="text-[11px] font-mono text-muted-foreground bg-muted px-1.5 py-0.5 rounded">89%</span>
            </li>
          </ul>
        </div>

        <div className="border-t border-border/60" />

        {/* Generated Reply */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1">
              <Sparkles className="h-3.5 w-3.5 text-emerald-500" /> 
              Suggested Reply
            </span>
            <button 
              onClick={handleCopy}
              className="text-muted-foreground hover:text-foreground transition-colors bg-muted p-1.5 rounded-md"
              title="Copy suggested reply"
            >
              {copied ? <Check className="h-3.5 w-3.5 text-emerald-500" /> : <Copy className="h-3.5 w-3.5" />}
            </button>
          </div>
          <div className="bg-background border border-border rounded-lg p-3 text-sm text-foreground leading-relaxed relative group whitespace-pre-wrap max-h-64 overflow-y-auto">
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
        </div>
      </div>
    </div>
  );
}
