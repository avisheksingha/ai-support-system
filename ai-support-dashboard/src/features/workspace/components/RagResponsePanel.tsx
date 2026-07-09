import { BookOpen, Sparkles, Copy, Check, FileText, ExternalLink } from "lucide-react";
import { useState } from "react";
import type { KnowledgeModel } from "@/shared/types/workspace";

interface RagResponsePanelProps {
  knowledge: KnowledgeModel;
}

export function RagResponsePanel({ knowledge }: RagResponsePanelProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(knowledge.generatedReply);
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
          <div className="bg-background border border-border rounded-lg p-3 text-sm text-foreground leading-relaxed relative group">
            {knowledge.generatedReply}
          </div>
        </div>

        {/* Source Documents */}
        {knowledge.sourceDocuments && knowledge.sourceDocuments.length > 0 && (
          <div>
             <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3 block">Knowledge Articles Used</span>
             <ul className="space-y-2">
               {knowledge.sourceDocuments.map((doc, idx) => (
                 <li key={idx} className="group border border-border bg-card rounded-lg p-3 cursor-pointer hover:border-border transition-colors">
                   <div className="flex justify-between items-start mb-1">
                     <div className="flex items-center gap-2">
                       <FileText className="h-4 w-4 text-emerald-500/70" />
                       <span className="text-sm font-medium text-foreground group-hover:text-emerald-400 transition-colors">{doc.title}</span>
                     </div>
                     {knowledge.similarityScore !== undefined && (
                        <span className="text-[10px] font-mono text-emerald-500/70 bg-emerald-500/10 px-1.5 py-0.5 rounded">
                          {(knowledge.similarityScore * 100).toFixed(0)}% match
                        </span>
                     )}
                   </div>
                   <div className="flex justify-between items-center mt-2">
                     <span className="text-[11px] text-muted-foreground line-clamp-1 max-w-[80%]">Excerpt preview unavailable.</span>
                     <ExternalLink className="h-3 w-3 text-muted-foreground group-hover:text-emerald-500 transition-colors" />
                   </div>
                 </li>
               ))}
             </ul>
          </div>
        )}
      </div>
    </div>
  );
}
