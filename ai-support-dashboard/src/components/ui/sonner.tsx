import { Toaster as Sonner, type ToasterProps } from "sonner";
import { CircleCheckIcon, InfoIcon, TriangleAlertIcon, OctagonXIcon, Loader2Icon } from "lucide-react";

export function Toaster({ ...props }: ToasterProps) {
  return (
    <Sonner
      theme="dark"
      position="bottom-right"
      className="toaster group"
      toastOptions={{
        classNames: {
          toast: "group toast group-[.toaster]:bg-card group-[.toaster]:text-foreground group-[.toaster]:border-border group-[.toaster]:shadow-lg group-[.toaster]:border-l-4 !rounded-l-sm !rounded-r-md min-h-[60px] relative overflow-hidden",
          success: "group-[.toaster]:!border-l-emerald-500 [&>[data-icon]]:text-emerald-500",
          error: "group-[.toaster]:!border-l-red-500 [&>[data-icon]]:text-red-500",
          warning: "group-[.toaster]:!border-l-amber-500 [&>[data-icon]]:text-amber-500",
          info: "group-[.toaster]:!border-l-[#0C66E4] [&>[data-icon]]:text-[#0C66E4]",
          description: "group-[.toast]:!text-muted-foreground group-[.toast]:text-xs group-[.toast]:mt-1 block",
          title: "group-[.toast]:font-semibold group-[.toast]:text-foreground",
        },
        style: {
          zIndex: 9999,
        }
      }}
      icons={{
        success: <CircleCheckIcon className="size-4" />,
        info: <InfoIcon className="size-4" />,
        warning: <TriangleAlertIcon className="size-4" />,
        error: <OctagonXIcon className="size-4" />,
        loading: <Loader2Icon className="size-4 animate-spin" />,
      }}
      style={
        {
          "--normal-bg": "hsl(var(--card))",
          "--normal-text": "hsl(var(--foreground))",
          "--normal-border": "hsl(var(--border))",
          "--border-radius": "var(--radius)",
          zIndex: 9999,
        } as React.CSSProperties
      }
      {...props}
    />
  );
}
