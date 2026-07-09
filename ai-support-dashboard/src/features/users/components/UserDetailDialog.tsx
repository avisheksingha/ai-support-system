import type { User } from "@/shared/types/auth";

import { UserStatusBadge } from "./UserStatusBadge";
import {
  Dialog,
  DialogContent,
} from "@/components/ui/dialog";
import { Shield, Mail, Calendar, Hash } from "lucide-react";

interface UserDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: User | null;
}

const ROLE_META: Record<string, { label: string; color: string }> = {
  ADMIN:    { label: "ADMIN", color: "text-blue-500 bg-blue-500/10 border-blue-500/20" },
  AGENT:    { label: "AGENT", color: "text-emerald-500 bg-emerald-500/10 border-emerald-500/20" },
  CUSTOMER: { label: "CUSTOMER", color: "text-amber-500 bg-amber-500/10 border-amber-500/20" },
};

export function UserDetailDialog({ open, onOpenChange, user }: UserDetailDialogProps) {
  if (!user) return null;

  const roleMeta = ROLE_META[user.role] ?? { label: user.role, color: "text-muted-foreground bg-muted border-border" };
  const displayName = user.fullName || user.email?.split("@")[0]?.replace(/[^a-zA-Z]/g, " ").replace(/\b\w/g, l => l.toUpperCase()) || "Unknown";
  const createdDate = user.createdAt
    ? new Date(user.createdAt).toLocaleDateString("en-US", { weekday: "long", year: "numeric", month: "long", day: "numeric" })
    : "—";

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md bg-background border-border text-foreground p-0 overflow-hidden gap-0">
        
        {/* Cover banner */}
        <div className="h-24 bg-gradient-to-r from-[#0C66E4]/20 via-[#0C66E4]/10 to-transparent relative w-full">
          <div className="absolute inset-0 opacity-30"
            style={{ backgroundImage: "radial-gradient(circle, #0C66E4 1px, transparent 1px)", backgroundSize: "20px 20px" }}
          />
        </div>

        <div className="px-6 pb-6 pt-0">
          <div className="flex items-end gap-4 -mt-8 mb-5">
            <div className="h-16 w-16 rounded-full bg-[#0C66E4] flex items-center justify-center shadow-sm ring-4 ring-background text-white text-xl font-bold shrink-0">
              {displayName.split(" ").map(w => w[0]).slice(0, 2).join("").toUpperCase()}
            </div>
            <div className="pb-1">
              <h2 className="text-xl font-bold text-foreground leading-none mb-1">{displayName}</h2>
              <p className="text-sm text-muted-foreground">{user?.email}</p>
            </div>
          </div>

        {/* Details Grid */}
        <div className="space-y-3 border-t border-border pt-4">
          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-card border border-border flex items-center justify-center shrink-0">
              <Hash className="h-3.5 w-3.5 text-muted-foreground" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">User ID</p>
              <p className="text-sm text-foreground font-mono">#{user.id}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-card border border-border flex items-center justify-center shrink-0">
              <Mail className="h-3.5 w-3.5 text-muted-foreground" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Email</p>
              <p className="text-sm text-foreground">{user.email}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-card border border-border flex items-center justify-center shrink-0">
              <Shield className="h-3.5 w-3.5 text-muted-foreground" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider mb-1">Role</p>
              <span className={`inline-flex items-center text-xs font-semibold px-2.5 py-0.5 rounded border ${roleMeta.color}`}>
                {roleMeta.label}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-card border border-border flex items-center justify-center shrink-0">
              <Calendar className="h-3.5 w-3.5 text-muted-foreground" />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Member Since</p>
              <p className="text-sm text-foreground">{createdDate}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-card border border-border flex items-center justify-center shrink-0">
              <div className={`h-2 w-2 rounded-full ${user.locked ? "bg-red-500" : "bg-emerald-500"}`} />
            </div>
            <div>
              <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Account Status</p>
              <UserStatusBadge locked={user.locked} />
            </div>
          </div>
        </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
