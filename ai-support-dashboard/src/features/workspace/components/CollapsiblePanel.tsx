import { ChevronDown } from "lucide-react";
import React from "react";

interface CollapsiblePanelProps {
  title: string;
  icon?: React.ReactNode;
  isExpanded: boolean;
  onToggle: () => void;
  children: React.ReactNode;
  className?: string;
  // Now accepts hex codes directly (e.g., "#6366f1")
  accentColor?: string;
  badge?: React.ReactNode;
}

export function CollapsiblePanel({
  title,
  icon,
  isExpanded,
  onToggle,
  children,
  className = "",
  accentColor = "#cbd5e1", // Default slate-300 hex
  badge
}: CollapsiblePanelProps) {
  // Generate a unique ID for accessibility linking
  const panelId = React.useId();

  return (
    <div
      className={`bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden transition-shadow hover:shadow-md ${className}`}
      style={{ borderTopWidth: '4px', borderTopColor: accentColor }}
    >
      <button
        onClick={onToggle}
        aria-expanded={isExpanded}
        aria-controls={panelId}
        className="w-full flex items-center justify-between px-4 py-3 hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-200 transition-colors"
      >
        <div className="flex items-center gap-2 min-w-0">
          {icon && <span className="flex-shrink-0 text-slate-500">{icon}</span>}
          <span className="text-xs font-semibold text-slate-600 uppercase tracking-wider whitespace-nowrap truncate">
            {title}
          </span>
        </div>

        <div className="flex items-center gap-3 flex-shrink-0">
          {badge && <div className="flex items-center gap-2">{badge}</div>}
          <ChevronDown
            className={`h-4 w-4 text-slate-400 transition-transform duration-300 ${isExpanded ? 'rotate-180' : ''}`}
          />
        </div>
      </button>

      {isExpanded && (
        <div
          id={panelId}
          role="region"
          className="px-4 pb-4 pt-1 animate-in fade-in slide-in-from-top-2 duration-300"
        >
          {children}
        </div>
      )}
    </div>
  );
}