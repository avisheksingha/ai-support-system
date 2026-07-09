import { useParams, useNavigate } from "react-router-dom";
import { useCustomerTicket } from "../hooks/useCustomerTickets";
import { formatDistanceToNow, format } from "date-fns";
import { ArrowLeft, Loader2, Clock, CheckCircle2, MessageSquare, Paperclip } from "lucide-react";
import { Button } from "@/components/ui/button";

export function CustomerTicketDetailPage() {
  const { ticketNumber } = useParams<{ ticketNumber: string }>();
  const navigate = useNavigate();
  const { data: ticket, isLoading, isError } = useCustomerTicket(ticketNumber || "");

  if (isLoading) {
    return (
      <div className="flex-1 flex items-center justify-center bg-zinc-950">
        <Loader2 className="h-8 w-8 animate-spin text-indigo-500" />
      </div>
    );
  }

  if (isError || !ticket) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center bg-zinc-950 text-zinc-100">
        <h2 className="text-xl font-semibold mb-2">Ticket Not Found</h2>
        <p className="text-zinc-500 mb-6">This ticket may not exist or you don't have access to it.</p>
        <Button onClick={() => navigate("/my-tickets")} variant="outline">
          Return to My Tickets
        </Button>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-zinc-950 text-zinc-50 overflow-hidden">
      {/* Header */}
      <div className="border-b border-zinc-800/60 bg-zinc-950/50 backdrop-blur p-6 shrink-0">
        <div className="max-w-4xl mx-auto w-full">
          <button 
            onClick={() => navigate("/my-tickets")}
            className="flex items-center gap-2 text-sm text-zinc-400 hover:text-zinc-200 transition-colors mb-4 group w-fit"
          >
            <ArrowLeft className="h-4 w-4 group-hover:-translate-x-1 transition-transform" />
            Back to tickets
          </button>
          
          <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <span className="text-sm font-mono text-indigo-400 bg-indigo-500/10 px-2 py-0.5 rounded border border-indigo-500/20">
                  {ticket.ticketNumber}
                </span>
                <StatusChip status={ticket.status} />
              </div>
              <h1 className="text-2xl font-bold tracking-tight text-zinc-100 mt-1">
                {ticket.subject}
              </h1>
            </div>
            
            <div className="flex items-center gap-4 text-sm text-zinc-500 bg-zinc-900/50 p-3 rounded-lg border border-zinc-800/50">
              <div className="flex flex-col">
                <span className="text-[10px] uppercase tracking-wider font-semibold text-zinc-600 mb-1">Created</span>
                <span className="flex items-center gap-1.5 text-zinc-300">
                  <Clock className="h-3.5 w-3.5" />
                  {format(new Date(ticket.createdAt), "MMM d, yyyy")}
                </span>
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
            <div className="bg-zinc-900/40 border border-zinc-800/60 rounded-xl overflow-hidden">
              <div className="px-5 py-4 border-b border-zinc-800/60 bg-zinc-900/60 flex items-center gap-3">
                <div className="h-8 w-8 rounded-full bg-indigo-500/20 flex items-center justify-center text-indigo-400 font-medium border border-indigo-500/30">
                  {ticket.customerName?.charAt(0)?.toUpperCase() || "U"}
                </div>
                <div>
                  <div className="text-sm font-medium text-zinc-200">You</div>
                  <div className="text-xs text-zinc-500">{formatDistanceToNow(new Date(ticket.createdAt), { addSuffix: true })}</div>
                </div>
              </div>
              <div className="p-5 text-sm text-zinc-300 whitespace-pre-wrap leading-relaxed">
                {ticket.message}
              </div>
            </div>
            
            {/* Placeholder for future comments */}
            <div className="flex items-center gap-3 text-sm text-zinc-500 py-4 before:h-px before:flex-1 before:bg-zinc-800/60 after:h-px after:flex-1 after:bg-zinc-800/60">
              <MessageSquare className="h-4 w-4" />
              <span>Communication History</span>
            </div>
            
            <div className="text-center p-8 border border-dashed border-zinc-800/60 rounded-xl bg-zinc-900/20">
              <p className="text-sm text-zinc-500">
                Support agents will reply here. (Comments coming soon)
              </p>
            </div>
          </div>
          
          {/* Sidebar / Status Timeline */}
          <div className="space-y-6">
            <div className="bg-zinc-900/40 border border-zinc-800/60 rounded-xl p-5">
              <h3 className="text-sm font-semibold text-zinc-100 mb-4">Request Timeline</h3>
              <TicketTimeline status={ticket.status} />
            </div>
            
            <div className="bg-zinc-900/40 border border-zinc-800/60 rounded-xl p-5 opacity-60">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-semibold text-zinc-100">Attachments</h3>
                <Paperclip className="h-4 w-4 text-zinc-500" />
              </div>
              <p className="text-xs text-zinc-500">No attachments provided. (Feature coming soon)</p>
            </div>
          </div>
          
        </div>
      </div>
    </div>
  );
}

function StatusChip({ status }: { status: string }) {
  let style = "text-zinc-400 bg-zinc-800/50 border-zinc-700";
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
      style = "text-zinc-400 bg-zinc-500/10 border-zinc-500/20";
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
                ? "bg-indigo-500/20 border-indigo-500 text-indigo-400" 
                : "bg-zinc-900 border-zinc-700 text-transparent"
            }`}>
              {step.active && <CheckCircle2 className="h-3.5 w-3.5" />}
            </div>
            {index < steps.length - 1 && (
              <div className={`w-px h-full min-h-[1.5rem] my-1 ${
                steps[index + 1]?.active ? "bg-indigo-500/50" : "bg-zinc-800"
              }`} />
            )}
          </div>
          <div className={`text-sm pt-0.5 ${step.active ? "text-zinc-200 font-medium" : "text-zinc-500"}`}>
            {step.label}
          </div>
        </div>
      ))}
    </div>
  );
}
