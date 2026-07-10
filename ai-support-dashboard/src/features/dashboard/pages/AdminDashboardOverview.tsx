import { useAuth } from "@/features/auth/hooks/useAuth";
import { useTicketList } from "@/features/workspace/hooks/useWorkspace";
import { useNavigate } from "react-router-dom";
import { 
  Activity, Users, Settings, Server, Database, BrainCircuit,
  Network, ShieldCheck, Zap, Bot, BookOpen
} from "lucide-react";

export function AdminDashboardOverview() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { isLoading } = useTicketList();

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
                <div className="text-4xl font-bold mb-4">{isLoading ? "-" : "143"} <span className="text-lg text-slate-300 font-medium">Tickets Today</span></div>
              </div>
              <div className="flex gap-4 text-sm font-medium bg-white/10 rounded-lg p-3">
                <div className="flex flex-col"><span className="text-white font-bold">432</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">Customers</span></div>
                <div className="flex flex-col"><span className="text-white font-bold">24</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">Agents</span></div>
                <div className="flex flex-col"><span className="text-white font-bold">2</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">Admins</span></div>
                <div className="flex flex-col ml-auto text-right"><span className="text-emerald-400 font-bold">18</span> <span className="text-[10px] font-medium uppercase tracking-wider text-slate-400">Active</span></div>
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
                  <span className="text-2xl font-bold text-foreground">98%</span>
                  <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">Analysis Success</span>
                </div>
                <div className="flex flex-col">
                  <span className="text-2xl font-bold text-foreground">100%</span>
                  <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">Routing Success</span>
                </div>
                <div className="flex flex-col">
                  <span className="text-2xl font-bold text-foreground">93%</span>
                  <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-wider">RAG Coverage</span>
                </div>
                <div className="flex flex-col">
                  <span className="text-2xl font-bold text-foreground">850<span className="text-sm text-muted-foreground ml-1">ms</span></span>
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
                  <WorkloadBar label="Billing" value={42} max={50} color="bg-blue-500" />
                  <WorkloadBar label="Support" value={38} max={50} color="bg-emerald-500" />
                  <WorkloadBar label="Security" value={14} max={50} color="bg-orange-500" />
                  <WorkloadBar label="Sales" value={8} max={50} color="bg-purple-500" />
                </div>
              </div>

              {/* Routing Metrics */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm flex-1">
                <h2 className="text-sm font-semibold text-foreground mb-4">Routing Overview</h2>
                <div className="grid grid-cols-2 gap-4">
                  <RoutingStat label="Billing Routed" value="32" />
                  <RoutingStat label="Security Routed" value="14" />
                  <RoutingStat label="Support Routed" value="48" />
                  <RoutingStat label="Manual Overrides" value="5" alert />
                </div>
              </div>
            </div>

            <div className="flex flex-col gap-6">
              {/* System Health */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
                <h2 className="text-sm font-semibold text-foreground mb-4">Platform Status</h2>
                <div className="grid grid-cols-2 gap-3">
                  <SystemStatus label="API Gateway" icon={Network} />
                  <SystemStatus label="Kafka" icon={Activity} />
                  <SystemStatus label="Postgres" icon={Database} />
                  <SystemStatus label="Redis" icon={Zap} />
                  <SystemStatus label="AI Models" icon={Bot} />
                  <SystemStatus label="Auth Service" icon={ShieldCheck} />
                </div>
              </div>

              {/* Recent Platform Events */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm flex-1">
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-sm font-semibold text-foreground">Recent Platform Events</h2>
                </div>
                <div className="space-y-4">
                  <EventItem label="Routing rule changed" sublabel="By admin@system" time="10m ago" />
                  <EventItem label="Knowledge article updated" sublabel="Refund Policy v2" time="1h ago" />
                  <EventItem label="Agent created" sublabel="john.doe@support" time="3h ago" />
                  <EventItem label="Customer registered" sublabel="new client" time="4h ago" />
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
                <span className="font-bold text-foreground">56</span>
              </div>
              <div className="flex justify-between items-center text-[13px]">
                <span className="text-muted-foreground font-medium">Vector Embedded</span>
                <span className="font-bold text-emerald-600">56</span>
              </div>
              <div className="flex justify-between items-center text-[13px] pt-2 border-t border-border mt-1">
                <span className="text-muted-foreground font-medium">Most Used</span>
                <span className="font-bold text-foreground text-right w-24 truncate">Refund Policy</span>
              </div>
            </div>
          </div>

          {/* Admin Activity */}
          <div className="bg-card border border-border rounded-xl p-5 shadow-sm">
            <h2 className="text-sm font-semibold text-foreground mb-4">My Admin Activity</h2>
            <div className="space-y-4">
              <ActivityItem label="Created user account" time="3h ago" color="text-blue-500" />
              <ActivityItem label="Assigned role AGENT" time="3h ago" color="text-emerald-500" />
              <ActivityItem label="Updated routing rule" time="Yesterday" color="text-orange-500" />
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

function SystemStatus({ label, icon: Icon }: { label: string, icon: any }) {
  return (
    <div className="flex items-center gap-2 p-2.5 rounded-lg border border-border bg-card">
      <div className="h-6 w-6 rounded bg-emerald-50 flex items-center justify-center shrink-0 border border-emerald-100">
        <Icon className="h-3.5 w-3.5 text-emerald-600" />
      </div>
      <div className="flex flex-col min-w-0 flex-1">
        <span className="text-[11px] font-bold text-foreground truncate">{label}</span>
        <span className="text-[9px] uppercase tracking-wider font-bold text-emerald-600">Healthy</span>
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
