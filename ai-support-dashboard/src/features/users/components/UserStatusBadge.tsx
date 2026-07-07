import { Badge } from "@/components/ui/badge";

interface UserStatusBadgeProps {
  locked: boolean;
}

export function UserStatusBadge({ locked }: UserStatusBadgeProps) {
  if (locked) {
    return (
      <Badge variant="outline" className="bg-red-500/10 text-red-400 border-red-500/20">
        <span className="w-1.5 h-1.5 rounded-full bg-red-400 mr-2" />
        Locked
      </Badge>
    );
  }

  return (
    <Badge variant="outline" className="bg-emerald-500/10 text-emerald-400 border-emerald-500/20">
      <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 mr-2" />
      Active
    </Badge>
  );
}
