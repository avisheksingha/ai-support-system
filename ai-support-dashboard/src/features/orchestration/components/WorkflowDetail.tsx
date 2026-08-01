import { useState } from "react";
import { formatTime, formatDuration } from "@/shared/utils/date";
import {
  CheckCircle2,
  AlertCircle,
  Bot,
  Shield,
  Wrench,
  ChevronDown,
  ChevronUp,
  Network,
  Zap,
  BookOpen,
  Send,
  Radio,
  Copy,
  Download,
  Check,
  Terminal
} from "lucide-react";
import { useWorkflowDetail, type WorkflowStep } from "../hooks/useWorkflowDetail";
import { ExecutionGraph, type ExecutionGraphNode } from "./ExecutionGraph";
import { Skeleton } from "@/components/ui/skeleton";

export function WorkflowDetail({ executionId }: { executionId: string }) {
  const { data: steps, isLoading, error } = useWorkflowDetail(executionId);
  const [diagnosticsOpen, setDiagnosticsOpen] = useState(false);
  const [copiedJson, setCopiedJson] = useState(false);
  const [activeStageTab, setActiveStageTab] = useState<"ALL" | "BUSINESS" | "TECHNICAL">("ALL");

  if (isLoading) {
    return (
      <div className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 flex flex-col items-start bg-[#F8FAFC]">
        <div className="w-full bg-white shadow-sm border border-slate-200 rounded-xl p-6 mb-8 relative overflow-hidden">
          <Skeleton className="h-8 w-64 mb-4" />
          <Skeleton className="h-4 w-96 mb-6" />
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[1, 2, 3, 4].map((i) => (
              <Skeleton key={i} className="h-16 w-full" />
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex-1 flex items-center justify-center p-8 text-center">
        <div className="text-red-500 text-sm font-medium bg-red-50 p-4 rounded-xl border border-red-200">
          Failed to load workflow execution trace
        </div>
      </div>
    );
  }

  if (!steps || steps.length === 0) {
    return <div className="p-8 text-slate-500">No trace data available for this execution.</div>;
  }

  // Calculated Metrics
  const totalDurationMs = steps.reduce((acc, s) => acc + (s.durationMs || 0), 0);
  const hasFailure = steps.some((s) => s.status === "FAILED");
  const lastStep = steps[steps.length - 1];
  
  const status = hasFailure
    ? lastStep?.status === "SUCCESS"
      ? "RECOVERED"
      : "FAILED"
    : "COMPLETED";

  const aiCallsCount = steps.filter((s) => s.type === "AI_PLANNING" || s.type === "WORKFLOW_DECISION").length;
  
  // Extract docs retrieved from payload if present
  const docsRetrievedCount = steps.reduce((acc, s) => {
    const docCount = s.payload?.retrievedDocumentCount || s.payload?.attributes?.knowledgeContext?.retrievedDocumentCount || 0;
    return acc + Number(docCount);
  }, 0);

  const retryCount = steps.filter((s) => s.status === "RETRYING").length;
  const errorCount = steps.filter((s) => s.status === "FAILED").length;

  const modelUsed =
    steps.find((s) => s.payload?.model)?.payload?.model ||
    steps.find((s) => s.payload?.attributes?.knowledgeContext?.model)?.payload?.attributes?.knowledgeContext?.model ||
    "gemini-2.5-flash";

  // Dynamic Nodes for Execution Graph
  const graphNodes: ExecutionGraphNode[] = steps.map((s) => ({
    id: s.id,
    name: s.stageName || s.name,
    type: s.type,
    status: s.status,
    durationMs: s.durationMs,
    percentage: s.percentage,
    category: s.category,
  }));

  const businessSteps = steps.filter((s) => s.category === "BUSINESS");
  const technicalSteps = steps.filter((s) => s.category === "TECHNICAL");
  const displayedSteps =
    activeStageTab === "BUSINESS"
      ? businessSteps
      : activeStageTab === "TECHNICAL"
      ? technicalSteps
      : steps;

  const handleCopyJson = () => {
    navigator.clipboard.writeText(JSON.stringify(parseAndCleanJson(steps), null, 2));
    setCopiedJson(true);
    setTimeout(() => setCopiedJson(false), 2000);
  };

  const handleDownloadJson = () => {
    const blob = new Blob([JSON.stringify(parseAndCleanJson(steps), null, 2)], {
      type: "application/json",
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `workflow-${executionId}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 flex flex-col bg-[#F8FAFC] space-y-6">
      
      {/* Header Panel */}
      <div className="w-full bg-white shadow-sm border border-slate-200 rounded-xl p-5 sm:p-6 relative shrink-0">
        <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-purple-500 via-indigo-500 to-blue-500 rounded-t-xl"></div>
        
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-4 pt-1">
          <div className="space-y-1 max-w-full">
            <span className="text-[10px] sm:text-xs font-bold uppercase tracking-widest text-slate-400 block">
              Execution Inspector
            </span>

            <div className="flex items-center gap-2.5 flex-wrap">
              <h1 className="text-lg sm:text-xl font-bold text-slate-900 leading-tight font-mono tracking-tight break-all">
                {executionId}
              </h1>
              {getStatusBadge(status)}
            </div>

            <p className="text-xs text-slate-500">
              Operational workflow trace • Model:{" "}
              <span className="font-mono text-purple-700 bg-purple-50 px-1.5 py-0.5 rounded border border-purple-200 font-semibold">
                {modelUsed}
              </span>
            </p>
          </div>

          <div className="flex items-center gap-3 text-xs shrink-0">
            <div className="text-right hidden sm:block">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">Started</span>
              <span className="text-slate-700 font-mono font-medium">{steps[0]?.timestamp ? formatTime(steps[0].timestamp) : 'N/A'}</span>
            </div>
            <div className="text-right hidden sm:block border-l border-slate-200 pl-3">
              <span className="text-[10px] text-slate-400 uppercase font-bold tracking-wider block">Total Duration</span>
              <span className="text-purple-700 font-mono font-bold">{formatDuration(totalDurationMs)}</span>
            </div>
          </div>
        </div>

        {/* Compact Workflow Metrics Summary Card */}
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-2.5 pt-4 border-t border-slate-100 shrink-0">
          <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-100">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">Execution Time</span>
            <span className="text-xs sm:text-sm font-mono font-bold text-slate-800">{formatDuration(totalDurationMs)}</span>
          </div>

          <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-100">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">Stages</span>
            <span className="text-xs sm:text-sm font-bold text-slate-800">{steps.length} ops</span>
          </div>

          <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-100">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">AI Calls</span>
            <span className="text-xs sm:text-sm font-bold text-purple-700">{aiCallsCount} calls</span>
          </div>

          <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-100">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">Knowledge Docs</span>
            <span className="text-xs sm:text-sm font-bold text-teal-700">{docsRetrievedCount} docs</span>
          </div>

          <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-100">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">Retries / Errors</span>
            <span className={`text-xs sm:text-sm font-bold ${errorCount > 0 ? 'text-red-600' : 'text-slate-700'}`}>
              {retryCount} / {errorCount}
            </span>
          </div>

          <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-100">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">Token Usage</span>
            <span className="text-xs font-mono font-medium text-slate-600">367 total</span>
          </div>

          <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-100">
            <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-0.5">Est. Cost</span>
            <span className="text-xs font-mono font-medium text-emerald-700">&lt; $0.001</span>
          </div>
        </div>
      </div>

      {/* Dynamic Execution Dependency Graph */}
      <ExecutionGraph nodes={graphNodes} totalDurationMs={totalDurationMs} />

      {/* Main Execution Trace Details */}
      <div className="w-full shrink-0 space-y-6">
        
        {/* Stage Category Tab Bar */}
        <div className="flex items-center justify-between border-b border-slate-200 pb-3">
          <div className="flex items-center gap-2">
            <h2 className="text-xs font-bold text-slate-600 uppercase tracking-widest flex items-center gap-2">
              <Network className="h-4 w-4 text-purple-600" /> Workflow Progression Trace
            </h2>
          </div>

          <div className="flex items-center gap-1.5 bg-slate-200/60 p-1 rounded-lg">
            <button
              onClick={() => setActiveStageTab("ALL")}
              className={`text-[11px] font-bold px-3 py-1 rounded-md transition-all ${
                activeStageTab === "ALL"
                  ? "bg-white text-purple-900 shadow-xs"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              All Stages ({steps.length})
            </button>

            <button
              onClick={() => setActiveStageTab("BUSINESS")}
              className={`text-[11px] font-bold px-3 py-1 rounded-md transition-all ${
                activeStageTab === "BUSINESS"
                  ? "bg-white text-purple-900 shadow-xs"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              Business ({businessSteps.length})
            </button>

            <button
              onClick={() => setActiveStageTab("TECHNICAL")}
              className={`text-[11px] font-bold px-3 py-1 rounded-md transition-all ${
                activeStageTab === "TECHNICAL"
                  ? "bg-white text-purple-900 shadow-xs"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              Technical ({technicalSteps.length})
            </button>
          </div>
        </div>

        {/* Progression Stage Cards */}
        <div className="space-y-4">
          {displayedSteps.map((step, idx) => (
            <StageDetailCard
              key={step.id || idx}
              step={step}
              index={idx}
              totalMs={totalDurationMs}
            />
          ))}
        </div>

        {/* Collapsible Diagnostics Section */}
        <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden mt-8">
          <button
            onClick={() => setDiagnosticsOpen(!diagnosticsOpen)}
            className="w-full px-5 py-4 bg-slate-50 border-b border-slate-200/80 flex items-center justify-between hover:bg-slate-100/60 transition-colors text-left"
          >
            <div className="flex items-center gap-2.5">
              <Terminal className="h-4 w-4 text-slate-600" />
              <span className="text-xs font-bold text-slate-800 uppercase tracking-widest">
                Diagnostics &amp; Raw Execution Data
              </span>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs text-slate-400 font-mono">Raw Payload &amp; Metadata</span>
              {diagnosticsOpen ? (
                <ChevronUp className="h-4 w-4 text-slate-400" />
              ) : (
                <ChevronDown className="h-4 w-4 text-slate-400" />
              )}
            </div>
          </button>

          {diagnosticsOpen && (
            <div className="p-5 space-y-4 bg-slate-900 text-slate-100 font-mono text-xs">
              <div className="flex items-center justify-between pb-3 border-b border-slate-800">
                <span className="text-[11px] text-slate-400 font-bold uppercase tracking-wider">
                  Raw JSON Payload
                </span>
                <div className="flex items-center gap-2">
                  <button
                    onClick={handleCopyJson}
                    className="flex items-center gap-1.5 px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] transition-colors"
                  >
                    {copiedJson ? (
                      <>
                        <Check className="h-3 w-3 text-emerald-400" /> Copied
                      </>
                    ) : (
                      <>
                        <Copy className="h-3 w-3" /> Copy JSON
                      </>
                    )}
                  </button>

                  <button
                    onClick={handleDownloadJson}
                    className="flex items-center gap-1.5 px-2.5 py-1 rounded bg-purple-900/60 hover:bg-purple-800/80 text-purple-200 text-[11px] transition-colors"
                  >
                    <Download className="h-3 w-3" /> Download JSON
                  </button>
                </div>
              </div>

              <pre className="p-4 bg-slate-950 rounded-lg overflow-x-auto text-[11px] leading-relaxed text-slate-300 border border-slate-800 max-h-[400px]">
                {JSON.stringify(parseAndCleanJson(steps), null, 2)}
              </pre>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}

function StageDetailCard({
  step,
  index: _index,
  totalMs: _totalMs,
}: {
  step: WorkflowStep;
  index: number;
  totalMs: number;
}) {
  const [expanded, setExpanded] = useState(false);

  const payload = step.payload || {};
  const attrs = payload.attributes || {};
  const analysis = attrs.analysisResult || {};
  const knowledge = attrs.knowledgeContext || {};
  const routing = attrs.routingDecision || {};

  const percentage = step.percentage || 0;
  const isFailed = step.status === "FAILED";
  const isSkipped = step.status === "SKIPPED";

  return (
    <div
      className={`bg-white rounded-xl border transition-all shadow-xs ${
        isFailed
          ? "border-red-200 bg-red-50/20"
          : isSkipped
          ? "border-slate-200 opacity-60 bg-slate-50/40"
          : "border-slate-200 hover:border-slate-300"
      }`}
    >
      <div
        className="p-4 sm:p-5 cursor-pointer flex flex-col sm:flex-row sm:items-center justify-between gap-3"
        onClick={() => setExpanded(!expanded)}
      >
        <div className="flex items-center gap-3">
          <div
            className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 border ${getStageIconBg(
              step.type,
              step.status
            )}`}
          >
            {getStageIcon(step.type)}
          </div>

          <div>
            <div className="flex items-center gap-2 flex-wrap mb-1">
              <span className="text-xs font-bold text-slate-900 font-mono">
                {step.stageName}
              </span>
              <span
                className={`text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded border ${
                  step.category === "BUSINESS"
                    ? "bg-purple-50 text-purple-700 border-purple-200"
                    : "bg-slate-100 text-slate-600 border-slate-200"
                }`}
              >
                {step.category}
              </span>
              {getStageStatusBadge(step.status)}
            </div>

            <p className="text-xs text-slate-500 line-clamp-1">{step.summary}</p>
          </div>
        </div>

        <div className="flex items-center gap-3 self-end sm:self-center">
          {/* Latency & Bottleneck percentage */}
          <div className="text-right">
            <span className="text-xs font-mono font-bold text-slate-800 block">
              {formatDuration(step.durationMs)}
            </span>
            {percentage > 0 && (
              <span
                className={`text-[10px] font-mono px-1.5 py-0.2 rounded border ${
                  percentage >= 50
                    ? "bg-red-50 text-red-700 border-red-200 font-bold"
                    : percentage >= 20
                    ? "bg-amber-50 text-amber-700 border-amber-200"
                    : "bg-slate-100 text-slate-600 border-slate-200"
                }`}
              >
                {percentage}% of total time
              </span>
            )}
          </div>

          {expanded ? (
            <ChevronUp className="h-4 w-4 text-slate-400 shrink-0" />
          ) : (
            <ChevronDown className="h-4 w-4 text-slate-400 shrink-0" />
          )}
        </div>
      </div>

      {/* Expanded Domain Stage Explanations */}
      {expanded && (
        <div className="px-5 pb-5 pt-3 border-t border-slate-100 bg-slate-50/40 rounded-b-xl space-y-4">
          
          {/* Failure Error Traceback */}
          {step.error && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-red-800 text-xs font-mono space-y-1">
              <div className="flex items-center gap-2 font-bold text-red-900">
                <AlertCircle className="h-4 w-4 shrink-0 text-red-600" /> Stage Execution Error
              </div>
              <p className="leading-relaxed">{step.error}</p>
            </div>
          )}

          {/* AI Analysis Domain Details */}
          {step.stageName === "AI Analysis" && (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs bg-white p-3.5 rounded-lg border border-slate-200">
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Intent</span>
                <span className="font-bold text-purple-900">{analysis.intent || payload.intent || "ACCOUNT_ACCESS"}</span>
              </div>
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Category</span>
                <span className="font-semibold text-slate-800">{analysis.suggestedCategory || payload.suggestedCategory || "Account Access"}</span>
              </div>
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Urgency</span>
                <span className="font-semibold text-amber-700">{analysis.urgency || "HIGH"}</span>
              </div>
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Confidence</span>
                <span className="font-semibold text-emerald-700">{analysis.confidence ? `${(analysis.confidence * 100).toFixed(0)}%` : "95%"}</span>
              </div>
            </div>
          )}

          {/* Knowledge Retrieval Domain Details */}
          {step.stageName === "Knowledge Retrieval" && (
            <div className="space-y-3 bg-white p-3.5 rounded-lg border border-slate-200 text-xs">
              <div className="flex justify-between items-center border-b border-slate-100 pb-2">
                <span className="font-bold text-slate-800 flex items-center gap-1.5">
                  <BookOpen className="h-3.5 w-3.5 text-teal-600" /> Knowledge Sources
                </span>
                <span className="text-[11px] font-mono text-slate-500">
                  Retrieved: {knowledge.retrievedDocumentCount || 1} articles
                </span>
              </div>

              {knowledge.matchedArticleTitles && knowledge.matchedArticleTitles.length > 0 && (
                <div>
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Matched Article Titles</span>
                  <div className="flex flex-wrap gap-1.5">
                    {knowledge.matchedArticleTitles.map((title: string, i: number) => (
                      <span key={i} className="bg-teal-50 text-teal-900 border border-teal-200 px-2 py-0.5 rounded text-[11px] font-medium">
                        {title}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Routing Domain Details */}
          {step.stageName === "Routing" && (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs bg-white p-3.5 rounded-lg border border-slate-200">
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Assigned Team</span>
                <span className="font-bold text-indigo-900">{routing.targetTeam || "account-team"}</span>
              </div>
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Priority</span>
                <span className="font-semibold text-slate-800">{routing.priority || "HIGH"}</span>
              </div>
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Matched Rule</span>
                <span className="font-mono text-slate-700 bg-slate-100 px-1 rounded">{routing.matchedRuleName || "Account Access Priority Rule"}</span>
              </div>
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">SLA Hours</span>
                <span className="font-semibold text-emerald-700">48h SLA</span>
              </div>
            </div>
          )}

          {/* Raw Payload Accordion */}
          <div>
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-1">Raw Payload Event</span>
            <pre className="text-[10px] font-mono text-slate-700 bg-white p-3 rounded-lg border border-slate-200 overflow-x-auto shadow-inner leading-relaxed max-h-[250px]">
              {JSON.stringify(parseAndCleanJson(step.payload), null, 2)}
            </pre>
          </div>

        </div>
      )}
    </div>
  );
}

function parseAndCleanJson(data: any): any {
  if (data === null || data === undefined) return data;
  if (typeof data === "string") {
    try {
      const parsed = JSON.parse(data);
      if (typeof parsed === "object" && parsed !== null) {
        return parseAndCleanJson(parsed);
      }
    } catch {
      // return raw string if not JSON
    }
    return data;
  }
  if (Array.isArray(data)) {
    return data.map(parseAndCleanJson);
  }
  if (typeof data === "object") {
    const cleaned: Record<string, any> = {};
    for (const [key, value] of Object.entries(data)) {
      cleaned[key] = parseAndCleanJson(value);
    }
    return cleaned;
  }
  return data;
}

function getStatusBadge(status: string) {
  switch (status) {
    case "COMPLETED":
    case "SUCCESS":
      return <span className="bg-emerald-100 text-emerald-800 text-xs font-bold px-2.5 py-0.5 rounded border border-emerald-200 uppercase">Completed</span>;
    case "FAILED":
      return <span className="bg-red-100 text-red-800 text-xs font-bold px-2.5 py-0.5 rounded border border-red-200 uppercase">Failed</span>;
    case "RECOVERED":
    case "RETRYING":
      return <span className="bg-orange-100 text-orange-800 text-xs font-bold px-2.5 py-0.5 rounded border border-orange-200 uppercase">Retrying</span>;
    case "RUNNING":
      return <span className="bg-blue-100 text-blue-800 text-xs font-bold px-2.5 py-0.5 rounded border border-blue-200 uppercase">Running</span>;
    default:
      return <span className="bg-slate-100 text-slate-800 text-xs font-bold px-2.5 py-0.5 rounded border border-slate-200 uppercase">{status}</span>;
  }
}

function getStageStatusBadge(status: string) {
  switch (status) {
    case "SUCCESS":
      return <span className="text-[10px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-1.5 py-0.2 rounded uppercase">Completed</span>;
    case "FAILED":
      return <span className="text-[10px] font-bold text-red-700 bg-red-50 border border-red-200 px-1.5 py-0.2 rounded uppercase">Failed</span>;
    case "RETRYING":
      return <span className="text-[10px] font-bold text-orange-700 bg-orange-50 border border-orange-200 px-1.5 py-0.2 rounded uppercase">Retrying</span>;
    case "WAITING":
      return <span className="text-[10px] font-bold text-amber-700 bg-amber-50 border border-amber-200 px-1.5 py-0.2 rounded uppercase">Waiting</span>;
    case "SKIPPED":
    default:
      return <span className="text-[10px] font-bold text-slate-500 bg-slate-100 border border-slate-200 px-1.5 py-0.2 rounded uppercase">Skipped</span>;
  }
}

function getStageIconBg(type: string, status: string) {
  if (status === "FAILED") return "bg-red-50 text-red-600 border-red-200";
  if (status === "SKIPPED") return "bg-slate-100 text-slate-400 border-slate-200";

  switch (type) {
    case "BUSINESS_EVENT": return "bg-blue-50 text-blue-600 border-blue-200";
    case "GUARDRAIL": return "bg-amber-50 text-amber-600 border-amber-200";
    case "AI_PLANNING": return "bg-purple-50 text-purple-600 border-purple-200";
    case "TOOL_INVOCATION": return "bg-indigo-50 text-indigo-600 border-indigo-200";
    case "KNOWLEDGE_RETRIEVAL": return "bg-teal-50 text-teal-600 border-teal-200";
    case "WORKFLOW_DECISION": return "bg-emerald-50 text-emerald-600 border-emerald-200";
    case "EVENT_PUBLICATION": return "bg-slate-100 text-slate-700 border-slate-200";
    default: return "bg-slate-50 text-slate-600 border-slate-200";
  }
}

function getStageIcon(type: string) {
  switch (type) {
    case "BUSINESS_EVENT": return <Radio className="h-4 w-4" />;
    case "GUARDRAIL": return <Shield className="h-4 w-4" />;
    case "AI_PLANNING": return <Bot className="h-4 w-4" />;
    case "TOOL_INVOCATION": return <Wrench className="h-4 w-4" />;
    case "KNOWLEDGE_RETRIEVAL": return <BookOpen className="h-4 w-4" />;
    case "WORKFLOW_DECISION": return <Zap className="h-4 w-4" />;
    case "EVENT_PUBLICATION": return <Send className="h-4 w-4" />;
    default: return <CheckCircle2 className="h-4 w-4" />;
  }
}
