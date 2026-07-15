import { useState } from "react";
import { TicketList } from "../components/TicketList";
import { TicketDetailView } from "../components/TicketDetailView";

export function TicketWorkspace() {
  const [selectedTicket, setSelectedTicket] = useState<string | null>(null);

  return (
    <div className="flex h-full w-full bg-[#F8FAFC] overflow-hidden text-slate-800">
      {/* Left Pane: Ticket List */}
      <div className="w-[320px] lg:w-[380px] shrink-0 border-r border-slate-200 bg-white flex flex-col z-10 shadow-[4px_0_24px_rgba(0,0,0,0.02)]">
        <TicketList selectedTicket={selectedTicket} onSelectTicket={setSelectedTicket} />
      </div>

      {/* Right Pane: Ticket Details & AI Workspace */}
      <div className="flex-1 flex flex-col overflow-hidden relative">
        {selectedTicket ? (
          <TicketDetailView ticketNumber={selectedTicket} />
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-slate-400 p-8 text-center bg-gradient-to-br from-[#F8FAFC] to-white">
            <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mb-4 border border-slate-200 shadow-sm">
              <span className="text-2xl">📥</span>
            </div>
            <h2 className="text-lg font-bold text-slate-700 mb-2">No Ticket Selected</h2>
            <p className="text-sm max-w-[250px] leading-relaxed">Select a ticket from the queue on the left to view details, AI insights, and respond to the customer.</p>
          </div>
        )}
      </div>
    </div>
  );
}
