import React, { Suspense } from "react";
import { useTicket, useAnalysis, useRouting, useTimeline, useUpdateTicketStatus } from "../hooks/useWorkspace";
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
  const isAdmin = user?.role === "ADMIN";
  const isDiagnosticsActive = showDiagnostics && isAdmin;
  
  // We only enable AI/Routing queries if we have the ticket ID
  const { data: analysis, isLoading: isAnalysisLoading } = useAnalysis(ticket?.id);
  const { data: routing, isLoading: isRoutingLoading } = useRouting(ticket?.id);
  const { data: timeline, isLoading: isTimelineLoading } = useTimeline(ticket?.id, ticket?.createdAt);

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
    <div className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 flex flex-col xl:flex-row gap-6 lg:gap-8 items-start">
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

        <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg p-5 mb-2">
          <div className="flex flex-col md:flex-row justify-between items-start gap-4">
            <div className="flex-1 min-w-0">
              <h1 className="text-2xl font-semibold text-foreground leading-snug mb-1 break-words">{ticket.subject}</h1>
              <div className="mb-4">
                <span className="text-muted-foreground font-mono text-[11px] uppercase tracking-wider">{ticket.ticketNumber}</span>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-y-2 gap-x-6 text-[13px] text-foreground/80 max-w-xl">
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground w-4 text-center">👤</span> {ticket.customerName}
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground w-4 text-center">📧</span> {ticket.customerEmail || 'customer@example.com'}
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground w-4 text-center">🏢</span> Support Department
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground w-4 text-center">🕒</span> Created {formatTimeAgo(ticket.createdAt)}
                </div>
              </div>
            </div>
            
            <div className="flex flex-col items-end gap-3 min-w-[180px]">
              <div className="flex items-center justify-between w-full text-sm">
                <span className="text-muted-foreground">Status</span>
                <Select 
                  value={ticket.status} 
                  onValueChange={(val) => updateStatus({ ticketNumber: ticket.ticketNumber, status: val as TicketStatus })}
                >
                  <SelectTrigger className="h-7 text-xs bg-muted text-foreground border-border w-[110px]">
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
              <div className="flex items-center justify-between w-full text-sm">
                <span className="text-muted-foreground">Priority</span>
                <Badge variant="outline" className={`h-7 px-2 text-[10px] uppercase justify-center w-[110px]
                  ${ticket.priority === 'CRITICAL' ? 'border-red-500/50 text-red-500 bg-red-500/10' : ''}
                  ${ticket.priority === 'HIGH' ? 'border-orange-500/50 text-orange-500 bg-orange-500/10' : ''}
                  ${ticket.priority === 'MEDIUM' ? 'border-yellow-500/50 text-yellow-500 bg-yellow-500/10' : ''}
                  ${ticket.priority === 'LOW' ? 'border-blue-500/50 text-blue-500 bg-blue-500/10' : ''}
                `}>
                  {ticket.priority}
                </Badge>
              </div>
              <div className="flex items-center justify-between w-full text-sm mt-1 pt-2 border-t border-border/50">
                <span className="text-muted-foreground">SLA</span>
                <span className="text-xs font-medium text-emerald-600 flex items-center gap-1.5 w-[110px] justify-center">
                  🟢 4h 15m left
                </span>
              </div>
            </div>
          </div>
        </div>

        <AiPipelineProgress ticket={ticket} />

        {/* Conversation */}
        <div className="mt-2">
          <h2 className="text-[11px] font-semibold text-muted-foreground mb-3 uppercase tracking-wider">Conversation</h2>
          
          <div className="space-y-6 bg-card/50 rounded-xl p-4 md:p-6 border border-border/50">
            {/* Customer Message Bubble */}
            <div className="flex gap-4">
              <div className="h-8 w-8 shrink-0 rounded-full bg-[#0C66E4]/10 text-[#0C66E4] flex items-center justify-center font-bold text-xs">
                {ticket.customerName.charAt(0)}
              </div>
              <div className="flex-1 space-y-1">
                <div className="flex items-baseline gap-2">
                  <span className="text-sm font-medium text-foreground">{ticket.customerName}</span>
                  <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider bg-muted px-1.5 py-0.5 rounded">Customer</span>
                  <span className="text-[10px] text-muted-foreground">{formatTimeAgo(ticket.createdAt)}</span>
                </div>
                <div className="bg-white border border-border/60 rounded-2xl rounded-tl-sm p-4 text-sm text-foreground whitespace-pre-wrap shadow-sm">
                  {ticket.message}
                </div>
              </div>
            </div>

            {/* AI Suggested Reply (Mocked for visual demonstration of feature) */}
            <div className="flex gap-4">
              <div className="h-8 w-8 shrink-0 rounded-full bg-purple-100 text-purple-600 flex items-center justify-center font-bold text-xs mt-1">
                ✨
              </div>
              <div className="flex-1 space-y-1">
                <div className="flex items-baseline gap-2">
                  <span className="text-sm font-medium text-purple-700">AI Suggested Reply</span>
                  <span className="text-[10px] text-muted-foreground">Just now</span>
                </div>
                <div className="bg-purple-50/50 border border-purple-100/60 rounded-2xl rounded-tl-sm p-4 text-sm text-foreground whitespace-pre-wrap shadow-sm">
                  <span className="italic text-muted-foreground/70 text-xs block mb-3">Drafting based on retrieved knowledge...</span>
                  Hi {ticket.customerName.split(' ')[0]},<br/><br/>
                  I understand you need assistance with your request. Based on our policies, I can help you resolve this right away. Let me know if you would like me to proceed with the necessary steps on your account.
                  <div className="mt-4 pt-3 border-t border-purple-200/50 flex flex-wrap gap-2">
                    <button className="text-[11px] px-3 py-1.5 bg-white border border-purple-200 rounded text-purple-700 hover:bg-purple-50 transition-colors font-medium">Use Reply</button>
                    <button className="text-[11px] px-3 py-1.5 bg-white border border-border rounded text-muted-foreground hover:bg-muted transition-colors font-medium">Regenerate</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Activity Feed */}
        <div className="mt-4">
          <h2 className="text-[11px] font-semibold text-muted-foreground mb-4 uppercase tracking-wider">Activity Feed</h2>
          {isTimelineLoading ? (
            <div className="space-y-4 ml-4">
              <Skeleton className="h-10 w-full bg-card" />
              <Skeleton className="h-10 w-full bg-card" />
              <Skeleton className="h-10 w-full bg-card" />
            </div>
          ) : timeline ? (
            <TicketTimeline events={timeline} />
          ) : (
            <div className="text-sm text-muted-foreground italic ml-4">No activity yet.</div>
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
