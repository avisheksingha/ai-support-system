import { Navigate } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import type { User } from "@/shared/types/auth";

interface RoleGuardProps {
  children: React.ReactNode;
  permissionCheck: (user: User | null) => boolean;
  fallbackRoute?: string;
}

export function RoleGuard({ children, permissionCheck, fallbackRoute = "/dashboard" }: RoleGuardProps) {
  const { user } = useAuth();

  if (!permissionCheck(user)) {
    return <Navigate to={fallbackRoute} replace />;
  }

  return <>{children}</>;
}
