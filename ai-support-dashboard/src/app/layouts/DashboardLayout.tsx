import { Outlet, NavLink, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { LogOut, LayoutDashboard, Ticket, Settings, User, Users, Bot, ChevronRight, ExternalLink, Radio } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

const NAV_ITEMS = [
  { name: "Dashboard",  path: "/dashboard", icon: LayoutDashboard, roles: ["ADMIN"] },
  { name: "Workspace",  path: "/tickets",   icon: Ticket,          roles: ["AGENT", "ADMIN"] },
  { name: "My Tickets", path: "/my-tickets",icon: Ticket,          roles: ["CUSTOMER"] },
  { name: "Users",      path: "/users",     icon: Users,           roles: ["ADMIN"] },
  { name: "Settings",   path: "/settings",  icon: Settings,        roles: ["ADMIN"] },
];

const BREADCRUMB_MAP: Record<string, string> = {
  "/dashboard": "Operations Center",
  "/tickets":   "Ticket Workspace",
  "/my-tickets":"Customer Portal",
  "/users":     "User Management",
  "/settings":  "Settings",
  "/profile":   "My Profile",
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
  const breadcrumbText = currentKey ? BREADCRUMB_MAP[currentKey] : "Platform";

  const displayName =
    user?.fullName ||
    user?.email?.split("@")[0]?.replace(/[^a-zA-Z]/g, " ").replace(/\b\w/g, (l) => l.toUpperCase()) ||
    "User";
  const initials = displayName.split(" ").map((w: string) => w[0]).slice(0, 2).join("").toUpperCase();

  return (
    <div className="min-h-screen bg-zinc-950 text-zinc-50 flex">

      {/* ── Sidebar ── */}
      <aside className="w-60 border-r border-zinc-800/60 bg-zinc-950 hidden md:flex flex-col shrink-0">

        {/* Logo */}
        <div className="h-16 flex items-center gap-2.5 px-5 border-b border-zinc-800/60">
          <div className="h-7 w-7 rounded-lg bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-lg shadow-indigo-500/20 shrink-0">
            <Bot className="h-4 w-4 text-white" />
          </div>
          <span className="font-bold text-sm tracking-tight bg-gradient-to-r from-indigo-400 to-purple-400 bg-clip-text text-transparent">
            AI Support Ops
          </span>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-3 space-y-0.5">
          <p className="text-[10px] font-semibold text-zinc-600 uppercase tracking-widest px-3 mb-3 mt-1">
            Navigation
          </p>
          {visibleNavItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `group flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all text-sm font-medium ${
                  isActive
                    ? "bg-indigo-500/10 text-indigo-400 border border-indigo-500/15 shadow-sm"
                    : "text-zinc-500 hover:bg-zinc-900 hover:text-zinc-200 border border-transparent"
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <item.icon className={`h-4 w-4 shrink-0 transition-colors ${isActive ? "text-indigo-400" : "text-zinc-600 group-hover:text-zinc-400"}`} />
                  <span className="flex-1">{item.name}</span>
                  {isActive && <ChevronRight className="h-3 w-3 text-indigo-500/60" />}
                </>
              )}
            </NavLink>
          ))}

          {/* External Tools — ADMIN only */}
          {user?.role === "ADMIN" && (
            <>
              <div className="my-3 border-t border-zinc-800/60" />
              <p className="text-[10px] font-semibold text-zinc-600 uppercase tracking-widest px-3 mb-3 mt-1">
                Infrastructure
              </p>
              <a
                href={import.meta.env.VITE_REDPANDA_URL || "http://localhost:9090"}
                target="_blank"
                rel="noopener noreferrer"
                className="group flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all text-sm font-medium text-zinc-500 hover:bg-zinc-900 hover:text-zinc-200 border border-transparent"
              >
                <Radio className="h-4 w-4 shrink-0 text-zinc-600 group-hover:text-orange-400 transition-colors" />
                <span className="flex-1">Kafka Console</span>
                <ExternalLink className="h-3 w-3 text-zinc-700 group-hover:text-zinc-500 transition-colors" />
              </a>
            </>
          )}
        </nav>

        {/* User footer */}
        <div className="p-3 border-t border-zinc-800/60">
          <DropdownMenu>
            {/* @ts-ignore: Radix UI asChild type incompatibility */}
            <DropdownMenuTrigger asChild>
              <button className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-zinc-900 transition-colors text-left outline-none border border-transparent hover:border-zinc-800">
                <div className="h-7 w-7 rounded-lg bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center text-white text-[11px] font-bold shrink-0 shadow-md shadow-indigo-500/20">
                  {initials}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-medium text-zinc-300 truncate">{displayName}</p>
                  <p className="text-[10px] text-zinc-600 truncate">{user?.role}</p>
                </div>
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent side="top" align="start" className="w-52 bg-zinc-950 border-zinc-800 text-zinc-200 mb-1">
              <DropdownMenuItem onClick={() => navigate("/profile")} className="focus:bg-zinc-900 cursor-pointer">
                <User className="mr-2 h-4 w-4" />
                <span>Profile</span>
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => navigate("/settings")} className="focus:bg-zinc-900 cursor-pointer">
                <Settings className="mr-2 h-4 w-4" />
                <span>Settings</span>
              </DropdownMenuItem>
              <DropdownMenuSeparator className="bg-zinc-800" />
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
        <header className="h-16 border-b border-zinc-800/60 bg-zinc-950/80 backdrop-blur-sm flex items-center px-6 justify-between sticky top-0 z-10">
          <div className="flex items-center gap-2 text-sm">
            <span className="text-zinc-600">Platform</span>
            <span className="text-zinc-800">/</span>
            <span className="font-medium text-zinc-200">{breadcrumbText}</span>
          </div>

          {/* Right: user info + avatar dropdown (mobile) */}
          <div className="flex items-center gap-3">
            <div className="hidden sm:flex flex-col items-end">
              <span className="text-xs font-medium text-zinc-300 leading-none">{displayName}</span>
              <span className="text-[10px] text-zinc-600 mt-0.5">{user?.role}</span>
            </div>
            <DropdownMenu>
              <DropdownMenuTrigger className="h-8 w-8 rounded-lg bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center text-white text-[11px] font-bold cursor-pointer outline-none hover:ring-2 hover:ring-indigo-500 hover:ring-offset-1 hover:ring-offset-zinc-950 focus-visible:ring-2 focus-visible:ring-indigo-500 transition-all shadow-md shadow-indigo-500/20 border-none p-0">
                {initials}
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-52 bg-zinc-950 border-zinc-800 text-zinc-200">
                <DropdownMenuItem onClick={() => navigate("/profile")} className="focus:bg-zinc-900 cursor-pointer">
                  <User className="mr-2 h-4 w-4" />
                  <span>Profile</span>
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => navigate("/settings")} className="focus:bg-zinc-900 cursor-pointer">
                  <Settings className="mr-2 h-4 w-4" />
                  <span>Settings</span>
                </DropdownMenuItem>
                <DropdownMenuSeparator className="bg-zinc-800" />
                <DropdownMenuItem onClick={handleLogout} className="text-red-400 focus:bg-red-500/10 focus:text-red-400 cursor-pointer">
                  <LogOut className="mr-2 h-4 w-4" />
                  <span>Log out</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>

        <div className="p-6 flex-1 overflow-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
