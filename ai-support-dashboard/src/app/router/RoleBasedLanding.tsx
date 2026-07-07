import { Navigate } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";

export function RoleBasedLanding() {
  const { user } = useAuth();
  
  if (user?.role === "ROLE_ADMIN") {
    return <Navigate to="/dashboard" replace />;
  }
  
  return <Navigate to="/tickets" replace />;
}
