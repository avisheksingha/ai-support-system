import type { User } from "@/shared/types/auth";
import {
  Dialog,
  DialogContent,
} from "@/components/ui/dialog";
import { Shield, Mail, Calendar, Hash, Activity } from "lucide-react";
import { formatLongDateStr } from "@/shared/utils/date";

interface UserDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: User | null;
}

const ROLE_META: Record<string, { label: string; color: string }> = {
  ADMIN:    { label: "Administrator",  color: "text-blue-400 bg-blue-500/10 border-blue-500/20" },
  AGENT:    { label: "Support Agent",  color: "text-emerald-400 bg-emerald-500/10 border-emerald-500/20" },
  CUSTOMER: { label: "Customer",       color: "text-amber-400 bg-amber-500/10 border-amber-500/20" },
};

export function UserDetailDialog({ open, onOpenChange, user }: UserDetailDialogProps) {
  if (!user) return null;

  const roleMeta = ROLE_META[user.role] ?? { label: user.role, color: "text-muted-foreground bg-muted border-border" };
  const displayName = user.fullName || "Unknown";
  const initials = displayName.split(" ").map(w => w[0]).slice(0, 2).join("").toUpperCase();
  const createdDate = user.createdAt
    ? formatLongDateStr(user.createdAt)
    : "—";

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md bg-card border-border text-foreground p-0 overflow-hidden gap-0">

        {/* Cover banner — same as Profile page */}
        <div className="h-24 bg-gradient-to-r from-[#0C66E4]/20 via-[#0C66E4]/10 to-transparent relative">
          <div className="absolute inset-0 opacity-30"
            style={{ backgroundImage: "radial-gradient(circle, #0C66E4 1px, transparent 1px)", backgroundSize: "20px 20px" }}
          />
        </div>

        {/* Avatar + name + role badge — same as Profile page */}
        <div className="px-6 pb-6">
          <div className="flex items-end gap-4 -mt-10 mb-5">
            <div className="h-20 w-20 rounded-full bg-[#0C66E4] flex items-center justify-center shadow-md ring-4 ring-card text-white text-2xl font-bold shrink-0">
              {initials}
            </div>
            <div className="pb-1">
              <h2 className="text-lg font-semibold text-foreground">{displayName}</h2>
              <span className={`inline-flex items-center text-xs font-semibold px-2.5 py-0.5 rounded-full border mt-1 ${roleMeta.color}`}>
                {roleMeta.label}
              </span>
            </div>
          </div>

          {/* Details grid — same DetailRow pattern as Profile page */}
          <div className="space-y-3 border-t border-border pt-5">
            <DetailRow icon={Hash} label="User ID" value={`#${user.id}`} mono />
            <DetailRow icon={Mail} label="Email Address" value={user.email} />
            <DetailRow icon={Shield} label="Access Role" value={roleMeta.label} />
            <DetailRow icon={Calendar} label="Member Since" value={createdDate} />
            <DetailRow icon={Activity} label="Account Status" value={user.locked ? "Locked" : "Active"} />
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

function DetailRow({ icon: Icon, label, value, mono }: {
  icon: React.ElementType;
  label: string;
  value: string;
  mono?: boolean;
}) {
  return (
    <div className="flex items-center gap-3 py-2">
      <div className="h-8 w-8 rounded-lg bg-card border border-border flex items-center justify-center shrink-0">
        <Icon className="h-3.5 w-3.5 text-muted-foreground" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-[10px] font-semibold text-muted-foreground uppercase tracking-widest">{label}</p>
        <p className={`text-sm text-foreground mt-0.5 truncate ${mono ? "font-mono" : ""}`}>{value}</p>
      </div>
    </div>
  );
}
