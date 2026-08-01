import { useState } from "react";
import { useApprovalQueue } from "../hooks/useGovernance";
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
import { Button } from "@/components/ui/button";
import { 
  Dialog, 
  DialogContent, 
  DialogTitle,
  DialogFooter
} from "@/components/ui/dialog";
import { Loader2, ShieldAlert, Activity, AlertCircle, Fingerprint, ExternalLink, ShieldCheck } from "lucide-react";
import type { ApprovalRequest } from "@/shared/types/workspace";

export function ApprovalQueue() {
  const { data: requests, isLoading } = useApprovalQueue();
  const [selectedRequest, setSelectedRequest] = useState<ApprovalRequest | null>(null);

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
        <h3 className="text-lg font-medium text-slate-900">All clear</h3>
        <p className="text-sm text-slate-500">No pending approval requests.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
      <Table>
        <TableHeader className="bg-slate-50">
          <TableRow>
            <TableHead className="w-[120px]">Request ID</TableHead>
            <TableHead>Intent</TableHead>
            <TableHead>Triggered Policy</TableHead>
            <TableHead>Ticket</TableHead>
            <TableHead>Requested</TableHead>
            <TableHead className="text-right">Action</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {requests.map((req) => (
            <TableRow key={req.id}>
              <TableCell className="font-mono text-xs text-slate-600">{req.id}</TableCell>
              <TableCell className="font-medium text-slate-900">
                <div className="flex items-center gap-2">
                  <Activity className="w-4 h-4 text-blue-500" />
                  {req.intent}
                </div>
              </TableCell>
              <TableCell>
                <Badge variant="outline" className="bg-amber-50 text-amber-700 border-amber-200 flex items-center gap-1 w-fit">
                  <ShieldAlert className="w-3 h-3" />
                  {req.triggeredPolicy}
                </Badge>
              </TableCell>
              <TableCell className="text-sm text-slate-600">TKT-{req.ticketId}</TableCell>
              <TableCell className="text-sm text-slate-600">{formatTimeAgo(req.createdAt)}</TableCell>
              <TableCell className="text-right">
                <Button size="sm" onClick={() => setSelectedRequest(req)}>
                  Review
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <Dialog open={!!selectedRequest} onOpenChange={(open) => !open && setSelectedRequest(null)}>
        <DialogContent className="sm:max-w-[600px] p-0 overflow-hidden">
          {/* Jira-style rich dialog banner */}
          <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-6 text-white relative">
            <div className="absolute top-0 right-0 p-4 opacity-20">
              <ShieldAlert className="w-24 h-24" />
            </div>
            <Badge className="bg-white/20 hover:bg-white/30 text-white border-none mb-3">
              {selectedRequest?.id}
            </Badge>
            <DialogTitle className="text-2xl font-bold text-white mb-1">
              Approval Required
            </DialogTitle>
            <p className="text-blue-100 text-sm">
              An automated action was halted by the Governance Engine and requires human authorization.
            </p>
          </div>
          
          {selectedRequest && (
            <div className="p-6 space-y-6">
              
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">AI Intent</span>
                  <div className="flex items-center gap-2 text-slate-900 font-medium">
                    <Activity className="w-4 h-4 text-blue-500" />
                    {selectedRequest.intent}
                  </div>
                </div>
                <div className="space-y-1">
                  <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">Confidence Score</span>
                  <div className="flex items-center gap-2">
                    <div className="w-full bg-slate-100 rounded-full h-2">
                      <div className="bg-emerald-500 h-2 rounded-full" style={{ width: `${selectedRequest.confidence * 100}%` }}></div>
                    </div>
                    <span className="text-sm font-medium text-slate-700">{(selectedRequest.confidence * 100).toFixed(0)}%</span>
                  </div>
                </div>
              </div>

              <div className="bg-amber-50 border border-amber-100 rounded-lg p-4">
                <div className="flex items-start gap-3">
                  <ShieldAlert className="w-5 h-5 text-amber-600 mt-0.5" />
                  <div>
                    <h4 className="text-sm font-semibold text-amber-900">{selectedRequest.triggeredPolicy}</h4>
                    <p className="text-sm text-amber-800 mt-1">{selectedRequest.reason}</p>
                  </div>
                </div>
              </div>

              <div className="space-y-3">
                <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">Recommended Action</span>
                <div className="bg-slate-50 border border-slate-200 rounded-lg p-3 text-sm text-slate-700 flex items-start gap-3">
                  <AlertCircle className="w-5 h-5 text-blue-500 shrink-0" />
                  <p>{selectedRequest.recommendedAction}</p>
                </div>
              </div>

              <div className="pt-4 border-t border-slate-100">
                <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider block mb-3">Execution Trace</span>
                <div className="grid grid-cols-3 gap-4 text-xs font-mono text-slate-600">
                  <div>
                    <span className="block text-[9px] font-sans text-slate-400 mb-1 uppercase tracking-wider">Workflow ID</span>
                    <div className="flex items-center gap-1"><Fingerprint className="w-3 h-3" />{selectedRequest.workflowId}</div>
                  </div>
                  <div>
                    <span className="block text-[9px] font-sans text-slate-400 mb-1 uppercase tracking-wider">Correlation ID</span>
                    {selectedRequest.correlationId}
                  </div>
                  <div>
                    <span className="block text-[9px] font-sans text-slate-400 mb-1 uppercase tracking-wider">Ticket</span>
                    <a href={`/tickets/TKT-${selectedRequest.ticketId}`} className="text-blue-600 hover:underline flex items-center gap-1">
                      TKT-{selectedRequest.ticketId} <ExternalLink className="w-3 h-3" />
                    </a>
                  </div>
                </div>
              </div>
            </div>
          )}

          <DialogFooter className="px-6 py-4 border-t border-slate-100 bg-slate-50">
            <Button variant="outline" onClick={() => setSelectedRequest(null)}>Cancel</Button>
            <Button variant="destructive" onClick={() => setSelectedRequest(null)}>Reject Action</Button>
            <Button onClick={() => setSelectedRequest(null)}>Approve Action</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
