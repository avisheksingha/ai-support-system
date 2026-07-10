import { useParams, useNavigate } from "react-router-dom";
import { useCustomerTicket } from "../hooks/useCustomerTickets";
import { formatTimeAgo, parseDate } from "@/shared/utils/date";
import { format } from "date-fns";
import { ArrowLeft, Loader2, Clock, CheckCircle2, MessageSquare, Paperclip } from "lucide-react";
import { Button } from "@/components/ui/button";

export function CustomerTicketDetailPage() {
  const { ticketNumber } = useParams<{ ticketNumber: string }>();
  const navigate = useNavigate();
  const { data: ticket, isLoading, isError } = useCustomerTicket(ticketNumber || "");

  if (isLoading) {
    return (
      <div className="flex-1 flex items-center justify-center bg-background">
        <Loader2 className="h-8 w-8 animate-spin text-blue-500" />
      </div>
    );
  }

  if (isError || !ticket) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center bg-background text-foreground">
        <h2 className="text-xl font-semibold mb-2">Ticket Not Found</h2>
        <p className="text-muted-foreground mb-6">This ticket may not exist or you don't have access to it.</p>
        <Button onClick={() => navigate("/my-tickets")} variant="outline">
          Return to My Tickets
        </Button>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-background text-foreground overflow-hidden">
      {/* Header */}
      <div className="border-b border-border bg-background/50 backdrop-blur p-6 shrink-0">
        <div className="max-w-4xl mx-auto w-full">
          <button 
            onClick={() => navigate("/my-tickets")}
            className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors mb-4 group w-fit"
          >
            <ArrowLeft className="h-4 w-4 group-hover:-translate-x-1 transition-transform" />
            Back to tickets
          </button>
          
          <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <span className="text-sm font-mono text-blue-400 bg-blue-500/10 px-2 py-0.5 rounded border border-blue-500/20">
                  {ticket.ticketNumber}
                </span>
                <StatusChip status={ticket.status} />
              </div>
              <h1 className="text-2xl font-bold tracking-tight text-foreground mt-1">
                {ticket.subject}
              </h1>
            </div>
            
            <div className="flex items-center gap-4 text-sm text-muted-foreground bg-card p-3 rounded-lg border border-border">
              <div className="flex flex-col">
                <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Created</span>
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-muted-foreground" />
                  {format(parseDate(ticket.createdAt), "MMM d, yyyy")}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto w-full grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Conversation/Details */}
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-card border border-border rounded-xl overflow-hidden">
              <div className="px-5 py-4 border-b border-border bg-card flex items-center gap-3">
                <div className="h-8 w-8 rounded-full bg-blue-500/20 flex items-center justify-center text-blue-400 font-medium border border-blue-500/30">
                  {ticket.customerName?.charAt(0)?.toUpperCase() || "U"}
                </div>
                <div>
                  <div className="text-sm font-medium text-foreground">You</div>
                  <div className="text-xs text-muted-foreground">{formatTimeAgo(ticket.createdAt)}</div>
                </div>
              </div>
              <div className="p-5 text-sm text-foreground whitespace-pre-wrap leading-relaxed">
                {ticket.message}
              </div>
            </div>
            
            {/* Placeholder for future comments */}
            <div className="flex items-center gap-3 text-sm text-muted-foreground py-4 before:h-px before:flex-1 before:bg-muted after:h-px after:flex-1 after:bg-muted">
              <MessageSquare className="h-4 w-4" />
              <span>Communication History</span>
            </div>
            
            <div className="text-center p-8 border border-dashed border-border rounded-xl bg-card">
              <p className="text-sm text-muted-foreground">
                Support agents will reply here. (Comments coming soon)
              </p>
            </div>
          </div>
          
          {/* Sidebar / Status Timeline */}
          <div className="space-y-6">
            <div className="bg-card border border-border rounded-xl p-5">
              <h3 className="text-sm font-semibold text-foreground mb-4">Request Timeline</h3>
              <TicketTimeline status={ticket.status} />
            </div>
            
            <div className="bg-card border border-border rounded-xl p-5 opacity-60">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-semibold text-foreground">Attachments</h3>
                <Paperclip className="h-4 w-4 text-muted-foreground" />
              </div>
              <p className="text-xs text-muted-foreground">No attachments provided. (Feature coming soon)</p>
            </div>
          </div>
          
        </div>
      </div>
    </div>
  );
}

function StatusChip({ status }: { status: string }) {
  let style = "text-muted-foreground bg-muted border-border";
  let label = status;

  switch (status) {
    case "NEW":
    case "ANALYZING":
    case "ANALYZED":
      style = "text-blue-400 bg-blue-500/10 border-blue-500/20";
      label = "Submitted";
      break;
    case "ASSIGNED":
    case "IN_PROGRESS":
      style = "text-amber-400 bg-amber-500/10 border-amber-500/20";
      label = "In Progress";
      break;
    case "RESOLVED":
      style = "text-emerald-400 bg-emerald-500/10 border-emerald-500/20";
      label = "Resolved";
      break;
    case "CLOSED":
      style = "text-muted-foreground bg-zinc-500/10 border-zinc-500/20";
      label = "Closed";
      break;
  }

  return (
    <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full border ${style}`}>
      {label}
    </span>
  );
}

function TicketTimeline({ status }: { status: string }) {
  const steps = [
    { key: "SUBMITTED", label: "Submitted", active: true },
    { key: "REVIEW", label: "Being Reviewed", active: ["ANALYZING", "ANALYZED", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"].includes(status) },
    { key: "PROGRESS", label: "In Progress", active: ["ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"].includes(status) },
    { key: "RESOLVED", label: "Resolved", active: ["RESOLVED", "CLOSED"].includes(status) },
  ];
  
  return (
    <div className="space-y-4">
      {steps.map((step, index) => (
        <div key={step.key} className="flex gap-3">
          <div className="flex flex-col items-center">
            <div className={`h-5 w-5 rounded-full flex items-center justify-center shrink-0 border ${
              step.active 
                ? "bg-blue-500/20 border-blue-500 text-blue-400" 
                : "bg-card border-border text-transparent"
            }`}>
              {step.active && <CheckCircle2 className="h-3.5 w-3.5" />}
            </div>
            {index < steps.length - 1 && (
              <div className={`w-px h-full min-h-[1.5rem] my-1 ${
                steps[index + 1]?.active ? "bg-blue-500/50" : "bg-muted"
              }`} />
            )}
          </div>
          <div className={`text-sm pt-0.5 ${step.active ? "text-foreground font-medium" : "text-muted-foreground"}`}>
            {step.label}
          </div>
        </div>
      ))}
    </div>
  );
}
