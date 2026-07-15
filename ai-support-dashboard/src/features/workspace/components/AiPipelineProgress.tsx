import type { TicketModel } from "@/shared/types/ticket";
import { Check, Loader2, Circle, ArrowRight } from "lucide-react";

interface AiPipelineProgressProps {
  ticket: TicketModel;
}

export function AiPipelineProgress({ ticket }: AiPipelineProgressProps) {
  // Determine state of pipeline based on ticket status
  // 1. Customer: always ✓ if ticket exists
  // 2. Analysis: ✓ if > ANALYZING
  // 3. Knowledge: ✓ if > ANALYZING (mocked for now, assuming RAG runs after analysis)
  // 4. Routing: ✓ if >= ASSIGNED
  // 5. Assignment: ✓ if >= ASSIGNED

  const getStatusLevel = () => {
    switch(ticket.status) {
      case "NEW": return 0;
      case "ANALYZING": return 1;
      case "ANALYZED": return 3; // Finished Analysis and Knowledge
      case "ASSIGNED": return 5;
      case "IN_PROGRESS": return 5;
      case "RESOLVED": 
      case "CLOSED": return 6;
      default: return 0;
    }
  };

  const level = getStatusLevel();

  const steps = [
    { label: "Customer", idx: 1 },
    { label: "AI Analysis", idx: 2 },
    { label: "Knowledge", idx: 3 },
    { label: "Routing", idx: 4 },
    { label: "Assigned", idx: 5 },
    { label: "Resolution", idx: 6 },
  ];

  return (
    <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg p-5 mb-2">
      <h2 className="text-[11px] font-semibold text-muted-foreground mb-4 uppercase tracking-wider">AI Pipeline Status</h2>
      <div className="flex flex-wrap items-center justify-between md:justify-start md:gap-4 overflow-x-auto pb-2">
        {steps.map((step, i) => {
          const isComplete = level >= step.idx;
          const isCurrent = level === step.idx - 1;
          
          return (
            <div key={step.label} className="flex items-center gap-2 md:gap-4">
              <div className="flex items-center gap-2">
                <span className={`text-[13px] font-medium ${isComplete || isCurrent ? 'text-foreground' : 'text-muted-foreground/60'}`}>
                  {step.label}
                </span>
                
                {isComplete ? (
                  <Check className="h-4 w-4 text-emerald-500 shrink-0" />
                ) : isCurrent ? (
                  <Loader2 className="h-4 w-4 text-[#0C66E4] animate-spin shrink-0" />
                ) : (
                  <Circle className="h-4 w-4 text-muted-foreground/30 shrink-0" />
                )}
              </div>
              
              {i < steps.length - 1 && (
                <ArrowRight className="h-3 w-3 text-muted-foreground/30 shrink-0 hidden md:block" />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
