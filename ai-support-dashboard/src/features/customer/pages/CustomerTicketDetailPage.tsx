import { useParams, useNavigate } from "react-router-dom";
import { useCustomerTicket } from "../hooks/useCustomerTickets";
import { formatTimeAgo, parseDate, formatTime } from "@/shared/utils/date";
import { format } from "date-fns";
import { Loader2, MessageSquare, Paperclip, CheckCircle2, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { TicketModel } from "@/shared/types/ticket";

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
          {/* Breadcrumb */}
          <div className="flex items-center gap-1.5 text-xs text-muted-foreground mb-4">
            <button onClick={() => navigate("/my-tickets")} className="hover:text-foreground transition-colors">Portal</button>
            <ChevronRight className="h-3 w-3" />
            <button onClick={() => navigate("/my-tickets")} className="hover:text-foreground transition-colors">My Tickets</button>
            <ChevronRight className="h-3 w-3" />
            <span className="font-medium text-foreground">{ticket.ticketNumber}</span>
          </div>
          
          <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <StatusChip status={ticket.status} />
              </div>
              <h1 className="text-2xl font-bold tracking-tight text-foreground mt-1">
                {ticket.subject}
              </h1>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-8 text-sm text-muted-foreground bg-card p-4 px-6 rounded-xl border border-border mt-6">
            <div className="flex flex-col">
              <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Ticket ID</span>
              <div className="font-medium text-foreground">{ticket.ticketNumber}</div>
            </div>
            <div className="flex flex-col">
              <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Created</span>
              <div className="font-medium text-foreground">{format(parseDate(ticket.createdAt), "dd MMM yyyy")}</div>
            </div>
            <div className="flex flex-col">
              <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Category</span>
              <div className="font-medium text-foreground">Support</div>
            </div>
            <div className="flex flex-col">
              <span className="text-[10px] uppercase tracking-wider font-semibold text-muted-foreground mb-1">Priority</span>
              <div className="font-medium text-foreground capitalize">{ticket.priority.toLowerCase()}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto w-full grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Conversation/Details */}
          <div className="lg:col-span-2 space-y-6">
            <div className="bg-card border border-border rounded-xl overflow-hidden shadow-sm">
              <div className="px-5 py-4 border-b border-border bg-card flex items-center gap-3">
                <div className="h-8 w-8 rounded-full bg-blue-500/10 flex items-center justify-center text-blue-600 font-medium border border-blue-500/20">
                  {ticket.customerName?.charAt(0)?.toUpperCase() || "U"}
                </div>
                <div>
                  <div className="flex items-baseline gap-2">
                    <span className="text-sm font-medium text-foreground">{ticket.customerName}</span>
                    <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider bg-muted px-1.5 py-0.5 rounded">Customer</span>
                  </div>
                  <div className="text-xs text-muted-foreground mt-0.5">{formatTimeAgo(ticket.createdAt)}</div>
                </div>
              </div>
              <div className="p-5 text-sm text-foreground whitespace-pre-wrap leading-relaxed">
                {ticket.message}
              </div>
            </div>
            
            <div className="flex items-center gap-3 text-sm text-muted-foreground py-2 before:h-px before:flex-1 before:bg-muted after:h-px after:flex-1 after:bg-muted">
              <MessageSquare className="h-4 w-4" />
              <span>Communication History</span>
            </div>
            
            <div className="text-center p-10 border border-dashed border-border rounded-xl bg-card shadow-sm">
              <h3 className="text-sm font-medium text-foreground mb-2">No replies yet.</h3>
              <p className="text-[13px] text-muted-foreground leading-relaxed">
                Our support team will respond here.<br/>You'll receive an email notification when there's an update.
              </p>
            </div>
          </div>
          
          {/* Sidebar */}
          <div className="space-y-6">
            <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
              <h3 className="text-sm font-semibold text-foreground mb-1">Current Status</h3>
              <p className="text-[13px] text-muted-foreground mb-5 pb-5 border-b border-border leading-relaxed">
                {getStatusExplanation(ticket.status)}
              </p>
              
              <TicketTimeline ticket={ticket} />
            </div>
            
            <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-semibold text-foreground">Attachments</h3>
              </div>
              <div className="flex flex-col items-center justify-center py-6 bg-muted/30 rounded-lg border border-dashed border-border text-center gap-2">
                <Paperclip className="h-5 w-5 text-muted-foreground/60" />
                <div className="text-xs text-muted-foreground mt-1">
                  <span className="font-medium text-foreground">No attachments</span>
                  <br/>
                  <span className="opacity-70">(Upload support coming soon)</span>
                </div>
              </div>
            </div>
          </div>
          
        </div>
      </div>
    </div>
  );
}

