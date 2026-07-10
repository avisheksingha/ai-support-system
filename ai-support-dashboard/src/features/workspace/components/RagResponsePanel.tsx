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
        {/* Generated Reply */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider flex items-center gap-1">
              <Sparkles className="h-3 w-3 text-emerald-500" /> 
              Suggested Reply
            </span>
            <button 
              onClick={handleCopy}
              className="text-muted-foreground hover:text-foreground transition-colors bg-muted p-1.5 rounded-md"
              title="Copy to clipboard"
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
