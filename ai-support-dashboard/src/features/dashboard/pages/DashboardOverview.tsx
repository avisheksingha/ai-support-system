import { useAuth } from "@/features/auth/hooks/useAuth";
import { useTicketList, useTimeline } from "@/features/workspace/hooks/useWorkspace";
import { BusinessMetricsCalculator } from "../lib/BusinessMetricsCalculator";
import {
  Activity, LayoutDashboard,
  TrendingUp, Ticket,
} from "lucide-react";
import { TicketTimeline } from "@/features/workspace/components/TicketTimeline";
import { Skeleton } from "@/components/ui/skeleton";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from "recharts";
import { CustomerDashboardOverview } from "./CustomerDashboardOverview";

const PRIORITY_COLORS: Record<string, string> = {
  CRITICAL: "#ef4444", HIGH: "#f97316", MEDIUM: "#eab308", LOW: "#3b82f6",
};

export function DashboardOverview() {
  const { user } = useAuth();
  
  if (user?.role === "CUSTOMER") {
    return <CustomerDashboardOverview />;
  }
  
  return <AdminDashboardOverview />;
}

function AdminDashboardOverview() {
  const { user } = useAuth();
  const { data: tickets, isLoading: isTicketsLoading } = useTicketList();

  const mostRecentTicket = tickets && tickets.length > 0 ? tickets[0] : null;
  const { data: recentTimeline, isLoading: isTimelineLoading } = useTimeline(mostRecentTicket?.id, mostRecentTicket?.createdAt);

  const openTickets = tickets ? BusinessMetricsCalculator.calculateOpenTickets(tickets) : 0;
  const resolvedToday = tickets ? BusinessMetricsCalculator.calculateResolvedToday(tickets) : 0;
  const priorityDist = tickets ? BusinessMetricsCalculator.calculatePriorityDistribution(tickets) : [];

  return (
    <div className="h-full overflow-auto p-6 flex flex-col gap-8">

      {/* Page Header */}
      <div className="flex items-start justify-between">
        <div>
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-medium mb-3">
            <span className="h-1.5 w-1.5 rounded-full bg-emerald-400 animate-pulse" />
            All Systems Operational
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Operations Center</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Welcome back, <span className="text-foreground font-medium">{user?.fullName || user?.email?.split("@")[0]}</span>
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
                iconColor="text-blue-400"
                iconBg="bg-blue-500/10 border-blue-500/20"
                title="Open Tickets"
                value={isTicketsLoading ? null : openTickets}
              />
              <MetricCard
                icon={TrendingUp}
                iconColor="text-emerald-400"
                iconBg="bg-emerald-500/10 border-emerald-500/20"
                title="Resolved Today"
                value={isTicketsLoading ? null : resolvedToday}
              />
              <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg p-5 flex flex-col">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider mb-3">Priority Distribution</p>
                <div className="flex-1 min-h-[110px]">
                  {isTicketsLoading ? (
                    <div className="h-full flex items-center justify-center">
                      <Skeleton className="h-24 w-24 rounded-full bg-muted" />
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
                    <div className="flex h-full items-center justify-center text-sm text-muted-foreground">No data yet</div>
                  )}
                </div>
              </div>
            </div>
          </section>


        </div>

        {/* Activity Sidebar */}
        <div className="xl:col-span-1">
          <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg p-5 h-full">
            <SectionHeader icon={Activity} label="Recent Activity" />
            {isTimelineLoading ? (
              <div className="space-y-3 mt-4">
                {[...Array(4)].map((_, i) => (
                  <Skeleton key={i} className="h-12 w-full bg-muted rounded-lg" />
                ))}
              </div>
            ) : recentTimeline ? (
              <div className="mt-4">
                <p className="text-xs text-muted-foreground mb-4 pb-3 border-b border-border">
                  Latest events · {mostRecentTicket?.ticketNumber}
                </p>
                <TicketTimeline events={recentTimeline} />
              </div>
            ) : (
              <div className="mt-6 text-center">
                <Activity className="h-8 w-8 text-zinc-800 mx-auto mb-2" />
                <p className="text-sm text-muted-foreground">No recent activity</p>
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
    <h2 className="text-xs font-semibold text-muted-foreground mb-4 uppercase tracking-widest flex items-center gap-2">
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
    <div className="bg-card shadow-sm border-0 ring-1 ring-border/50 rounded-lg p-5 flex flex-col gap-3 transition-colors">
      <div className="flex items-center justify-between">
        <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">{title}</p>
        <div className={`h-7 w-7 rounded-lg border flex items-center justify-center ${iconBg}`}>
          <Icon className={`h-3.5 w-3.5 ${iconColor}`} />
        </div>
      </div>
      <div className="text-3xl font-bold text-foreground tabular-nums">
        {value === null ? <Skeleton className="h-9 w-16 bg-muted rounded" /> : value}
      </div>
      {trend && (
        <p className="text-xs text-muted-foreground">{trend}</p>
      )}
    </div>
  );
}


