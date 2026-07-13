import type { TimelineEvent, TimelineEventType } from "@/shared/types/workspace";
import { formatTime, parseDate } from "@/shared/utils/date";

interface TicketTimelineProps {
  events: TimelineEvent[];
}

export function TicketTimeline({ events }: TicketTimelineProps) {
  // Sort events by timestamp ascending
  const sortedEvents = [...events].sort((a, b) => parseDate(a.timestamp).getTime() - parseDate(b.timestamp).getTime());

  // Group events by processingStage
  const groupedEvents = sortedEvents.reduce((acc, event) => {
    const stage = event.processingStage || "Unknown Stage";
    if (!acc[stage]) acc[stage] = [];
    acc[stage].push(event);
    return acc;
  }, {} as Record<string, TimelineEvent[]>);

  return (
    <div className="space-y-6 px-2">
      {Object.entries(groupedEvents).map(([stage, stageEvents]) => (
        <div key={stage} className="flex flex-col space-y-4">
          <div className="font-semibold text-sm text-foreground/90 pb-1 border-b border-border/40">
            ▼ {stage}
          </div>
          
          {stageEvents.map((event, index) => (
            <div key={event.eventId} className="flex flex-col pl-4 border-l-2 border-border/60 ml-2 relative">
              {/* Timeline dot */}
              <div className={`absolute -left-[5px] top-1.5 w-2 h-2 rounded-full ${getSeverityColor(event.severity)}`} />

              <div className="flex items-center gap-2 mb-1">
                <span className="text-sm"><EventIcon type={event.type} /></span>
                <span className="text-sm font-semibold text-foreground">
                  {event.title}
                </span>
                {event.outcome && <OutcomeIcon outcome={event.outcome} />}
              </div>
              
              <div className="flex justify-between items-start pl-6">
                <p className="text-[13px] text-foreground/80 leading-snug max-w-[80%]">
                  {event.description}
                </p>
                <span className="text-xs text-muted-foreground font-mono whitespace-nowrap">
                  {formatTime(event.timestamp)}
                </span>
              </div>

              {/* Execution Details */}
              {(event.latencyMs || event.tools || event.tokens || event.model) && (
                <div className="pl-6 mt-3 space-y-2">
                  {/* Latency Bar */}
                  {event.latencyMs && (
                    <div className="flex items-center gap-2 text-xs font-mono text-muted-foreground">
                      <span className="text-blue-500/80">{getLatencyBar(event.latencyMs)}</span>
                      <span>{event.latencyMs} ms</span>
                    </div>
                  )}

                  {/* Badges */}
                  <div className="flex flex-wrap gap-2 mt-2">
                    {event.model && <Badge label="Model" value={event.model} />}
                    {event.tokens && <Badge label="Tokens" value={event.tokens.toString()} />}
                    {event.workflowVersion && <Badge label="Workflow" value={`v${event.workflowVersion}`} />}
                    {event.promptVersion && <Badge label="Prompt" value={`v${event.promptVersion}`} />}
                  </div>

                  {/* Tools */}
                  {event.tools && event.tools.length > 0 && (
                    <div className="flex flex-wrap gap-1 mt-1">
                      <span className="text-xs text-muted-foreground font-medium mr-1">Tools:</span>
                      {event.tools.map(t => (
                        <span key={t} className="px-1.5 py-0.5 rounded-sm bg-muted text-muted-foreground text-[10px] font-mono border border-border/50">
                          {t}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}

function getLatencyBar(ms: number) {
  const blocks = Math.min(Math.max(Math.ceil(ms / 200), 1), 10);
  return "█".repeat(blocks);
}

function getSeverityColor(severity: string) {
  switch (severity) {
    case "ERROR": return "bg-red-500";
    case "WARNING": return "bg-amber-500";
    case "SUCCESS": return "bg-green-500";
    default: return "bg-blue-500";
  }
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

function Badge({ label, value }: { label: string, value: string }) {
  return (
    <div className="flex items-center text-[10px] bg-secondary/50 rounded-md overflow-hidden border border-border/40">
      <span className="px-1.5 py-0.5 bg-secondary text-secondary-foreground font-medium">{label}</span>
      <span className="px-1.5 py-0.5 font-mono text-muted-foreground">{value}</span>
    </div>
  );
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
