import { useBlockedRequests } from "../hooks/useGovernance";
import { formatTimeAgo } from "@/shared/utils/date";
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Loader2, ShieldCheck, Ban, ShieldAlert, Cpu, User } from "lucide-react";

export function BlockedRequests() {
  const { data: requests, isLoading } = useBlockedRequests();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64 bg-white rounded-xl shadow-sm border border-slate-200">
        <Loader2 className="w-8 h-8 animate-spin text-slate-300" />
      </div>
    );
  }

  if (!requests || requests.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 bg-white rounded-xl shadow-sm border border-slate-200">
        <ShieldCheck className="w-12 h-12 text-emerald-400 mb-3" />
        <h3 className="text-lg font-medium text-slate-900">No Blocked Requests</h3>
        <p className="text-sm text-slate-500">No operations have been blocked by guardrails recently.</p>
      </div>
    );
  }

  const getGuardrailColor = (guardrail: string) => {
    switch (guardrail) {
      case "PII Redaction":
      case "Sensitive Data":
      case "Sensitive Information Protection":
        return "bg-rose-50 text-rose-700 border-rose-200";
      case "Prompt Injection Detection":
      case "Unsafe Tool Invocation":
        return "bg-red-50 text-red-700 border-red-200 font-bold";
      case "Policy Restriction":
      default:
        return "bg-slate-100 text-slate-700 border-slate-300";
    }
  };

  const getActorIcon = (actor: string) => {
    if (actor.toLowerCase().includes("ai") || actor.toLowerCase().includes("node") || actor.toLowerCase().includes("system")) {
      return <Cpu className="w-3 h-3 text-slate-400" />;
    }
    return <User className="w-3 h-3 text-slate-400" />;
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
      <div className="p-4 border-b border-slate-200 bg-slate-50 flex items-center justify-between">
        <div>
          <h3 className="text-base font-semibold text-slate-900 flex items-center gap-2">
            <Ban className="w-4 h-4 text-rose-500" />
            Hard-Blocked Requests
          </h3>
          <p className="text-sm text-slate-500 mt-1">
            Requests that failed mandatory safety and security guardrails. No human override is possible.
          </p>
        </div>
      </div>
      
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-[120px]">Block ID</TableHead>
            <TableHead>Triggered Guardrail</TableHead>
            <TableHead>Reason</TableHead>
            <TableHead>Actor</TableHead>
            <TableHead>Ticket</TableHead>
            <TableHead className="text-right">Time</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {requests.map((req) => (
            <TableRow key={req.id}>
              <TableCell className="font-mono text-xs text-slate-600">{req.id}</TableCell>
              <TableCell>
                <Badge variant="outline" className={`flex items-center gap-1.5 w-fit ${getGuardrailColor(req.guardrail)}`}>
                  <ShieldAlert className="w-3 h-3" />
                  {req.guardrail}
                </Badge>
              </TableCell>
              <TableCell className="text-sm text-slate-700 max-w-xs truncate" title={req.reason}>
                {req.reason}
              </TableCell>
              <TableCell>
                <div className="flex items-center gap-1.5 text-sm font-medium text-slate-600">
                  {getActorIcon(req.actor)}
                  {req.actor}
                </div>
              </TableCell>
              <TableCell className="text-sm text-blue-600 hover:underline cursor-pointer">
                TKT-{req.ticketId}
              </TableCell>
              <TableCell className="text-right text-sm text-slate-500">
                {formatTimeAgo(req.blockedAt)}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
