import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import type { User } from "@/shared/types/auth";

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
  
  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent className="bg-zinc-950 border-zinc-800 text-zinc-100">
        <AlertDialogHeader>
          <AlertDialogTitle>
            {isLocking ? "Lock User Account?" : "Unlock User Account?"}
          </AlertDialogTitle>
          <AlertDialogDescription className="text-zinc-400">
            {isLocking 
              ? `Are you sure you want to lock ${user.email}? They will immediately lose access to the system and will not be able to log in.`
              : `Are you sure you want to unlock ${user.email}? Their access will be restored immediately.`}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isLoading} className="border-zinc-800 hover:bg-zinc-800 hover:text-zinc-100">
            Cancel
          </AlertDialogCancel>
          <AlertDialogAction 
            onClick={(e) => {
              e.preventDefault();
              onConfirm();
            }}
            disabled={isLoading}
            className={isLocking ? "bg-red-600 text-white hover:bg-red-500" : "bg-emerald-600 text-white hover:bg-emerald-500"}
          >
            {isLoading ? "Processing..." : isLocking ? "Lock Account" : "Unlock Account"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
