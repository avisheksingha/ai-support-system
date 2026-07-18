import React, { Suspense } from "react";
import { useTicket, useWorkspaceAggregation, useTimeline, useUpdateTicketStatus, useMessages, useAddMessage } from "../hooks/useWorkspace";
import { Check } from "lucide-react";
import { TicketTimeline } from "./TicketTimeline";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ErrorBoundary } from "react-error-boundary";
import { WorkspaceErrorFallback } from "@/components/ui/ErrorFallbacks";
import { formatTimeAgo } from "@/shared/utils/date";

// Lazy Loaded Panels
const AiInsightsPanel = React.lazy(() => import("./AiInsightsPanel").then(m => ({ default: m.AiInsightsPanel })));
const RagResponsePanel = React.lazy(() => import("./RagResponsePanel").then(m => ({ default: m.RagResponsePanel })));
const AiDecisionPanel = React.lazy(() => import("./AiDecisionPanel").then(m => ({ default: m.AiDecisionPanel })));
const RoutingPanel = React.lazy(() => import("./RoutingPanel").then(m => ({ default: m.RoutingPanel })));
const DiagnosticsPanel = React.lazy(() => import("./DiagnosticsPanel").then(m => ({ default: m.DiagnosticsPanel })));
import { AiPipelineProgress } from "./AiPipelineProgress";
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
  const isAdmin = user?.role === "ADMIN";
  const isDiagnosticsActive = showDiagnostics && isAdmin;
  
  // We only enable AI/Routing queries if we have the ticket ID
  const { data: workspaceData, isLoading: isWorkspaceLoading } = useWorkspaceAggregation(ticket?.id, ticket?.status);
  const analysis = workspaceData?.analysis;
  const routing = workspaceData?.routing;
  const knowledge = workspaceData?.knowledge;
  const aiDecision = workspaceData?.aiDecision;
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
    <div className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 flex flex-col xl:flex-row gap-6 lg:gap-8 items-start bg-[#F8FAFC]">
      {/* Primary Column: Conversation & Actions */}
      <div className="flex-1 min-w-0 flex flex-col gap-6">
        
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

        <div className="bg-white shadow-sm border border-slate-200 rounded-xl p-6 relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-500 to-indigo-500"></div>
          <div className="flex flex-col gap-6">
            <div className="w-full">
              <div className="mb-3 flex flex-wrap items-center gap-3">
                <span className="text-slate-500 font-mono text-[11px] font-bold uppercase tracking-widest bg-slate-100 px-2 py-1 rounded">{ticket.ticketNumber}</span>
                <span className="text-xs text-slate-400 font-medium">Created {formatTimeAgo(ticket.createdAt)}</span>
              </div>
              <h1 className="text-2xl font-bold text-slate-900 leading-tight mb-5 break-words">{ticket.subject}</h1>
              
              <div className="flex flex-wrap items-center gap-y-3 gap-x-6 text-sm text-slate-600">
                <div className="flex items-center gap-2">
                  <div className="w-6 h-6 rounded-full bg-blue-50 border border-blue-100 flex items-center justify-center text-[10px]">👤</div>
                  <span className="font-medium text-slate-800">{ticket.customerName}</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-6 h-6 rounded-full bg-slate-50 border border-slate-100 flex items-center justify-center text-[10px]">📧</div>
                  <span className="truncate max-w-[200px] sm:max-w-none">{ticket.customerEmail || 'customer@example.com'}</span>
                </div>
              </div>
            </div>
            
            <div className="w-full grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="bg-slate-50/80 border border-slate-100 rounded-lg p-3 flex flex-col justify-between gap-2">
                <span className="text-[10px] font-bold uppercase text-slate-500 tracking-wider">Status</span>
                <Select 
                  value={ticket.status} 
                  onValueChange={(val) => updateStatus({ ticketNumber: ticket.ticketNumber, status: val as TicketStatus })}
                >
                  <SelectTrigger className="h-8 text-xs font-semibold bg-white text-slate-800 border-slate-200 w-full shadow-sm">
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
              <div className="bg-slate-50/80 border border-slate-100 rounded-lg p-3 flex flex-col justify-between gap-2">
                <span className="text-[10px] font-bold uppercase text-slate-500 tracking-wider">Priority</span>
                <Badge variant="outline" className={`h-8 px-3 text-[10px] font-bold uppercase justify-center w-full shadow-sm
                  ${ticket.priority === 'CRITICAL' ? 'border-red-200 text-red-700 bg-red-50' : ''}
                  ${ticket.priority === 'HIGH' ? 'border-orange-200 text-orange-700 bg-orange-50' : ''}
                  ${ticket.priority === 'MEDIUM' ? 'border-amber-200 text-amber-700 bg-amber-50' : ''}
                  ${ticket.priority === 'LOW' ? 'border-blue-200 text-blue-700 bg-blue-50' : ''}
                `}>
                  {ticket.priority}
                </Badge>
              </div>
              
              {/* SLA Tracking - Real Data or Fallback */}
              <div className="bg-slate-50/80 border border-slate-100 rounded-lg px-3 py-2.5 flex flex-col justify-between gap-2.5 sm:col-span-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-bold uppercase text-slate-500 tracking-wider">SLA</span>
                  <span className={`text-[11px] font-bold flex items-center gap-1.5 ${slaData?.isBreached ? 'text-red-600' : 'text-amber-600'}`}>
                    <span className={`w-1.5 h-1.5 rounded-full animate-pulse ${slaData?.isBreached ? 'bg-red-500' : 'bg-amber-500'}`}></span> {slaData ? slaData.text : '2h 15m left'}
                  </span>
                </div>
                <div className="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden mb-0.5">
                  <div className={`h-full rounded-full ${slaData?.isBreached ? 'bg-red-500' : 'bg-amber-500'}`} style={{ width: `${slaData ? slaData.percent : 65}%` }}></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Ticket Lifecycle Visual - Railway Style */}
        <div className="bg-white shadow-sm border border-slate-200 rounded-xl px-8 py-9">
          <div className="relative flex justify-between items-center w-full">
            {/* Background Track Line */}
            <div className="absolute left-0 right-0 top-1/2 -translate-y-1/2 h-1 bg-slate-100 rounded-full z-0"></div>
            
            {(() => {
              const statusOrder = ['NEW', 'ANALYZING', 'ANALYZED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
              const currentIndex = Math.max(0, statusOrder.indexOf(ticket.status));
              
              // Map the detailed backend statuses to the 5 visual stations
              let stationIndex = 0; // NEW, ANALYZING
              if (currentIndex >= statusOrder.indexOf('ANALYZED')) stationIndex = 1;
              if (currentIndex >= statusOrder.indexOf('ASSIGNED')) stationIndex = 2;
              if (currentIndex >= statusOrder.indexOf('IN_PROGRESS')) stationIndex = 3;
              if (currentIndex >= statusOrder.indexOf('RESOLVED')) stationIndex = 4;

              const stations = [
                { label: 'SUBMITTED' },
                { label: 'AI ANALYZED' },
                { label: 'AGENT ASSIGNED' },
                { label: 'WORKING' },
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

        <AiPipelineProgress ticket={ticket} analysis={analysis} knowledge={knowledge} routing={routing} aiDecision={aiDecision} />

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
        <div>
          <h2 className="text-[11px] font-bold text-slate-500 mb-4 uppercase tracking-widest px-1">Activity Feed</h2>
          {isTimelineLoading ? (
            <div className="space-y-4 ml-4">
              <Skeleton className="h-10 w-full bg-slate-100 rounded-lg" />
              <Skeleton className="h-10 w-full bg-slate-100 rounded-lg" />
            </div>
          ) : timeline ? (
            <div className="bg-white rounded-xl p-6 border border-slate-200 shadow-sm">
              <TicketTimeline events={timeline} />
            </div>
          ) : (
            <div className="text-sm text-slate-500 italic ml-4 bg-white p-4 rounded-xl border border-slate-200">No activity yet.</div>
          )}
        </div>
      </div>

      {/* Secondary Column: AI & Automation Workspace */}
      <div className="w-full xl:w-[360px] shrink-0 flex flex-col gap-6 h-fit">
        
        {/* AI Insights Panel */}
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

        {/* Knowledge Panel */}
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

        {/* AI Decision Panel */}
        {aiDecision && (
          <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
            <Suspense fallback={<Skeleton className="h-48 w-full bg-card rounded-xl" />}>
              <AiDecisionPanel decision={aiDecision} onUseReply={setReplyText} />
            </Suspense>
          </ErrorBoundary>
        )}


        {/* Assignment Panel */}
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
      </div>
    </div>
  );
}
