import { useAuth } from "@/features/auth/hooks/useAuth";
import { Mail, Shield, Hash, Calendar, Info } from "lucide-react";

const ROLE_META: Record<string, { label: string; color: string }> = {
  ADMIN:    { label: "Administrator", color: "text-indigo-400 bg-indigo-500/10 border-indigo-500/20" },
  AGENT:    { label: "Support Agent", color: "text-emerald-400 bg-emerald-500/10 border-emerald-500/20" },
  CUSTOMER: { label: "Customer",      color: "text-amber-400 bg-amber-500/10 border-amber-500/20" },
};

export function ProfilePage() {
  const { user } = useAuth();

  const emailStr = user?.email ?? "";
  const displayName = user?.fullName
    || (emailStr.split("@")[0] || "").replace(/[^a-zA-Z]/g, " ").replace(/\b\w/g, (l) => l.toUpperCase())
    || "System User";
  const roleMeta = ROLE_META[user?.role ?? ""] ?? { label: user?.role ?? "—", color: "text-zinc-400 bg-zinc-800 border-zinc-700" };
  const initials = displayName.split(" ").map((w) => w[0]).slice(0, 2).join("").toUpperCase();

  return (
    <div className="max-w-2xl space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-zinc-100">My Profile</h1>
        <p className="text-sm text-zinc-500 mt-1">Your account details and access level.</p>
      </div>

      {/* Profile card */}
      <div className="rounded-xl border border-zinc-800 bg-zinc-900/40 overflow-hidden">

        {/* Cover banner */}
        <div className="h-24 bg-gradient-to-r from-indigo-900/50 via-purple-900/40 to-zinc-900/50 relative">
          <div className="absolute inset-0 opacity-20"
            style={{ backgroundImage: "radial-gradient(circle, #a5b4fc 1px, transparent 1px)", backgroundSize: "20px 20px" }}
          />
        </div>

        {/* Avatar + name */}
        <div className="px-6 pb-6">
          <div className="flex items-end gap-4 -mt-10 mb-5">
            <div className="h-20 w-20 rounded-2xl bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center shadow-xl shadow-indigo-500/20 ring-4 ring-zinc-900 text-white text-xl font-bold">
              {initials}
            </div>
            <div className="pb-1">
              <h2 className="text-lg font-semibold text-zinc-100">{displayName}</h2>
              <span className={`inline-flex items-center text-xs font-semibold px-2.5 py-0.5 rounded-full border mt-1 ${roleMeta.color}`}>
                {roleMeta.label}
              </span>
            </div>
          </div>

          {/* Details grid */}
          <div className="space-y-3 border-t border-zinc-800 pt-5">
            <DetailRow icon={Hash} label="User ID" value={`#${user?.id ?? "—"}`} mono />
            <DetailRow icon={Mail} label="Email Address" value={user?.email ?? "—"} />
            <DetailRow icon={Shield} label="Access Role" value={roleMeta.label} />
            <DetailRow icon={Calendar} label="Member Since" value={
              user?.createdAt
                ? new Date(user.createdAt).toLocaleDateString("en-US", { year: "numeric", month: "long", day: "numeric" })
                : "—"
            } />
          </div>
        </div>
      </div>

      {/* Info banner */}
      <div className="flex items-start gap-3 p-4 rounded-xl bg-indigo-500/8 border border-indigo-500/20 text-indigo-300">
        <Info className="h-4 w-4 mt-0.5 shrink-0" />
        <p className="text-sm leading-relaxed">
          Profile editing and password management will be available in a future update.
          Contact your administrator to make changes.
        </p>
      </div>
    </div>
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
      <div className="h-8 w-8 rounded-lg bg-zinc-900 border border-zinc-800 flex items-center justify-center shrink-0">
        <Icon className="h-3.5 w-3.5 text-zinc-500" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-[10px] font-semibold text-zinc-600 uppercase tracking-widest">{label}</p>
        <p className={`text-sm text-zinc-300 mt-0.5 truncate ${mono ? "font-mono" : ""}`}>{value}</p>
      </div>
    </div>
  );
}
