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
        {/* Mocked Knowledge Articles Used */}
        <div>
          <span className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-3 block flex items-center gap-1.5">
            <BookOpen className="h-3.5 w-3.5" /> Sources Used
          </span>
          <ul className="space-y-2 text-[13px] text-slate-700 pl-1">
            <li className="bg-slate-50 p-3 rounded-lg border border-slate-100 flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-2 font-semibold text-slate-800"><Check className="h-4 w-4 text-emerald-500" /> Subscription Upgrades</span>
                <span className="text-[11px] font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded shadow-sm border border-emerald-100">94% match</span>
              </div>
              <div className="flex items-center gap-3 text-[10px] text-slate-500 font-medium pl-6">
                <span className="bg-white px-1.5 py-0.5 rounded border border-slate-200">Billing</span>
                <span>Updated: 2 days ago</span>
                <span>Audience: All Agents</span>
              </div>
            </li>
            <li className="bg-slate-50 p-3 rounded-lg border border-slate-100 flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-2 font-semibold text-slate-800"><Check className="h-4 w-4 text-emerald-500" /> Billing Policy</span>
                <span className="text-[11px] font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded shadow-sm border border-emerald-100">89% match</span>
              </div>
              <div className="flex items-center gap-3 text-[10px] text-slate-500 font-medium pl-6">
                <span className="bg-white px-1.5 py-0.5 rounded border border-slate-200">Policy</span>
                <span>Updated: 1 week ago</span>
                <span>Audience: Tier 2</span>
              </div>
            </li>
          </ul>
        </div>

        <div className="border-t border-slate-100" />

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
        </div>
      </div>
    </div>
  );
}
