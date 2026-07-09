import { useState } from "react";
import { useCustomerTickets } from "../hooks/useCustomerTickets";
import { CustomerTicketCard } from "../components/CustomerTicketCard";
import { CreateTicketDialog } from "../components/CreateTicketDialog";
import { Loader2, Inbox } from "lucide-react";
import { useNavigate } from "react-router-dom";

type FilterStatus = "ALL" | "OPEN" | "RESOLVED" | "CLOSED";

export function MyTicketsPage() {
  const { data: tickets, isLoading } = useCustomerTickets();
  const [filter, setFilter] = useState<FilterStatus>("ALL");
  const navigate = useNavigate();

  const filteredTickets = tickets?.filter(ticket => {
    if (filter === "ALL") return true;
    if (filter === "OPEN") {
      return ["NEW", "ANALYZING", "ANALYZED", "ASSIGNED", "IN_PROGRESS"].includes(ticket.status);
    }
    return ticket.status === filter;
  }) || [];

  const handleTicketClick = (ticketNumber: string) => {
    navigate(`/my-tickets/${ticketNumber}`);
  };

  return (
    <div className="flex flex-col h-full bg-zinc-950 text-zinc-50 overflow-hidden">
      <div className="border-b border-zinc-800/60 bg-zinc-950/50 backdrop-blur supports-[backdrop-filter]:bg-zinc-950/20 p-6 flex-shrink-0">
        <div className="max-w-4xl mx-auto w-full flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-zinc-100">My Tickets</h1>
            <p className="text-sm text-zinc-400 mt-1">Manage your support requests and track their progress.</p>
          </div>
          <CreateTicketDialog />
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-6">
        <div className="max-w-4xl mx-auto w-full space-y-6">
          <div className="flex items-center gap-2">
            <FilterChip label="All" count={tickets?.length} active={filter === "ALL"} onClick={() => setFilter("ALL")} />
            <FilterChip 
              label="Open" 
              count={tickets?.filter(t => ["NEW", "ANALYZING", "ANALYZED", "ASSIGNED", "IN_PROGRESS"].includes(t.status)).length} 
              active={filter === "OPEN"} 
              onClick={() => setFilter("OPEN")} 
            />
            <FilterChip 
              label="Resolved" 
              count={tickets?.filter(t => t.status === "RESOLVED").length} 
              active={filter === "RESOLVED"} 
              onClick={() => setFilter("RESOLVED")} 
            />
            <FilterChip 
              label="Closed" 
              count={tickets?.filter(t => t.status === "CLOSED").length} 
              active={filter === "CLOSED"} 
              onClick={() => setFilter("CLOSED")} 
            />
          </div>

          {isLoading ? (
            <div className="flex justify-center p-12">
              <Loader2 className="h-8 w-8 animate-spin text-indigo-500" />
            </div>
          ) : filteredTickets.length === 0 ? (
            <div className="flex flex-col items-center justify-center p-16 text-center border border-dashed border-zinc-800 rounded-xl bg-zinc-900/20">
              <div className="h-12 w-12 rounded-full bg-zinc-900 flex items-center justify-center mb-4">
                <Inbox className="h-6 w-6 text-zinc-500" />
              </div>
              <h3 className="text-lg font-medium text-zinc-300 mb-1">No tickets found</h3>
              <p className="text-sm text-zinc-500 max-w-[250px]">
                {filter === "ALL" 
                  ? "You haven't submitted any support requests yet." 
                  : `You don't have any ${filter.toLowerCase()} tickets at the moment.`}
              </p>
            </div>
          ) : (
            <div className="grid gap-3">
              {filteredTickets.map(ticket => (
                <CustomerTicketCard 
                  key={ticket.id} 
                  ticket={ticket} 
                  onClick={() => handleTicketClick(ticket.ticketNumber)} 
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function FilterChip({ label, count, active, onClick }: { label: string, count?: number | undefined, active: boolean, onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all ${
        active 
          ? "bg-zinc-800 text-zinc-100 shadow-sm border border-zinc-700" 
          : "bg-transparent text-zinc-400 hover:text-zinc-200 hover:bg-zinc-900 border border-transparent"
      }`}
    >
      {label} {count !== undefined && <span className="ml-1.5 opacity-60">({count})</span>}
    </button>
  );
}
