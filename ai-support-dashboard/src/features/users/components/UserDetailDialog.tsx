import type { User } from "@/shared/types/auth";
import { Avatar } from "@/components/common/Avatar";
import { UserStatusBadge } from "./UserStatusBadge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Shield, Mail, Calendar, Hash } from "lucide-react";

interface UserDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: User | null;
}

const ROLE_META: Record<string, { label: string; color: string }> = {
  ADMIN:    { label: "Admin",    color: "text-indigo-400 bg-indigo-500/10 border-indigo-500/20" },
  AGENT:    { label: "Agent",    color: "text-emerald-400 bg-emerald-500/10 border-emerald-500/20" },
  CUSTOMER: { label: "Customer", color: "text-amber-400 bg-amber-500/10 border-amber-500/20" },
};

export function UserDetailDialog({ open, onOpenChange, user }: UserDetailDialogProps) {
  if (!user) return null;

  const roleMeta = ROLE_META[user.role] ?? { label: user.role, color: "text-zinc-400 bg-zinc-800 border-zinc-700" };
  const displayName = user.fullName || user.email?.split("@")[0]?.replace(/[^a-zA-Z]/g, " ").replace(/\b\w/g, l => l.toUpperCase()) || "Unknown";
  const createdDate = user.createdAt
    ? new Date(user.createdAt).toLocaleDateString("en-US", { weekday: "long", year: "numeric", month: "long", day: "numeric" })
    : "—";

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md bg-zinc-950 border-zinc-800 text-zinc-100">
        <DialogHeader>
          <DialogTitle className="text-zinc-100">User Details</DialogTitle>
        </DialogHeader>

        <div className="flex items-center gap-4 py-2">
          <Avatar nameOrEmail={user?.email || undefined} size="lg" className="h-16 w-16" />
          <div>
            <p className="text-lg font-semibold text-zinc-100">{displayName}</p>
            <p className="text-sm text-zinc-400">{user?.email}</p>
          </div>
        </div>

        {/* Details Grid */}
        <div className="space-y-3 border-t border-zinc-800 pt-4">
          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-zinc-900 border border-zinc-800 flex items-center justify-center shrink-0">
              <Hash className="h-3.5 w-3.5 text-zinc-500" />
            </div>
            <div>
              <p className="text-xs text-zinc-500 font-medium uppercase tracking-wider">User ID</p>
              <p className="text-sm text-zinc-300 font-mono">#{user.id}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-zinc-900 border border-zinc-800 flex items-center justify-center shrink-0">
              <Mail className="h-3.5 w-3.5 text-zinc-500" />
            </div>
            <div>
              <p className="text-xs text-zinc-500 font-medium uppercase tracking-wider">Email</p>
              <p className="text-sm text-zinc-300">{user.email}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-zinc-900 border border-zinc-800 flex items-center justify-center shrink-0">
              <Shield className="h-3.5 w-3.5 text-zinc-500" />
            </div>
            <div>
              <p className="text-xs text-zinc-500 font-medium uppercase tracking-wider">Role</p>
              <span className={`inline-flex items-center text-xs font-semibold px-2.5 py-0.5 rounded-full border ${roleMeta.color}`}>
                {roleMeta.label}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-zinc-900 border border-zinc-800 flex items-center justify-center shrink-0">
              <Calendar className="h-3.5 w-3.5 text-zinc-500" />
            </div>
            <div>
              <p className="text-xs text-zinc-500 font-medium uppercase tracking-wider">Member Since</p>
              <p className="text-sm text-zinc-300">{createdDate}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-zinc-900 border border-zinc-800 flex items-center justify-center shrink-0">
              <div className={`h-2 w-2 rounded-full ${user.locked ? "bg-red-500" : "bg-emerald-500"}`} />
            </div>
            <div>
              <p className="text-xs text-zinc-500 font-medium uppercase tracking-wider">Account Status</p>
              <UserStatusBadge locked={user.locked} />
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
