import React, { Suspense } from "react";
import { useTicket, useAnalysis, useRouting, useTimeline, useUpdateTicketStatus, useMessages, useAddMessage } from "../hooks/useWorkspace";
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
const RoutingPanel = React.lazy(() => import("./RoutingPanel").then(m => ({ default: m.RoutingPanel })));
const DiagnosticsPanel = React.lazy(() => import("./DiagnosticsPanel").then(m => ({ default: m.DiagnosticsPanel })));
import { AiPipelineProgress } from "./AiPipelineProgress";
import type { TicketStatus } from "@/shared/types/ticket";
import { useSearchParams } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";

interface TicketDetailViewProps {
  ticketNumber: string;
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
  const { data: analysis, isLoading: isAnalysisLoading } = useAnalysis(ticket?.id);
  const { data: routing, isLoading: isRoutingLoading } = useRouting(ticket?.id);
  const { data: timeline, isLoading: isTimelineLoading } = useTimeline(ticket?.id);
  const { data: messages, isLoading: isMessagesLoading } = useMessages(ticket?.ticketNumber);
  const { mutate: addMessage, isPending: isSendingMessage } = useAddMessage();

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

        <div className="bg-white shadow-sm border border-slate-200 rounded-xl p-6 mb-2 relative overflow-hidden">
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
                <div className="flex items-center gap-2">
                  <div className="w-6 h-6 rounded-full bg-slate-50 border border-slate-100 flex items-center justify-center text-[10px]">🏢</div>
                  Support Department
                </div>
              </div>
            </div>
            
