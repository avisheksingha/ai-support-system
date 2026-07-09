import React from "react";
import { User } from "lucide-react";
import { cn } from "@/lib/utils";

interface AvatarProps extends React.HTMLAttributes<HTMLDivElement> {
  nameOrEmail?: string | undefined;
  size?: "sm" | "md" | "lg" | "xl";
}

export function Avatar({ nameOrEmail, size = "md", className, ...props }: AvatarProps) {
  const sizeClasses = {
    sm: "w-8 h-8",
    md: "w-10 h-10",
    lg: "w-12 h-12",
    xl: "w-20 h-20",
  };

  const iconSizeClasses = {
    sm: "w-4 h-4",
    md: "w-5 h-5",
    lg: "w-6 h-6",
    xl: "w-10 h-10",
  };

  const getInitials = (str?: string | null) => {
    if (!str) return null;
    
    // If it's an email, try to extract first and last name from the first part
    let namePart = str as string;
    if (namePart.includes('@')) {
      namePart = namePart.split('@')[0] || '';
    }
    
    // Replace non-letters with spaces
    namePart = namePart.replace(/[^a-zA-Z]/g, ' ').trim();
    
    if (!namePart) return null;

    const parts = namePart.split(/\s+/);
    if (parts.length >= 2 && parts[0] && parts[parts.length - 1]) {
      const first = parts[0]?.charAt(0) || '';
      const last = parts[parts.length - 1]?.charAt(0) || '';
      return (first + last).toUpperCase();
    }
    return parts[0]?.substring(0, 2).toUpperCase() || null;
  };

  const initials = getInitials(nameOrEmail || "?");

  return (
    <div
      className={cn(
        "rounded-full bg-gradient-to-br from-blue-600 to-blue-600 flex items-center justify-center shadow-md",
        sizeClasses[size],
        className
      )}
      {...props}
    >
      {initials ? (
        <span className="text-white font-semibold text-xs tracking-wider">{initials}</span>
      ) : (
        <User className={cn("text-white", iconSizeClasses[size])} />
      )}
    </div>
  );
}
