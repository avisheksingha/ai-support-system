import { useState, useEffect } from "react";
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

  useEffect(() => {
    if (!selectedTicket && tickets && tickets.length > 0 && tickets[0]?.ticketNumber) {
      onSelectTicket(tickets[0].ticketNumber);
    }
  }, [tickets, selectedTicket, onSelectTicket]);

  const filteredTickets = tickets?.filter((t: TicketModel) => 
    t.ticketNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
    t.subject.toLowerCase().includes(searchTerm.toLowerCase()) ||
    t.customerName.toLowerCase().includes(searchTerm.toLowerCase())
  ) || [];

  return (
    <div className="flex flex-col h-full overflow-hidden bg-white">
      <div className="shrink-0 bg-slate-50/50">
        <div className="h-16 flex items-center px-6 border-b border-slate-200">
          <span className="font-bold text-sm tracking-wide text-slate-800 uppercase flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-blue-500"></span>
            My Queue
          </span>
        </div>
        <div className="p-4 border-b border-slate-200 bg-white">
          <div className="relative">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-slate-400" />
            <Input 
              placeholder="Search by ID, name, or subject..." 
              className="pl-9 bg-slate-50/50 border-slate-200 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-500 shadow-sm"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
      </div>
      
      <div className="flex-1 overflow-y-auto p-3 space-y-2 bg-slate-50/30">
        {isLoading && (
          <div className="flex justify-center p-8 text-slate-400">
            <Loader2 className="h-6 w-6 animate-spin" />
          </div>
        )}
        
        {isError && (
          <div className="text-red-400 p-4 text-sm text-center font-medium bg-red-50 rounded-lg border border-red-100 mx-2">
            Failed to load queue.
          </div>
        )}

        {!isLoading && filteredTickets.length === 0 && (
          <div className="text-slate-500 p-8 text-sm text-center border-2 border-dashed border-slate-200 rounded-xl mx-2 mt-4 bg-white">
            <span className="block text-2xl mb-2">📭</span>
            No tickets match your search.
          </div>
        )}

        {filteredTickets.map((ticket) => (
          <button
            key={ticket.id}
            onClick={() => onSelectTicket(ticket.ticketNumber)}
            className={`w-full text-left p-4 rounded-xl border transition-all duration-200 ease-in-out group ${
              selectedTicket === ticket.ticketNumber
                ? "bg-indigo-50 border-indigo-200 ring-1 ring-indigo-500/20 shadow-sm"
                : "bg-white border-slate-200 hover:border-slate-300 hover:shadow-sm"
            }`}
          >
            <div className="flex items-center justify-between mb-2">
              <span className={`text-[10px] font-bold tracking-widest uppercase ${
                selectedTicket === ticket.ticketNumber ? "text-indigo-600" : "text-slate-500"
              }`}>{ticket.ticketNumber}</span>
              <span className="text-[10px] font-medium text-slate-400">{ticket.updatedAt ? formatTimeAgo(ticket.updatedAt) : 'just now'}</span>
            </div>
            <h3 className={`text-base font-semibold line-clamp-2 mb-3 leading-tight ${
              selectedTicket === ticket.ticketNumber ? "text-indigo-950" : "text-slate-800 group-hover:text-slate-900"
            }`}>
              {ticket.subject}
            </h3>
            
            <div className="space-y-1.5 mb-3">
              <div className="text-[11px] text-slate-600 font-medium flex items-center gap-2">
                <div className="w-4 h-4 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-[8px]">👤</div> 
                <span className="truncate">{ticket.customerName || "Customer"}</span>
              </div>
            </div>

            {/* Mini AI Triage Badges */}
            <div className="flex flex-wrap gap-1.5 items-center">
              <Badge variant="outline" className={`text-[9px] uppercase px-1.5 py-0 shadow-none font-bold ${getPriorityColor(ticket.priority)}`}>
                {ticket.priority}
              </Badge>
              <Badge variant="outline" className={`text-[9px] uppercase px-1.5 py-0 shadow-none font-bold ${getStatusColor(ticket.status)}`}>
                {ticket.status}
              </Badge>
              <span className="text-[9px] font-bold text-purple-700 bg-purple-50 px-1.5 py-0.5 rounded border border-purple-200">
                95% AI
              </span>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}

function getPriorityColor(priority: string) {
  switch (priority) {
    case "CRITICAL": return "text-red-700 border-red-200 bg-red-50";
    case "HIGH": return "text-orange-700 border-orange-200 bg-orange-50";
    case "MEDIUM": return "text-amber-700 border-amber-200 bg-amber-50";
    case "LOW": return "text-blue-700 border-blue-200 bg-blue-50";
    default: return "text-slate-500 border-slate-200 bg-slate-50";
  }
}

function getStatusColor(status: string) {
  switch (status) {
    case "NEW": return "text-blue-700 border-blue-200 bg-blue-50";
    case "ANALYZING":
    case "ANALYZED": return "text-purple-700 border-purple-200 bg-purple-50";
    case "ASSIGNED": return "text-indigo-700 border-indigo-200 bg-indigo-50";
    case "IN_PROGRESS": return "text-cyan-700 border-cyan-200 bg-cyan-50";
    case "RESOLVED": return "text-emerald-700 border-emerald-200 bg-emerald-50";
    case "CLOSED": return "text-slate-700 border-slate-200 bg-slate-50";
    default: return "text-slate-500 border-slate-200 bg-slate-50";
  }
}
