import type { TimelineEvent, TimelineEventType } from "@/shared/types/workspace";
import { formatTime, parseDate } from "@/shared/utils/date";
import { Clock, CheckCircle, AlertCircle, Loader2 } from "lucide-react";

interface TicketTimelineProps {
  events: TimelineEvent[];
}

export function TicketTimeline({ events }: TicketTimelineProps) {
  // Sort events by timestamp ascending
  const sortedEvents = [...events].sort((a, b) => parseDate(a.timestamp).getTime() - parseDate(b.timestamp).getTime());

  // Filter to show only key workflow milestones
  const keyMilestones = sortedEvents.filter(event => 
    event.subType.includes('SUBMITTED') ||
    event.subType.includes('ANALYSIS') ||
    event.subType.includes('KNOWLEDGE') ||
    event.subType.includes('DECISION') ||
    event.subType.includes('ROUTING') ||
    event.subType.includes('ASSIGNED') ||
    event.subType.includes('RESOLVED')
  );

  const displayEvents = keyMilestones.length > 0 ? keyMilestones : sortedEvents;

  // Helper to determine effective outcome
  const getEffectiveOutcome = (event: TimelineEvent) => {
    if (event.outcome) return event.outcome;
    
    // Infer outcome from title/description if outcome is missing
    const text = (event.title + ' ' + event.description).toLowerCase();
    if (text.includes('completed') || text.includes('success')) return 'COMPLETED';
    if (text.includes('failed') || text.includes('error')) return 'FAILED';
    return 'IN_PROGRESS';
  };

  return (
    <div className="space-y-3">
      {displayEvents.map((event, index) => {
        const effectiveOutcome = getEffectiveOutcome(event);
        
        return (
          <div key={event.eventId} className="flex gap-3 items-start">
            {/* Timeline Icon */}
            <div className="flex flex-col items-center">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${
                effectiveOutcome === 'COMPLETED' ? 'bg-emerald-100 border border-emerald-200' :
                effectiveOutcome === 'FAILED' ? 'bg-red-100 border border-red-200' :
                effectiveOutcome === 'PARTIAL_SUCCESS' ? 'bg-amber-100 border border-amber-200' :
                'bg-slate-100 border border-slate-200'
              }`}>
                {effectiveOutcome === 'COMPLETED' ? (
                  <CheckCircle className="h-4 w-4 text-emerald-600" />
                ) : effectiveOutcome === 'FAILED' ? (
                  <AlertCircle className="h-4 w-4 text-red-600" />
                ) : effectiveOutcome === 'PARTIAL_SUCCESS' ? (
                  <AlertCircle className="h-4 w-4 text-amber-600" />
                ) : (
                  <Loader2 className="h-4 w-4 text-slate-400 animate-spin" />
                )}
              </div>
              {index < displayEvents.length - 1 && (
                <div className="w-0.5 h-full bg-slate-200 my-1" />
              )}
            </div>

            {/* Event Content */}
            <div className="flex-1 pb-4">
              <div className="flex items-center justify-between mb-1">
                <div className="flex items-center gap-2">
                  <span className="text-sm"><EventIcon type={event.type} /></span>
                  <span className="text-sm font-semibold text-slate-800">
                    {event.title}
                  </span>
                  {effectiveOutcome && <OutcomeIcon outcome={effectiveOutcome} />}
                </div>
              <div className="flex items-center gap-1 text-[10px] text-slate-400">
                <Clock className="h-3 w-3" />
                <span className="font-mono">{formatTime(event.timestamp)}</span>
              </div>
            </div>
            
            <p className="text-[11px] text-slate-600 leading-snug">
              {event.description}
            </p>

            {/* Execution Details - Compact */}
            {(event.latencyMs || event.model) && (
              <div className="flex items-center gap-2 mt-2">
                {event.model && (
                  <span className="px-1.5 py-0.5 bg-slate-100 border border-slate-200 rounded text-[9px] font-mono text-slate-600">
                    {event.model}
                  </span>
                )}
                {event.latencyMs && (
                  <span className="text-[9px] text-slate-400 font-mono">
                    {event.latencyMs}ms
                  </span>
                )}
              </div>
            )}
          </div>
        </div>
        );
      })}
      
      {displayEvents.length === 0 && (
        <div className="text-center py-8 text-slate-400 text-sm">
          No timeline events available
        </div>
      )}
    </div>
  );
}

function OutcomeIcon({ outcome }: { outcome: string }) {
  switch (outcome) {
    case "COMPLETED": return <span className="text-green-500 text-xs ml-1" title="Completed">✓</span>;
    case "PARTIAL_SUCCESS": return <span className="text-amber-500 text-xs ml-1" title="Partial Success">⚠</span>;
    case "FAILED": return <span className="text-red-500 text-xs ml-1" title="Failed">✖</span>;
    case "WAITING_APPROVAL": return <span className="text-blue-500 text-xs ml-1" title="Waiting Approval">⏸</span>;
    default: return null;
  }
}


function EventIcon({ type }: { type: TimelineEventType }) {
  switch (type) {
    case "SYSTEM": return "⚙️";
    case "WORKFLOW": return "🔄";
    case "AI": return "🤖";
    case "TOOL": return "🔧";
    case "GOVERNANCE": return "🛡️";
    case "USER": return "👤";
    case "STATUS": return "📌";
    case "NOTIFICATION": return "🔔";
    default: return "🔹";
  }
}
