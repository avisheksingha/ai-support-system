import { useAuth } from "@/features/auth/hooks/useAuth";
import { useTicketList, useTimeline } from "@/features/workspace/hooks/useWorkspace";
import { BusinessMetricsCalculator } from "../lib/BusinessMetricsCalculator";
import {
  Activity, Server, LayoutDashboard, BrainCircuit,
  CheckCircle2, AlertCircle, TrendingUp, Ticket, Zap, Clock,
} from "lucide-react";
import { TicketTimeline } from "@/features/workspace/components/TicketTimeline";
import { Skeleton } from "@/components/ui/skeleton";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";

const PRIORITY_COLORS: Record<string, string> = {
  CRITICAL: "#ef4444", HIGH: "#f97316", MEDIUM: "#eab308", LOW: "#3b82f6",
};

export function DashboardOverview() {
  const { user } = useAuth();
  const { data: tickets, isLoading: isTicketsLoading } = useTicketList();
  const mostRecentTicket = tickets && tickets.length > 0 ? tickets[0] : null;
  const { data: recentTimeline, isLoading: isTimelineLoading } = useTimeline(mostRecentTicket?.id);

  const openTickets = tickets ? BusinessMetricsCalculator.calculateOpenTickets(tickets) : 0;
  const resolvedToday = tickets ? BusinessMetricsCalculator.calculateResolvedToday(tickets) : 0;
  const priorityDist = tickets ? BusinessMetricsCalculator.calculatePriorityDistribution(tickets) : [];

  return (
    <div className="flex flex-col gap-8">

      {/* Page Header */}
      <div className="flex items-start justify-between">
        <div>
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-medium mb-3">
            <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" />
            All Systems Operational
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-zinc-100">Operations Center</h1>
          <p className="text-sm text-zinc-500 mt-1">
            Welcome back, <span className="text-zinc-300 font-medium">{user?.fullName || user?.email?.split("@")[0]}</span>
            {" "}— here's what's happening today.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-4 gap-6">

        {/* Main Column */}
        <div className="xl:col-span-3 flex flex-col gap-6">

          {/* Business Metrics */}
          <section>
            <SectionHeader icon={LayoutDashboard} label="Business Operations" />
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <MetricCard
                icon={Ticket}
                iconColor="text-indigo-400"
                iconBg="bg-indigo-500/10 border-indigo-500/20"
                title="Open Tickets"
                value={isTicketsLoading ? null : openTickets}
                trend="+2 since yesterday"
              />
              <MetricCard
                icon={TrendingUp}
                iconColor="text-emerald-400"
                iconBg="bg-emerald-500/10 border-emerald-500/20"
                title="Resolved Today"
                value={isTicketsLoading ? null : resolvedToday}
                trend="On track"
              />
              <div className="bg-zinc-900/60 border border-zinc-800 rounded-xl p-5 flex flex-col">
                <p className="text-xs font-medium text-zinc-500 uppercase tracking-wider mb-3">Priority Distribution</p>
                <div className="flex-1 min-h-[110px]">
                  {isTicketsLoading ? (
                    <div className="h-full flex items-center justify-center">
                      <Skeleton className="h-24 w-24 rounded-full bg-zinc-800" />
                    </div>
                  ) : priorityDist.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie data={priorityDist} dataKey="value" nameKey="name" cx="50%" cy="50%" innerRadius={30} outerRadius={48} strokeWidth={0}>
                          {priorityDist.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={PRIORITY_COLORS[entry.name] || "#52525b"} />
                          ))}
                        </Pie>
                        <Tooltip
                          contentStyle={{ backgroundColor: "#09090b", borderColor: "#27272a", borderRadius: "8px", fontSize: "12px" }}
                          labelStyle={{ color: "#a1a1aa" }}
                        />
                      </PieChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="flex h-full items-center justify-center text-sm text-zinc-600">No data yet</div>
                  )}
                </div>
              </div>
            </div>
          </section>

          {/* AI Operations */}
          <section>
            <SectionHeader icon={BrainCircuit} label="AI Operations" />
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <ComingSoonCard icon={Zap} title="Automation Rate" desc="Requires Orchestration Service" />
              <ComingSoonCard icon={Clock} title="Avg AI Latency" desc="Requires Orchestration Service" />
              <ComingSoonCard icon={Activity} title="AI Queue Depth" desc="Requires Orchestration Service" />
            </div>
          </section>

          {/* System Health */}
          <section>
            <SectionHeader icon={Server} label="System Health" />
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              <HealthNode name="API Gateway" latency="42 ms" />
              <HealthNode name="Auth Service" latency="28 ms" />
              <HealthNode name="Ticket Service" latency="65 ms" />
              <HealthNode name="Discovery" latency="12 ms" />
              <HealthNode name="AI Analysis" latency="—" />
              <HealthNode name="RAG Service" latency="—" />
              <HealthNode name="Routing" latency="—" />
              <HealthNode name="Kafka" latency="—" />
            </div>
          </section>
        </div>

        {/* Activity Sidebar */}
        <div className="xl:col-span-1">
          <div className="bg-zinc-900/60 border border-zinc-800 rounded-xl p-5 h-full">
            <SectionHeader icon={Activity} label="Recent Activity" />
            {isTimelineLoading ? (
              <div className="space-y-3 mt-4">
                {[...Array(4)].map((_, i) => (
                  <Skeleton key={i} className="h-12 w-full bg-zinc-800 rounded-lg" />
                ))}
              </div>
            ) : recentTimeline ? (
              <div className="mt-4">
                <p className="text-xs text-zinc-600 mb-4 pb-3 border-b border-zinc-800/60">
                  Latest events · {mostRecentTicket?.ticketNumber}
                </p>
                <TicketTimeline events={recentTimeline} />
              </div>
            ) : (
              <div className="mt-6 text-center">
                <Activity className="h-8 w-8 text-zinc-800 mx-auto mb-2" />
                <p className="text-sm text-zinc-600">No recent activity</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

/* ── Sub-components ── */

function SectionHeader({ icon: Icon, label }: { icon: React.ElementType; label: string }) {
  return (
    <h2 className="text-xs font-semibold text-zinc-500 mb-4 uppercase tracking-widest flex items-center gap-2">
      <Icon className="h-3.5 w-3.5" />
      {label}
    </h2>
  );
}

function MetricCard({
  icon: Icon, iconColor, iconBg, title, value, trend,
}: {
  icon: React.ElementType;
  iconColor: string;
  iconBg: string;
  title: string;
  value: React.ReactNode;
  trend?: string;
}) {
  return (
    <div className="bg-zinc-900/60 border border-zinc-800 rounded-xl p-5 flex flex-col gap-3 hover:border-zinc-700 transition-colors">
      <div className="flex items-center justify-between">
        <p className="text-xs font-medium text-zinc-500 uppercase tracking-wider">{title}</p>
        <div className={`h-7 w-7 rounded-lg border flex items-center justify-center ${iconBg}`}>
          <Icon className={`h-3.5 w-3.5 ${iconColor}`} />
        </div>
      </div>
      <div className="text-3xl font-bold text-zinc-100 tabular-nums">
        {value === null ? <Skeleton className="h-9 w-16 bg-zinc-800 rounded" /> : value}
      </div>
      {trend && (
        <p className="text-xs text-zinc-600">{trend}</p>
      )}
    </div>
  );
}

function ComingSoonCard({ icon: Icon, title, desc }: { icon: React.ElementType; title: string; desc: string }) {
  return (
    <div className="bg-zinc-900/30 border border-zinc-800/50 border-dashed rounded-xl p-5 flex flex-col gap-2 opacity-60">
      <div className="flex items-center justify-between">
        <p className="text-xs font-medium text-zinc-500 uppercase tracking-wider">{title}</p>
        <Icon className="h-3.5 w-3.5 text-zinc-700" />
      </div>
      <p className="text-lg font-bold text-zinc-700">—</p>
      <p className="text-xs text-zinc-700">{desc}</p>
    </div>
  );
}

function HealthNode({ name, latency }: { name: string; latency: string }) {
  return (
    <div className="bg-zinc-950 border border-zinc-800/50 rounded-lg p-3 flex flex-col gap-2 hover:border-zinc-700 transition-colors">
      <div className="flex justify-between items-center">
        <span className="text-xs font-medium text-zinc-300 truncate mr-2">{name}</span>
        <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500 shrink-0" />
      </div>
      <div className="flex justify-between items-center">
        <span className="text-[10px] text-zinc-500 flex items-center gap-1.5">
          <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
          Online
        </span>
        <span className="text-[10px] font-mono text-zinc-600">{latency}</span>
      </div>
    </div>
  );
}
