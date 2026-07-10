import { useAuth } from "@/features/auth/hooks/useAuth";
import { useTicketList } from "@/features/workspace/hooks/useWorkspace";
import { useNavigate } from "react-router-dom";
import { 
  Activity, CheckCircle2, Ticket, BrainCircuit, ShieldAlert,
  Inbox, Users, AlertTriangle
} from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { formatTimeAgo } from "@/shared/utils/date";

export function AgentDashboardOverview() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { data: tickets, isLoading } = useTicketList();

  const myTickets = tickets?.filter(t => t.assignedTo === user?.email) || [];
  const openCount = myTickets.filter(t => ["NEW", "ANALYZING", "ANALYZED", "ASSIGNED", "IN_PROGRESS"].includes(t.status)).length || 0;
  const criticalCount = myTickets.filter(t => t.priority === "CRITICAL").length || 0;
  const highCount = myTickets.filter(t => t.priority === "HIGH").length || 0;
  const mediumCount = myTickets.filter(t => t.priority === "MEDIUM").length || 0;

  const recentAssigned = myTickets.slice(0, 3);

  return (
    <div className="h-full overflow-auto p-6 flex flex-col gap-6 bg-background">
      
      {/* Header Profile Info */}
      <div className="flex items-center gap-4 border-b border-border pb-4">
        <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 font-bold text-xl">
          {user?.fullName?.charAt(0) || user?.email?.charAt(0) || "A"}
        </div>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">{user?.fullName || user?.email?.split("@")[0]}</h1>
          <div className="flex items-center gap-2 mt-1">
            <span className="text-sm font-medium text-muted-foreground">Billing Team</span>
            <span className="h-1 w-1 rounded-full bg-border mx-1" />
            <span className="text-xs text-emerald-500 font-bold uppercase tracking-wider flex items-center gap-1.5">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" /> Available
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        
        {/* Main Column (Hero) */}
        <div className="lg:col-span-3 flex flex-col gap-6">
          
          {/* Top KPI Row */}
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
            {/* My Queue Hero */}
            <div className="sm:col-span-2 bg-gradient-to-br from-blue-600 to-blue-800 rounded-xl p-5 shadow-sm text-white flex flex-col justify-between">
              <div>
                <p className="text-blue-100 font-medium text-xs uppercase tracking-wider mb-1">My Queue</p>
                <div className="text-4xl font-bold mb-4">{isLoading ? "-" : openCount} Tickets</div>
              </div>
              <div className="flex gap-4 text-sm font-medium bg-black/20 rounded-lg p-3">
                <div className="flex flex-col"><span className="text-red-300 font-bold">{criticalCount}</span> <span className="text-[10px] font-medium uppercase tracking-wider text-blue-200">Critical</span></div>
                <div className="flex flex-col"><span className="text-orange-300 font-bold">{highCount}</span> <span className="text-[10px] font-medium uppercase tracking-wider text-blue-200">High</span></div>
                <div className="flex flex-col"><span className="text-yellow-300 font-bold">{mediumCount}</span> <span className="text-[10px] font-medium uppercase tracking-wider text-blue-200">Medium</span></div>
              </div>
            </div>

            {/* SLA Widget */}
            <div className="bg-card border border-border rounded-xl p-5 shadow-sm flex flex-col gap-2 relative overflow-hidden">
              <div className="absolute top-0 left-0 w-full h-1 bg-orange-500" />
              <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider flex items-center gap-2">
                <AlertTriangle className="h-3.5 w-3.5 text-orange-500" /> SLA At Risk
              </p>
              <div className="text-3xl font-bold text-foreground">3</div>
              <p className="text-xs text-orange-500 font-medium mt-auto bg-orange-50 px-2 py-1 rounded inline-block w-fit">Next breach in 42m</p>
            </div>

            {/* Performance */}
            <div className="bg-card border border-border rounded-xl p-5 shadow-sm flex flex-col gap-2 relative overflow-hidden">
               <div className="absolute top-0 left-0 w-full h-1 bg-emerald-500" />
              <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider flex items-center gap-2">
                <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500" /> Resolved Today
              </p>
              <div className="text-3xl font-bold text-foreground">8</div>
              <p className="text-xs text-emerald-600 font-medium mt-auto bg-emerald-50 px-2 py-1 rounded inline-block w-fit">Avg Response 19m</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* AI Copilot View */}
            <div className="bg-indigo-50/50 border border-indigo-100 rounded-xl p-5 shadow-sm flex flex-col">
              <div className="flex items-center gap-2 mb-4">
                <BrainCircuit className="h-4 w-4 text-indigo-600" />
                <h2 className="text-sm font-bold text-indigo-900 tracking-tight">AI Copilot</h2>
                <span className="ml-auto bg-indigo-100 text-indigo-700 text-[10px] uppercase font-bold px-2 py-0.5 rounded-full">3 Suggestions</span>
              </div>
              <div className="space-y-3 flex-1">
                <AiActionCard title="Refund ticket" desc="Suggested reply ready" type="reply" />
                <AiActionCard title="Duplicate detected" desc="Merge recommended" type="merge" />
                <AiActionCard title="Escalation required" desc="Manager approval needed" type="escalate" />
              </div>
            </div>

            {/* Today's Workload & Recent */}
            <div className="flex flex-col gap-6">
              {/* Workload */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
                <h2 className="text-sm font-semibold text-foreground mb-3">Today's Workload</h2>
                <div className="flex items-center gap-1 mb-2">
                  <div className="h-2 rounded-full bg-blue-500" style={{ width: '40%' }}></div>
                  <div className="h-2 rounded-full bg-emerald-500" style={{ width: '20%' }}></div>
                  <div className="h-2 rounded-full bg-muted flex-1"></div>
                </div>
                <div className="flex justify-between text-[11px] font-bold uppercase tracking-wider mt-3">
                  <span className="text-blue-600">11 Assigned</span>
                  <span className="text-emerald-600">3 Completed</span>
                  <span className="text-muted-foreground">8 Rem</span>
                </div>
              </div>

              {/* Recently Assigned */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm flex-1">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-sm font-semibold text-foreground">Recently Assigned</h2>
                </div>
                <div className="space-y-0">
                  {isLoading ? (
                    <Skeleton className="h-12 w-full" />
                  ) : recentAssigned.length > 0 ? (
                    recentAssigned.map((ticket, i) => (
                      <div key={ticket.id}>
                        <div onClick={() => navigate(`/tickets`)} className="py-2.5 flex items-center justify-between cursor-pointer group">
                          <p className="text-[13px] font-medium text-foreground group-hover:text-blue-600 transition-colors truncate pr-4">{ticket.subject}</p>
                          <span className="text-[11px] font-medium text-muted-foreground shrink-0">{formatTimeAgo(ticket.createdAt)}</span>
                        </div>
                        {i < recentAssigned.length - 1 && <div className="border-b border-border" />}
                      </div>
                    ))
                  ) : (
                    <p className="text-xs text-muted-foreground">No recent assignments</p>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Sidebar */}
        <div className="lg:col-span-1 space-y-6">
          
          {/* Quick Actions */}
          <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
            <h2 className="text-sm font-semibold text-foreground mb-3">Quick Actions</h2>
            <div className="space-y-1">
              <SidebarAction icon={Inbox} label="Open Queue" onClick={() => navigate("/tickets")} />
              <SidebarAction icon={Users} label="Unassigned Tickets" onClick={() => navigate("/tickets")} />
              <SidebarAction icon={ShieldAlert} label="Escalations" onClick={() => {}} />
            </div>
          </div>

          {/* Department Info */}
          <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
            <h2 className="text-sm font-semibold text-foreground mb-4">Billing Team</h2>
            <div className="flex flex-col gap-3">
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Active Tickets</span>
                <span className="font-bold text-foreground">42</span>
              </div>
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Agents Online</span>
                <span className="font-bold text-foreground">3</span>
              </div>
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Avg SLA</span>
                <span className="font-bold text-emerald-600">2h 15m</span>
              </div>
            </div>
          </div>

          {/* Recent Activity */}
          <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
            <h2 className="text-sm font-semibold text-foreground mb-4">My Activity</h2>
            <div className="space-y-4">
              <ActivityItem label="You resolved ticket" time="15m ago" icon={CheckCircle2} color="text-emerald-500" />
              <ActivityItem label="Customer replied" time="1h ago" icon={Activity} color="text-blue-500" />
              <ActivityItem label="You assigned ticket" time="2h ago" icon={Ticket} color="text-purple-500" />
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}

function SidebarAction({ icon: Icon, label, onClick }: { icon: any, label: string, onClick: () => void }) {
  return (
    <button onClick={onClick} className="w-full flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-muted transition-colors text-left group">
      <Icon className="h-4 w-4 text-muted-foreground group-hover:text-foreground transition-colors" />
      <span className="text-[13px] font-medium text-foreground">{label}</span>
    </button>
  );
}

function AiActionCard({ title, desc, type }: { title: string, desc: string, type: 'reply' | 'merge' | 'escalate' }) {
  let style = "";
  switch(type) {
    case 'reply': style = "bg-blue-50 border-blue-100 text-blue-700"; break;
    case 'merge': style = "bg-amber-50 border-amber-100 text-amber-700"; break;
    case 'escalate': style = "bg-red-50 border-red-100 text-red-700"; break;
  }
  return (
    <div className={`flex flex-col p-3 rounded-xl border border-indigo-100 cursor-pointer hover:shadow-md transition-shadow bg-white`}>
      <div className="flex items-center justify-between mb-1">
        <span className="text-[13px] font-bold text-indigo-950">{title}</span>
        <span className={`text-[9px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full ${style}`}>{type}</span>
      </div>
      <span className="text-[11px] font-medium text-slate-500">{desc}</span>
    </div>
  );
}

function ActivityItem({ label, time, icon: Icon, color }: { label: string, time: string, icon: any, color: string }) {
  return (
    <div className="flex items-start gap-3">
      <div className={`mt-0.5 h-6 w-6 rounded-full bg-muted flex items-center justify-center shrink-0`}>
        <Icon className={`h-3 w-3 ${color}`} />
      </div>
      <div className="flex flex-col">
        <span className="text-[13px] font-medium text-foreground leading-tight">{label}</span>
        <span className="text-[11px] font-medium text-muted-foreground mt-0.5">{time}</span>
      </div>
    </div>
  );
}
