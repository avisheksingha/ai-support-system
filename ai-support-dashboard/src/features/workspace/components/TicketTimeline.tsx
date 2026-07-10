
import type { TimelineEvent, TimelineEventType } from "@/shared/types/workspace";
import { formatTime, parseDate } from "@/shared/utils/date";

interface TicketTimelineProps {
  events: TimelineEvent[];
}

export function TicketTimeline({ events }: TicketTimelineProps) {
  // Sort events by timestamp ascending
  const sortedEvents = [...events].sort((a, b) => parseDate(a.timestamp).getTime() - parseDate(b.timestamp).getTime());

  return (
    <div className="space-y-4 px-2">
      {sortedEvents.map((event, index) => (
        <div key={event.id} className="flex flex-col">
          <div className="flex items-center gap-2 mb-1.5">
            <span className="text-sm"><EventIcon type={event.type} /></span>
            <span className={`text-sm font-semibold ${event.status === 'completed' ? 'text-foreground' : 'text-muted-foreground'}`}>
              {event.title}
            </span>
          </div>
          
          <div className="flex justify-between items-start pl-6">
            <p className="text-[13px] text-foreground/80 leading-snug max-w-[80%]">
              {event.description}
            </p>
            <span className="text-xs text-muted-foreground font-mono whitespace-nowrap">
              {formatTime(event.timestamp)}
            </span>
          </div>

          {index < sortedEvents.length - 1 && (
            <div className="border-b border-border/60 my-4 ml-6" />
          )}
        </div>
      ))}
    </div>
  );
}

function EventIcon({ type }: { type: TimelineEventType }) {
  switch (type) {
    case "CREATED": return "📝";
    case "AI_ANALYSIS": return "🤖";
    case "KNOWLEDGE_RETRIEVED": return "📚";
    case "ROUTING_DECISION": return "🧭";
    case "ASSIGNMENT": return "👤";
    case "STATUS_CHANGE": return "🔄";
    case "RESOLVED": return "✅";
    default: return "🔹";
  }
}
