import { useAuth } from "@/features/auth/hooks/useAuth";
import { useCustomerDashboard } from "@/features/customer/hooks/useCustomerDashboard";
import { useNavigate } from "react-router-dom";
import { Plus, Ticket, Activity, User, BookOpen, Clock, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { formatTimeAgo } from "@/shared/utils/date";
import { CreateTicketDialog } from "@/features/customer/components/CreateTicketDialog";

export function CustomerDashboardOverview() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { data: dashboard, isLoading } = useCustomerDashboard();

  const openCount = dashboard?.summary.openRequests || 0;
  const waitingCount = dashboard?.summary.waitingForSupport || 0;
  const resolvedCount = dashboard?.summary.resolved || 0;

  const recentTickets = dashboard?.tickets?.slice(0, 3) || [];

  return (
    <div className="h-full overflow-auto p-6 flex flex-col gap-8 bg-background">
      {/* Welcome Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between bg-card border border-border rounded-xl p-6 shadow-sm gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            Hi {user?.fullName || user?.email?.split("@")[0]} 👋
          </h1>
          <p className="text-muted-foreground mt-1 text-sm">How can we help you today?</p>
        </div>
        <CreateTicketDialog>
          <Button className="shrink-0 shadow-sm h-9 px-4 text-sm font-medium gap-1.5 pb-0.5">
            <Plus className="h-4 w-4" />
            Create Ticket
          </Button>
        </CreateTicketDialog>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Main Column */}
        <div className="lg:col-span-2 flex flex-col gap-6">
          
          {/* Customer Metrics */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <MetricCard title="Open Requests" value={isLoading ? null : openCount} />
            <MetricCard title="Waiting for Support" value={isLoading ? null : waitingCount} />
            <MetricCard title="Resolved" value={isLoading ? null : resolvedCount} />
          </div>

          {/* Recent Tickets */}
          <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-sm font-semibold text-foreground">Recent Tickets</h2>
              <button onClick={() => navigate("/my-tickets")} className="text-xs text-blue-600 hover:text-blue-700 font-medium transition-colors">View All &rarr;</button>
            </div>
            
            <div className="space-y-3">
              {isLoading ? (
                [1,2,3].map(i => <Skeleton key={i} className="h-16 w-full rounded-lg bg-muted" />)
              ) : recentTickets.length > 0 ? (
                recentTickets.map(ticket => (
                  <div key={ticket.ticketNumber} onClick={() => navigate(`/my-tickets/${ticket.ticketNumber}`)} className="flex items-center justify-between p-3 rounded-lg hover:bg-muted/50 cursor-pointer border border-transparent hover:border-border transition-colors">
                    <div>
                      <p className="font-medium text-sm text-foreground mb-1 truncate max-w-[200px] sm:max-w-[300px]">{ticket.subject}</p>
                      <p className="text-xs text-muted-foreground">{ticket.lastUpdated ? formatTimeAgo(ticket.lastUpdated) : 'just now'}</p>
                    </div>
                    <CustomerStatusBadge status={ticket.status} />
                  </div>
                ))
              ) : (
                <div className="text-center py-6 border border-dashed border-border rounded-lg">
                  <p className="text-sm text-muted-foreground">No recent tickets.</p>
                </div>
              )}
            </div>
          </div>

          {/* Recommended Resources or Helpful Articles */}
          {dashboard?.recommendedResources ? (
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <h2 className="text-sm font-semibold text-foreground mb-3">{dashboard.recommendedResources.title}</h2>
              <p className="text-sm text-muted-foreground mb-4 leading-relaxed line-clamp-3">
                {dashboard.recommendedResources.summary}
              </p>
              {dashboard.recommendedResources.resourceLinks && dashboard.recommendedResources.resourceLinks.length > 0 && (
                <div className="flex flex-col gap-2">
                  {dashboard.recommendedResources.resourceLinks.map((link, idx) => (
                    <a key={idx} href="#" className="text-sm text-blue-600 hover:underline">{link}</a>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
              <h2 className="text-sm font-semibold text-foreground mb-4">Helpful Articles</h2>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <ArticleLink title="Refund Policy" />
                <ArticleLink title="Payment Issues" />
                <ArticleLink title="Subscription Upgrades" />
                <ArticleLink title="Login Troubleshooting" />
              </div>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          
          {/* Quick Actions */}
          <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
            <h2 className="text-sm font-semibold text-foreground mb-4">Quick Actions</h2>
            <div className="flex flex-col gap-2">
              <CreateTicketDialog>
                <button className="flex items-center gap-3 w-full p-3 rounded-lg hover:bg-muted text-left transition-colors group">
                  <div className="h-8 w-8 rounded-full bg-blue-50 flex items-center justify-center group-hover:bg-blue-100 transition-colors">
                    <Plus className="h-4 w-4 text-blue-600" />
                  </div>
                  <span className="text-sm font-medium text-foreground">Create Ticket</span>
                </button>
              </CreateTicketDialog>
              <QuickAction icon={BookOpen} label="Browse Knowledge Base" onClick={() => {}} />
              <QuickAction icon={Ticket} label="My Tickets" onClick={() => navigate("/my-tickets")} />
              <QuickAction icon={Activity} label="Service Status" onClick={() => {}} active />
              <QuickAction icon={User} label="Profile" onClick={() => navigate("/profile")} />
            </div>
          </div>

          {/* Recent Updates */}
          <div className="bg-card border border-border rounded-xl p-6 shadow-sm">
            <h2 className="text-sm font-semibold text-foreground mb-4">Recent Updates</h2>
            {isLoading ? (
               <Skeleton className="h-32 w-full rounded-lg bg-muted" />
            ) : recentTickets.length > 0 ? (
              <div className="space-y-0">
                <UpdateItem label="Ticket received" time={recentTickets[0]?.lastUpdated ? formatTimeAgo(recentTickets[0].lastUpdated) : ""} active isLast={false} />
                <UpdateItem label={`Status: ${dashboard?.summary?.latestTicketStatus || 'In Review'}`} time="" active={false} isLast={true} />
              </div>
            ) : (
              <p className="text-sm text-muted-foreground text-center py-4">No recent updates.</p>
            )}
          </div>
        </div>
        
      </div>
    </div>
  );
}

function MetricCard({ title, value }: { title: string, value: number | null }) {
  return (
    <div className="bg-card border border-border rounded-xl p-5 flex flex-col gap-2 shadow-sm">
      <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider">{title}</p>
      <div className="text-3xl font-bold text-foreground">
        {value === null ? <Skeleton className="h-9 w-12 bg-muted rounded" /> : value}
      </div>
    </div>
  );
}

function CustomerStatusBadge({ status }: { status: string }) {
  let style = "text-muted-foreground bg-muted";
  let label = status;

  switch (status) {
    case "NEW":
    case "ANALYZING":
    case "ANALYZED":
      style = "text-blue-600 bg-blue-50 border border-blue-200";
      label = "Submitted";
      break;
    case "ASSIGNED":
      style = "text-purple-600 bg-purple-50 border border-purple-200";
      label = "In Review";
      break;
    case "IN_PROGRESS":
      style = "text-orange-600 bg-orange-50 border border-orange-200";
      label = "In Progress";
      break;
    case "RESOLVED":
      style = "text-emerald-600 bg-emerald-50 border border-emerald-200";
      label = "Resolved";
      break;
    case "CLOSED":
      style = "text-gray-600 bg-gray-50 border border-gray-200";
      label = "Closed";
      break;
  }
  return <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded ${style}`}>{label}</span>;
}

function ArticleLink({ title }: { title: string }) {
  return (
    <button className="flex items-center gap-3 p-3 rounded-lg hover:bg-muted/50 border border-transparent hover:border-border transition-colors text-left group">
      <div className="h-8 w-8 rounded-full bg-blue-50 flex items-center justify-center shrink-0">
        <BookOpen className="h-4 w-4 text-blue-600 group-hover:scale-110 transition-transform" />
      </div>
      <span className="text-sm font-medium text-foreground">{title}</span>
    </button>
  );
}

function QuickAction({ icon: Icon, label, onClick, active }: { icon: any, label: string, onClick: () => void, active?: boolean }) {
  return (
    <button onClick={onClick} className="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-muted/50 border border-transparent hover:border-border transition-colors text-left">
      <div className="h-8 w-8 rounded-lg bg-muted flex items-center justify-center shrink-0">
        <Icon className="h-4 w-4 text-muted-foreground" />
      </div>
      <span className="text-sm font-medium text-foreground flex-1">{label}</span>
      {active && <span className="text-[10px] uppercase tracking-wider font-bold text-emerald-600 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded">Operational</span>}
    </button>
  );
}

function UpdateItem({ label, time, active, isLast }: { label: string, time: string, active?: boolean, isLast: boolean }) {
  return (
    <div className="flex gap-4">
      <div className="flex flex-col items-center">
        <div className={`h-4 w-4 mt-0.5 rounded-full flex items-center justify-center shrink-0 border ${active ? "bg-blue-50 border-blue-200 text-blue-600" : "bg-card border-border text-muted-foreground"}`}>
          {active ? <CheckCircle2 className="h-3 w-3" /> : <Clock className="h-3 w-3" />}
        </div>
        {!isLast && <div className="w-px h-full min-h-[1.5rem] my-1 bg-muted" />}
      </div>
      <div className="flex flex-col pb-4">
        <span className={`text-[13px] font-medium leading-tight pt-0.5 ${active ? "text-foreground" : "text-muted-foreground"}`}>{label}</span>
        {time && <span className="text-[11px] text-muted-foreground mt-1">{time}</span>}
      </div>
    </div>
  );
}
