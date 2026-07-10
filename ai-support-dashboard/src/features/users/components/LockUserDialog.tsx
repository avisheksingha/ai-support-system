import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import type { User } from "@/shared/types/auth";
import { Lock, Unlock } from "lucide-react";

interface LockUserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: User;
  onConfirm: () => void;
  isLoading?: boolean;
}

export function LockUserDialog({
  open,
  onOpenChange,
  user,
  onConfirm,
  isLoading,
}: LockUserDialogProps) {
  const isLocking = !user.locked;
  
  if (!user) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px] bg-background border-border text-foreground">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            {isLocking ? (
              <Lock className="h-5 w-5 text-red-500" />
            ) : (
              <Unlock className="h-5 w-5 text-emerald-500" />
            )}
            {isLocking ? "Lock User Account?" : "Unlock User Account?"}
          </DialogTitle>
          <DialogDescription className="text-muted-foreground pt-2">
            {isLocking 
              ? `Are you sure you want to lock ${user.email}? They will immediately lose access to the system and will not be able to log in.`
              : `Are you sure you want to unlock ${user.email}? Their access will be restored immediately.`}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter className="pt-4">
          <Button 
            variant="outline" 
            onClick={() => onOpenChange(false)} 
            disabled={isLoading} 
            className="border-border hover:bg-muted hover:text-foreground shadow-sm"
          >
            Cancel
          </Button>
          <Button 
            onClick={(e) => {
              e.preventDefault();
              onConfirm();
            }}
            disabled={isLoading}
            className={isLocking ? "bg-red-600 text-white hover:bg-red-500 shadow-sm min-w-[120px]" : "bg-emerald-600 text-white hover:bg-emerald-500 shadow-sm min-w-[120px]"}
          >
            {isLoading ? "Processing..." : isLocking ? "Lock Account" : "Unlock Account"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
