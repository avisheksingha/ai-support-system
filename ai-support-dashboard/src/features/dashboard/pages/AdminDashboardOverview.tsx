import { useAuth } from "@/features/auth/hooks/useAuth";
import { useAdminDashboard, useOrchestrationHealth } from "../hooks/useAdminDashboard";
import { useNavigate } from "react-router-dom";
import { 
  Activity, Users, Settings, Server, BrainCircuit,
  Network, ShieldCheck, Bot, BookOpen, Zap
} from "lucide-react";

export function AdminDashboardOverview() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { data, isLoading } = useAdminDashboard();
  const { data: orchestrationHealth, isLoading: healthLoading } = useOrchestrationHealth();

  return (
    <div className="h-full overflow-auto p-6 flex flex-col gap-6 bg-background">
      
      {/* Header Profile Info */}
      <div className="flex items-center gap-4 border-b border-border pb-4">
        <div className="h-12 w-12 rounded-full bg-slate-800 flex items-center justify-center text-white font-bold text-xl">
          {user?.fullName?.charAt(0) || user?.email?.charAt(0) || "A"}
        </div>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">{user?.fullName || user?.email?.split("@")[0]}</h1>
          <div className="flex items-center gap-2 mt-1">
            <span className="text-sm font-medium text-muted-foreground">Platform Administrator</span>
            <span className="h-1 w-1 rounded-full bg-border mx-1" />
            <span className="text-xs text-emerald-500 font-bold uppercase tracking-wider flex items-center gap-1.5">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" /> Online
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        
        {/* Main Column (Hero) */}
        <div className="lg:col-span-3 flex flex-col gap-6">
          
          {/* Top KPI Row */}
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
            {/* Platform Overview Hero */}
            <div className="sm:col-span-2 bg-gradient-to-br from-slate-800 to-slate-950 rounded-xl p-5 shadow-sm text-white flex flex-col justify-between">
              <div>
                <p className="text-slate-400 font-medium text-xs uppercase tracking-wider mb-1 flex items-center gap-2">
                  <Server className="h-3.5 w-3.5" /> Platform Overview
                </p>
                <div className="text-4xl font-bold mb-4">{isLoading ? "-" : data?.platformOverview.ticketsToday || 0} <span className="text-lg text-slate-300 font-medium">Tickets Today</span></div>
              </div>
              <div className="flex gap-4 text-sm font-medium bg-white/10 rounded-lg p-3 overflow-x-auto">
                <div className="flex flex-col"><span className="text-white font-bold">{isLoading ? "-" : data?.platformOverview.activeTickets || 0}</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">Active</span></div>
                <div className="flex flex-col"><span className="text-white font-bold">{isLoading ? "-" : data?.platformOverview.resolvedToday || 0}</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">Resolved</span></div>
                <div className="flex flex-col"><span className="text-white font-bold">{isLoading ? "-" : data?.platformOverview.aiProcessedToday || 0}</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">AI Processed</span></div>
                <div className="flex flex-col"><span className="text-white font-bold">{isLoading ? "-" : data?.platformOverview.totalCustomers || 0}</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">Customers</span></div>
                <div className="flex flex-col"><span className="text-white font-bold">{isLoading ? "-" : data?.platformOverview.totalAgents || 0}</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">Agents</span></div>
                <div className="flex flex-col"><span className="text-white font-bold">{isLoading ? "-" : data?.platformOverview.totalAdmins || 0}</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">Admins</span></div>
              </div>
            </div>

            {/* AI Health Widget */}
            <div className="sm:col-span-2 bg-card border border-border rounded-xl p-5 shadow-sm flex flex-col justify-between relative overflow-hidden">
              <div className="absolute top-0 left-0 w-full h-1 bg-indigo-500" />
              <div className="flex justify-between items-start mb-2">
                <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider flex items-center gap-2">
                  <BrainCircuit className="h-3.5 w-3.5 text-indigo-500" /> AI Governance
                </p>
                <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-1 rounded">Optimal</span>
              </div>
              
              <div className="grid grid-cols-2 gap-4 mt-auto">
                <div className="flex flex-col">
                  <span className="text-2xl font-bold text-foreground">{isLoading ? "-" : data?.aiGovernance.highConfidenceRate || "N/A"}</span>
                  <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">High Confidence Rate</span>
                </div>
                <div className="flex flex-col">
                  <span className="text-2xl font-bold text-foreground">{isLoading ? "-" : data?.aiGovernance.assignmentRate || "N/A"}</span>
                  <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">Assignment Rate</span>
                </div>
                <div className="flex flex-col">
                  <span className="text-2xl font-bold text-foreground">{isLoading ? "-" : data?.aiGovernance.knowledgeCoverage || "N/A"}</span>
                  <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">Knowledge Coverage</span>
                </div>
                <div className="flex flex-col">
                  <span className="text-2xl font-bold text-foreground">{isLoading ? "-" : data?.aiGovernance.averageLatency || "N/A"}</span>
                  <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">Avg Latency</span>
                </div>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            <div className="flex flex-col gap-6">
              {/* Department Workload */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
                <h2 className="text-sm font-semibold text-foreground mb-4">Department Workload</h2>
                <div className="space-y-4">
                  {isLoading ? (
                    <div className="text-sm text-muted-foreground">Loading...</div>
                  ) : !data?.departmentWorkload || Object.keys(data.departmentWorkload).length === 0 ? (
                    <div className="text-sm text-muted-foreground italic">No current workload data.</div>
                  ) : (
                    Object.entries(data.departmentWorkload).map(([team, count], index) => {
                      const colors = ["bg-blue-500", "bg-emerald-500", "bg-orange-500", "bg-purple-500", "bg-indigo-500"];
                      const max = Math.max(...Object.values(data.departmentWorkload), 10);
                      return (
                        <WorkloadBar key={team} label={team} value={count} max={max} color={colors[index % colors.length] || "bg-blue-500"} />
                      );
                    })
                  )}
                </div>
              </div>

              {/* Routing Metrics */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm flex-1">
                <h2 className="text-sm font-semibold text-foreground mb-4">Routing Overview</h2>
                <div className="grid grid-cols-2 gap-4">
                  {isLoading ? (
                    <div className="text-sm text-muted-foreground col-span-2">Loading...</div>
                  ) : !data?.routingOverview || Object.keys(data.routingOverview).length === 0 ? (
                    <div className="text-sm text-muted-foreground col-span-2 italic">No routing data available.</div>
                  ) : (
                    Object.entries(data.routingOverview).map(([label, count]) => (
                      <RoutingStat key={label} label={label} value={count.toString()} alert={label.toLowerCase().includes("override") && count > 0} />
                    ))
                  )}
                </div>
              </div>
            </div>

            <div className="flex flex-col gap-6">
              {/* System Health */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
                <h2 className="text-sm font-semibold text-foreground mb-4">Platform Status</h2>
                <div className="grid grid-cols-2 gap-3">
                  {isLoading ? (
                    <div className="text-sm text-muted-foreground col-span-2">Checking systems...</div>
                  ) : (
                    data?.systemHealth?.map((sys: any) => {
                      let Icon = Server;
                      if (sys.serviceName.includes("Gateway") || sys.serviceName.includes("API-GATEWAY")) Icon = Network;
                      else if (sys.serviceName.includes("Ticket") || sys.serviceName.includes("Kafka") || sys.serviceName.includes("TICKET")) Icon = Activity;
                      else if (sys.serviceName.includes("Auth") || sys.serviceName.includes("AUTH")) Icon = ShieldCheck;
                      else if (sys.serviceName.includes("AI") || sys.serviceName.includes("RAG") || sys.serviceName.includes("ORCHESTRATION")) Icon = Bot;
                      
                      const label = sys.serviceName.replace("-SERVICE", "").replace("-", " ");
                      
                      return (
                        <SystemStatus key={sys.serviceName} label={label} status={sys.status} icon={Icon} sys={sys} />
                      );
                    })
                  )}
                </div>
              </div>

              {/* Recent Platform Events */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm flex-1">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-sm font-semibold text-foreground">Recent Platform Events</h2>
                </div>
                <div className="space-y-4">
                  {isLoading ? (
                    <div className="text-sm text-muted-foreground">Loading...</div>
                  ) : !data?.recentEvents || data.recentEvents.length === 0 ? (
                    <div className="text-sm text-muted-foreground italic">No platform events available.</div>
                  ) : (
                    data.recentEvents.map((evt, idx) => (
                      <EventItem key={idx} label={evt.label} sublabel={evt.sublabel} time={evt.time} />
                    ))
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
            <h2 className="text-sm font-semibold text-foreground mb-3">Administration</h2>
            <div className="space-y-1">
              <SidebarAction icon={Users} label="Manage Users & Roles" onClick={() => navigate("/users")} />
              <SidebarAction icon={Network} label="Routing Rules" onClick={() => {}} />
              <SidebarAction icon={BookOpen} label="Knowledge Base" onClick={() => {}} />
              <SidebarAction icon={Settings} label="AI Settings" onClick={() => navigate("/settings")} />
            </div>
          </div>

          {/* Knowledge Base Summary */}
          <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
            <h2 className="text-sm font-semibold text-foreground mb-4 flex items-center gap-2"><BookOpen className="h-4 w-4 text-blue-500" /> RAG Knowledge</h2>
            <div className="flex flex-col gap-3">
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Total Articles</span>
                <span className="font-bold text-foreground">{isLoading ? "-" : data?.ragKnowledge.totalArticles || 0}</span>
              </div>
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Embedded Articles</span>
                <span className="font-bold text-emerald-600">{isLoading ? "-" : data?.ragKnowledge.embeddedArticles || 0}</span>
              </div>
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Embedding Coverage</span>
                <span className="font-bold text-emerald-600">{isLoading ? "-" : data?.ragKnowledge.embeddingCoverage || "0%"}</span>
              </div>
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Knowledge Coverage</span>
                <span className="font-bold text-blue-600">{isLoading ? "-" : data?.ragKnowledge.knowledgeCoverage || "0%"}</span>
              </div>
              <div className="flex flex-col gap-1 pt-2 border-t border-border mt-1">
                <span className="text-muted-foreground font-medium text-[13px]">Most Used Article</span>
                <span className="font-bold text-foreground text-[13px] truncate" title={data?.ragKnowledge.mostUsedArticle || "N/A"}>{isLoading ? "-" : data?.ragKnowledge.mostUsedArticle || "N/A"}</span>
              </div>
            </div>
          </div>

          {/* Platform Information */}
          <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
            <h2 className="text-sm font-semibold text-foreground mb-4">Platform Information</h2>
            <div className="flex flex-col gap-3">
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Platform Name</span>
                <span className="font-bold text-foreground">{isLoading ? "-" : data?.platformInfo?.platformName || "N/A"}</span>
              </div>
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Version</span>
                <span className="font-bold text-foreground">{isLoading ? "-" : data?.platformInfo?.platformVersion || "N/A"}</span>
              </div>
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Environment</span>
                <span className="font-bold text-foreground">{isLoading ? "-" : data?.platformInfo?.environment || "N/A"}</span>
              </div>
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Build</span>
                <span className="font-bold text-foreground truncate max-w-[120px]">{isLoading ? "-" : data?.platformInfo?.buildVersion || "N/A"}</span>
              </div>
            </div>
          </div>

          {/* Admin Activity */}
          <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
            <h2 className="text-sm font-semibold text-foreground mb-4">My Admin Activity</h2>
            <div className="space-y-4">
              {isLoading ? (
                <div className="text-sm text-muted-foreground">Loading...</div>
              ) : !data?.myActivity || data.myActivity.length === 0 ? (
                <div className="text-sm text-muted-foreground italic">No recent activity.</div>
              ) : (
                data.myActivity.map((act, idx) => (
                  <ActivityItem key={idx} label={act.label} time={act.time} color={act.color} />
                ))
              )}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}

function WorkloadBar({ label, value, max, color }: { label: string, value: number, max: number, color: string }) {
  const percent = Math.min(100, Math.round((value / max) * 100));
  return (
    <div className="flex flex-col gap-1.5">
      <div className="flex justify-between items-center text-[13px]">
        <span className="font-medium text-foreground">{label}</span>
        <span className="font-bold text-muted-foreground">{value}</span>
      </div>
      <div className="h-2 w-full bg-muted rounded-full overflow-hidden">
        <div className={`h-full ${color} rounded-full`} style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
}

function RoutingStat({ label, value, alert }: { label: string, value: string, alert?: boolean }) {
  return (
    <div className={`p-3 rounded-lg border ${alert ? 'border-orange-200 bg-orange-50' : 'border-border bg-card'}`}>
      <p className={`text-[10px] font-bold uppercase tracking-wider mb-1 ${alert ? 'text-orange-600' : 'text-muted-foreground'}`}>{label}</p>
      <p className={`text-xl font-bold ${alert ? 'text-orange-700' : 'text-foreground'}`}>{value}</p>
    </div>
  );
}

function SystemStatus({ label, status, icon: Icon, sys }: { label: string, status: string, icon: any, sys?: any }) {
  const isHealthy = status === "HEALTHY" || status === "UP";
  const isDown = status === "DOWN";
  const isChecking = status === "CHECKING";
  
  const bgClass = isHealthy ? "bg-emerald-50 border-emerald-100" : isDown ? "bg-red-50 border-red-100" : isChecking ? "bg-slate-50 border-slate-100" : "bg-orange-50 border-orange-100";
  const textClass = isHealthy ? "text-emerald-600" : isDown ? "text-red-600" : isChecking ? "text-slate-400" : "text-orange-600";

  return (
    <div className="flex flex-col gap-1 p-2.5 rounded-lg border border-border bg-card">
      <div className="flex items-center gap-2">
        <div className={`h-6 w-6 rounded flex items-center justify-center shrink-0 border ${bgClass}`}>
          <Icon className={`h-3.5 w-3.5 ${textClass}`} />
        </div>
        <div className="flex flex-col min-w-0 flex-1">
          <span className="text-[11px] font-bold text-foreground truncate">{label}</span>
          <span className={`text-[9px] uppercase tracking-wider font-bold ${textClass}`}>{status}</span>
        </div>
      </div>
      {sys && sys.version && sys.version !== "N/A" && (
        <div className="mt-1 pl-8 flex flex-col gap-0.5 text-[9px] text-muted-foreground">
          <div>Ver: {sys.version}</div>
        </div>
      )}
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

function EventItem({ label, sublabel, time }: { label: string, sublabel: string, time: string }) {
  return (
    <div className="flex items-start gap-3">
      <div className="mt-1 h-1.5 w-1.5 rounded-full bg-blue-500 shrink-0" />
      <div className="flex flex-col">
        <span className="text-[13px] font-medium text-foreground leading-tight">{label}</span>
        <div className="flex items-center gap-2 mt-0.5">
          <span className="text-[11px] text-muted-foreground">{sublabel}</span>
          <span className="text-[11px] text-muted-foreground">&bull;</span>
          <span className="text-[11px] text-muted-foreground">{time}</span>
        </div>
      </div>
    </div>
  );
}

function ActivityItem({ label, time, color }: { label: string, time: string, color: string }) {
  return (
    <div className="flex items-start gap-3">
      <div className={`mt-0.5 h-6 w-6 rounded-full bg-muted flex items-center justify-center shrink-0`}>
        <Activity className={`h-3 w-3 ${color}`} />
      </div>
      <div className="flex flex-col">
        <span className="text-[13px] font-medium text-foreground leading-tight">{label}</span>
        <span className="text-[11px] font-medium text-muted-foreground mt-0.5">{time}</span>
      </div>
    </div>
  );
}
