import React, { Suspense, useState } from "react";
import { useTicket, useWorkspaceAggregation, useTimeline, useUpdateTicketStatus, useMessages, useAddMessage } from "../hooks/useWorkspace";
import { Check, User, Mail, Clock, ArrowUpRight, MessageSquare, CheckCircle } from "lucide-react";
import { TicketTimeline } from "./TicketTimeline";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ErrorBoundary } from "react-error-boundary";
import { WorkspaceErrorFallback } from "@/components/ui/ErrorFallbacks";
import { formatTimeAgo } from "@/shared/utils/date";
import { Button } from "@/components/ui/button";

// Lazy Loaded Panels
const AiInsightsPanel = React.lazy(() => import("./AiInsightsPanel").then(m => ({ default: m.AiInsightsPanel })));
const RagResponsePanel = React.lazy(() => import("./RagResponsePanel").then(m => ({ default: m.RagResponsePanel })));
const AiDecisionPanel = React.lazy(() => import("./AiDecisionPanel").then(m => ({ default: m.AiDecisionPanel })));
const RoutingPanel = React.lazy(() => import("./RoutingPanel").then(m => ({ default: m.RoutingPanel })));
const DiagnosticsPanel = React.lazy(() => import("./DiagnosticsPanel").then(m => ({ default: m.DiagnosticsPanel })));
import { AiPipelineProgress } from "./AiPipelineProgress";
import { AiContextDrawer } from "./AiContextDrawer";
import { CollapsiblePanel } from "./CollapsiblePanel";
import { BrainCircuit } from "lucide-react";
import { BookOpen } from "lucide-react";
import { Bot } from "lucide-react";
import { Network } from "lucide-react";
import { Activity } from "lucide-react";
import type { TicketStatus } from "@/shared/types/ticket";
import { useSearchParams } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";

interface TicketDetailViewProps {
  ticketNumber: string;
}

// Helper to compute SLA progress
function calculateSLA(createdAt: string, slaHours?: number) {
  if (!slaHours) return null;
  const created = new Date(createdAt).getTime();
  const target = created + (slaHours * 60 * 60 * 1000);
  const now = Date.now();
  const timeLeft = target - now;

  if (timeLeft <= 0) {
    return { text: "Breached", percent: 100, isBreached: true };
  }

  const totalMs = slaHours * 60 * 60 * 1000;
  const elapsed = now - created;
  const percent = Math.min(100, Math.max(0, (elapsed / totalMs) * 100));

  const hoursLeft = Math.floor(timeLeft / (1000 * 60 * 60));
  const minsLeft = Math.floor((timeLeft % (1000 * 60 * 60)) / (1000 * 60));

  return {
    text: `${hoursLeft}h ${minsLeft}m left`,
    percent,
    isBreached: false
  };
}

