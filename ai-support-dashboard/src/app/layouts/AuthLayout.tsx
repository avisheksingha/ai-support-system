import { Outlet } from "react-router-dom";
import { Bot } from "lucide-react";

export function AuthLayout() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-muted text-foreground relative py-12 px-4 sm:px-6 lg:px-8">
      {/* Subtle background element */}
      <div className="absolute top-0 w-full h-[300px] bg-blue-600/5 blur-[100px] pointer-events-none" />

      <div className="relative z-10 w-full max-w-[420px] bg-card border border-transparent shadow-[0_1px_3px_rgba(9,30,66,0.12),0_1px_2px_rgba(9,30,66,0.14)] rounded-lg p-8 flex flex-col items-center">
        {/* Logo Header */}
        <div className="flex flex-col items-center gap-3 mb-8">
          <div className="h-10 w-10 rounded-lg bg-[#0C66E4] flex items-center justify-center">
            <Bot className="h-6 w-6 text-white" />
          </div>
          <h1 className="text-xl font-bold tracking-tight text-foreground">AI Support Ops</h1>
        </div>

        {/* The Login Form */}
        <div className="w-full">
          <Outlet />
        </div>

        {/* Footer inside the card */}
        <div className="mt-8 border-t border-border w-full pt-6 text-center space-y-1">
          <p className="text-xs text-muted-foreground">
            © {new Date().getFullYear()} AI Support System
          </p>
          <p className="text-[10px] text-muted-foreground/60">
            Secured by JWT · Role-Based Access Control
          </p>
        </div>
      </div>
    </div>
  );
}
