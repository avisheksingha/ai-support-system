import type { User } from "@/shared/types/auth";

export const canManageUsers = (user: User | null): boolean => {
  return user?.role === "ADMIN";
};

export const canViewDashboard = (user: User | null): boolean => {
  return !!user && (user.role === "ADMIN" || user.role === "AGENT");
};

export const canManageTickets = (user: User | null): boolean => {
  return !!user && (user.role === "ADMIN" || user.role === "AGENT");
};

export const canViewOwnTickets = (user: User | null): boolean => {
  return !!user && user.role === "CUSTOMER";
};

export const canCreateTicket = (user: User | null): boolean => {
  return !!user && user.role === "CUSTOMER";
};
