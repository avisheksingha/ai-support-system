import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { userApi } from "../api/userApi";
import type { GetUsersParams } from "../api/userApi";
import { userKeys } from "./userKeys";
import type { Role, User } from "@/shared/types/auth";
import type { PageResponse } from "@/shared/types/api";

export function useUsersQuery(params: GetUsersParams) {
  return useQuery({
    queryKey: userKeys.list(params),
    queryFn: () => userApi.getUsers(params),
  });
}

/**
 * Patches a single user in-place across all cached list pages.
 * This avoids a full re-fetch (which would re-sort the list by updatedAt).
 */
function patchUserInCache(queryClient: ReturnType<typeof useQueryClient>, updatedUser: User) {
  queryClient.setQueriesData<PageResponse<User>>(
    { queryKey: userKeys.lists() },
    (oldData) => {
      if (!oldData) return oldData;
      return {
        ...oldData,
        content: oldData.content.map((u) => (u.id === updatedUser.id ? updatedUser : u)),
      };
    }
  );
}

export function useUpdateRoleMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, role }: { id: number; role: Role }) => userApi.updateUserRole(id, role),
    onSuccess: (updatedUser) => {
      // Patch the user in-place — preserves list order
      patchUserInCache(queryClient, updatedUser);
    },
  });
}

export function useLockUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => userApi.lockUser(id),
    onSuccess: (updatedUser) => {
      patchUserInCache(queryClient, updatedUser);
    },
  });
}

export function useUnlockUserMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => userApi.unlockUser(id),
    onSuccess: (updatedUser) => {
      patchUserInCache(queryClient, updatedUser);
    },
  });
}
