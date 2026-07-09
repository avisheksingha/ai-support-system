import React, { Suspense } from "react";
import { useTicket, useAnalysis, useKnowledge, useRouting, useTimeline, useUpdateTicketStatus } from "../hooks/useWorkspace";
import { TicketTimeline } from "./TicketTimeline";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ErrorBoundary } from "react-error-boundary";
import { WorkspaceErrorFallback } from "@/components/ui/ErrorFallbacks";

// Lazy Loaded Panels
const AiInsightsPanel = React.lazy(() => import("./AiInsightsPanel").then(m => ({ default: m.AiInsightsPanel })));
const RagResponsePanel = React.lazy(() => import("./RagResponsePanel").then(m => ({ default: m.RagResponsePanel })));
const RoutingPanel = React.lazy(() => import("./RoutingPanel").then(m => ({ default: m.RoutingPanel })));
const DiagnosticsPanel = React.lazy(() => import("./DiagnosticsPanel").then(m => ({ default: m.DiagnosticsPanel })));
import type { TicketStatus } from "@/shared/types/ticket";
import { useSearchParams } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { formatDistanceToNow } from "date-fns";

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
  const { data: knowledge, isLoading: isKnowledgeLoading } = useKnowledge(ticket?.id);
  const { data: routing, isLoading: isRoutingLoading } = useRouting(ticket?.id);
  const { data: timeline, isLoading: isTimelineLoading } = useTimeline(ticket?.id);

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
    <div className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8 flex flex-col xl:flex-row gap-6 lg:gap-8">
      {/* Primary Column: Conversation & Actions */}
      <div className="flex-1 min-w-0 flex flex-col gap-6">
        
        {isDiagnosticsActive && (
          <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
            <Suspense fallback={<Skeleton className="h-64 w-full bg-card rounded-xl" />}>
              <DiagnosticsPanel 
                 ticket={ticket} 
                 analysis={analysis} 
                 knowledge={knowledge} 
              />
            </Suspense>
          </ErrorBoundary>
        )}

        <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg p-5 mb-2">
          <div className="flex flex-col md:flex-row justify-between items-start gap-4 mb-4">
            <div className="flex-1 min-w-0">
              <div className="mb-1">
                <span className="text-muted-foreground font-mono text-[11px] uppercase tracking-wider">{ticket.ticketNumber}</span>
              </div>
              <h1 className="text-2xl font-medium text-foreground leading-snug mb-3 break-words">{ticket.subject}</h1>
              
              <div className="flex flex-wrap items-center gap-2 text-[13px]">
                <span className="text-muted-foreground">Created {formatDistanceToNow(new Date(ticket.createdAt))} ago</span>
                <span className="text-muted-foreground font-bold px-1">·</span>
                <span className="text-muted-foreground">Customer: <span className="text-foreground font-medium">{ticket.customerName}</span></span>
              </div>
            </div>
            
            <div className="flex items-center gap-3">
              <Badge variant="outline" className={`
                ${ticket.priority === 'CRITICAL' ? 'border-red-500/50 text-red-400 bg-red-500/10' : ''}
                ${ticket.priority === 'HIGH' ? 'border-orange-500/50 text-orange-400 bg-orange-500/10' : ''}
                ${ticket.priority === 'MEDIUM' ? 'border-yellow-500/50 text-yellow-400 bg-yellow-500/10' : ''}
                ${ticket.priority === 'LOW' ? 'border-blue-500/50 text-blue-400 bg-blue-500/10' : ''}
              `}>
                {ticket.priority}
              </Badge>
              <Select 
                value={ticket.status} 
                onValueChange={(val) => updateStatus({ ticketNumber: ticket.ticketNumber, status: val as TicketStatus })}
              >
                <SelectTrigger className="h-8 text-xs bg-muted text-foreground border-border w-[130px]">
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
        </div>

        {/* Conversation (Customer Message) */}
        <div>
          <h2 className="text-[11px] font-semibold text-muted-foreground mb-3 uppercase tracking-wider">Conversation</h2>
          <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="h-8 w-8 rounded-full bg-[#0C66E4]/10 text-[#0C66E4] flex items-center justify-center font-bold text-xs">
                {ticket.customerName.charAt(0)}
              </div>
              <div>
                <div className="text-sm font-medium text-foreground">{ticket.customerName}</div>
                <div className="text-xs text-muted-foreground">Customer</div>
              </div>
            </div>
            <p className="text-foreground whitespace-pre-wrap text-sm leading-relaxed ml-11">{ticket.message}</p>
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
      <div className="w-full xl:w-[360px] shrink-0 flex flex-col gap-6">
        
        {/* AI Insights Panel */}
        {isAnalysisLoading ? (
          <Skeleton className="h-48 w-full bg-card rounded-xl" />
        ) : analysis ? (
          <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
            <Suspense fallback={<Skeleton className="h-48 w-full bg-card rounded-xl" />}>
              <AiInsightsPanel analysis={analysis} />
            </Suspense>
          </ErrorBoundary>
        ) : (
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
        )}

        {/* Knowledge Panel */}
        {isKnowledgeLoading ? (
           <Skeleton className="h-48 w-full bg-card rounded-xl" />
        ) : knowledge ? (
          <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
            <Suspense fallback={<Skeleton className="h-48 w-full bg-card rounded-xl" />}>
              <RagResponsePanel knowledge={knowledge} />
            </Suspense>
          </ErrorBoundary>
        ) : (
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
        )}

        {/* Assignment Panel */}
        {isRoutingLoading ? (
           <Skeleton className="h-32 w-full bg-card rounded-xl" />
        ) : routing ? (
          <ErrorBoundary FallbackComponent={WorkspaceErrorFallback}>
            <Suspense fallback={<Skeleton className="h-32 w-full bg-card rounded-xl" />}>
              <RoutingPanel routing={routing} />
            </Suspense>
          </ErrorBoundary>
        ) : (
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
        )}
      </div>
    </div>
  );
}
