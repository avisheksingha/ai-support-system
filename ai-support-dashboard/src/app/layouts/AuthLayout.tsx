import { Outlet } from "react-router-dom";
import { Bot, Zap, ShieldCheck, BarChart3 } from "lucide-react";

const features = [
  { icon: Bot, title: "AI-Powered Triage", desc: "Instant ticket analysis with sentiment & intent detection" },
  { icon: Zap, title: "Smart Routing", desc: "Rule-based engine assigns tickets to the right team automatically" },
  { icon: BarChart3, title: "Live Analytics", desc: "Real-time metrics across all support operations" },
  { icon: ShieldCheck, title: "Secure by Default", desc: "JWT auth, RBAC, and audit logging built in" },
];

export function AuthLayout() {
  return (
    <div className="min-h-screen flex bg-zinc-950 text-zinc-50 overflow-hidden">

      {/* ── Left panel: branding ── */}
      <div className="hidden lg:flex flex-col flex-1 relative overflow-hidden px-14 py-12">
        {/* Layered glow orbs */}
        <div className="absolute -top-40 -left-40 w-[600px] h-[600px] bg-indigo-600/20 blur-[130px] rounded-full pointer-events-none" />
        <div className="absolute -bottom-40 -right-20 w-[500px] h-[500px] bg-purple-600/15 blur-[120px] rounded-full pointer-events-none" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[350px] h-[350px] bg-indigo-500/8 blur-[80px] rounded-full pointer-events-none" />

        {/* Dot grid texture */}
        <div
          className="absolute inset-0 opacity-[0.03] pointer-events-none"
          style={{
            backgroundImage: "radial-gradient(circle, #a5b4fc 1px, transparent 1px)",
            backgroundSize: "28px 28px",
          }}
        />

        {/* Logo */}
        <div className="relative z-10 flex items-center gap-3">
          <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-lg shadow-indigo-500/30">
            <Bot className="h-5 w-5 text-white" />
          </div>
          <span className="text-sm font-semibold tracking-wide text-zinc-200">AI Support Ops</span>
        </div>

        {/* Hero copy */}
        <div className="relative z-10 mt-auto mb-auto">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-medium mb-6">
            <span className="h-1.5 w-1.5 rounded-full bg-indigo-400 animate-pulse" />
            Enterprise AI Platform
          </div>
          <h1 className="text-4xl xl:text-5xl font-extrabold tracking-tight leading-tight text-zinc-50 mb-4">
            Support operations,<br />
            <span className="bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent">
              powered by AI.
            </span>
          </h1>
          <p className="text-zinc-400 text-base leading-relaxed max-w-sm">
            A unified command center for your support team — from ticket creation to AI-driven resolution.
          </p>

          {/* Feature pills */}
          <div className="mt-10 grid grid-cols-1 gap-3 max-w-sm">
            {features.map(({ icon: Icon, title, desc }) => (
              <div key={title} className="flex items-start gap-3 p-3.5 rounded-xl bg-zinc-900/50 border border-zinc-800/60 backdrop-blur-sm">
                <div className="h-8 w-8 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center shrink-0 mt-0.5">
                  <Icon className="h-4 w-4 text-indigo-400" />
                </div>
                <div>
                  <p className="text-sm font-semibold text-zinc-200">{title}</p>
                  <p className="text-xs text-zinc-500 mt-0.5 leading-relaxed">{desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Footer */}
        <p className="relative z-10 text-xs text-zinc-600">
          © {new Date().getFullYear()} AI Support System · All rights reserved
        </p>
      </div>

      {/* Divider */}
      <div className="hidden lg:block w-px bg-gradient-to-b from-transparent via-zinc-800 to-transparent" />

      {/* ── Right panel: form ── */}
      <div className="flex flex-col flex-1 lg:max-w-[480px] xl:max-w-[520px] relative px-8 md:px-12">
        {/* Mobile-only glow */}
        <div className="lg:hidden absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-indigo-500/10 blur-[120px] rounded-full pointer-events-none" />

        {/* Top spacer */}
        <div className="flex-1 min-h-[60px]" />

        {/* Centered form */}
        <div className="relative z-10 w-full max-w-[360px] mx-auto">
          <Outlet />
        </div>

        {/* Bottom spacer + footer */}
        <div className="flex-1 min-h-[60px] flex items-end justify-center pb-8">
          <p className="text-xs text-zinc-700 text-center">
            Secured by JWT · Role-Based Access Control
          </p>
        </div>
      </div>
    </div>
  );
}
