import { AlertTriangle, RefreshCcw } from "lucide-react";
import { Button } from "./button";

interface FallbackProps {
  error: Error;
  resetErrorBoundary: () => void;
}

export function AppErrorFallback({ error, resetErrorBoundary }: FallbackProps) {
  return (
    <div className="flex h-screen w-full items-center justify-center bg-zinc-950 p-6 text-zinc-50 font-sans">
      <div className="max-w-md w-full bg-red-950/20 border border-red-900/50 rounded-xl p-8 flex flex-col items-center text-center gap-6 shadow-lg">
        <div className="h-16 w-16 bg-red-500/10 text-red-500 rounded-full flex items-center justify-center">
          <AlertTriangle className="h-8 w-8" />
        </div>
        <div>
          <h2 className="text-xl font-bold text-zinc-100 mb-2">Critical System Error</h2>
          <p className="text-sm text-zinc-400">
            The application encountered an unexpected error. Our engineering team has been notified via automated telemetry.
          </p>
        </div>
        
        <div className="w-full bg-black/40 rounded border border-red-900/30 p-4 text-left overflow-x-auto">
           <code className="text-[11px] text-red-400 font-mono whitespace-pre-wrap break-all">
             {error.message || "Unknown error occurred"}
           </code>
        </div>

        <Button 
          onClick={resetErrorBoundary}
          className="w-full bg-red-600 hover:bg-red-700 text-white border-none mt-2 flex items-center gap-2"
        >
          <RefreshCcw className="h-4 w-4" />
          Reload Application
        </Button>
      </div>
    </div>
  );
}

export function WorkspaceErrorFallback({ error, resetErrorBoundary }: FallbackProps) {
  return (
    <div className="bg-red-950/10 border border-red-900/30 border-dashed rounded-xl p-6 flex flex-col items-center justify-center text-center gap-3">
      <AlertTriangle className="h-6 w-6 text-red-500/70" />
      <div className="flex flex-col items-center gap-1">
        <p className="text-sm font-medium text-zinc-300">Workspace Panel Failed</p>
        <p className="text-xs text-red-400/80 font-mono">{error.message}</p>
      </div>
      <Button 
        variant="outline" 
        size="sm" 
        onClick={resetErrorBoundary}
        className="mt-2 text-xs border-zinc-800 text-zinc-400 hover:text-zinc-200"
      >
        Retry Panel
      </Button>
    </div>
  );
}
