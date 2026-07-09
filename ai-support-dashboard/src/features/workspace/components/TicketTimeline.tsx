import { format } from "date-fns";
import { 
  CheckCircle2, 
  Clock, 
  PlayCircle, 
  CircleDashed,
  FileText,
  BrainCircuit,
  BookOpen,
  Network,
  UserCheck,
  CheckSquare
} from "lucide-react";
import type { TimelineEvent, TimelineEventType } from "@/shared/types/workspace";

interface TicketTimelineProps {
  events: TimelineEvent[];
}

export function TicketTimeline({ events }: TicketTimelineProps) {
  // Sort events by timestamp ascending
  const sortedEvents = [...events].sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

  return (
    <div className="relative border-l border-border ml-3 py-2 space-y-7">
      {sortedEvents.map((event) => {
        return (
          <div key={event.id} className="relative pl-6">
            <div className={`absolute -left-[14px] top-0.5 bg-background p-1 border border-border rounded-full`}>
              <EventIcon type={event.type} />
            </div>
            
            <div className="flex flex-col gap-1.5 pt-0.5">
              <div className="flex items-center gap-2">
                <span className={`text-sm font-medium ${event.status === 'completed' ? 'text-foreground' : 'text-muted-foreground'}`}>
                  {event.title}
                </span>
                <span className="text-xs text-muted-foreground font-mono">
                  {format(new Date(event.timestamp), "HH:mm:ss")}
                </span>
              </div>
              
              <div className="flex items-start gap-2">
                <StatusIndicator status={event.status} />
                <p className="text-xs text-muted-foreground">
                  {event.description}
                </p>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

function EventIcon({ type }: { type: TimelineEventType }) {
  switch (type) {
    case "CREATED": return <FileText className="h-4 w-4 text-blue-400" />;
    case "AI_ANALYSIS": return <BrainCircuit className="h-4 w-4 text-blue-400" />;
    case "KNOWLEDGE_RETRIEVED": return <BookOpen className="h-4 w-4 text-emerald-400" />;
    case "ROUTING_DECISION": return <Network className="h-4 w-4 text-blue-400" />;
    case "ASSIGNMENT": return <UserCheck className="h-4 w-4 text-orange-400" />;
    case "STATUS_CHANGE": return <CircleDashed className="h-4 w-4 text-muted-foreground" />;
    case "RESOLVED": return <CheckSquare className="h-4 w-4 text-emerald-500" />;
    default: return <CircleDashed className="h-4 w-4 text-muted-foreground" />;
  }
}

function StatusIndicator({ status }: { status: TimelineEvent['status'] }) {
  switch (status) {
    case "completed":
      return <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500/70 mt-0.5 shrink-0" />;
    case "in_progress":
      return <PlayCircle className="h-3.5 w-3.5 text-blue-400/70 mt-0.5 shrink-0" />;
    case "pending":
      return <Clock className="h-3.5 w-3.5 text-muted-foreground mt-0.5 shrink-0" />;
    case "failed":
      return <CircleDashed className="h-3.5 w-3.5 text-red-400/70 mt-0.5 shrink-0" />;
  }
}
