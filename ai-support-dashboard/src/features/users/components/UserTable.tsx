import { useState } from "react";
import type { User, Role } from "@/shared/types/auth";
import { Avatar } from "@/components/common/Avatar";
import { UserStatusBadge } from "./UserStatusBadge";
import { UserActionsMenu } from "./UserActionsMenu";
import { UserRoleDialog } from "./UserRoleDialog";
import { LockUserDialog } from "./LockUserDialog";
import { UserDetailDialog } from "./UserDetailDialog";
import { useUpdateRoleMutation, useLockUserMutation, useUnlockUserMutation } from "../hooks/useUsers";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

interface UserTableProps {
  users: User[];
}

export function UserTable({ users }: UserTableProps) {
  const [editingRoleUser, setEditingRoleUser] = useState<User | null>(null);
  const [lockingUser, setLockingUser] = useState<User | null>(null);
  const [viewingUser, setViewingUser] = useState<User | null>(null);

  const updateRoleMutation = useUpdateRoleMutation();
  const lockMutation = useLockUserMutation();
  const unlockMutation = useUnlockUserMutation();

  const handleSaveRole = (newRole: Role) => {
    if (editingRoleUser) {
      updateRoleMutation.mutate(
        { id: editingRoleUser.id, role: newRole },
        {
          onSuccess: () => setEditingRoleUser(null),
        }
      );
    }
  };

  const handleConfirmLockToggle = () => {
    if (!lockingUser) return;
    
    const mutation = lockingUser.locked ? unlockMutation : lockMutation;
    
    mutation.mutate(lockingUser.id, {
      onSuccess: () => setLockingUser(null),
    });
  };

  return (
    <div className="rounded-xl border border-zinc-800 bg-zinc-950 overflow-hidden shadow-lg shadow-black/20">
      <div className="overflow-x-auto">
        <Table>
          <TableHeader className="bg-zinc-900/50">
            <TableRow className="border-zinc-800 hover:bg-transparent">
              <TableHead className="text-zinc-400 font-medium w-16">User</TableHead>
              <TableHead className="text-zinc-400 font-medium">Name & Email</TableHead>
              <TableHead className="text-zinc-400 font-medium">Role</TableHead>
              <TableHead className="text-zinc-400 font-medium">Status</TableHead>
              <TableHead className="text-zinc-400 font-medium">Created At</TableHead>
              <TableHead className="text-zinc-400 font-medium w-20 text-center">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.length === 0 ? (
              <TableRow className="border-zinc-800 hover:bg-zinc-900/30">
                <TableCell colSpan={6} className="h-24 text-center text-zinc-500">
                  No users found matching the criteria.
                </TableCell>
              </TableRow>
            ) : (
              users.map((user) => (
                <TableRow key={user.id} className="border-zinc-800 hover:bg-zinc-900/50 transition-colors">
                  <TableCell className="py-3">
                    <Avatar nameOrEmail={user.email} size="sm" />
                  </TableCell>
                  <TableCell className="py-3">
                    <div className="flex flex-col">
                      <span className="font-medium text-zinc-200">
                        {user.email?.split('@')[0].replace(/[^a-zA-Z]/g, ' ').replace(/\b\w/g, l => l.toUpperCase()) || 'System User'}
                      </span>
                      <span className="text-xs text-zinc-500">{user.email}</span>
                    </div>
                  </TableCell>
                  <TableCell className="py-3">
                    <span className="text-sm text-zinc-300 font-medium bg-zinc-800/50 px-2.5 py-1 rounded-md border border-zinc-800">
                      {user.role}
                    </span>
                  </TableCell>
                  <TableCell className="py-3">
                    <UserStatusBadge locked={user.locked} />
                  </TableCell>
                  <TableCell className="py-3">
                    {user.createdAt ? (
                      <span className="text-sm text-zinc-400">
                        {new Date(user.createdAt).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })}
                      </span>
                    ) : (
                      <span className="text-sm text-zinc-600">—</span>
                    )}
                  </TableCell>
                  <TableCell className="py-3 text-center">
                    <UserActionsMenu
                      user={user}
                      onEditRole={() => setEditingRoleUser(user)}
                      onToggleLock={() => setLockingUser(user)}
                      onViewDetails={() => setViewingUser(user)}
                    />
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Dialogs */}
      {editingRoleUser && (
        <UserRoleDialog
          open={!!editingRoleUser}
          onOpenChange={(open) => !open && setEditingRoleUser(null)}
          currentRole={editingRoleUser.role}
          onSave={handleSaveRole}
          isLoading={updateRoleMutation.isPending}
        />
      )}

      {lockingUser && (
        <LockUserDialog
          open={!!lockingUser}
          onOpenChange={(open) => !open && setLockingUser(null)}
          user={lockingUser}
          onConfirm={handleConfirmLockToggle}
          isLoading={lockMutation.isPending || unlockMutation.isPending}
        />
      )}

      <UserDetailDialog
        open={!!viewingUser}
        onOpenChange={(open) => !open && setViewingUser(null)}
        user={viewingUser}
      />
    </div>
  );
}
