import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useCustomerAddMessage, customerKeys } from "../hooks/useCustomerTickets";
import { useCustomerTicketDetail } from "../hooks/useCustomerDashboard";
import { formatTimeAgo, parseDate, formatTime } from "@/shared/utils/date";
import { format } from "date-fns";
import { Loader2, Paperclip, CheckCircle2, ChevronRight, User, Bot, HeadphonesIcon, UploadCloud } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { TicketDetailDTO } from "../api/customerDashboardApi";
import { getCustomerStatusMapping } from "../utils/customer-status";
import { wsClient } from "@/lib/websocket";
import { useQueryClient } from "@tanstack/react-query";


export function CustomerTicketDetailPage() {
  const { ticketNumber } = useParams<{ ticketNumber: string }>();
  const navigate = useNavigate();
  const { data: detailData, isLoading, isError } = useCustomerTicketDetail(ticketNumber || "");
  const ticket = detailData?.ticket;
  const messages = detailData?.messages || [];
  const customerAssistance = detailData?.customerAssistance;
  const { mutate: addMessage, isPending: isSendingMessage } = useCustomerAddMessage();
  const [replyText, setReplyText] = useState("");
  const queryClient = useQueryClient();

  React.useEffect(() => {
    if (!ticketNumber) return;
    
    const topic = `/topic/tickets.${ticketNumber}`;
    
    wsClient.subscribe(topic, (event) => {
      console.log("Customer Portal received WebSocket event:", event);
      if (event.eventType === "AGENT_REPLY_ADDED" || event.eventType === "CUSTOMER_REPLY_ADDED" || event.eventType === "MESSAGE_ADDED") {
          queryClient.invalidateQueries({ queryKey: [...customerKeys.all, "messages", ticketNumber] });
      }
    });

    return () => {
      wsClient.unsubscribe(topic);
    };
  }, [ticketNumber, queryClient]);

  const handleSendReply = () => {
    if (!replyText.trim()) return;
    addMessage({ ticketNumber: ticket!.ticketNumber, content: replyText }, {
      onSuccess: () => setReplyText("")
    });
  };

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
                <div className="h-8 w-8 shrink-0 rounded-full bg-slate-100 border border-slate-200 shadow-sm flex items-center justify-center text-sm">
                  👤
                </div>
                <div>
                  <div className="flex items-baseline gap-2">
                    <span className="text-sm font-medium text-foreground">You</span>
                    <span className="text-[10px] text-muted-foreground">{formatTimeAgo(ticket.createdAt)}</span>
                  </div>
                </div>
              </div>
              <div className="p-5 text-sm text-foreground whitespace-pre-wrap leading-relaxed">
                {ticket.message}
              </div>
            </div>
            
            <div className="mt-4">
              <h2 className="text-[11px] font-bold text-muted-foreground mb-4 uppercase tracking-widest px-1">Communication History</h2>
              
              <div className="space-y-4 bg-card rounded-xl p-5 border border-border shadow-sm flex flex-col">
              {isLoading ? (
                <div className="text-center p-10 border border-dashed border-border rounded-xl bg-card shadow-sm animate-pulse">
                   <h3 className="text-sm font-medium text-muted-foreground mb-2">Loading messages...</h3>
                </div>
              ) : messages && messages.length > 0 && (
                <div className="space-y-6">
                  {messages.filter((m: any) => !m.isInternal && !m.internal && m.type !== 'INTERNAL_NOTE').map((msg: any) => {
                    const isCustomer = msg.type === 'CUSTOMER_MESSAGE';
                    const isSystem = msg.type === 'SYSTEM_MESSAGE' || msg.senderName === 'System';
                    
                    const avatar = isCustomer ? <User className="h-4 w-4" /> : isSystem ? <Bot className="h-4 w-4" /> : <HeadphonesIcon className="h-4 w-4" />;
                    const avatarBg = isCustomer ? 'bg-blue-100 text-blue-700 border-blue-200' : isSystem ? 'bg-slate-100 text-slate-600 border-slate-200' : 'bg-emerald-100 text-emerald-700 border-emerald-200';
                    const displayName = isCustomer ? 'Me' : isSystem ? 'System' : (msg.senderName || 'Support Agent');
                    
                    const msgDate = parseDate(msg.createdAt);
                    const timeString = `${format(msgDate, "h:mm a")} (${formatTimeAgo(msg.createdAt)})`;

                    return (
                      <div key={msg.id} className={`flex gap-3 ${isCustomer ? 'flex-row-reverse' : ''}`}>
                        <div className={`h-8 w-8 shrink-0 rounded-full border shadow-sm flex items-center justify-center mt-1 ${avatarBg}`}>
                          {avatar}
                        </div>
                        <div className={`flex flex-col max-w-[85%] ${isCustomer ? 'items-end' : 'items-start'}`}>
                          <div className={`flex items-baseline gap-2 mb-1 ${isCustomer ? 'flex-row-reverse' : ''}`}>
                            <span className="text-[11px] font-bold text-slate-700">{displayName}</span>
                            <span className="text-[10px] text-slate-400 font-medium">{timeString}</span>
                          </div>
                          <div className={`px-4 py-2.5 text-[13px] whitespace-pre-wrap leading-relaxed shadow-sm ${isCustomer ? 'bg-white border border-slate-200 text-slate-800 rounded-2xl rounded-tr-sm text-left' : isSystem ? 'bg-slate-100 border border-slate-200 text-slate-700 rounded-2xl text-left' : 'bg-blue-600 text-white rounded-2xl rounded-tl-sm text-left'}`}>
                            {msg.content}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
              
              {/* Message Input Box */}
              <div className="mt-2 pt-2 border-t border-border flex gap-2">
                 <div className="flex-1 flex flex-col gap-1.5">
                   <textarea 
                     className="w-full border border-border bg-background rounded-lg p-2.5 text-[12px] min-h-[80px] focus:outline-none focus:ring-2 focus:ring-blue-500/50 resize-y leading-tight" 
                     placeholder="Add a reply..."
                     value={replyText}
                     onChange={e => setReplyText(e.target.value)}
                   ></textarea>
                   <div className="flex justify-end mt-1">
                      <Button 
                        size="sm"
                        onClick={handleSendReply}
                        disabled={isSendingMessage || !replyText.trim()}
                        className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-1.5 text-[11px] h-auto"
                      >
                        {isSendingMessage ? 'Sending...' : 'Send Reply'}
                      </Button>
                   </div>
               </div>
             </div>
           </div>
          </div>
        </div>
        
        {/* Sidebar */}
          <div className="space-y-6">
            <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
              <h3 className="text-sm font-semibold text-foreground mb-1">Current Status</h3>
              <div className="flex flex-col gap-4 mb-5 pb-5 border-b border-border">
                <div className="flex justify-between items-center">
                  <span className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Status</span>
                  <span className="text-sm font-medium text-foreground">{ticket.status}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Assigned Team</span>
                  <span className="text-sm font-medium text-foreground">{ticket.assignedSupportStatus}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Est. Response</span>
                  <span className="text-sm font-medium text-foreground">{ticket.estimatedResponse}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Last Updated</span>
                  <span className="text-sm font-medium text-foreground">{ticket.lastUpdated ? formatTimeAgo(ticket.lastUpdated) : 'just now'}</span>
                </div>
              </div>
              
              <TicketTimeline ticket={ticket as any} />
            </div>
            
            {customerAssistance && (
              <div className="bg-blue-50/50 border border-blue-200 rounded-xl p-5 shadow-sm">
                <h3 className="text-sm font-semibold text-blue-900 mb-2">Recommended Resources</h3>
                <h4 className="text-xs font-bold text-blue-800 mb-1">{customerAssistance.title}</h4>
                <p className="text-[13px] text-blue-800/80 mb-4 leading-relaxed">
                  {customerAssistance.summary}
                </p>
                {customerAssistance.resourceLinks && customerAssistance.resourceLinks.length > 0 && (
                  <div className="flex flex-col gap-2">
                    {customerAssistance.resourceLinks.map((link, idx) => (
                      <a key={idx} href="#" className="text-xs text-blue-600 hover:underline font-medium flex items-center gap-1">
                        <ChevronRight className="h-3 w-3" /> {link}
                      </a>
                    ))}
                  </div>
                )}
              </div>
            )}
            
            <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-sm font-semibold text-foreground">Attachments</h3>
              </div>
              <div className="flex flex-col items-center justify-center py-8 bg-muted/20 hover:bg-muted/40 transition-colors rounded-lg border-2 border-dashed border-border/80 text-center gap-2 cursor-not-allowed opacity-80">
                <div className="h-10 w-10 rounded-full bg-slate-100 flex items-center justify-center mb-1">
                  <UploadCloud className="h-5 w-5 text-slate-500" />
                </div>
                <div className="text-sm text-foreground font-medium">
                  Drag & drop files here or <span className="text-blue-600">Browse Files</span>
                </div>
                <div className="text-[11px] text-muted-foreground uppercase tracking-wider font-semibold mt-1">
                  Coming Soon
                </div>
              </div>
            </div>
          </div>
          
        </div>
      </div>
    </div>
  );
}


function StatusChip({ status }: { status: string }) {
  const mapping = getCustomerStatusMapping(status);

  return (
    <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded border ${mapping.style}`}>
      {mapping.label}
    </span>
  );
}

function TicketTimeline({ ticket }: { ticket: TicketDetailDTO }) {
  const mapping = getCustomerStatusMapping(ticket.status);
  const { isReviewed, isAssigned, isResolved } = mapping.progress;
  
  const createdAt = parseDate(ticket.createdAt);
  const lastUpdated = ticket.lastUpdated ? parseDate(ticket.lastUpdated) : null;
  
  const steps = [
    { key: "SUBMITTED", label: "Submitted", time: formatTime(createdAt), active: true, done: true },
    { key: "REVIEW", label: "Under Review", time: (isReviewed && !isAssigned && lastUpdated) ? formatTime(lastUpdated) : null, active: isReviewed, done: isAssigned },
    { key: "PROGRESS", label: "Assigned to Agent", time: (isAssigned && !isResolved && lastUpdated) ? formatTime(lastUpdated) : null, active: isAssigned, done: isResolved },
    { key: "RESOLVED", label: "Resolved", time: (isResolved && lastUpdated) ? formatTime(lastUpdated) : null, active: isResolved, done: isResolved },
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
