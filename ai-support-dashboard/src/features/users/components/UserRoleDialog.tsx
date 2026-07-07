import { useState } from "react";
import type { Role } from "@/shared/types/auth";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface UserRoleDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  currentRole: Role;
  onSave: (newRole: Role) => void;
  isLoading?: boolean;
}

export function UserRoleDialog({
  open,
  onOpenChange,
  currentRole,
  onSave,
  isLoading,
}: UserRoleDialogProps) {
  const [selectedRole, setSelectedRole] = useState<Role>(currentRole);

  const handleSave = () => {
    if (selectedRole !== currentRole) {
      onSave(selectedRole);
    } else {
      onOpenChange(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px] bg-zinc-950 border-zinc-800 text-zinc-100">
        <DialogHeader>
          <DialogTitle>Edit User Role</DialogTitle>
          <DialogDescription className="text-zinc-400">
            Change the permission level for this user. This determines what features they can access.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <Select value={selectedRole} onValueChange={(val) => setSelectedRole(val as Role)}>
            <SelectTrigger className="w-full bg-zinc-900 border-zinc-800">
              <SelectValue placeholder="Select a role" />
            </SelectTrigger>
            <SelectContent className="bg-zinc-900 border-zinc-800 text-zinc-100">
              <SelectItem value="ADMIN">Admin (Full Access)</SelectItem>
              <SelectItem value="AGENT">Agent (Workspace & Tickets)</SelectItem>
              <SelectItem value="CUSTOMER">Customer (Portal Only)</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            className="border-zinc-800 hover:bg-zinc-800 hover:text-zinc-100"
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button 
            onClick={handleSave} 
            disabled={selectedRole === currentRole || isLoading}
            className="bg-indigo-600 text-white hover:bg-indigo-500"
          >
            {isLoading ? "Saving..." : "Save changes"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