            <div className="w-full grid grid-cols-1 sm:grid-cols-3 gap-3">
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
              <div className="bg-slate-50/80 border border-slate-100 rounded-lg p-3 flex flex-col justify-between gap-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-bold uppercase text-slate-500 tracking-wider">SLA</span>
                  <span className="text-[11px] font-bold text-amber-600 flex items-center gap-1.5">
                    <span className="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse"></span> 2h 15m left
                  </span>
                </div>
                <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden mt-1">
                  <div className="h-full bg-amber-500 w-[65%] rounded-full"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Ticket Lifecycle Visual */}
        <div className="bg-white shadow-sm border border-slate-200 rounded-xl p-4 mb-2 flex items-center justify-between overflow-x-auto text-[10px] font-bold uppercase tracking-wider text-slate-400">
          <div className="flex items-center gap-2 text-emerald-600">
            <div className="w-4 h-4 rounded-full bg-emerald-100 flex items-center justify-center"><Check className="h-2.5 w-2.5" /></div>
            Submitted
          </div>
          <div className="w-8 h-px bg-slate-200"></div>
          <div className="flex items-center gap-2 text-emerald-600">
            <div className="w-4 h-4 rounded-full bg-emerald-100 flex items-center justify-center"><Check className="h-2.5 w-2.5" /></div>
            AI Analyzed
          </div>
          <div className="w-8 h-px bg-slate-200"></div>
          <div className="flex items-center gap-2 text-blue-600">
            <div className="w-4 h-4 rounded-full bg-blue-100 flex items-center justify-center"><span className="animate-pulse w-1.5 h-1.5 bg-blue-600 rounded-full"></span></div>
            Agent Assigned
          </div>
          <div className="w-8 h-px bg-slate-200"></div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded-full bg-slate-100"></div>
            Working
          </div>
          <div className="w-8 h-px bg-slate-200"></div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded-full bg-slate-100"></div>
            Resolved
          </div>
        </div>

        <AiPipelineProgress ticket={ticket} />

        {/* Conversation */}
        <div className="mt-4">
          <h2 className="text-[11px] font-bold text-slate-500 mb-4 uppercase tracking-widest px-1">Conversation</h2>
          
          <div className="space-y-6 bg-white rounded-xl p-6 border border-slate-200 shadow-sm flex flex-col">
            
            {/* Real Messages Map */}
            {isMessagesLoading ? (
              <div className="space-y-4">
                <Skeleton className="h-16 w-full rounded-xl bg-slate-100" />
                <Skeleton className="h-16 w-3/4 self-end rounded-xl bg-slate-100" />
              </div>
            ) : messages && messages.length > 0 ? (
              messages.map((msg: any) => {
                const isAgent = msg.type === 'AGENT_MESSAGE' || msg.isInternal;
                const isSystem = msg.senderName === 'System' || msg.type === 'SYSTEM_MESSAGE';
                const avatarText = isAgent ? 'AG' : isSystem ? '⚙️' : (ticket.customerName?.charAt(0)?.toUpperCase() || 'C');
                const badgeText = isAgent ? 'AGENT' : isSystem ? 'SYSTEM' : 'CUSTOMER';
                const displayName = isAgent ? 'You' : isSystem ? 'System' : ticket.customerName;

                return (
                  <div key={msg.id} className={`flex gap-4 ${isAgent ? 'flex-row-reverse' : ''}`}>
                    <div className={`h-10 w-10 shrink-0 rounded-full flex items-center justify-center font-bold text-sm shadow-sm ring-4 ${isAgent ? 'bg-cyan-600 text-white ring-cyan-50' : isSystem ? 'bg-slate-800 text-white ring-slate-100' : 'bg-blue-600 text-white ring-blue-50'}`}>
                      {avatarText}
                    </div>
                    <div className={`flex-1 space-y-1.5 flex flex-col ${isAgent ? 'items-end' : ''}`}>
                      <div className={`flex items-baseline gap-2 ${isAgent ? 'flex-row-reverse' : ''}`}>
                        <span className="text-sm font-bold text-slate-900">{displayName}</span>
                        <span className={`text-[9px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full ${isAgent ? 'text-cyan-700 bg-cyan-50 border border-cyan-100' : 'text-slate-500 bg-slate-100'}`}>
                          {badgeText}
                        </span>
                        <span className="text-[10px] text-slate-400 font-medium mx-2">{formatTimeAgo(msg.createdAt)}</span>
                      </div>
                      <div className={`border p-4 text-sm text-slate-700 whitespace-pre-wrap leading-relaxed shadow-sm relative ${isAgent ? 'bg-cyan-50/50 border-cyan-100 rounded-2xl rounded-tr-sm text-right' : 'bg-slate-50 border-slate-100 rounded-2xl rounded-tl-sm'}`}>
                        {msg.content}
                      </div>
                    </div>
                  </div>
                );
              })
            ) : (
              <div className="text-center text-slate-400 text-sm py-4 italic">No messages yet.</div>
            )}

            {/* AI Suggested Reply (Mocked for visual demonstration of feature) */}
            <div className="flex gap-4 mt-6 pt-6 border-t border-slate-100">
              <div className="h-10 w-10 shrink-0 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 text-white flex items-center justify-center font-bold text-sm shadow-sm ring-4 ring-indigo-50 mt-1">
                ✨
              </div>
              <div className="flex-1 space-y-1.5">
                <div className="flex items-baseline gap-2">
                  <span className="text-sm font-bold text-indigo-700">Copilot Draft</span>
                  <span className="text-[10px] text-slate-400 font-medium ml-2">Generated just now</span>
                </div>
                <div className="bg-indigo-50/50 border border-indigo-100 rounded-2xl rounded-tl-sm p-5 text-sm text-slate-800 whitespace-pre-wrap shadow-sm">
                  <div className="flex items-center gap-2 mb-3">
                    <span className="flex h-2 w-2 rounded-full bg-indigo-500"></span>
                    <span className="italic text-indigo-600/80 font-medium text-xs">Drafted based on matched knowledge article</span>
                  </div>
                  <div className="bg-white p-4 rounded-xl border border-indigo-50 shadow-sm leading-relaxed">
                    Hi {ticket.customerName.split(' ')[0]},<br/><br/>
                    I understand you need assistance with your request. Based on our policies, I can help you resolve this right away. Let me know if you would like me to proceed with the necessary steps on your account.
                  </div>
                  <div className="mt-4 flex flex-wrap gap-2 w-full">
                    <button className="text-[11px] px-4 py-2 bg-indigo-600 shadow-sm border border-transparent rounded-md text-white hover:bg-indigo-700 transition-all font-semibold flex items-center gap-1.5" onClick={() => setReplyText("Hi " + ticket.customerName.split(' ')[0] + ",\n\nI understand you need assistance with your request. Based on our policies, I can help you resolve this right away. Let me know if you would like me to proceed with the necessary steps on your account.")}>
                      <Check className="w-3.5 h-3.5" /> Use Reply
                    </button>
                    <button className="text-[11px] px-4 py-2 bg-white shadow-sm border border-slate-200 rounded-md text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all font-semibold flex items-center gap-1.5">
                      Regenerate
                    </button>
                    <div className="flex-1"></div>
                    <button className="text-[11px] px-4 py-2 bg-white shadow-sm border border-slate-200 rounded-md text-rose-600 hover:bg-rose-50 transition-all font-semibold flex items-center gap-1.5">
                      Escalate
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            {/* Message Input Box */}
            <div className="mt-6 pt-6 border-t border-slate-100 flex gap-4">
               <div className="h-10 w-10 shrink-0 rounded-full bg-cyan-600 text-white flex items-center justify-center font-bold text-sm shadow-sm ring-4 ring-cyan-50">
                 AG
               </div>
               <div className="flex-1 flex flex-col gap-2">
                 <textarea 
                   className="w-full border border-slate-200 rounded-lg p-3 text-sm min-h-[100px] focus:outline-none focus:ring-2 focus:ring-blue-500/50 resize-y" 
                   placeholder="Type your reply to the customer..."
                   value={replyText}
                   onChange={e => setReplyText(e.target.value)}
                 ></textarea>
                 <div className="flex justify-end gap-2">
                    <button 
                      className="px-4 py-2 bg-slate-100 text-slate-700 font-semibold text-xs rounded-md hover:bg-slate-200 transition-colors"
                      onClick={() => addMessage({ ticketNumber: ticket.ticketNumber, content: replyText, isInternal: true }, { onSuccess: () => setReplyText("") })}
                      disabled={isSendingMessage || !replyText.trim()}
                    >
                      Add Internal Note
                    </button>
                    <button 
                      className="px-6 py-2 bg-blue-600 text-white font-semibold text-xs rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50"
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
        <div className="mt-6 mb-8">
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
        {isAnalysisLoading || !analysis ? (
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
        {!ticket.ragResponse ? (
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
              <RagResponsePanel ragResponse={ticket.ragResponse} />
            </Suspense>
          </ErrorBoundary>
        )}


        {/* Assignment Panel */}
        {isRoutingLoading || !routing ? (
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
