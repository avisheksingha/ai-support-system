import { createContext, useContext, useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { authApi } from "../api/authApi";
import type { User, LoginRequest } from "@/shared/types/auth";
import { defaultTokenManager } from "@/lib/token-manager";
import { useNavigate } from "react-router-dom";

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isInitializing, setIsInitializing] = useState(true);
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  // The single source of truth for the user object
  const { data: user, isLoading: isUserLoading, refetch } = useQuery({
    queryKey: ["currentUser"],
    queryFn: authApi.getMe,
    enabled: false, // Don't fetch automatically until we verify token exists
    retry: false,
  });

  useEffect(() => {
    const initializeAuth = async () => {
      const accessToken = defaultTokenManager.getAccessToken();
      const refreshToken = defaultTokenManager.getRefreshToken();
      
      if (accessToken || refreshToken) {
        try {
          await refetch();
        } catch {
          // If refetch fails (and our interceptor fails to refresh), clear tokens
          defaultTokenManager.clear();
        }
      }
      setIsInitializing(false);
    };
    initializeAuth();

    // Listen for unauthorized events emitted by interceptor
    const handleUnauthorized = () => {
      queryClient.setQueryData(["currentUser"], null);
      navigate("/auth/login");
    };

    window.addEventListener("auth:unauthorized", handleUnauthorized);
    return () => window.removeEventListener("auth:unauthorized", handleUnauthorized);
  }, [refetch, navigate, queryClient]);

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: async (data) => {
      defaultTokenManager.setAccessToken(data.accessToken);
      defaultTokenManager.setRefreshToken(data.refreshToken);
      // Fetch the user data using the new token before navigating
      await refetch();
      navigate("/dashboard");
    },
  });

  const logoutMutation = useMutation({
    mutationFn: authApi.logout,
    onSettled: () => {
      defaultTokenManager.clear();
      queryClient.setQueryData(["currentUser"], null);
      navigate("/auth/login");
    },
  });

  const value: AuthState = {
    user: user || null,
    isAuthenticated: !!user,
    isLoading: isInitializing || isUserLoading || loginMutation.isPending || logoutMutation.isPending,
    login: async (data: LoginRequest) => {
      await loginMutation.mutateAsync(data);
    },
    logout: () => logoutMutation.mutate(),
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
