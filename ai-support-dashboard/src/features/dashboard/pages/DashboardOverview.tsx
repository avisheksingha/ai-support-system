import { useAuth } from "@/features/auth/hooks/useAuth";
import { CustomerDashboardOverview } from "./CustomerDashboardOverview";
import { AgentDashboardOverview } from "./AgentDashboardOverview";
import { AdminDashboardOverview } from "./AdminDashboardOverview";

export function DashboardOverview() {
  const { user } = useAuth();
  
  if (user?.role === "CUSTOMER") {
    return <CustomerDashboardOverview />;
  }
  
  if (user?.role === "AGENT") {
    return <AgentDashboardOverview />;
  }
  
  return <AdminDashboardOverview />;
}
