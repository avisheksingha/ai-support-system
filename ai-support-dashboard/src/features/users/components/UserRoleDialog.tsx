import { useState } from "react";
import type { Role, User } from "@/shared/types/auth";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Shield } from "lucide-react";

interface UserRoleDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: User | null;
  currentRole: Role;
  onSave: (newRole: Role) => void;
  isLoading?: boolean;
}

const ROLE_META: Record<string, { label: string; color: string }> = {
  ADMIN:    { label: "Administrator",  color: "text-blue-400 bg-blue-500/10 border-blue-500/20" },
  AGENT:    { label: "Support Agent",  color: "text-emerald-400 bg-emerald-500/10 border-emerald-500/20" },
  CUSTOMER: { label: "Customer",       color: "text-amber-400 bg-amber-500/10 border-amber-500/20" },
};

export function UserRoleDialog({
  open,
  onOpenChange,
  user,
  currentRole,
  onSave,
  isLoading,
}: UserRoleDialogProps) {
  const [selectedRole, setSelectedRole] = useState<Role>(currentRole);

  if (!user) return null;

  const roleMeta = ROLE_META[user.role] ?? { label: user.role, color: "text-muted-foreground bg-muted border-border" };
  const displayName = user.fullName || "Unknown";
  const initials = displayName.split(" ").map(w => w[0]).slice(0, 2).join("").toUpperCase();

  const handleSave = () => {
    if (selectedRole !== currentRole) {
      onSave(selectedRole);
    } else {
      onOpenChange(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md bg-card border-border text-foreground p-0 overflow-hidden gap-0">
        
        {/* Cover banner */}
        <div className="h-24 bg-gradient-to-r from-[#0C66E4]/20 via-[#0C66E4]/10 to-transparent relative">
          <div className="absolute inset-0 opacity-30"
            style={{ backgroundImage: "radial-gradient(circle, #0C66E4 1px, transparent 1px)", backgroundSize: "20px 20px" }}
          />
        </div>

        {/* Avatar + name */}
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

          <div className="space-y-4 border-t border-border pt-5">
            <div>
              <h3 className="text-sm font-semibold text-foreground mb-1 flex items-center gap-2">
                <Shield className="h-4 w-4 text-[#0C66E4]" />
                Edit User Role
              </h3>
              <p className="text-xs text-muted-foreground mb-4">
                Change the permission level for this user. This determines what features they can access.
              </p>

              <Select value={selectedRole} onValueChange={(val) => setSelectedRole(val as Role)}>
                <SelectTrigger className="w-full bg-card border-border">
                  <SelectValue placeholder="Select a role" />
                </SelectTrigger>
                <SelectContent className="bg-card border-border text-foreground">
                  <SelectItem value="ADMIN">Admin (Full Access)</SelectItem>
                  <SelectItem value="AGENT">Agent (Workspace & Tickets)</SelectItem>
                  <SelectItem value="CUSTOMER">Customer (Portal Only)</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex justify-end gap-2 pt-4">
              <Button
                variant="outline"
                onClick={() => onOpenChange(false)}
                className="border-border hover:bg-muted hover:text-foreground shadow-sm"
                disabled={isLoading}
              >
                Cancel
              </Button>
              <Button 
                onClick={handleSave} 
                disabled={selectedRole === currentRole || isLoading}
                className="shadow-sm min-w-[120px]"
              >
                {isLoading ? "Saving..." : "Save Changes"}
              </Button>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
