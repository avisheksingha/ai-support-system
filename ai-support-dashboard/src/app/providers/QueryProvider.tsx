import { QueryClient, QueryClientProvider as RQProvider } from "@tanstack/react-query";
import { useState } from "react";

export function QueryProvider({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 1000 * 60 * 5, // 5 minutes
            retry: 1, // Reasonable retry policy
            refetchOnWindowFocus: false, // Prevent aggressive refetching unless needed
          },
        },
      })
  );

  return <RQProvider client={queryClient}>{children}</RQProvider>;
}
