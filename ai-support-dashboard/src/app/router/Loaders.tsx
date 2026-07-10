import { Skeleton } from "@/components/ui/skeleton";

export function LoginSkeleton() {
  return (
    <div className="flex h-screen w-full items-center justify-center bg-background p-4">
      <div className="w-full max-w-sm flex flex-col gap-6">
        <div className="flex flex-col gap-2 items-center">
          <Skeleton className="h-10 w-10 rounded-full bg-card" />
          <Skeleton className="h-6 w-32 bg-card" />
        </div>
        <div className="bg-card border border-border rounded-xl p-6 flex flex-col gap-4">
          <Skeleton className="h-10 w-full bg-card" />
          <Skeleton className="h-10 w-full bg-card" />
          <Skeleton className="h-10 w-full bg-muted mt-2" />
        </div>
      </div>
    </div>
  );
}

export function DashboardSkeleton() {
  return (
    <div className="flex flex-col gap-8 p-6 md:p-8 h-full bg-background">
      <div>
        <Skeleton className="h-8 w-64 bg-card mb-2" />
        <Skeleton className="h-4 w-96 bg-card" />
      </div>
      <div className="grid grid-cols-1 xl:grid-cols-4 gap-8">
        <div className="xl:col-span-3 flex flex-col gap-8">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Skeleton className="h-32 w-full bg-card rounded-xl" />
            <Skeleton className="h-32 w-full bg-card rounded-xl" />
            <Skeleton className="h-32 w-full bg-card rounded-xl" />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Skeleton className="h-32 w-full bg-card rounded-xl" />
            <Skeleton className="h-32 w-full bg-card rounded-xl" />
            <Skeleton className="h-32 w-full bg-card rounded-xl" />
          </div>
        </div>
        <div className="xl:col-span-1">
          <Skeleton className="h-full min-h-[400px] w-full bg-card rounded-xl" />
        </div>
      </div>
    </div>
  );
}

export function WorkspaceSkeleton() {
  return (
    <div className="flex h-full bg-background">
      {/* Sidebar */}
      <div className="w-80 border-r border-zinc-900 p-4 flex flex-col gap-4">
        <Skeleton className="h-10 w-full bg-card rounded-lg" />
        <div className="flex flex-col gap-2 mt-4">
          <Skeleton className="h-20 w-full bg-card rounded-lg" />
          <Skeleton className="h-20 w-full bg-card rounded-lg" />
          <Skeleton className="h-20 w-full bg-card rounded-lg" />
        </div>
      </div>
      {/* Main Area */}
      <div className="flex-1 p-6 md:p-8 flex flex-col xl:flex-row gap-8">
        <div className="flex-1 flex flex-col gap-6">
          <Skeleton className="h-24 w-full bg-card rounded-xl" />
          <Skeleton className="h-48 w-full bg-card rounded-xl" />
        </div>
        <div className="xl:w-[400px] flex flex-col gap-6">
          <Skeleton className="h-48 w-full bg-card rounded-xl" />
          <Skeleton className="h-48 w-full bg-card rounded-xl" />
        </div>
      </div>
    </div>
  );
}
