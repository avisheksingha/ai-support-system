import { Navigate } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";

export function RoleBasedLanding() {
  const { user } = useAuth();
  
  if (user?.role === "ADMIN") {
    return <Navigate to="/dashboard" replace />;
  }

  if (user?.role === "CUSTOMER") {
    return <Navigate to="/my-tickets" replace />;
  }
  
  return <Navigate to="/tickets" replace />;
}
