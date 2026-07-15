import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ShieldAlert, AlertOctagon, CheckSquare, ShieldCheck, Activity } from "lucide-react";

export function GovernanceOverview() {
  return (
    <div className="space-y-4">
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card className="border-slate-200 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-slate-600">Policy Evaluations</CardTitle>
            <ShieldCheck className="h-4 w-4 text-emerald-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">14,231</div>
            <p className="text-xs text-slate-500 mt-1">
              <span className="text-emerald-500 font-medium">↑ 12%</span> from last week
            </p>
          </CardContent>
        </Card>
        <Card className="border-slate-200 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-slate-600">Guardrail Blocks</CardTitle>
            <AlertOctagon className="h-4 w-4 text-rose-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">23</div>
            <p className="text-xs text-slate-500 mt-1">
              <span className="text-rose-500 font-medium">↑ 3</span> today
            </p>
          </CardContent>
        </Card>
        <Card className="border-slate-200 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-slate-600">Human Approvals</CardTitle>
            <CheckSquare className="h-4 w-4 text-amber-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">14</div>
            <p className="text-xs text-slate-500 mt-1">
              <span className="text-amber-500 font-medium">2 pending</span> review
            </p>
          </CardContent>
        </Card>
        <Card className="border-slate-200 shadow-sm">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-slate-600">Avg Evaluation Time</CardTitle>
            <Activity className="h-4 w-4 text-blue-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-slate-900">18ms</div>
            <p className="text-xs text-slate-500 mt-1">
              <span className="text-emerald-500 font-medium">↓ 2ms</span> from last week
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
        <Card className="col-span-4 border-slate-200 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg font-semibold text-slate-900">Governance Violations Trend</CardTitle>
            <CardDescription>Number of policy blocks and flagged requests over time</CardDescription>
          </CardHeader>
          <CardContent className="pl-2 h-[300px] flex items-end justify-between px-4 pb-4">
            {/* Mock Chart Area - using simple flex bars for v1 */}
            <div className="w-full flex items-end justify-between gap-2 h-full pt-4 border-b border-l border-slate-200 relative">
               <div className="absolute top-0 left-0 text-[10px] text-slate-400 -translate-x-full pr-2">30</div>
               <div className="absolute top-1/2 left-0 text-[10px] text-slate-400 -translate-x-full pr-2 -translate-y-1/2">15</div>
               <div className="absolute bottom-0 left-0 text-[10px] text-slate-400 -translate-x-full pr-2">0</div>
               
               {[12, 19, 15, 8, 22, 14, 25, 18, 10, 14, 12, 28, 16, 20].map((val, i) => (
                 <div key={i} className="flex-1 flex flex-col justify-end gap-1 group">
                    <div className="w-full bg-indigo-500/80 rounded-t-sm hover:bg-indigo-600 transition-colors" style={{ height: `${(val/30)*100}%` }}></div>
                    <div className="text-[9px] text-slate-400 text-center">{i + 8}:00</div>
                 </div>
               ))}
            </div>
          </CardContent>
        </Card>
        
        <Card className="col-span-3 border-slate-200 shadow-sm">
          <CardHeader>
            <CardTitle className="text-lg font-semibold text-slate-900">Active Guardrails</CardTitle>
            <CardDescription>Policies currently enforced in production</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-6">
              {[
                { name: "PII Redaction", type: "Security", status: "Enforcing", count: 12 },
                { name: "Prompt Injection Detection", type: "Security", status: "Enforcing", count: 5 },
                { name: "Hallucination Risk Review", type: "Quality", status: "Monitoring", count: 48 },
                { name: "Manual Approval Required", type: "Workflow", status: "Enforcing", count: 14 },
                { name: "Unsafe Tool Invocation", type: "Security", status: "Enforcing", count: 2 },
              ].map((policy, i) => (
                <div key={i} className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className={`p-2 rounded-lg ${policy.status === 'Enforcing' ? 'bg-emerald-100 text-emerald-600' : 'bg-blue-100 text-blue-600'}`}>
                      <ShieldAlert className="w-4 h-4" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-slate-900">{policy.name}</p>
                      <p className="text-xs text-slate-500">{policy.type} • {policy.status}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-bold text-slate-900">{policy.count}</p>
                    <p className="text-[10px] uppercase text-slate-400">Hits</p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
