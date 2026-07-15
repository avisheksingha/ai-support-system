import React, { Suspense } from "react";
import { createBrowserRouter, Navigate, Outlet } from "react-router-dom";
import { AuthLayout } from "../layouts/AuthLayout";
import { DashboardLayout } from "../layouts/DashboardLayout";
import { AuthProvider } from "@/features/auth/hooks/useAuth";
import { ProtectedRoute } from "./guards/ProtectedRoute";
import { LoginSkeleton, DashboardSkeleton, WorkspaceSkeleton } from "./Loaders";
import { NotFound } from "@/components/common/NotFound";

// Lazy Loaded Routes
const LoginPage = React.lazy(() => import("@/features/auth/pages/LoginPage").then(module => ({ default: module.LoginPage })));
const SignupPage = React.lazy(() => import("@/features/auth/pages/SignupPage").then(module => ({ default: module.SignupPage })));
const TicketWorkspace = React.lazy(() => import("@/features/workspace/pages/TicketWorkspace").then(module => ({ default: module.TicketWorkspace })));
const DashboardOverview = React.lazy(() => import("@/features/dashboard/pages/DashboardOverview").then(module => ({ default: module.DashboardOverview })));
const SettingsPage = React.lazy(() => import("@/features/dashboard/pages/SettingsPage").then(module => ({ default: module.SettingsPage })));
const ProfilePage = React.lazy(() => import("@/features/dashboard/pages/ProfilePage").then(module => ({ default: module.ProfilePage })));
const UsersPage = React.lazy(() => import("@/features/users/pages/UsersPage").then(module => ({ default: module.UsersPage })));
const MyTicketsPage = React.lazy(() => import("@/features/customer/pages/MyTicketsPage").then(module => ({ default: module.MyTicketsPage })));
const CustomerTicketDetailPage = React.lazy(() => import("@/features/customer/pages/CustomerTicketDetailPage").then(module => ({ default: module.CustomerTicketDetailPage })));
const WorkflowExplorer = React.lazy(() => import("@/features/orchestration/pages/WorkflowExplorer").then(module => ({ default: module.WorkflowExplorer })));
const GovernanceDashboard = React.lazy(() => import("@/features/observability/pages/GovernanceDashboard").then(module => ({ default: module.GovernanceDashboard })));
import { RoleBasedLanding } from "./RoleBasedLanding";

// A root boundary that injects Auth context so it has access to routing hooks
const RootBoundary = () => (
  <AuthProvider>
    <Outlet />
  </AuthProvider>
);

export const router = createBrowserRouter([
  {
    element: <RootBoundary />,
    children: [
      {
        path: "/auth",
        element: <AuthLayout />,
        children: [
          {
            path: "login",
            element: (
              <Suspense fallback={<LoginSkeleton />}>
                <LoginPage />
              </Suspense>
            ),
          },
          {
            path: "signup",
            element: (
              <Suspense fallback={<LoginSkeleton />}>
                <SignupPage />
              </Suspense>
            ),
          },
          {
            path: "",
            element: <Navigate to="/auth/login" replace />,
          }
        ],
      },
      {
        path: "/",
        element: (
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        ),
        children: [
          {
            path: "tickets",
            element: (
              <Suspense fallback={<WorkspaceSkeleton />}>
                <TicketWorkspace />
              </Suspense>
            ),
          },
          {
            path: "dashboard",
            element: (
              <Suspense fallback={<DashboardSkeleton />}>
                <DashboardOverview />
              </Suspense>
            ),
          },
          {
            path: "settings",
            element: (
              <Suspense fallback={<DashboardSkeleton />}>
                <SettingsPage />
              </Suspense>
            ),
          },
          {
            path: "users",
            element: (
              <Suspense fallback={<DashboardSkeleton />}>
                <UsersPage />
              </Suspense>
            ),
          },
          {
            path: "profile",
            element: (
              <Suspense fallback={<DashboardSkeleton />}>
                <ProfilePage />
              </Suspense>
            ),
          },
          {
            path: "my-tickets",
            element: (
              <Suspense fallback={<DashboardSkeleton />}>
                <MyTicketsPage />
              </Suspense>
            ),
          },
          {
            path: "my-tickets/:ticketNumber",
            element: (
              <Suspense fallback={<DashboardSkeleton />}>
                <CustomerTicketDetailPage />
              </Suspense>
            ),
          },
          {
            path: "workflows",
            element: (
              <Suspense fallback={<DashboardSkeleton />}>
                <WorkflowExplorer />
              </Suspense>
            ),
          },
          {
            path: "governance",
            element: (
              <Suspense fallback={<DashboardSkeleton />}>
                <GovernanceDashboard />
              </Suspense>
            ),
          },
          {
            path: "",
            element: <RoleBasedLanding />,
          }
        ],
      },
      {
        path: "*",
        element: <NotFound />,
      }
    ]
  }
]);
