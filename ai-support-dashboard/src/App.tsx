import { RouterProvider } from "react-router-dom";
import { QueryProvider } from "./app/providers/QueryProvider";
import { router } from "./app/router";
import { Toaster } from "./components/ui/sonner";

import { ErrorBoundary } from "react-error-boundary";
import { AppErrorFallback } from "./components/ui/ErrorFallbacks";

function App() {
  return (
    <ErrorBoundary FallbackComponent={AppErrorFallback} onReset={() => window.location.reload()}>
      <QueryProvider>
        <RouterProvider router={router} />
        <Toaster />
      </QueryProvider>
    </ErrorBoundary>
  );
}

export default App;
