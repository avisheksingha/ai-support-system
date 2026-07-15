import { useState } from "react";
import { WorkflowList } from "../components/WorkflowList";
import { WorkflowDetail } from "../components/WorkflowDetail";

export function WorkflowExplorer() {
  const [selectedExecutionId, setSelectedExecutionId] = useState<string | null>("wf-exc-9042");

  return (
    <div className="flex h-full w-full bg-[#F8FAFC] overflow-hidden text-slate-800">
      {/* Left Pane: Execution List */}
      <div className="w-[320px] lg:w-[400px] shrink-0 border-r border-slate-200 bg-white flex flex-col z-10 shadow-[4px_0_24px_rgba(0,0,0,0.02)]">
        <WorkflowList selectedId={selectedExecutionId} onSelect={setSelectedExecutionId} />
      </div>

      {/* Right Pane: Execution Details */}
      <div className="flex-1 flex flex-col overflow-hidden relative">
        {selectedExecutionId ? (
          <WorkflowDetail executionId={selectedExecutionId} />
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-slate-400 p-8 text-center bg-gradient-to-br from-[#F8FAFC] to-white">
            <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mb-4 border border-slate-200 shadow-sm">
              <span className="text-2xl">⚡</span>
            </div>
            <h2 className="text-lg font-bold text-slate-700 mb-2">No Execution Selected</h2>
            <p className="text-sm max-w-[250px] leading-relaxed">Select a workflow execution from the list to view its orchestration trace, tool invocations, and AI decisions.</p>
          </div>
        )}
      </div>
    </div>
  );
}
