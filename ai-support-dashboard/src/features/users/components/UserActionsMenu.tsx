import { useState } from "react";
import { MoreHorizontal, Shield, Lock, Unlock, Eye } from "lucide-react";
import type { User } from "@/shared/types/auth";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useAuth } from "@/features/auth/hooks/useAuth";

interface UserActionsMenuProps {
  user: User;
  onEditRole: () => void;
  onToggleLock: () => void;
  onViewDetails: () => void;
}

export function UserActionsMenu({ user, onEditRole, onToggleLock, onViewDetails }: UserActionsMenuProps) {
  const { user: currentUser } = useAuth();
  const [open, setOpen] = useState(false);

  // Self protection: Cannot lock yourself or change your own role
  const isSelf = currentUser?.email === user.email;

  return (
    <DropdownMenu open={open} onOpenChange={setOpen}>
      <DropdownMenuTrigger className="h-8 w-8 p-0 text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800 rounded-md inline-flex items-center justify-center outline-none border-none">
        <span className="sr-only">Open menu</span>
        <MoreHorizontal className="h-4 w-4" />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-48 bg-zinc-950 border-zinc-800 text-zinc-200">
        <DropdownMenuItem
          onClick={onViewDetails}
          className="focus:bg-zinc-900 cursor-pointer"
        >
          <Eye className="mr-2 h-4 w-4" />
          <span>View Details</span>
        </DropdownMenuItem>

        <DropdownMenuItem
          onClick={onEditRole}
          disabled={isSelf}
          className="focus:bg-zinc-900 cursor-pointer"
        >
          <Shield className="mr-2 h-4 w-4" />
          <span>Edit Role</span>
        </DropdownMenuItem>

        <DropdownMenuSeparator className="bg-zinc-800" />

        <DropdownMenuItem
          onClick={onToggleLock}
          disabled={isSelf}
          className={
            user.locked
              ? "text-emerald-400 focus:bg-emerald-500/10 focus:text-emerald-400 cursor-pointer"
              : "text-red-400 focus:bg-red-500/10 focus:text-red-400 cursor-pointer"
          }
        >
          {user.locked
            ? <Unlock className="mr-2 h-4 w-4" />
            : <Lock className="mr-2 h-4 w-4" />
          }
          <span>{user.locked ? "Unlock Account" : "Lock Account"}</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
