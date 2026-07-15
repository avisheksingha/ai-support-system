import { useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Activity, AlertOctagon, CheckSquare, List } from "lucide-react";
import { GovernanceOverview } from "../components/GovernanceOverview";
import { ApprovalQueue } from "../components/ApprovalQueue";
import { BlockedRequests } from "../components/BlockedRequests";
import { AuditLogs } from "../components/AuditLogs";

export function GovernanceDashboard() {
  const [activeTab, setActiveTab] = useState("overview");

  return (
    <div className="flex-1 space-y-4 p-4 md:p-8 pt-6 overflow-y-auto bg-slate-50 dark:bg-slate-900">
      <div className="flex items-center justify-between space-y-2 mb-6">
        <div>
          <h2 className="text-3xl font-bold tracking-tight text-slate-900">AI Governance & Trust Center</h2>
          <p className="text-slate-500 mt-1">Operational guardrails, policy enforcement, and audit logs.</p>
        </div>
        <div className="flex items-center space-x-2">
          <div className="text-sm text-slate-500 bg-white px-3 py-1.5 rounded-full border border-slate-200 shadow-sm flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            Policies Active
          </div>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList className="bg-slate-200/50 p-1 rounded-lg">
          <TabsTrigger value="overview" className="rounded-md data-[state=active]:bg-white data-[state=active]:shadow-sm"><Activity className="w-4 h-4 mr-2" />Overview</TabsTrigger>
          <TabsTrigger value="approval_queue" className="rounded-md data-[state=active]:bg-white data-[state=active]:shadow-sm"><CheckSquare className="w-4 h-4 mr-2" />Approval Queue</TabsTrigger>
          <TabsTrigger value="blocked_requests" className="rounded-md data-[state=active]:bg-white data-[state=active]:shadow-sm"><AlertOctagon className="w-4 h-4 mr-2" />Blocked Requests</TabsTrigger>
          <TabsTrigger value="audit_logs" className="rounded-md data-[state=active]:bg-white data-[state=active]:shadow-sm"><List className="w-4 h-4 mr-2" />Audit Logs</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <GovernanceOverview />
        </TabsContent>

        <TabsContent value="approval_queue" className="space-y-4">
          <ApprovalQueue />
        </TabsContent>

        <TabsContent value="blocked_requests" className="space-y-4">
          <BlockedRequests />
        </TabsContent>

        <TabsContent value="audit_logs" className="space-y-4">
          <AuditLogs />
        </TabsContent>

      </Tabs>
    </div>
  );
}
