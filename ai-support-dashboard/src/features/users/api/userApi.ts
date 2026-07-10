import { apiClient } from "@/lib/api-client";
import type { User, Role } from "@/shared/types/auth";
import type { PageResponse } from "@/shared/types/api";

export interface GetUsersParams {
  page?: number;
  size?: number;
  search?: string;
  role?: string;
  status?: string;
  sort?: string;
}

export const userApi = {
  getUsers: async (params?: GetUsersParams): Promise<PageResponse<User>> => {
    // Backend currently supports page and size. We pass the rest anyway to be future-ready.
    const { data } = await apiClient.get<PageResponse<User>>("/auth/admin/users", { params });
    return data;
  },

  updateUserRole: async (id: number, role: Role): Promise<User> => {
    const { data } = await apiClient.patch<User>(`/auth/admin/users/${id}/role`, { role });
    return data;
  },

  lockUser: async (id: number): Promise<User> => {
    const { data } = await apiClient.post<User>(`/auth/admin/users/${id}/lock`);
    return data;
  },

  unlockUser: async (id: number): Promise<User> => {
    const { data } = await apiClient.post<User>(`/auth/admin/users/${id}/unlock`);
    return data;
  },
};
