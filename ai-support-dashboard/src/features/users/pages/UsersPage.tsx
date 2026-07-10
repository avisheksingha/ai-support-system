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
    <div className="h-full overflow-auto p-6 space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <Users className="h-6 w-6 text-blue-400" />
            User Management
          </h1>
          <p className="text-sm text-muted-foreground mt-1">Manage platform access, assign roles, and lock inactive accounts.</p>
        </div>
      </div>
      
      {/* Filters Bar */}
      <div className="flex flex-col sm:flex-row gap-3 items-center">
        <div className="relative flex-1 w-full max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input 
            placeholder="Search by email or name..." 
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9 bg-background border-border text-foreground placeholder:text-muted-foreground focus-visible:ring-blue-500"
          />
        </div>
        <Button variant="outline" className="w-full sm:w-auto border-border bg-background text-foreground hover:bg-card hover:text-foreground">
          <Filter className="mr-2 h-4 w-4" />
          Filters
        </Button>
      </div>

      {isLoading ? (
        <div className="h-64 flex items-center justify-center rounded-xl border border-border bg-background">
          <div className="flex flex-col items-center gap-3">
            <div className="h-8 w-8 rounded-full border-2 border-blue-500 border-t-transparent animate-spin" />
            <p className="text-sm text-muted-foreground font-medium animate-pulse">Loading users...</p>
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
          <div className="flex items-center justify-between pt-4 mt-4 border-t border-border">
            <p className="text-sm text-muted-foreground">
              {data?.totalElements
                ? `Showing ${(page * size) + 1} to ${Math.min((page + 1) * size, data.totalElements)} of ${data.totalElements} users`
                : "No users found"
              }
            </p>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="h-8 px-3 text-xs shadow-sm bg-background border-border text-foreground hover:bg-muted transition-colors"
              >
                Previous
              </Button>
              {data?.totalPages && data.totalPages > 0 && (
                <div className="flex items-center gap-1 mx-2">
                  {Array.from({ length: Math.min(data.totalPages, 5) }).map((_, i) => (
                    <button
                      key={i}
                      onClick={() => setPage(i)}
                      className={`h-8 min-w-8 px-2 rounded text-sm font-medium transition-colors ${
                        page === i
                          ? "bg-primary text-primary-foreground shadow-sm"
                          : "text-muted-foreground hover:bg-muted hover:text-foreground"
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}
                  {data.totalPages > 5 && (
                    <span className="text-muted-foreground text-sm px-1">...</span>
                  )}
                </div>
              )}
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => p + 1)}
                disabled={!data || page >= data.totalPages - 1}
                className="h-8 px-3 text-xs shadow-sm bg-background border-border text-foreground hover:bg-muted transition-colors"
              >
                Next
              </Button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
