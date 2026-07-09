import { apiClient } from "@/lib/api-client";
import type { AuthResponse, LoginRequest, RegisterRequest, User } from "@/shared/types/auth";

export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>("/auth/login", data);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>("/auth/register", data);
    return response.data;
  },
  
  // The refresh is mostly handled by the interceptor, but we can expose it if manual refresh is needed
  refresh: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>("/auth/refresh", { refreshToken });
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post("/auth/logout");
  },

  getMe: async (): Promise<User> => {
    // The backend endpoint returns a UserResponse which usually maps identically to User in our case
    const response = await apiClient.get<User>("/auth/me");
    return response.data;
  }
};
