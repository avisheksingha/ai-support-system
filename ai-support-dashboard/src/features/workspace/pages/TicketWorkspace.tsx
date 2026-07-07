import { useState } from "react";
import { TicketList } from "../components/TicketList";
import { TicketDetailView } from "../components/TicketDetailView";

export function TicketWorkspace() {
  const [selectedTicket, setSelectedTicket] = useState<string | null>(null);

  return (
    <div className="flex h-full w-full bg-zinc-950 overflow-hidden text-zinc-50">
      {/* Left Pane: Ticket List */}
      <div className="w-1/3 min-w-[320px] max-w-[400px] border-r border-zinc-800 bg-zinc-950 flex flex-col">
        <TicketList selectedTicket={selectedTicket} onSelectTicket={setSelectedTicket} />
      </div>

      {/* Right Pane: Ticket Details & AI Workspace */}
      <div className="flex-1 flex flex-col bg-zinc-950 overflow-y-auto">
        {selectedTicket ? (
          <TicketDetailView ticketNumber={selectedTicket} />
        ) : (
          <div className="flex-1 flex items-center justify-center text-zinc-500">
            Select a ticket from the list to view its workspace.
          </div>
        )}
      </div>
    </div>
  );
}
