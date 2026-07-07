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
const TicketWorkspace = React.lazy(() => import("@/features/workspace/pages/TicketWorkspace").then(module => ({ default: module.TicketWorkspace })));
const DashboardOverview = React.lazy(() => import("@/features/dashboard/pages/DashboardOverview").then(module => ({ default: module.DashboardOverview })));
const SettingsPage = React.lazy(() => import("@/features/dashboard/pages/SettingsPage").then(module => ({ default: module.SettingsPage })));
const ProfilePage = React.lazy(() => import("@/features/dashboard/pages/ProfilePage").then(module => ({ default: module.ProfilePage })));
const UsersPage = React.lazy(() => import("@/features/users/pages/UsersPage").then(module => ({ default: module.UsersPage })));
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
