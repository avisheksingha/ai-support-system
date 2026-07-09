import { useState } from "react";
import { useTicketList } from "../hooks/useWorkspace";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { formatDistanceToNow } from "date-fns";
import { Search, Loader2 } from "lucide-react";
import type { TicketModel } from "@/shared/types/ticket";

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
      <div className="p-4 border-b border-zinc-800 shrink-0">
        <h2 className="text-xl font-bold mb-4">Tickets</h2>
        <div className="relative">
          <Search className="absolute left-3 top-2.5 h-4 w-4 text-zinc-500" />
          <Input 
            placeholder="Search tickets..." 
            className="pl-9 bg-zinc-900 border-zinc-700 text-zinc-100 placeholder:text-zinc-500 focus-visible:ring-indigo-500"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>
      
      <div className="flex-1 overflow-y-auto p-2 space-y-1">
        {isLoading && (
          <div className="flex justify-center p-8 text-zinc-500">
            <Loader2 className="h-6 w-6 animate-spin" />
          </div>
        )}
        
        {isError && (
          <div className="text-red-400 p-4 text-sm text-center">
            Failed to load tickets.
          </div>
        )}

        {!isLoading && filteredTickets.length === 0 && (
          <div className="text-zinc-500 p-4 text-sm text-center">
            No tickets found.
          </div>
        )}

        {filteredTickets.map((ticket) => (
          <button
            key={ticket.id}
            onClick={() => onSelectTicket(ticket.ticketNumber)}
            className={`w-full text-left p-4 rounded-xl border transition-all duration-300 ease-in-out ${
              selectedTicket === ticket.ticketNumber
                ? "bg-indigo-500/10 border-indigo-500/50 scale-[1.02]"
                : "bg-zinc-900/30 border-transparent hover:bg-zinc-900 hover:border-zinc-800 hover:scale-[1.01]"
            }`}
          >
            <div className="flex justify-between items-start mb-2">
              <span className="text-xs font-mono text-zinc-400">{ticket.ticketNumber}</span>
              <span className="text-xs text-zinc-500">
                {ticket.updatedAt ? formatDistanceToNow(new Date(ticket.updatedAt), { addSuffix: true }) : 'just now'}
              </span>
            </div>
            <h3 className="text-sm font-medium text-zinc-100 line-clamp-2 mb-3">
              {ticket.subject}
            </h3>
            
            <div className="flex flex-wrap gap-2 items-center">
              <Badge variant="outline" className={`text-[10px] uppercase ${getPriorityColor(ticket.priority)}`}>
                {ticket.priority}
              </Badge>
              <Badge variant="secondary" className="text-[10px] bg-zinc-800 text-zinc-300">
                {ticket.status}
              </Badge>
              {ticket.assignedTo && (
                <div className="ml-auto text-xs text-zinc-500 truncate max-w-[80px]">
                  @{ticket.assignedTo}
                </div>
              )}
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}

function getPriorityColor(priority: string) {
  switch (priority) {
    case "CRITICAL": return "text-red-400 border-red-400/30 bg-red-400/10";
    case "HIGH": return "text-orange-400 border-orange-400/30 bg-orange-400/10";
    case "MEDIUM": return "text-yellow-400 border-yellow-400/30 bg-yellow-400/10";
    case "LOW": return "text-blue-400 border-blue-400/30 bg-blue-400/10";
    default: return "text-zinc-400 border-zinc-700";
  }
}