function getStatusExplanation(status: string) {
  if (["NEW", "ANALYZING", "ANALYZED"].includes(status)) {
    return "Our support system is reviewing your request. We've received it and are preparing a response.";
  }
  if (["ASSIGNED", "IN_PROGRESS"].includes(status)) {
    return "Your request has been assigned to a support agent. Estimated response: within 4 hours.";
  }
  return "This ticket has been resolved and closed.";
}

function StatusChip({ status }: { status: string }) {
  let style = "text-muted-foreground bg-muted border-border";
  let label = status;

  switch (status) {
    case "NEW":
    case "ANALYZING":
    case "ANALYZED":
      style = "text-blue-600 bg-blue-50 border-blue-200";
      label = "Submitted";
      break;
    case "ASSIGNED":
      style = "text-purple-600 bg-purple-50 border-purple-200";
      label = "In Review";
      break;
    case "IN_PROGRESS":
      style = "text-orange-600 bg-orange-50 border-orange-200";
      label = "In Progress";
      break;
    case "RESOLVED":
      style = "text-emerald-600 bg-emerald-50 border-emerald-200";
      label = "Resolved";
      break;
    case "CLOSED":
      style = "text-gray-600 bg-gray-50 border-gray-200";
      label = "Closed";
      break;
  }

  return (
    <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded border ${style}`}>
      {label}
    </span>
  );
}

function TicketTimeline({ ticket }: { ticket: TicketModel }) {
  const status = ticket.status;
  const createdAt = parseDate(ticket.createdAt);
  const m1 = new Date(createdAt.getTime() + 60000); // 1 min later
  
  const steps = [
    { key: "SUBMITTED", label: "Submitted", time: formatTime(createdAt), active: true, done: true },
    { key: "REVIEW", label: "Under Review", time: formatTime(m1), active: ["ANALYZING", "ANALYZED", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"].includes(status), done: ["ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"].includes(status) },
    { key: "PROGRESS", label: "Assigned to Agent", time: null, active: ["ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"].includes(status), done: ["RESOLVED", "CLOSED"].includes(status) },
    { key: "RESOLVED", label: "Resolved", time: null, active: ["RESOLVED", "CLOSED"].includes(status), done: ["RESOLVED", "CLOSED"].includes(status) },
  ];
  
  return (
    <div className="space-y-0">
      {steps.map((step, index) => (
        <div key={step.key} className="flex gap-4">
          <div className="flex flex-col items-center">
            <div className={`h-4 w-4 mt-0.5 rounded-full flex items-center justify-center shrink-0 border ${
              step.done 
                ? "bg-blue-50 border-blue-200 text-blue-600" 
                : step.active ? "bg-blue-600 border-blue-600" : "bg-card border-border"
            }`}>
              {step.done ? <CheckCircle2 className="h-3 w-3" /> : (step.active ? <div className="h-1.5 w-1.5 bg-white rounded-full" /> : null)}
            </div>
            {index < steps.length - 1 && (
              <div className={`w-px min-h-[2rem] h-full my-1 ${
                step.done ? "bg-blue-200" : "bg-muted"
              }`} />
            )}
          </div>
          <div className={`pb-4 pt-0 flex flex-col ${step.active || step.done ? "text-foreground font-medium" : "text-muted-foreground"}`}>
            <span className="text-[13px] leading-tight">{step.label}</span>
            {(step.time && (step.active || step.done)) && <span className="text-[11px] text-muted-foreground font-normal mt-0.5">{step.time}</span>}
          </div>
        </div>
      ))}
    </div>
  );
}
