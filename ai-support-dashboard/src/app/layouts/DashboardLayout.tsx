import { Outlet, NavLink, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { LogOut, LayoutDashboard, Ticket, Settings, User, Users, Bot, ExternalLink, Radio } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

const NAV_ITEMS = [
  { name: "Dashboard",  path: "/dashboard", icon: LayoutDashboard, roles: ["ADMIN", "CUSTOMER", "AGENT"] },
  { name: "Workspace",  path: "/tickets",   icon: Ticket,          roles: ["AGENT", "ADMIN"] },
  { name: "My Tickets", path: "/my-tickets",icon: Ticket,          roles: ["CUSTOMER"] },
  { name: "Users",      path: "/users",     icon: Users,           roles: ["ADMIN"] },
  { name: "Settings",   path: "/settings",  icon: Settings,        roles: ["ADMIN"] },
];

const BREADCRUMB_MAP: Record<string, Record<string, string>> = {
  "/dashboard": { default: "Operations Center", CUSTOMER: "My Dashboard", AGENT: "Agent Workbench" },
  "/tickets":   { default: "Ticket Workspace" },
  "/my-tickets":{ default: "My Tickets" },
  "/users":     { default: "User Management" },
  "/settings":  { default: "Settings" },
  "/profile":   { default: "My Profile" },
};

export function DashboardLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate("/auth/login");
  };

  const visibleNavItems = NAV_ITEMS.filter(
    (item) => !item.roles || (user?.role && item.roles.includes(user.role))
  );

  const currentKey = Object.keys(BREADCRUMB_MAP).find((k) => location.pathname.startsWith(k));
  const breadcrumbEntry = currentKey ? BREADCRUMB_MAP[currentKey] : undefined;
  const breadcrumbText = breadcrumbEntry
    ? (user?.role && breadcrumbEntry[user.role]) || breadcrumbEntry["default"]
    : "Home";
  const breadcrumbRoot = user?.role === "CUSTOMER" ? "Portal" : "Platform";

  const displayName =
    user?.fullName ||
    user?.email?.split("@")[0]?.replace(/[^a-zA-Z]/g, " ").replace(/\b\w/g, (l) => l.toUpperCase()) ||
    "User";
  const initials = displayName.split(" ").map((w: string) => w[0]).slice(0, 2).join("").toUpperCase();

  return (
    <div className="h-screen overflow-hidden bg-background text-foreground flex">

      {/* ── Sidebar ── */}
      <aside className="w-60 border-r border-border bg-card hidden md:flex flex-col shrink-0 z-20 relative">

        {/* Logo */}
        <div className="h-16 flex items-center gap-2.5 px-5 border-b border-border">
          <div className="h-7 w-7 rounded bg-[#0C66E4] flex items-center justify-center shrink-0">
            <Bot className="h-4 w-4 text-white" />
          </div>
          <span className="font-bold text-sm tracking-tight text-foreground">
            AI Support Ops
          </span>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-3 space-y-0.5">
          <p className="text-[10px] font-semibold text-muted-foreground uppercase tracking-widest px-3 mb-3 mt-1">
            Navigation
          </p>
          {visibleNavItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `group flex items-center gap-3 px-3 py-2 rounded transition-colors text-sm font-medium ${
                  isActive
                    ? "bg-[#0C66E4]/10 text-[#0C66E4]"
                    : "text-muted-foreground hover:bg-muted hover:text-foreground"
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <item.icon className={`h-4 w-4 shrink-0 transition-colors ${isActive ? "text-[#0C66E4]" : "text-muted-foreground group-hover:text-foreground"}`} />
                  <span className="flex-1">{item.name}</span>
                </>
              )}
            </NavLink>
          ))}

          {/* External Tools — ADMIN only */}
          {user?.role === "ADMIN" && (
            <>
              <div className="my-3 border-t border-border" />
              <p className="text-[10px] font-semibold text-muted-foreground uppercase tracking-widest px-3 mb-3 mt-1">
                Infrastructure
              </p>
              <a
                href={import.meta.env.VITE_REDPANDA_URL || "http://localhost:9090"}
                target="_blank"
                rel="noopener noreferrer"
                className="group flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all text-sm font-medium text-muted-foreground hover:bg-card hover:text-foreground border border-transparent"
              >
                <Radio className="h-4 w-4 shrink-0 text-muted-foreground group-hover:text-orange-400 transition-colors" />
                <span className="flex-1">Kafka Console</span>
                <ExternalLink className="h-3 w-3 text-zinc-700 group-hover:text-muted-foreground transition-colors" />
              </a>
            </>
          )}
        </nav>

        {/* User footer */}
        <div className="p-3 border-t border-border">
          <DropdownMenu>
            {/* @ts-ignore: Radix UI asChild type incompatibility */}
            <DropdownMenuTrigger asChild>
              <button className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-card transition-colors text-left outline-none border border-transparent hover:border-border">
                <div className="h-7 w-7 rounded bg-[#0C66E4] flex items-center justify-center text-white text-[11px] font-bold shrink-0 shadow-sm">
                  {initials}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-medium text-foreground truncate">{displayName}</p>
                  <p className="text-[10px] text-muted-foreground truncate">{user?.role}</p>
                </div>
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent side="top" align="start" className="w-52 bg-background border-border text-foreground mb-1">
              <DropdownMenuItem onClick={() => navigate("/profile")} className="focus:bg-card cursor-pointer">
                <User className="mr-2 h-4 w-4" />
                <span>Profile</span>
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => navigate("/settings")} className="focus:bg-card cursor-pointer">
                <Settings className="mr-2 h-4 w-4" />
                <span>Settings</span>
              </DropdownMenuItem>
              <DropdownMenuSeparator className="bg-muted" />
              <DropdownMenuItem onClick={handleLogout} className="text-red-400 focus:bg-red-500/10 focus:text-red-400 cursor-pointer">
                <LogOut className="mr-2 h-4 w-4" />
                <span>Log out</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </aside>

      {/* ── Main Content ── */}
      <main className="flex-1 flex flex-col min-w-0">

        {/* Top header */}
        <header className="h-16 border-b border-border bg-card flex items-center px-6 justify-between shrink-0 z-10">
          <div className="flex items-center gap-2 text-sm">
            <span className="text-muted-foreground">{breadcrumbRoot}</span>
            <span className="text-zinc-800">/</span>
            <span className="font-medium text-foreground">{breadcrumbText}</span>
          </div>

          {/* Right: user info + avatar dropdown (mobile) */}
          <div className="flex items-center gap-3">
            <div className="hidden sm:flex flex-col items-end">
              <span className="text-xs font-medium text-foreground leading-none">{displayName}</span>
              <span className="text-[10px] text-muted-foreground mt-0.5">{user?.role}</span>
            </div>
            <DropdownMenu>
              <DropdownMenuTrigger className="h-8 w-8 rounded bg-[#0C66E4] flex items-center justify-center text-white text-[11px] font-bold cursor-pointer outline-none transition-all shadow-sm border-none p-0">
                {initials}
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-52 bg-background border-border text-foreground">
                <DropdownMenuItem onClick={() => navigate("/profile")} className="focus:bg-card cursor-pointer">
                  <User className="mr-2 h-4 w-4" />
                  <span>Profile</span>
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => navigate("/settings")} className="focus:bg-card cursor-pointer">
                  <Settings className="mr-2 h-4 w-4" />
                  <span>Settings</span>
                </DropdownMenuItem>
                <DropdownMenuSeparator className="bg-muted" />
                <DropdownMenuItem onClick={handleLogout} className="text-red-400 focus:bg-red-500/10 focus:text-red-400 cursor-pointer">
                  <LogOut className="mr-2 h-4 w-4" />
                  <span>Log out</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>

        <div className="flex-1 overflow-hidden relative">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
