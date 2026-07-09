import { useState } from "react";
import { TicketList } from "../components/TicketList";
import { TicketDetailView } from "../components/TicketDetailView";

export function TicketWorkspace() {
  const [selectedTicket, setSelectedTicket] = useState<string | null>(null);

  return (
    <div className="flex h-full w-full bg-background overflow-hidden text-foreground">
      {/* Left Pane: Ticket List */}
      <div className="w-[320px] shrink-0 border-r border-border bg-card flex flex-col z-10">
        <TicketList selectedTicket={selectedTicket} onSelectTicket={setSelectedTicket} />
      </div>

      {/* Right Pane: Ticket Details & AI Workspace */}
      <div className="flex-1 flex flex-col bg-background overflow-y-auto">
        {selectedTicket ? (
          <TicketDetailView ticketNumber={selectedTicket} />
        ) : (
          <div className="flex-1 flex items-center justify-center text-muted-foreground">
            Select a ticket from the list to view its workspace.
          </div>
        )}
      </div>
    </div>
  );
}
