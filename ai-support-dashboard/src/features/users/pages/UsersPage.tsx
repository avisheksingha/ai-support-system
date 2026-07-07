import { useState } from "react";
import { Users, Search, Filter } from "lucide-react";
import { useUsersQuery } from "../hooks/useUsers";
import { UserTable } from "../components/UserTable";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export function UsersPage() {
  const [page, setPage] = useState(0);
  const size = 10;
  
  // Future ready: state for search/filters
  const [search, setSearch] = useState("");
  
  const { data, isLoading, isError } = useUsersQuery({ page, size, ...(search ? { search } : {}) });

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-zinc-100 flex items-center gap-2">
            <Users className="h-6 w-6 text-indigo-400" />
            User Management
          </h1>
          <p className="text-sm text-zinc-400 mt-1">Manage platform access, assign roles, and lock inactive accounts.</p>
        </div>
      </div>
      
      {/* Filters Bar */}
      <div className="flex flex-col sm:flex-row gap-3 items-center">
        <div className="relative flex-1 w-full max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-500" />
          <Input 
            placeholder="Search by email or name..." 
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9 bg-zinc-950 border-zinc-800 text-zinc-200 placeholder:text-zinc-500 focus-visible:ring-indigo-500"
          />
        </div>
        <Button variant="outline" className="w-full sm:w-auto border-zinc-800 bg-zinc-950 text-zinc-300 hover:bg-zinc-900 hover:text-zinc-100">
          <Filter className="mr-2 h-4 w-4" />
          Filters
        </Button>
      </div>

      {isLoading ? (
        <div className="h-64 flex items-center justify-center rounded-xl border border-zinc-800 bg-zinc-950">
          <div className="flex flex-col items-center gap-3">
            <div className="h-8 w-8 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
            <p className="text-sm text-zinc-400 font-medium animate-pulse">Loading users...</p>
          </div>
        </div>
      ) : isError ? (
        <div className="h-64 flex flex-col items-center justify-center rounded-xl border border-red-500/20 bg-red-500/5 text-red-400 gap-2">
          <p className="font-semibold">Failed to load users</p>
          <p className="text-sm opacity-80">Check your connection or try again later.</p>
        </div>
      ) : (
        <>
          <UserTable users={data?.content || []} />
          
          {/* Pagination Controls */}
          <div className="flex items-center justify-between px-1 py-2">
            {/* Left: record count */}
            <div className="flex items-center gap-2.5">
              <span className="inline-flex items-center justify-center h-7 min-w-[2rem] px-2.5 rounded-md bg-indigo-500/10 border border-indigo-500/20 text-xs font-bold text-indigo-400 tabular-nums">
                {data?.totalElements ?? 0}
              </span>
              <p className="text-sm text-zinc-400">
                {data?.totalElements
                  ? <>
                      Results &mdash; showing{" "}
                      <span className="font-semibold text-zinc-200">{(page * size) + 1}–{Math.min((page + 1) * size, data.totalElements)}</span>
                    </>
                  : "No users found"
                }
              </p>
            </div>

            {/* Right: navigation */}
            <div className="flex items-center gap-1">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800 disabled:opacity-30 disabled:cursor-not-allowed px-3"
              >
                ← Previous
              </Button>
              <span className="text-xs text-zinc-600 px-2">
                Page {page + 1} of {data?.totalPages ?? 1}
              </span>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setPage(p => p + 1)}
                disabled={!data || page >= data.totalPages - 1}
                className="text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800 disabled:opacity-30 disabled:cursor-not-allowed px-3"
              >
                Next →
              </Button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
