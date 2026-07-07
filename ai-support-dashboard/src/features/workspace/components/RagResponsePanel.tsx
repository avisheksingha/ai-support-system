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
    <div className="bg-zinc-900/50 border border-zinc-800 rounded-xl overflow-hidden">
      <div className="bg-zinc-900 border-b border-zinc-800 p-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <BookOpen className="h-5 w-5 text-emerald-400" />
          <h3 className="font-semibold text-zinc-100">Knowledge</h3>
        </div>
      </div>

      <div className="p-4 flex flex-col gap-5">
        {/* Generated Reply */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold text-zinc-400 uppercase tracking-wider flex items-center gap-1">
              <Sparkles className="h-3 w-3 text-emerald-500" /> 
              Suggested Reply
            </span>
            <button 
              onClick={handleCopy}
              className="text-zinc-500 hover:text-zinc-200 transition-colors bg-zinc-800 p-1.5 rounded-md"
              title="Copy to clipboard"
            >
              {copied ? <Check className="h-3.5 w-3.5 text-emerald-500" /> : <Copy className="h-3.5 w-3.5" />}
            </button>
          </div>
          <div className="bg-zinc-950 border border-zinc-800 rounded-lg p-3 text-sm text-zinc-300 leading-relaxed relative group">
            {knowledge.generatedReply}
          </div>
        </div>

        {/* Source Documents */}
        {knowledge.sourceDocuments && knowledge.sourceDocuments.length > 0 && (
          <div>
             <span className="text-xs font-semibold text-zinc-400 uppercase tracking-wider mb-3 block">Knowledge Articles Used</span>
             <ul className="space-y-2">
               {knowledge.sourceDocuments.map((doc, idx) => (
                 <li key={idx} className="group border border-zinc-800 bg-zinc-900 rounded-lg p-3 cursor-pointer hover:border-zinc-700 transition-colors">
                   <div className="flex justify-between items-start mb-1">
                     <div className="flex items-center gap-2">
                       <FileText className="h-4 w-4 text-emerald-500/70" />
                       <span className="text-sm font-medium text-zinc-200 group-hover:text-emerald-400 transition-colors">{doc.title}</span>
                     </div>
                     {knowledge.similarityScore !== undefined && (
                        <span className="text-[10px] font-mono text-emerald-500/70 bg-emerald-500/10 px-1.5 py-0.5 rounded">
                          {(knowledge.similarityScore * 100).toFixed(0)}% match
                        </span>
                     )}
                   </div>
                   <div className="flex justify-between items-center mt-2">
                     <span className="text-[11px] text-zinc-500 line-clamp-1 max-w-[80%]">Excerpt preview unavailable.</span>
                     <ExternalLink className="h-3 w-3 text-zinc-600 group-hover:text-emerald-500 transition-colors" />
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
