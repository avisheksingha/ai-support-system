import { useState } from "react";
import { useTicketList } from "../hooks/useWorkspace";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Search, Loader2 } from "lucide-react";
import type { TicketModel } from "@/shared/types/ticket";
import { formatTimeAgo } from "@/shared/utils/date";

interface TicketListProps {
  selectedTicket: string | null;
  onSelectTicket: (ticketNumber: string) => void;
}

export function TicketList({ selectedTicket, onSelectTicket }: TicketListProps) {
  const { data: tickets, isLoading, isError } = useTicketList();
  const [searchTerm, setSearchTerm] = useState("");

  const filteredTickets = tickets?.filter((t: TicketModel) => 
    t.ticketNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
    t.subject.toLowerCase().includes(searchTerm.toLowerCase()) ||
    t.customerName.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  return (
    <div className="flex flex-col h-full overflow-hidden">
      <div className="shrink-0">
        <div className="h-16 flex items-center px-5 border-b border-border">
          <span className="font-bold text-sm tracking-tight text-foreground">
            Tickets
          </span>
        </div>
        <div className="p-4 border-b border-border">
          <div className="relative">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input 
              placeholder="Search tickets..." 
              className="pl-9 bg-card border-border text-foreground placeholder:text-muted-foreground focus-visible:ring-[#0C66E4] shadow-sm"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
      </div>
      
      <div className="flex-1 overflow-y-auto p-2 space-y-1">
        {isLoading && (
          <div className="flex justify-center p-8 text-muted-foreground">
            <Loader2 className="h-6 w-6 animate-spin" />
          </div>
        )}
        
        {isError && (
          <div className="text-red-400 p-4 text-sm text-center">
            Failed to load tickets.
          </div>
        )}

        {!isLoading && filteredTickets.length === 0 && (
          <div className="text-muted-foreground p-4 text-sm text-center">
            No tickets found.
          </div>
        )}

        {filteredTickets.map((ticket) => (
          <button
            key={ticket.id}
            onClick={() => onSelectTicket(ticket.ticketNumber)}
            className={`w-full text-left p-4 rounded-xl border transition-all duration-300 ease-in-out ${
              selectedTicket === ticket.ticketNumber
                ? "bg-blue-500/10 border-blue-500/50 scale-[1.02]"
                : "bg-card border-transparent hover:bg-card hover:border-border hover:scale-[1.01]"
            }`}
          >
            <div className="mb-1">
              <span className="text-[10px] font-mono text-muted-foreground">{ticket.ticketNumber}</span>
            </div>
            <h3 className="text-base font-semibold text-foreground line-clamp-2 mb-3 leading-tight">
              {ticket.subject}
            </h3>
            
            <div className="space-y-1.5 mb-3">
              <div className="text-xs text-foreground/80 flex items-center gap-2">
                <span>👤</span> {ticket.customerName || "Customer"}
              </div>
              <div className="text-xs text-foreground/80 flex items-center gap-2">
                <span>👨</span> {ticket.assignedTo ? `Assigned: ${ticket.assignedTo}` : "Unassigned"}
              </div>
              <div className="text-[11px] text-muted-foreground flex items-center gap-2">
                <span>🕒</span> Updated {ticket.updatedAt ? formatTimeAgo(ticket.updatedAt) : 'just now'}
              </div>
            </div>

            <div className="flex flex-wrap gap-2 items-center mt-3 pt-3 border-t border-border/50">
              <Badge variant="outline" className={`text-[10px] uppercase ${getPriorityColor(ticket.priority)}`}>
                {ticket.priority}
              </Badge>
              <Badge variant="outline" className={`text-[10px] uppercase ${getStatusColor(ticket.status)}`}>
                {ticket.status}
              </Badge>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}

function getPriorityColor(priority: string) {
  switch (priority) {
    case "CRITICAL": return "text-red-600 border-red-200 bg-red-50";
    case "HIGH": return "text-orange-600 border-orange-200 bg-orange-50";
    case "MEDIUM": return "text-yellow-600 border-yellow-200 bg-yellow-50";
    case "LOW": return "text-blue-600 border-blue-200 bg-blue-50";
    default: return "text-muted-foreground border-border";
  }
}

function getStatusColor(status: string) {
  switch (status) {
    case "NEW": return "text-blue-600 border-blue-200 bg-blue-50";
    case "ANALYZING":
    case "ANALYZED": return "text-purple-600 border-purple-200 bg-purple-50";
    case "ASSIGNED": return "text-emerald-600 border-emerald-200 bg-emerald-50";
    case "IN_PROGRESS": return "text-cyan-600 border-cyan-200 bg-cyan-50";
    case "RESOLVED": return "text-green-600 border-green-200 bg-green-50";
    case "CLOSED": return "text-gray-600 border-gray-200 bg-gray-50";
    default: return "text-muted-foreground border-border";
  }
}
