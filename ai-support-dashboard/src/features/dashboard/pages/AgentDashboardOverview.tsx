import { useAuth } from "@/features/auth/hooks/useAuth";
import { useQuery } from "@tanstack/react-query";
import { workspaceApi } from "@/features/workspace/api/workspaceApi";
import { useNavigate } from "react-router-dom";
import { 
  Activity, CheckCircle2, Inbox, Users, AlertTriangle, TrendingUp, TrendingDown, Clock,
  Calendar, Zap, Server, Cpu, Search, Sparkles, Bot, LineChart as LineChartIcon, FileText, ArrowRight, ShieldAlert, Briefcase, Layers, BrainCircuit
} from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { formatDistanceToNow } from "date-fns";

export function AgentDashboardOverview() {
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const { data: dashboard, isLoading } = useQuery({
    queryKey: ["agent-dashboard"],
    queryFn: () => workspaceApi.getAgentDashboard(),
    refetchInterval: 60000,
  });

  return (
    <div className="h-full overflow-auto p-4 md:p-6 lg:p-8 flex flex-col gap-8 bg-[#F8FAFC]">
      
      {/* 1. HEADER */}
      <div className="flex flex-col gap-4 md:flex-row md:items-center justify-between border-b border-border/50 pb-6">
        <div className="flex items-center gap-5">
          <div className="relative">
            <div className="h-16 w-16 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 font-bold text-2xl shadow-sm border border-blue-200">
              {user?.fullName?.charAt(0) || user?.email?.charAt(0) || "A"}
            </div>
            <div className="absolute bottom-0 right-0 h-4 w-4 bg-emerald-500 rounded-full border-2 border-white shadow-sm ring-1 ring-black/5" />
          </div>
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-foreground">{user?.fullName || dashboard?.profile?.name || user?.email?.split("@")[0]}</h1>
            <div className="flex items-center gap-3 mt-2">
              <span className="text-sm font-medium text-muted-foreground bg-slate-100 px-2 py-0.5 rounded-md">{dashboard?.profile?.team || "Support"}</span>
              <span className="text-[11px] text-emerald-600 font-bold uppercase tracking-wider bg-emerald-50 px-2 py-0.5 rounded-md">
                {dashboard?.profile?.status || "Online"}
              </span>
            </div>
          </div>
        </div>
        <div className="grid grid-cols-2 md:flex items-center gap-8 md:gap-12 mt-4 md:mt-0 bg-white p-4 md:px-6 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex flex-col">
            <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider flex items-center gap-1.5"><Calendar className="w-3 h-3"/> Today</span>
            <span className="text-sm font-semibold text-slate-700 mt-0.5">{dashboard?.topSummary?.currentDate || "Loading..."}</span>
          </div>
          <div className="flex flex-col">
            <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider flex items-center gap-1.5"><Clock className="w-3 h-3"/> Shift</span>
            <span className="text-sm font-semibold text-slate-700 mt-0.5">{dashboard?.topSummary?.shift || "--"}</span>
          </div>
          <div className="flex flex-col">
            <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Assigned Today</span>
            <span className="text-sm font-semibold text-slate-700 mt-0.5">{dashboard?.topSummary?.assignedToday ?? "--"} Tickets</span>
          </div>
          <div className="flex flex-col">
            <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Current SLA</span>
            <span className="text-sm font-semibold text-emerald-600 mt-0.5 flex items-center gap-1"><CheckCircle2 className="w-3.5 h-3.5"/> {dashboard?.topSummary?.currentSla || "--"}</span>
          </div>
        </div>
      </div>

      {/* --- SECTION: ME --- */}
      <div className="flex flex-col gap-6">
        <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider flex items-center gap-2">
          <Users className="w-4 h-4 text-blue-600"/> Me
        </h2>
        
        {/* 2. PRIMARY KPI ROW */}
        <div className={`grid grid-cols-1 md:grid-cols-2 gap-4 ${dashboard?.myPerformance ? 'lg:grid-cols-4' : 'lg:grid-cols-3'}`}>
          
          {/* My Queue */}
          <div 
            className="bg-gradient-to-br from-blue-500 to-blue-700 rounded-xl p-5 shadow-md shadow-blue-900/10 text-white flex flex-col relative overflow-hidden group cursor-pointer hover:shadow-lg transition-all"
            onClick={() => navigate("/tickets")}
          >
            <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
              <Inbox className="w-24 h-24" />
            </div>
            <p className="text-blue-100 font-medium text-xs uppercase tracking-wider mb-2 relative z-10">My Queue</p>
            <div className="text-4xl font-bold mb-4 relative z-10">{isLoading ? <Skeleton className="h-10 w-16 bg-blue-400/50" /> : dashboard?.myQueue?.assignedTickets ?? 0}</div>
            <div className="grid grid-cols-4 gap-2 text-center bg-black/20 rounded-lg p-2.5 relative z-10 mt-auto backdrop-blur-sm">
              <div className="flex flex-col"><span className="text-red-300 font-bold text-sm">{dashboard?.myQueue?.critical ?? 0}</span> <span className="text-[9px] uppercase font-bold tracking-widest text-white/70 mt-0.5">Crit</span></div>
              <div className="flex flex-col"><span className="text-orange-300 font-bold text-sm">{dashboard?.myQueue?.high ?? 0}</span> <span className="text-[9px] uppercase font-bold tracking-widest text-white/70 mt-0.5">High</span></div>
              <div className="flex flex-col"><span className="text-yellow-300 font-bold text-sm">{dashboard?.myQueue?.medium ?? 0}</span> <span className="text-[9px] uppercase font-bold tracking-widest text-white/70 mt-0.5">Med</span></div>
              <div className="flex flex-col"><span className="text-blue-200 font-bold text-sm">{dashboard?.myQueue?.low ?? 0}</span> <span className="text-[9px] uppercase font-bold tracking-widest text-white/70 mt-0.5">Low</span></div>
            </div>
            <div className="flex justify-between items-center text-[10px] text-blue-200 uppercase tracking-wider mt-3 relative z-10">
              <span>Avg Wait: {dashboard?.myQueue?.averageWaitTime && dashboard.myQueue.averageWaitTime !== "--" ? dashboard.myQueue.averageWaitTime : "N/A"}</span>
              <span>Oldest: {dashboard?.myQueue?.oldestTicketAge && dashboard.myQueue.oldestTicketAge !== "--" ? dashboard.myQueue.oldestTicketAge : "None"}</span>
            </div>
          </div>

          {/* SLA At Risk */}
          <div className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm flex flex-col relative overflow-hidden">
            {dashboard?.slaAtRisk?.ticketsNearBreach ? (
              <>
                <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-orange-400 to-red-500" />
                <div className="flex justify-between items-start mb-4">
                  <p className="text-[11px] font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1.5">
                    <AlertTriangle className="h-3.5 w-3.5 text-orange-500" /> SLA At Risk
                  </p>
                  <span className="text-[10px] font-bold uppercase tracking-wider bg-red-50 text-red-600 px-2 py-0.5 rounded-full border border-red-100">Elevated</span>
                </div>
                <div className="flex items-baseline gap-2 mb-4">
                  <span className="text-4xl font-bold text-slate-800">{dashboard.slaAtRisk.ticketsNearBreach}</span>
                  <span className="text-sm font-medium text-slate-500">Tickets near breach</span>
                </div>
                <div className="space-y-3 mt-auto">
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-slate-500 font-medium">Next Breach</span>
                    <span className="font-bold text-orange-600 bg-orange-50 px-2 py-0.5 rounded border border-orange-100">{dashboard.slaAtRisk.nextSlaBreach}</span>
                  </div>
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-slate-500 font-medium">Avg Remaining</span>
                    <span className="font-bold text-slate-700">{dashboard.slaAtRisk.averageRemainingTime}</span>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex flex-col items-center justify-center h-full text-center py-6">
                <CheckCircle2 className="w-12 h-12 text-emerald-400 mb-3" />
                <p className="text-sm font-bold text-slate-600">✓ No SLA risks</p>
              </div>
            )}
          </div>

          {/* Resolved Today */}
          <div className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm flex flex-col relative overflow-hidden">
             <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-emerald-400 to-emerald-600" />
            <div className="flex justify-between items-start mb-4">
              <p className="text-[11px] font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1.5">
                <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500" /> Resolved Today
              </p>
            </div>
            {dashboard?.resolvedToday?.ticketsResolvedToday && dashboard.resolvedToday.ticketsResolvedToday > 0 ? (
              <>
                <div className="text-4xl font-bold text-slate-800 mb-4">{dashboard.resolvedToday.ticketsResolvedToday}</div>
                <div className="space-y-3 mt-auto">
                  {dashboard?.resolvedToday?.averageHandleTime && dashboard.resolvedToday.averageHandleTime !== "--" && (
                    <div className="flex justify-between items-center text-sm">
                      <span className="text-slate-500 font-medium">Avg Handle Time</span>
                      <span className="font-bold text-slate-700">{dashboard.resolvedToday.averageHandleTime}</span>
                    </div>
                  )}
                  {dashboard?.resolvedToday?.averageFirstResponseTime && dashboard.resolvedToday.averageFirstResponseTime !== "--" && (
                    <div className="flex justify-between items-center text-sm">
                      <span className="text-slate-500 font-medium">First Response</span>
                      <span className="font-bold text-slate-700">{dashboard.resolvedToday.averageFirstResponseTime}</span>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex flex-col items-center justify-center h-full text-center py-4">
                <CheckCircle2 className="w-8 h-8 text-slate-200 mb-2" />
                <p className="text-[13px] font-medium text-slate-500">No tickets resolved yet today.</p>
              </div>
            )}
          </div>

          {/* My Performance */}
          {dashboard?.myPerformance && (
            <div className="bg-white border border-slate-200 rounded-xl p-5 shadow-sm flex flex-col relative overflow-hidden">
              <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-indigo-400 to-indigo-600" />
              <p className="text-[11px] font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1.5 mb-4">
                <LineChartIcon className="h-3.5 w-3.5 text-indigo-500" /> My Performance
              </p>
              <div className="flex justify-between items-end mb-4">
                <div>
                  <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-1">CSAT</span>
                  <div className="text-3xl font-bold text-slate-800">{dashboard.myPerformance.csat}%</div>
                </div>
              </div>
              <div className="space-y-3 mt-auto">
                <div className="flex justify-between items-center text-sm">
                  <span className="text-slate-500 font-medium">Resolution Rate</span>
                  <span className="font-bold text-indigo-600">{dashboard.myPerformance.resolutionRate}%</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-slate-500 font-medium">Quality Score</span>
                  <span className="font-bold text-slate-700">{dashboard.myPerformance.qualityScore}</span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 3. AI ASSISTANT & ACTIVITY WIDGET */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* AI Assistant (Hero Section) */}
          <div className="lg:col-span-2 bg-gradient-to-b from-indigo-50/80 to-white border-2 border-indigo-100 rounded-xl p-6 md:p-8 shadow-sm flex flex-col">
            <div className="flex items-center justify-between mb-8">
              <div className="flex items-center gap-4">
                <div className="h-12 w-12 bg-indigo-600 rounded-xl shadow-inner flex items-center justify-center">
                  <Sparkles className="h-6 w-6 text-white" />
                </div>
                <div>
                  <h2 className="text-xl font-bold text-slate-900 tracking-tight">AI Assistant</h2>
                  <p className="text-sm font-medium text-slate-500 mt-0.5">Today's Highest Value Recommendations</p>
                </div>
              </div>
              <span className="bg-indigo-100 text-indigo-700 text-xs uppercase font-bold px-3 py-1.5 rounded-full ring-1 ring-indigo-200">
                {dashboard?.aiRecommendations?.length || 0} Pending
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {isLoading ? (
                <>
                  <Skeleton className="h-32 w-full" />
                  <Skeleton className="h-32 w-full" />
                </>
              ) : dashboard?.aiRecommendations?.length ? (
                dashboard.aiRecommendations.map((rec, i) => (
                  <AiRecommendationCard 
                    key={i}
                    ticket={rec.ticketNumber} 
                    title={rec.subject} 
                    reason={rec.businessReason}
                    confidence={Math.round(rec.confidence * 100)}
                    intent={rec.intent}
                    expectedImpact={rec.suggestedAction}
                  />
                ))
              ) : (
                <div className="col-span-2 flex flex-col items-center justify-center py-8 text-center bg-white/50 rounded-xl border border-dashed border-indigo-200">
                  <Bot className="w-10 h-10 text-indigo-200 mb-3" />
                  <p className="text-sm font-medium text-indigo-800">No pending AI recommendations</p>
                </div>
              )}
            </div>
          </div>

          {/* AI Activity Widget */}
          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col">
            <div className="flex items-center gap-2 mb-6">
              <Bot className="h-5 w-5 text-purple-600" />
              <h2 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider">AI Activity Today</h2>
            </div>
            
            <div className="flex-1 grid grid-cols-2 gap-4 mb-4">
              {dashboard?.aiActivityToday?.aiDecisionsGenerated != null && (
                <div className="flex flex-col p-4 bg-purple-50 rounded-xl border border-purple-100">
                  <span className="text-[10px] uppercase font-bold text-purple-600/80 mb-1">AI Decisions</span>
                  <span className="text-2xl font-black text-purple-700">{dashboard.aiActivityToday.aiDecisionsGenerated}</span>
                </div>
              )}
              {dashboard?.aiActivityToday?.suggestedRepliesGenerated != null && (
                <div className="flex flex-col p-4 bg-indigo-50 rounded-xl border border-indigo-100">
                  <span className="text-[10px] uppercase font-bold text-indigo-600/80 mb-1">Drafts</span>
                  <span className="text-2xl font-black text-indigo-700">{dashboard.aiActivityToday.suggestedRepliesGenerated}</span>
                </div>
              )}
              {dashboard?.aiActivityToday?.knowledgeSearches != null && (
                <div className="flex flex-col p-4 bg-emerald-50 rounded-xl border border-emerald-100">
                  <span className="text-[10px] uppercase font-bold text-emerald-600/80 mb-1">KB Searches</span>
                  <span className="text-2xl font-black text-emerald-700">{dashboard.aiActivityToday.knowledgeSearches}</span>
                </div>
              )}
              {dashboard?.aiActivityToday?.averageAiConfidence != null && (
                <div className="flex flex-col p-4 bg-orange-50 rounded-xl border border-orange-100">
                  <span className="text-[10px] uppercase font-bold text-orange-600/80 mb-1">AI Confidence</span>
                  <span className="text-2xl font-black text-orange-700">{dashboard.aiActivityToday.averageAiConfidence}%</span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* AI Processing Widget (Optional) */}
        {dashboard?.aiProcessing && (
          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col mt-4">
            <div className="flex items-center gap-2 mb-6">
              <BrainCircuit className="h-5 w-5 text-blue-600" />
              <h2 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider">AI Processing Engine</h2>
            </div>
            
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="flex flex-col">
                <span className="text-[10px] uppercase font-bold text-slate-500 mb-1">Workflows Executed</span>
                <span className="text-xl font-bold text-slate-800">{dashboard.aiProcessing.workflowsExecuted}</span>
              </div>
              <div className="flex flex-col border-l border-slate-100 pl-4">
                <span className="text-[10px] uppercase font-bold text-slate-500 mb-1">Avg Duration</span>
                <span className="text-xl font-bold text-slate-800">{dashboard.aiProcessing.averageProcessingDurationMs}ms</span>
              </div>
              <div className="flex flex-col border-l border-slate-100 pl-4">
                <span className="text-[10px] uppercase font-bold text-slate-500 mb-1">Success Rate</span>
                <span className="text-xl font-bold text-emerald-600">{dashboard.aiProcessing.successRate}%</span>
              </div>
              <div className="flex flex-col border-l border-slate-100 pl-4">
                <span className="text-[10px] uppercase font-bold text-slate-500 mb-1">Primary Model</span>
                <span className="text-sm font-bold text-slate-700 bg-slate-50 px-2 py-1 rounded inline-flex w-max mt-1 border border-slate-200">
                  {dashboard.aiProcessing.primaryModel}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* --- SECTION: MY WORK & OPERATIONS --- */}
      <div className="flex flex-col gap-6 mt-2">
        <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider flex items-center gap-2">
          <Briefcase className="w-4 h-4 text-emerald-600"/> My Work & Operations
        </h2>
        
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Quick Actions (Modern Grid) */}
          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col">
            <h3 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider mb-5 flex items-center gap-2">
              <Zap className="w-4 h-4 text-amber-500" /> Quick Actions
            </h3>
            <div className="grid grid-cols-2 gap-3">
              <QuickActionButton icon={Inbox} label="Open Queue" onClick={() => navigate("/tickets")} />
              <QuickActionButton icon={Sparkles} label="AI Suggestions" onClick={() => navigate("/tickets")} highlight />
              <QuickActionButton icon={Users} label="Unassigned" onClick={() => navigate("/tickets")} />
              <QuickActionButton icon={ShieldAlert} label="Escalations" onClick={() => {}} />
              <QuickActionButton icon={Search} label="KB Search" onClick={() => {}} />
              <QuickActionButton icon={FileText} label="New Ticket" onClick={() => {}} />
            </div>
          </div>

          {/* Workload Visualization - Temporarily hidden */}
          {false && (
            <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col justify-center">
              <h3 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider mb-6 flex items-center gap-2">
                <Activity className="w-4 h-4 text-blue-500" /> Today's Workload
              </h3>
              
              {/* Segmented Progress */}
              <div className="w-full h-4 flex rounded-full overflow-hidden shadow-inner mb-6 ring-1 ring-slate-200">
                <div className="bg-slate-300 w-[15%]" title="Assigned: 15%"></div>
                <div className="bg-blue-500 w-[35%]" title="In Progress: 35%"></div>
                <div className="bg-amber-400 w-[20%]" title="Waiting Customer: 20%"></div>
                <div className="bg-emerald-500 w-[25%]" title="Resolved: 25%"></div>
                <div className="bg-red-500 w-[5%]" title="Escalated: 5%"></div>
              </div>
              
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-y-4 gap-x-2">
                <WorkloadLegend label="Assigned" color="bg-slate-300" count="3" percent="15%" />
                <WorkloadLegend label="In Progress" color="bg-blue-500" count="7" percent="35%" />
                <WorkloadLegend label="Waiting" color="bg-amber-400" count="4" percent="20%" />
                <WorkloadLegend label="Resolved" color="bg-emerald-500" count="5" percent="25%" />
                <WorkloadLegend label="Escalated" color="bg-red-500" count="1" percent="5%" />
              </div>
            </div>
          )}

          {/* Workflow Activity Card - Temporarily hidden */}
          {false && (
            <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col">
               <h3 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider mb-5 flex items-center gap-2">
                <Layers className="w-4 h-4 text-indigo-500" /> Workflow Activity
              </h3>
              <div className="space-y-4">
                <div className="flex justify-between items-center text-sm">
                  <span className="text-slate-500 font-medium">Workflows Executed</span>
                  <span className="font-bold text-slate-800">4,192</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-slate-500 font-medium">Success Rate</span>
                  <span className="font-bold text-emerald-600">99.8%</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-slate-500 font-medium">Auto-Recoveries</span>
                  <span className="font-bold text-amber-600">14</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-slate-500 font-medium">Pending Approvals</span>
                  <span className="font-bold text-slate-800">3</span>
                </div>
                <div className="flex justify-between items-center text-sm pt-3 border-t border-slate-100">
                  <span className="text-slate-500 font-medium">Avg Execution Time</span>
                  <span className="font-bold text-slate-800">1.2s</span>
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Recent Assignments Table - Temporarily hidden */}
          {false && (
            <div className="bg-white border border-slate-200 rounded-xl shadow-sm flex flex-col overflow-hidden">
              <div className="p-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
                 <h3 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider flex items-center gap-2">
                  <Inbox className="w-4 h-4 text-slate-500" /> Recent Assignments
                </h3>
                <button className="text-xs font-semibold text-blue-600 hover:text-blue-800 transition-colors flex items-center gap-1" onClick={() => navigate("/tickets")}>
                  View All <ArrowRight className="w-3 h-3"/>
                </button>
              </div>
              
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-white text-[11px] uppercase tracking-wider text-slate-400 font-semibold border-b border-slate-200">
                      <th className="px-5 py-3">Ticket</th>
                      <th className="px-5 py-3">Priority</th>
                      <th className="px-5 py-3">Workflow</th>
                      <th className="px-5 py-3">AI Summary</th>
                      <th className="px-5 py-3 text-right">SLA</th>
                    </tr>
                  </thead>
                  <tbody className="text-sm divide-y divide-slate-100">
                    <AssignmentRow ticket="INC-48291" priority="HIGH" workflow="AI_ANALYSIS" summary="Customer requesting urgent refund for duplicate charge." sla="42m" />
                    <AssignmentRow ticket="REQ-8123" priority="MEDIUM" workflow="AUTO_ROUTED" summary="Login failure on iOS app after latest update." sla="2h 15m" />
                    <AssignmentRow ticket="INC-48284" priority="CRITICAL" workflow="WAITING_APPROVAL" summary="Database connection timeout affecting multiple users." sla="18m" />
                    <AssignmentRow ticket="SR-11281" priority="LOW" workflow="RAG_SEARCH" summary="Question regarding API rate limits." sla="1d 4h" />
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* My Activity Timeline */}
          <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col h-full">
            <h3 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider mb-6 flex items-center gap-2">
              <Activity className="w-4 h-4 text-purple-500" /> My Activity
            </h3>
            <div className="relative border-l-2 border-slate-100 ml-3 space-y-6 flex-1 pb-2">
              {isLoading ? (
                <>
                  <Skeleton className="h-8 w-full mb-4" />
                  <Skeleton className="h-8 w-full mb-4" />
                </>
              ) : dashboard?.recentActivity?.length ? (
                dashboard.recentActivity.map((activity, i) => (
                  <TimelineItem 
                    key={activity.id || i}
                    label={activity.description} 
                    time={activity.timestamp ? formatDistanceToNow(new Date(activity.timestamp), { addSuffix: true }) : "recently"} 
                    icon={Activity} 
                    color="text-indigo-500 bg-indigo-50 border-indigo-200" 
                  />
                ))
              ) : (
                <div className="flex flex-col items-center justify-center py-6 text-center h-full">
                  <Activity className="w-8 h-8 text-slate-200 mb-2" />
                  <p className="text-sm font-medium text-slate-400">No recent activity</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* --- SECTION: TEAM & SYSTEM HEALTH (Temporarily Hidden) --- */}
      {false && (
        <div className="flex flex-col gap-6 mt-2 mb-8">
          <h2 className="text-sm font-bold text-slate-800 uppercase tracking-wider flex items-center gap-2">
            <Server className="w-4 h-4 text-slate-600"/> Team & Infrastructure
          </h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {/* Team Panel */}
            <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col">
              <h3 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider mb-5 flex items-center gap-2">
                <Users className="w-4 h-4 text-blue-500" /> Billing Team
              </h3>
              
              <div className="grid grid-cols-4 gap-2 mb-6 text-center">
                <div className="flex flex-col items-center p-2 rounded-lg bg-emerald-50 border border-emerald-100">
                  <span className="text-lg font-bold text-emerald-700">14</span>
                  <span className="text-[9px] uppercase font-bold text-emerald-600/70 mt-0.5">Online</span>
                </div>
                <div className="flex flex-col items-center p-2 rounded-lg bg-amber-50 border border-amber-100">
                  <span className="text-lg font-bold text-amber-700">4</span>
                  <span className="text-[9px] uppercase font-bold text-amber-600/70 mt-0.5">Busy</span>
                </div>
                <div className="flex flex-col items-center p-2 rounded-lg bg-blue-50 border border-blue-100">
                  <span className="text-lg font-bold text-blue-700">8</span>
                  <span className="text-[9px] uppercase font-bold text-blue-600/70 mt-0.5">Avail</span>
                </div>
                <div className="flex flex-col items-center p-2 rounded-lg bg-slate-50 border border-slate-200">
                  <span className="text-lg font-bold text-slate-500">2</span>
                  <span className="text-[9px] uppercase font-bold text-slate-400 mt-0.5">Away</span>
                </div>
              </div>

              <div className="space-y-4">
                <div className="flex justify-between items-center text-sm">
                  <span className="text-slate-500 font-medium">Queue Health</span>
                  <span className="font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">Healthy</span>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-slate-500 font-medium">Average SLA</span>
                  <span className="font-bold text-slate-800">2h 15m</span>
                </div>
              </div>
            </div>

            {/* System Health */}
            <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col">
              <h3 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider mb-5 flex items-center gap-2">
                <Cpu className="w-4 h-4 text-slate-500" /> System Health
              </h3>
              <div className="space-y-3.5">
                <HealthRow label="API Gateway" latency="24ms" status="green" />
                <HealthRow label="Apache Kafka" latency="12ms" status="green" />
                <HealthRow label="Workflow Engine" latency="45ms" status="green" />
                <HealthRow label="AI Orchestrator" latency="180ms" status="yellow" />
                <HealthRow label="Vector DB (pgvector)" latency="8ms" status="green" />
                <HealthRow label="Google LLM API" latency="850ms" status="yellow" />
              </div>
            </div>

            {/* AI Insights */}
            <div className="bg-white border border-slate-200 rounded-xl p-6 shadow-sm flex flex-col">
              <h3 className="text-[13px] font-bold text-slate-800 uppercase tracking-wider mb-5 flex items-center gap-2">
                <BrainCircuit className="w-4 h-4 text-indigo-500" /> AI Insights
              </h3>
              <div className="space-y-4">
                <InsightBullet type="trend-up" text="Refund requests increased 18% today." />
                <InsightBullet type="trend-down" text="Login failures decreasing across all regions." />
                <InsightBullet type="alert" text="Potential duplicate incidents detected in Billing." />
                <InsightBullet type="info" text="Knowledge base coverage for new features is 92%." />
                <InsightBullet type="trend-up" text="Customer sentiment improved by 4% this week." />
              </div>
            </div>

          </div>
        </div>
      )}

    </div>
  );
}

// --- Subcomponents ---

function AiRecommendationCard({ ticket, title, reason, confidence, intent, expectedImpact }: { ticket: string, title: string, reason: string, confidence: number, intent: string, expectedImpact: string }) {
  const isHighConf = confidence >= 95;
  const pColor = intent === 'CANCELLATION' || intent === 'REFUND' ? 'bg-orange-500' : 'bg-blue-500';
  
  return (
    <div className="bg-white border border-indigo-100/50 rounded-lg p-5 flex flex-col shadow-sm hover:shadow-md hover:border-indigo-300 transition-all group">
      <div className="flex justify-between items-start mb-3">
        <div className="flex gap-2 items-center">
          <div className={`w-2 h-2 rounded-full ${pColor}`} title={`Intent: ${intent}`} />
          <span className="text-xs font-bold text-slate-500">{ticket}</span>
        </div>
        <div className="flex items-center gap-2 bg-slate-50 px-2 py-1 rounded border border-slate-100">
          <div className="flex w-8 h-1.5 bg-slate-200 rounded-full overflow-hidden">
            <div className={`h-full ${isHighConf ? 'bg-emerald-500' : 'bg-blue-500'}`} style={{ width: `${confidence}%` }} />
          </div>
          <span className={`text-[11px] font-black ${isHighConf ? 'text-emerald-600' : 'text-blue-600'}`}>{confidence}%</span>
        </div>
      </div>
      <h4 className="text-sm font-bold text-slate-800 mb-1 leading-tight">{title}</h4>
      <p className="text-xs text-slate-500 font-medium leading-snug mb-3 h-8 overflow-hidden">{reason}</p>
      
      <div className="flex items-center gap-1.5 mb-4">
        <div className="h-1.5 w-1.5 rounded-full bg-indigo-400" />
        <span className="text-[11px] font-bold text-indigo-700 uppercase tracking-wider">{expectedImpact}</span>
      </div>

      <div className="flex justify-end items-center mt-auto pt-4 border-t border-slate-50">
        <div className="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
          <button className="px-3 py-1.5 rounded text-[10px] font-bold text-slate-500 hover:bg-slate-100 transition-colors uppercase tracking-wider">Dismiss</button>
          <button className="px-3 py-1.5 rounded text-[10px] font-bold bg-indigo-600 text-white hover:bg-indigo-700 transition-colors uppercase tracking-wider shadow-sm">Apply</button>
        </div>
      </div>
    </div>
  );
}

function QuickActionButton({ icon: Icon, label, onClick, highlight = false }: { icon: any, label: string, onClick: () => void, highlight?: boolean }) {
  return (
    <button onClick={onClick} className={`flex flex-col items-center justify-center p-4 rounded-xl border transition-all duration-200 group
      ${highlight ? 'bg-indigo-50 border-indigo-100 hover:bg-indigo-100 hover:border-indigo-200' : 'bg-slate-50 border-slate-100 hover:bg-slate-100 hover:border-slate-200'}`}>
      <Icon className={`w-5 h-5 mb-2 ${highlight ? 'text-indigo-600' : 'text-slate-500 group-hover:text-slate-700'} transition-colors`} />
      <span className={`text-[11px] font-bold uppercase tracking-wider ${highlight ? 'text-indigo-800' : 'text-slate-600'}`}>{label}</span>
    </button>
  );
}

function WorkloadLegend({ label, color, count, percent }: { label: string, color: string, count: string, percent: string }) {
  return (
    <div className="flex items-center gap-2">
      <div className={`w-2.5 h-2.5 rounded-sm ${color}`} />
      <div className="flex flex-col">
        <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider leading-none">{label}</span>
        <div className="flex items-baseline gap-1 mt-0.5">
          <span className="text-sm font-bold text-slate-700 leading-none">{count}</span>
          <span className="text-[10px] font-medium text-slate-400">({percent})</span>
        </div>
      </div>
    </div>
  );
}

function AssignmentRow({ ticket, priority, workflow, summary, sla }: { ticket: string, priority: string, workflow: string, summary: string, sla: string }) {
  const pColor = priority === 'CRITICAL' ? 'bg-red-500' : priority === 'HIGH' ? 'bg-orange-500' : priority === 'MEDIUM' ? 'bg-amber-400' : 'bg-blue-500';
  
  return (
    <tr className="hover:bg-slate-50 transition-colors group cursor-pointer">
      <td className="px-5 py-3 whitespace-nowrap">
        <span className="text-xs font-bold text-blue-600 group-hover:underline">{ticket}</span>
      </td>
      <td className="px-5 py-3 whitespace-nowrap">
        <div className="flex items-center gap-1.5">
          <div className={`w-2 h-2 rounded-full ${pColor}`} />
          <span className="text-[11px] font-bold text-slate-600">{priority}</span>
        </div>
      </td>
      <td className="px-5 py-3 whitespace-nowrap">
        <span className="text-[10px] uppercase font-bold tracking-wider bg-slate-100 text-slate-500 px-2 py-0.5 rounded border border-slate-200">
          {workflow}
        </span>
      </td>
      <td className="px-5 py-3">
        <span className="text-xs text-slate-600 font-medium line-clamp-1">{summary}</span>
      </td>
      <td className="px-5 py-3 whitespace-nowrap text-right">
        <span className="text-xs font-bold text-slate-800">{sla}</span>
      </td>
    </tr>
  );
}

function TimelineItem({ label, time, icon: Icon, color }: { label: string, time: string, icon: any, color: string }) {
  return (
    <div className="relative pl-6">
      <div className={`absolute -left-[13px] top-0 h-6 w-6 rounded-full border-2 flex items-center justify-center bg-white shadow-sm ring-4 ring-white ${color.split(' ')[2]}`}>
        <Icon className={`h-3 w-3 ${color.split(' ')[0]}`} />
      </div>
      <div className="flex flex-col pt-0.5">
        <span className="text-[13px] font-bold text-slate-700 leading-tight">{label}</span>
        <span className="text-[11px] font-medium text-slate-400 mt-0.5">{time}</span>
      </div>
    </div>
  );
}

function HealthRow({ label, latency, status }: { label: string, latency: string, status: 'green' | 'yellow' | 'red' }) {
  const color = status === 'green' ? 'bg-emerald-500' : status === 'yellow' ? 'bg-amber-400' : 'bg-red-500';
  const pulse = status !== 'green' ? 'animate-pulse' : '';
  
  return (
    <div className="flex items-center justify-between text-sm p-2 rounded hover:bg-slate-50 transition-colors">
      <div className="flex items-center gap-2">
        <div className={`w-2 h-2 rounded-full ${color} ${pulse}`} />
        <span className="text-slate-700 font-medium">{label}</span>
      </div>
      <span className="text-xs font-mono font-medium text-slate-400">{latency}</span>
    </div>
  );
}

function InsightBullet({ type, text }: { type: 'trend-up' | 'trend-down' | 'alert' | 'info', text: string }) {
  let Icon = TrendingUp;
  let color = "text-blue-500 bg-blue-50";
  
  if (type === 'trend-down') {
    Icon = TrendingDown;
    color = "text-emerald-500 bg-emerald-50";
  } else if (type === 'alert') {
    Icon = AlertTriangle;
    color = "text-orange-500 bg-orange-50";
  } else if (type === 'info') {
    Icon = Sparkles;
    color = "text-indigo-500 bg-indigo-50";
  }

  return (
    <div className="flex gap-3 items-start">
      <div className={`mt-0.5 h-6 w-6 rounded-full flex items-center justify-center shrink-0 ${color}`}>
        <Icon className="h-3 w-3" />
      </div>
      <span className="text-sm font-medium text-slate-700 leading-snug">{text}</span>
    </div>
  );
}
