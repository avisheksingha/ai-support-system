import { useState } from "react";
import type { User, Role } from "@/shared/types/auth";
import { Avatar } from "@/components/common/Avatar";
import { UserStatusBadge } from "./UserStatusBadge";
import { UserActionsMenu } from "./UserActionsMenu";
import { UserRoleDialog } from "./UserRoleDialog";
import { LockUserDialog } from "./LockUserDialog";
import { UserDetailDialog } from "./UserDetailDialog";
import { formatDateStr } from "@/shared/utils/date";
import { useUpdateRoleMutation, useLockUserMutation, useUnlockUserMutation } from "../hooks/useUsers";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

const ROLE_META: Record<string, { label: string; color: string }> = {
  ADMIN:    { label: "ADMIN", color: "text-blue-500 bg-blue-500/10 border-blue-500/20" },
  AGENT:    { label: "AGENT", color: "text-emerald-500 bg-emerald-500/10 border-emerald-500/20" },
  CUSTOMER: { label: "CUSTOMER", color: "text-amber-500 bg-amber-500/10 border-amber-500/20" },
};

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
    <div className="rounded-xl border border-border bg-background overflow-hidden shadow-sm">
      <div className="overflow-x-auto">
        <Table>
          <TableHeader className="bg-muted/30">
            <TableRow className="border-border hover:bg-transparent">
              <TableHead className="text-muted-foreground font-medium w-16">User</TableHead>
              <TableHead className="text-muted-foreground font-medium">Name & Email</TableHead>
              <TableHead className="text-muted-foreground font-medium">Role</TableHead>
              <TableHead className="text-muted-foreground font-medium">Status</TableHead>
              <TableHead className="text-muted-foreground font-medium">Member Since</TableHead>
              <TableHead className="text-muted-foreground font-medium w-20 text-center">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.length === 0 ? (
              <TableRow className="border-border hover:bg-card">
                <TableCell colSpan={6} className="h-24 text-center text-muted-foreground">
                  No users found matching the criteria.
                </TableCell>
              </TableRow>
            ) : (
              users.map((user) => (
                <TableRow key={user.id} className="border-border hover:bg-card transition-colors">
                  <TableCell className="py-3">
                    <Avatar nameOrEmail={user.fullName || user.email || undefined} size="sm" />
                  </TableCell>
                  <TableCell className="py-3">
                    <div className="flex flex-col">
                      <span className="font-medium text-foreground">
                        {user.fullName || 'System User'}
                      </span>
                      <span className="text-xs text-muted-foreground">{user.email}</span>
                    </div>
                  </TableCell>
                  <TableCell className="py-3">
                    <span className={`text-xs font-semibold px-2.5 py-1 rounded border ${ROLE_META[user.role]?.color || "text-muted-foreground bg-muted border-border"}`}>
                      {ROLE_META[user.role]?.label || user.role}
                    </span>
                  </TableCell>
                  <TableCell className="py-3">
                    <UserStatusBadge locked={user.locked} />
                  </TableCell>
                  <TableCell className="py-3">
                    {user.createdAt ? (
                      <span className="text-sm text-muted-foreground">
                        {formatDateStr(user.createdAt)}
                      </span>
                    ) : (
                      <span className="text-sm text-muted-foreground">—</span>
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
          user={editingRoleUser}
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
