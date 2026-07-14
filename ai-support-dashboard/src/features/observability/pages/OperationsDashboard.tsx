import { useState } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useOperationsOverview } from "../hooks/useObservability";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Activity, ShieldAlert, Cpu, Hammer, HeartPulse, Search, Info } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import {
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar
} from "recharts";
import { WorkflowExplorer } from "../components/WorkflowExplorer";

export const OperationsDashboard = () => {
  const { data, isLoading, error } = useOperationsOverview();
  const [activeTab, setActiveTab] = useState("overview");

  if (isLoading) {
    return <div className="p-8 flex items-center justify-center">Loading Operations Telemetry...</div>;
  }

  if (error || !data) {
    return (
      <div className="p-8">
        <Alert variant="destructive">
          <ShieldAlert className="h-4 w-4" />
          <AlertTitle>Telemetry Unavailable</AlertTitle>
          <AlertDescription>Could not connect to AI Orchestration Service observability endpoints.</AlertDescription>
        </Alert>
      </div>
    );
  }

  const { overview } = data;

  return (
    <div className="flex-1 space-y-4 p-4 md:p-8 pt-6 overflow-y-auto bg-slate-50 dark:bg-slate-900">
      <div className="flex items-center justify-between space-y-2">
        <h2 className="text-3xl font-bold tracking-tight">Operations Center</h2>
        <div className="flex items-center space-x-2">
          <div className="text-sm text-muted-foreground">
            Platform Status: <span className="text-emerald-500 font-medium">Operational</span>
          </div>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList className="bg-slate-200 dark:bg-slate-800">
          <TabsTrigger value="overview"><Activity className="w-4 h-4 mr-2" />Overview</TabsTrigger>
          <TabsTrigger value="ai"><Cpu className="w-4 h-4 mr-2" />AI Runtime</TabsTrigger>
          <TabsTrigger value="tools"><Hammer className="w-4 h-4 mr-2" />Tools</TabsTrigger>
          <TabsTrigger value="governance"><ShieldAlert className="w-4 h-4 mr-2" />Governance</TabsTrigger>
          <TabsTrigger value="health"><HeartPulse className="w-4 h-4 mr-2" />Health</TabsTrigger>
          <TabsTrigger value="explorer"><Search className="w-4 h-4 mr-2" />Workflow Explorer</TabsTrigger>
          <TabsTrigger value="system"><Info className="w-4 h-4 mr-2" />System Info</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Active Workflows</CardTitle>
                <Activity className="h-4 w-4 text-blue-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{overview.runtime.activeWorkflows}</div>
                <p className="text-xs text-muted-foreground">Currently executing</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Completed Today</CardTitle>
                <Activity className="h-4 w-4 text-emerald-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{overview.runtime.completedToday}</div>
                <p className="text-xs text-muted-foreground">Success rate ~{(overview.runtime.completedToday / (overview.runtime.completedToday + overview.runtime.failedToday || 1) * 100).toFixed(1)}%</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Avg Execution Time</CardTitle>
                <Activity className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{overview.runtime.averageDurationMs} ms</div>
                <p className="text-xs text-muted-foreground">Across all workflow types</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Policy Violations</CardTitle>
                <ShieldAlert className="h-4 w-4 text-red-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{overview.governance.policyViolations}</div>
                <p className="text-xs text-muted-foreground">Blocked in last 24h</p>
              </CardContent>
            </Card>
          </div>

          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
            <Card className="col-span-4">
              <CardHeader>
                <CardTitle>Hourly Throughput</CardTitle>
              </CardHeader>
              <CardContent className="pl-2">
                <div className="h-[300px] w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={overview.runtime.hourlyThroughput}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis dataKey="label" />
                      <YAxis />
                      <Tooltip />
                      <Bar dataKey="value" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </CardContent>
            </Card>
            <Card className="col-span-3">
              <CardHeader>
                <CardTitle>Recent Executions</CardTitle>
                <CardDescription>Top 10 latest orchestration runs</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {data.recentExecutions?.map((exec) => (
                    <div key={exec.workflowId} className="flex items-center justify-between border-b pb-2 last:border-0">
                      <div>
                        <p className="text-sm font-medium">Ticket #{exec.ticketId}</p>
                        <p className="text-xs text-muted-foreground">{exec.definitionId}</p>
                      </div>
                      <div className="text-right">
                        <span className={`text-xs px-2 py-1 rounded-full ${exec.state === "COMPLETED" ? "bg-emerald-100 text-emerald-700" : "bg-red-100 text-red-700"}`}>
                          {exec.state}
                        </span>
                        <p className="text-xs text-muted-foreground mt-1">{exec.durationMs} ms</p>
                      </div>
                    </div>
                  ))}
                  {(!data.recentExecutions || data.recentExecutions.length === 0) && (
                    <div className="text-sm text-muted-foreground text-center py-4">No recent executions found.</div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Other Tabs Content */}
        <TabsContent value="ai" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Avg Total Tokens</CardTitle>
                <Cpu className="h-4 w-4 text-violet-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{overview.ai.averageTotalTokens}</div>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Avg Prompt Tokens</CardTitle>
                <Cpu className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{overview.ai.averagePromptTokens}</div>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Avg Completion Tokens</CardTitle>
                <Cpu className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{overview.ai.averageCompletionTokens}</div>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Avg AI Latency</CardTitle>
                <Activity className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{overview.ai.averageLatencyMs} ms</div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="tools" className="space-y-4">
           <div className="grid gap-4 md:grid-cols-1 lg:grid-cols-2">
            {overview.tools?.map((tool) => (
              <Card key={tool.providerId}>
                <CardHeader>
                  <CardTitle className="text-lg">{tool.providerId}</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-sm font-medium">Invocations</p>
                      <p className="text-2xl font-bold">{tool.invocations}</p>
                    </div>
                    <div>
                      <p className="text-sm font-medium">Success Rate</p>
                      <p className="text-2xl font-bold text-emerald-500">
                        {tool.invocations ? ((tool.successes / tool.invocations) * 100).toFixed(0) : 0}%
                      </p>
                    </div>
                    <div>
                      <p className="text-sm font-medium">Failures</p>
                      <p className="text-2xl font-bold text-red-500">{tool.failures}</p>
                    </div>
                    <div>
                      <p className="text-sm font-medium">Avg Latency</p>
                      <p className="text-2xl font-bold">{tool.avgLatencyMs} ms</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
            {(!overview.tools || overview.tools.length === 0) && (
              <div className="text-muted-foreground p-4">No tool metrics available.</div>
            )}
           </div>
        </TabsContent>
        
        <TabsContent value="governance" className="space-y-4">
           <div className="grid gap-4 md:grid-cols-3">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Policy Violations</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-bold text-red-500">{overview.governance.policyViolations}</div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Guardrail Blocks</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-bold text-orange-500">{overview.governance.guardrailBlocks}</div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Approval Requests</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-bold text-amber-500">{overview.governance.approvalRequests}</div>
                </CardContent>
              </Card>
           </div>
        </TabsContent>

        <TabsContent value="health" className="space-y-4">
           <div className="grid gap-4 md:grid-cols-3">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Database Connected</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-xl font-bold text-emerald-500">{overview.health.databaseConnected ? "Online" : "Offline"}</div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Kafka Connected</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-xl font-bold text-emerald-500">{overview.health.kafkaConnected ? "Online" : "Offline"}</div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Circuit Breakers Open</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className={`text-3xl font-bold ${overview.health.circuitBreakersOpen > 0 ? 'text-red-500' : 'text-emerald-500'}`}>
                    {overview.health.circuitBreakersOpen}
                  </div>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Outbox Queue Delay</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl font-bold">{overview.health.avgQueueDelayMs} ms</div>
                  <div className="text-xs text-muted-foreground mt-1">Queue Size: {overview.health.outboxQueueSize}</div>
                </CardContent>
              </Card>
           </div>
        </TabsContent>

        <TabsContent value="system" className="space-y-4">
           <div className="grid gap-4 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Backend Version</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    <div className="flex justify-between border-b pb-2">
                      <span className="font-medium">Version</span>
                      <span>{overview.systemInfo.serviceVersion}</span>
                    </div>
                    <div className="flex justify-between border-b pb-2">
                      <span className="font-medium">Build</span>
                      <span>{overview.systemInfo.buildNumber}</span>
                    </div>
                    <div className="flex justify-between border-b pb-2">
                      <span className="font-medium">Commit</span>
                      <span className="font-mono text-xs bg-slate-100 p-1 rounded">{overview.systemInfo.gitCommit}</span>
                    </div>
                    <div className="flex justify-between pb-2">
                      <span className="font-medium">Environment</span>
                      <span className="uppercase">{overview.systemInfo.environment}</span>
                    </div>
                  </div>
                </CardContent>
              </Card>
           </div>
        </TabsContent>

        <TabsContent value="explorer" className="space-y-4">
           <WorkflowExplorer />
        </TabsContent>

      </Tabs>
    </div>
  );
};