export function TicketDetailView({ ticketNumber }: TicketDetailViewProps) {
  const { data: ticket, isLoading: isTicketLoading } = useTicket(ticketNumber);
  const { mutate: updateStatus } = useUpdateTicketStatus();

  const [searchParams] = useSearchParams();
  const showDiagnostics = searchParams.get("diagnostics") === "true";
  const { user } = useAuth();
  const [replyText, setReplyText] = React.useState("");
  const [isDrawerOpen, setIsDrawerOpen] = React.useState(false);
  const isAdmin = user?.role === "ADMIN";
  const isDiagnosticsActive = showDiagnostics && isAdmin;

  // Collapsible panel states
  const [expandedPanels, setExpandedPanels] = useState({
    aiAnalysis: true, // AI Analysis expanded by default
    knowledgeBase: false,
    aiDecision: false,
    routing: false,
    timeline: false,
  });

  const togglePanel = (panel: keyof typeof expandedPanels) => {
    setExpandedPanels(prev => ({ ...prev, [panel]: !prev[panel] }));
  };

  // We only enable AI/Routing queries if we have the ticket ID
  const { data: workspaceData, isLoading: isWorkspaceLoading } = useWorkspaceAggregation(ticket?.id, ticket?.status);
  const analysis = workspaceData?.analysis;
  const routing = workspaceData?.routing;
  const knowledge = workspaceData?.knowledge;
  const aiDecision = workspaceData?.aiDecision;
  const workflowMetadata = workspaceData?.workflowMetadata;
  const { data: timeline, isLoading: isTimelineLoading } = useTimeline(ticket?.id);
  const { data: messages, isLoading: isMessagesLoading } = useMessages(ticket?.ticketNumber);
  const { mutate: addMessage, isPending: isSendingMessage } = useAddMessage();

  const slaData = ticket ? calculateSLA(ticket.createdAt, ticket.slaHours) : null;

  const handleSendReply = () => {
    if (!replyText.trim()) return;
    addMessage({ ticketNumber: ticket!.ticketNumber, content: replyText, isInternal: false }, {
      onSuccess: () => setReplyText("")
    });
  };

  if (isTicketLoading) {
    return (
      <div className="flex-1 p-6 md:p-8 flex flex-col xl:flex-row gap-8">
        <div className="flex-1 min-w-[50%] flex flex-col gap-6">
          <Skeleton className="h-8 w-2/3 bg-card" />
          <Skeleton className="h-4 w-1/3 bg-card" />
          <Skeleton className="h-32 w-full bg-card rounded-xl" />
        </div>
        <div className="xl:w-[400px] shrink-0 flex flex-col gap-6">
          <Skeleton className="h-6 w-1/2 bg-card" />
          <Skeleton className="h-48 w-full bg-card rounded-xl" />
          <Skeleton className="h-48 w-full bg-card rounded-xl" />
        </div>
      </div>
    );
  }

  if (!ticket) {
    return <div className="p-8 text-red-400">Ticket not found</div>;
  }

  return (
    <div className="flex-1 overflow-y-auto p-3 sm:p-4 md:p-5 lg:p-6 flex flex-col xl:flex-row gap-3 sm:gap-4 lg:gap-6 items-start bg-[#F8FAFC]">
      {/* Primary Column: Conversation & Actions */}
      <div className="flex-1 min-w-0 flex flex-col gap-3 sm:gap-4">

        {isDiagnosticsActive && (
          <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
            <Suspense fallback={<Skeleton className="h-64 w-full bg-card rounded-xl" />}>
              <DiagnosticsPanel
                ticket={ticket}
                analysis={analysis}
                routing={routing}
              />
            </Suspense>
          </ErrorBoundary>
        )}

        <div className="bg-white shadow-sm border border-slate-200 rounded-xl p-5 relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-500 to-indigo-500"></div>

          {/* Header: Ticket ID and Created Time */}
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <span className="text-slate-500 font-mono text-[11px] font-bold uppercase tracking-widest bg-slate-100 px-2 py-1 rounded">{ticket.ticketNumber}</span>
              <div className="flex items-center gap-1.5 text-xs text-slate-400">
                <Clock className="h-3.5 w-3.5" />
                <span className="font-medium">Created {formatTimeAgo(ticket.createdAt)}</span>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="outline" className={`h-7 px-3 text-[10px] font-bold uppercase shadow-sm
                ${ticket.priority === 'CRITICAL' ? 'border-red-200 text-red-700 bg-red-50' : ''}
                ${ticket.priority === 'HIGH' ? 'border-orange-200 text-orange-700 bg-orange-50' : ''}
                ${ticket.priority === 'MEDIUM' ? 'border-amber-200 text-amber-700 bg-amber-50' : ''}
                ${ticket.priority === 'LOW' ? 'border-blue-200 text-blue-700 bg-blue-50' : ''}
              `}>
                {ticket.priority}
              </Badge>
              <Select
                value={ticket.status}
                onValueChange={(val) => updateStatus({ ticketNumber: ticket.ticketNumber, status: val as TicketStatus })}
              >
                <SelectTrigger className="h-7 text-xs font-semibold bg-white text-slate-800 border-slate-200 shadow-sm w-[140px]">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="NEW">New</SelectItem>
                  <SelectItem value="ANALYZING">Analyzing</SelectItem>
                  <SelectItem value="ANALYZED">Analyzed</SelectItem>
                  <SelectItem value="ASSIGNED">Assigned</SelectItem>
                  <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
                  <SelectItem value="RESOLVED">Resolved</SelectItem>
                  <SelectItem value="CLOSED">Closed</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Subject */}
          <h1 className="text-xl font-bold text-slate-900 leading-tight mb-4 break-words">{ticket.subject}</h1>

          {/* Customer Info */}
          <div className="flex items-center gap-4 mb-4 pb-4 border-b border-slate-100 flex-wrap justify-between">
            <div className="flex items-center gap-4 flex-wrap">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-full bg-blue-50 border border-blue-100 flex items-center justify-center">
                  <User className="h-4 w-4 text-blue-600" />
                </div>
                <div>
                  <div className="text-sm font-semibold text-slate-800">{ticket.customerName}</div>
                  <div className="text-xs text-slate-500">Customer</div>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-full bg-slate-50 border border-slate-100 flex items-center justify-center">
                  <Mail className="h-4 w-4 text-slate-400" />
                </div>
                <div className="text-xs text-slate-600 truncate max-w-[200px]">{ticket.customerEmail || 'customer@example.com'}</div>
              </div>
            </div>

            <div className="flex items-center gap-1.5 flex-wrap">
              <span className="text-[10px] font-bold uppercase px-2 py-0.5 rounded bg-purple-50 text-purple-700 border border-purple-200">
                {ticket.customerTier || (ticket.customerName?.includes("Inc") ? "Enterprise" : "Standard")} Tier
              </span>
              <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-slate-100 text-slate-700 border border-slate-200">
                {ticket.channel || "Web Portal"}
              </span>
              <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-slate-50 text-slate-600 border border-slate-200" title="Previous Tickets">
                0 Past Tickets
              </span>
              <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-slate-50 text-slate-600 border border-slate-200" title="Last Interaction">
                Last: First Contact
              </span>
              <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-slate-50 text-slate-600 border border-slate-200" title="Customer Since">
                Member: 2026
              </span>
            </div>
          </div>

          {/* Ticket Details Grid */}
          <div className="grid grid-cols-2 gap-2 sm:gap-3 mb-4">
            <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
              <span className="text-[9px] font-bold uppercase text-slate-500 tracking-wider block mb-1">Intent</span>
              <span className="text-xs font-semibold text-indigo-700">{analysis?.intent ? formatSemanticString(analysis.intent) : 'Unknown'}</span>
            </div>

            {/* Enhanced SLA Visualization */}
            <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
              <div className="flex items-center justify-between mb-1">
                <span className="text-[9px] font-bold uppercase text-slate-500 tracking-wider">SLA Deadline</span>
                <span className={`text-[10px] font-bold ${slaData?.isBreached ? 'text-red-600' : 'text-emerald-600'}`}>
                  {slaData ? slaData.text : '48h 0m left'}
                </span>
              </div>
              <div className="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden">
                <div
                  className={`h-full transition-all duration-300 rounded-full ${slaData?.isBreached ? 'bg-red-500' : 'bg-emerald-500'}`}
                  style={{ width: `${slaData ? slaData.percent : 15}%` }}
                ></div>
              </div>
            </div>

            <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
              <span className="text-[9px] font-bold uppercase text-slate-500 tracking-wider block mb-1">Support Domain</span>
              <span className="text-xs font-semibold text-slate-700">{analysis?.suggestedCategory || 'Uncategorized'}</span>
            </div>
            <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100">
              <span className="text-[9px] font-bold uppercase text-slate-500 tracking-wider block mb-1">Assigned Team</span>
              <span className="text-xs font-semibold text-slate-700">{routing?.assignedTeam || 'Unassigned'}</span>
            </div>
            <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100 col-span-2 sm:col-span-1">
              <span className="text-[9px] font-bold uppercase text-slate-500 tracking-wider block mb-1">Assigned Agent</span>
              <span className="text-xs font-semibold text-slate-700">{ticket.assignedTo || 'Unassigned'}</span>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="flex items-center gap-1.5 sm:gap-2 pt-3 border-t border-slate-100 flex-wrap">
            {/* Primary Action Button: Reply */}
            <Button
              size="sm"
              className="h-7 sm:h-8 text-[10px] sm:text-xs font-semibold gap-1.5 flex-1 sm:flex-none bg-[#0C66E4] hover:bg-[#0052CC] text-white shadow-sm"
              onClick={() => {
                const replyElem = document.querySelector("textarea");
                if (replyElem) replyElem.focus();
              }}
            >
              <MessageSquare className="h-3 w-3.5 sm:h-3.5" />
              <span>Reply</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="h-7 sm:h-8 text-[10px] sm:text-xs font-medium gap-1.5 flex-1 sm:flex-none"
              onClick={() => {/* TODO: Implement assign modal */ }}
            >
              <User className="h-3 w-3.5 sm:h-3.5" />
              <span>Assign</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="h-7 sm:h-8 text-[10px] sm:text-xs font-medium gap-1.5 flex-1 sm:flex-none text-purple-700 border-purple-200 bg-purple-50/50 hover:bg-purple-100/60"
              onClick={() => setIsDrawerOpen(true)}
            >
              <ArrowUpRight className="h-3 w-3.5 sm:h-3.5" />
              <span>View AI Context</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="h-7 sm:h-8 text-[10px] sm:text-xs font-medium gap-1.5 flex-1 sm:flex-none"
              onClick={() => updateStatus({ ticketNumber: ticket.ticketNumber, status: 'RESOLVED' })}
            >
              <CheckCircle className="h-3 w-3.5 sm:h-3.5" />
              <span>Resolve</span>
            </Button>
          </div>
        </div>

        {/* Ticket Lifecycle Visual - Railway Style */}
        <div className="bg-white shadow-sm border border-slate-200 rounded-xl px-8 sm:px-12 py-8 my-5">
          <div className="relative flex justify-between items-center w-full">
            {/* Background Track Line */}
            <div className="absolute left-0 right-0 top-1/2 -translate-y-1/2 h-1 bg-slate-100 rounded-full z-0"></div>

            {(() => {
              const statusOrder = ['NEW', 'ANALYZING', 'ANALYZED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
              const currentIndex = Math.max(0, statusOrder.indexOf(ticket.status));

              let stationIndex = 0; // NEW
              if (currentIndex >= statusOrder.indexOf('ANALYZING')) stationIndex = 1;
              if (currentIndex >= statusOrder.indexOf('ANALYZED')) stationIndex = 3;
              if (currentIndex >= statusOrder.indexOf('ASSIGNED')) stationIndex = 4;
              if (currentIndex >= statusOrder.indexOf('RESOLVED')) stationIndex = 5;

              const stations = [
                { label: 'SUBMITTED' },
                { label: 'AI ANALYSIS' },
                { label: 'KNOWLEDGE RETRIEVED' },
                { label: 'ROUTING' },
                { label: 'ASSIGNED' },
                { label: 'RESOLVED' }
              ];

              return (
                <>
                  {/* Progress Fill Line */}
                  <div
                    className="absolute left-0 top-1/2 -translate-y-1/2 h-1 bg-emerald-400 transition-all duration-700 ease-in-out rounded-full z-0"
                    style={{ width: `${(stationIndex / (stations.length - 1)) * 100}%` }}
                  ></div>

                  {stations.map((station, idx) => {
                    const isPassed = stationIndex > idx;
                    const isCurrent = stationIndex === idx;

                    // Center all labels! The px-8 padding on the container gives enough room so the first/last don't clip.
                    const labelAlignment = "left-1/2 -translate-x-1/2 text-center";

                    // Alternate labels above and below the track
                    const verticalAlignment = idx % 2 === 0 ? "top-7" : "bottom-7";

                    return (
                      <div key={station.label} className="relative z-10 flex flex-col items-center">
                        {/* Station Node */}
                        <div className={`w-5 h-5 rounded-full flex items-center justify-center ring-4 ring-white shadow-sm transition-colors duration-500 ${isPassed ? 'bg-emerald-500' : isCurrent ? 'bg-blue-600 ring-blue-50' : 'bg-slate-200'}`}>
                          {isPassed ? <Check className="w-3 h-3 text-white" /> : isCurrent ? <div className="w-1.5 h-1.5 bg-white rounded-full animate-pulse" /> : <div className="w-1.5 h-1.5 bg-white rounded-full opacity-50" />}
                        </div>
                        {/* Station Label */}
                        <div className={`absolute ${verticalAlignment} whitespace-nowrap text-[9px] sm:text-[10px] font-bold tracking-wider transition-colors duration-500 ${labelAlignment} ${isPassed ? 'text-emerald-700' : isCurrent ? 'text-blue-700' : 'text-slate-400'}`}>
                          {station.label}
                        </div>
                      </div>
                    );
                  })}
                </>
              );
            })()}
          </div>
        </div>

        <AiPipelineProgress ticket={ticket} analysis={analysis} knowledge={knowledge} routing={routing} aiDecision={aiDecision} workflowMetadata={workflowMetadata} />

        {/* Conversation */}
        <div>
          <h2 className="text-[11px] font-bold text-slate-500 mb-4 uppercase tracking-widest px-1">Conversation</h2>

          <div className="space-y-4 bg-white rounded-xl p-5 border border-slate-200 shadow-sm flex flex-col">

            {/* Real Messages Map */}
            {isMessagesLoading ? (
              <div className="space-y-4">
                <Skeleton className="h-12 w-full rounded-xl bg-slate-100" />
                <Skeleton className="h-12 w-3/4 self-end rounded-xl bg-slate-100" />
              </div>
            ) : (
              messages && messages.length > 0 && messages.map((msg: any) => {
                const isInternalNote = msg.isInternal || msg.internal || msg.type === 'INTERNAL_NOTE';
                const isAgent = msg.type === 'AGENT_MESSAGE' || isInternalNote;
                const isSystem = msg.senderName === 'System' || msg.type === 'SYSTEM_MESSAGE';

                const avatarText = isAgent ? '🎧' : isSystem ? '⚙️' : '👤';
                const displayName = isAgent ? 'You' : isSystem ? 'System' : ticket.customerName;

                return (
                  <div key={msg.id} className={`flex gap-3 ${isAgent ? 'flex-row-reverse' : ''}`}>
                    <div className="h-8 w-8 shrink-0 rounded-full bg-slate-100 border border-slate-200 shadow-sm flex items-center justify-center text-sm mt-1">
                      {avatarText}
                    </div>
                    <div className={`flex flex-col max-w-[85%] ${isAgent ? 'items-end' : 'items-start'}`}>
                      <div className={`flex items-baseline gap-2 mb-1 ${isAgent ? 'flex-row-reverse' : ''}`}>
                        <span className="text-[11px] font-bold text-slate-700">{displayName}</span>
                        {isInternalNote && <span className="text-[9px] font-bold uppercase tracking-widest px-1.5 py-0.5 rounded text-amber-700 bg-amber-100">Internal</span>}
                        <span className="text-[10px] text-slate-400 font-medium">{formatTimeAgo(msg.createdAt)}</span>
                      </div>
                      <div className={`px-4 py-2.5 text-xs whitespace-pre-wrap leading-relaxed shadow-sm ${isAgent && !isInternalNote ? 'bg-white border border-slate-200 text-slate-800 rounded-2xl rounded-tr-sm text-left' : isInternalNote ? 'bg-amber-50 border border-amber-200 text-amber-900 rounded-2xl rounded-tr-sm text-left' : isSystem ? 'bg-slate-100 border border-slate-200 text-slate-700 rounded-2xl text-left' : 'bg-blue-600 text-white rounded-2xl rounded-tl-sm text-left'}`}>
                        {msg.content}
                      </div>
                    </div>
                  </div>
                );
              })
            )}

            {/* Message Input Box */}
            <div className="mt-2 pt-2 border-t border-slate-100 flex gap-2">
              <div className="flex-1 flex flex-col gap-1.5">
                <textarea
                  className="w-full border border-slate-200 rounded-lg p-2.5 text-[12px] min-h-[80px] focus:outline-none focus:ring-2 focus:ring-blue-500/50 resize-y leading-tight"
                  placeholder="Type your reply to the customer..."
                  value={replyText}
                  onChange={e => setReplyText(e.target.value)}
                ></textarea>
                <div className="flex justify-end gap-1.5">
                  <button
                    className="px-3 py-1.5 bg-slate-100 text-slate-700 font-semibold text-[11px] rounded-md hover:bg-slate-200 transition-colors shadow-sm"
                    onClick={() => addMessage({ ticketNumber: ticket.ticketNumber, content: replyText, isInternal: true }, { onSuccess: () => setReplyText("") })}
                    disabled={isSendingMessage || !replyText.trim()}
                  >
                    Add Internal Note
                  </button>
                  <button
                    className="px-4 py-1.5 bg-blue-600 text-white font-semibold text-[11px] rounded-md hover:bg-blue-700 transition-colors shadow-sm disabled:opacity-50"
                    onClick={handleSendReply}
                    disabled={isSendingMessage || !replyText.trim()}
                  >
                    {isSendingMessage ? 'Sending...' : 'Send Reply'}
                  </button>
                </div>
              </div>
            </div>

          </div>
        </div>

        {/* Activity Feed */}
        <CollapsiblePanel
          title="Workflow Timeline"
          icon={<Activity className="h-4 w-4 text-blue-500" />}
          isExpanded={expandedPanels.timeline}
          onToggle={() => togglePanel('timeline')}
        >
          {isTimelineLoading ? (
            <div className="space-y-3 ml-4">
              <Skeleton className="h-10 w-full bg-slate-100 rounded-lg" />
              <Skeleton className="h-10 w-full bg-slate-100 rounded-lg" />
            </div>
          ) : timeline ? (
            <div className="bg-white rounded-xl p-4 border border-slate-200 shadow-sm">
              <TicketTimeline events={timeline} />
            </div>
          ) : (
            <div className="text-sm text-slate-500 italic ml-4 bg-white p-4 rounded-xl border border-slate-200">No activity yet.</div>
          )}
        </CollapsiblePanel>
      </div>

      {/* Secondary Column: AI & Automation Workspace */}
      <div className="w-full xl:w-[340px] shrink-0 flex flex-col gap-3 sm:gap-4 h-fit order-first xl:order-last">

        {/* AI Insights Panel */}
        <CollapsiblePanel
          title="AI Analysis"
          icon={<BrainCircuit className="h-4 w-4 text-indigo-500" />}
          isExpanded={expandedPanels.aiAnalysis}
          onToggle={() => togglePanel('aiAnalysis')}
          badge={analysis && !isWorkspaceLoading ? (
            <div className="flex items-center gap-2">
              <span className="text-[11px] font-bold text-indigo-700 uppercase">{analysis.urgency}</span>
              <Badge className="h-6 px-2 text-[10px] font-bold bg-indigo-50 text-indigo-700 border-indigo-200">
                {Math.round(analysis.confidenceScore * 100)}%
              </Badge>
            </div>
          ) : null}
        >
          {isWorkspaceLoading || !analysis ? (
            <div className="border border-border border-dashed rounded-xl p-6 flex flex-col items-center justify-center text-center gap-3">
              <div className="text-2xl animate-bounce">🤖</div>
              <div className="flex flex-col items-center gap-1">
                <p className="text-sm font-medium text-foreground">Analyzing customer issue</p>
                <div className="flex gap-1 text-blue-500">
                  <span className="animate-pulse delay-75">●</span>
                  <span className="animate-pulse delay-150">○</span>
                  <span className="animate-pulse delay-300">○</span>
                </div>
              </div>
            </div>
          ) : (
            <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
              <Suspense fallback={<Skeleton className="h-48 w-full bg-card rounded-xl" />}>
                <AiInsightsPanel analysis={analysis} />
              </Suspense>
            </ErrorBoundary>
          )}
        </CollapsiblePanel>

        {/* Knowledge Panel */}
        <CollapsiblePanel
          title="Knowledge Base"
          icon={<BookOpen className="h-4 w-4 text-emerald-500" />}
          isExpanded={expandedPanels.knowledgeBase}
          onToggle={() => togglePanel('knowledgeBase')}
          badge={knowledge && !isWorkspaceLoading && knowledge.knowledgeFound ? (
            <div className="flex items-center gap-2">
              <Badge className="h-6 px-2 text-[10px] font-bold bg-emerald-50 text-emerald-700 border-emerald-200">
                {knowledge.retrievedDocumentCount ?? knowledge.matchedArticleTitles?.length ?? 0}
              </Badge>
              <span className="text-[11px] font-bold text-emerald-700 uppercase">High Match</span>
            </div>
          ) : null}
        >
          {isWorkspaceLoading || !knowledge ? (
            <div className="border border-border border-dashed rounded-xl p-6 flex flex-col items-center justify-center text-center gap-3">
              <div className="text-2xl animate-bounce" style={{ animationDelay: '0.2s' }}>📚</div>
              <div className="flex flex-col items-center gap-1">
                <p className="text-sm font-medium text-foreground">Retrieving Knowledge</p>
                <div className="flex gap-1 text-emerald-500">
                  <span className="animate-pulse delay-75">●</span>
                  <span className="animate-pulse delay-150">○</span>
                  <span className="animate-pulse delay-300">○</span>
                </div>
              </div>
            </div>
          ) : (
            <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
              <Suspense fallback={<Skeleton className="h-48 w-full bg-card rounded-xl" />}>
                <RagResponsePanel knowledge={knowledge} />
              </Suspense>
            </ErrorBoundary>
          )}
        </CollapsiblePanel>

        {/* AI Decision Panel */}
        {aiDecision && (
          <CollapsiblePanel
            title="AI Decision"
            icon={<Bot className="h-4 w-4 text-purple-500" />}
            isExpanded={expandedPanels.aiDecision}
            onToggle={() => togglePanel('aiDecision')}
            badge={
              <div className="flex items-center gap-2">
                <Badge className="h-6 px-2 text-[10px] font-bold bg-purple-50 text-purple-700 border-purple-200">
                  {Math.round(aiDecision.confidence * 100)}%
                </Badge>
                <span className="text-[11px] font-bold text-purple-700 uppercase">Ready</span>
              </div>
            }
          >
            <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
              <Suspense fallback={<Skeleton className="h-48 w-full bg-card rounded-xl" />}>
                <AiDecisionPanel
                  decision={aiDecision}
                  onUseReply={setReplyText}
                />
              </Suspense>
            </ErrorBoundary>
          </CollapsiblePanel>
        )}


        {/* Assignment Panel */}
        <CollapsiblePanel
          title="Routing"
          icon={<Network className="h-4 w-4 text-cyan-500" />}
          isExpanded={expandedPanels.routing}
          onToggle={() => togglePanel('routing')}
          badge={routing && !isWorkspaceLoading ? (
            <div className="flex items-center gap-2">
              <span className="text-[11px] font-bold text-cyan-700 uppercase">{routing.priority}</span>
              <Badge className="h-6 px-2 text-[10px] font-bold bg-cyan-50 text-cyan-700 border-cyan-200">
                {routing.slaHours}h SLA
              </Badge>
            </div>
          ) : null}
        >
          {isWorkspaceLoading || !routing ? (
            <div className="border border-border border-dashed rounded-xl p-6 flex flex-col items-center justify-center text-center gap-3">
              <div className="text-2xl animate-bounce" style={{ animationDelay: '0.4s' }}>🧭</div>
              <div className="flex flex-col items-center gap-1">
                <p className="text-sm font-medium text-foreground">Evaluating Routing Rules</p>
                <div className="flex gap-1 text-blue-500">
                  <span className="animate-pulse delay-75">●</span>
                  <span className="animate-pulse delay-150">○</span>
                  <span className="animate-pulse delay-300">○</span>
                </div>
              </div>
            </div>
          ) : (
            <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
              <Suspense fallback={<Skeleton className="h-32 w-full bg-card rounded-xl" />}>
                <RoutingPanel routing={routing} ticket={ticket} />
              </Suspense>
            </ErrorBoundary>
          )}
        </CollapsiblePanel>
      </div>

      {/* AI Context Side Drawer */}
      {ticket && (
        <AiContextDrawer
          isOpen={isDrawerOpen}
          onClose={() => setIsDrawerOpen(false)}
          ticket={ticket}
          analysis={analysis}
          knowledge={knowledge}
          routing={routing}
          aiDecision={aiDecision}
          workflowMetadata={workflowMetadata}
        />
      )}
    </div>
  );
}

function formatSemanticString(val: string) {
  return val.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase());
}
