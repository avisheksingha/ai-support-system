import { Network, UserCheck, ShieldAlert, Cpu } from "lucide-react";
import type { RoutingModel } from "@/shared/types/workspace";
import { Badge } from "@/components/ui/badge";

interface RoutingPanelProps {
  routing: RoutingModel;
}

export function RoutingPanel({ routing }: RoutingPanelProps) {
  return (
    <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg overflow-hidden">
      <div className="bg-card border-b border-border p-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Network className="h-5 w-5 text-blue-400" />
          <h3 className="font-semibold text-foreground">Assignment</h3>
        </div>
      </div>

      <div className="p-4 space-y-5">
        <div>
           <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2 block">Assigned To</span>
           <div className="flex gap-2 items-center">
             <Badge variant="outline" className="text-foreground border-border bg-muted text-sm py-1">
               {routing.suggestedDepartment}
             </Badge>
             {routing.suggestedAgent && (
                <div className="flex items-center gap-1.5 text-sm text-foreground">
                  <UserCheck className="h-4 w-4 text-blue-400" />
                  @{routing.suggestedAgent}
                </div>
             )}
           </div>
        </div>

        <div>
          <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2 block">Routing Explanation</span>
          <div className="bg-background border border-border rounded-lg p-3 space-y-3">
            
            <div>
              <div className="flex items-center gap-1.5 mb-1">
                <ShieldAlert className="h-3.5 w-3.5 text-muted-foreground" />
                <span className="text-[11px] text-muted-foreground font-semibold uppercase">Reason</span>
              </div>
              <p className="text-xs text-foreground leading-relaxed pl-5">
                {routing.reasoning}
              </p>
            </div>

            <div className="border-t border-border pt-3">
              <div className="flex items-center gap-1.5 mb-1">
                <Cpu className="h-3.5 w-3.5 text-muted-foreground" />
                <span className="text-[11px] text-muted-foreground font-semibold uppercase">Rule Triggered</span>
              </div>
              <div className="pl-5 flex justify-between items-center">
                <code className="text-[10px] text-blue-400 bg-blue-400/10 px-1.5 py-0.5 rounded border border-blue-500/20">
                  {extractRuleName(routing.reasoning)}
                </code>
                <span className="text-[10px] font-mono text-blue-400 bg-blue-400/10 px-1.5 py-0.5 rounded">
                  {(routing.confidenceScore * 100).toFixed(0)}% Confidence
                </span>
              </div>
            </div>
            
          </div>
        </div>
      </div>
    </div>
  );
}

function extractRuleName(reasoning: string) {
  // A simple heuristic to extract a rule name or fallback
  if (reasoning.toLowerCase().includes("login")) return "LOGIN_RULE_01";
  if (reasoning.toLowerCase().includes("billing")) return "BILLING_RULE_02";
  if (reasoning.toLowerCase().includes("critical")) return "CRITICAL_SLA_RULE";
  return "GENERAL_ROUTING_RULE";
}
