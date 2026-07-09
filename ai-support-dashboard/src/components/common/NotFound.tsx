import { FileQuestion, Home } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";

export function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-background px-4 text-center">
      <div className="rounded-full bg-card p-6 mb-8 border border-border shadow-xl shadow-black/40">
        <FileQuestion className="h-16 w-16 text-blue-500" />
      </div>
      <h1 className="text-4xl font-extrabold tracking-tight text-foreground sm:text-5xl mb-4">
        Page Not Found
      </h1>
      <p className="text-lg text-muted-foreground max-w-md mx-auto mb-10">
        We can't seem to find the page you're looking for. It might have been removed, had its name changed, or is temporarily unavailable.
      </p>
      <Link to="/">
        <Button size="lg" className="bg-blue-600 hover:bg-blue-700 text-white shadow-lg shadow-blue-900/20 border-none transition-all">
          <Home className="mr-2 h-5 w-5" />
          Back to Home
        </Button>
      </Link>
    </div>
  );
}
