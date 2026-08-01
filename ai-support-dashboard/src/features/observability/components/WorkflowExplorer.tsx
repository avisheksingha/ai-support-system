import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Search } from "lucide-react";
import { useWorkflowSearch } from "../hooks/useObservability";

export const WorkflowExplorer = () => {
  const [searchParams, setSearchParams] = useState<Record<string, any>>({});
  const { data, isLoading, error } = useWorkflowSearch(searchParams);

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const params: Record<string, any> = {};
    formData.forEach((value, key) => {
      if (value) params[key] = value;
    });
    setSearchParams(params);
  };

  return (
    <div className="space-y-4">
      <form onSubmit={handleSearch} className="flex gap-2">
        <Input
          name="ticketId"
          placeholder="Search by Ticket ID"
          className="flex-1"
        />
        <Input
          name="workflowId"
          placeholder="Search by Workflow ID"
          className="flex-1"
        />
        <Button type="submit" variant="default">
          <Search className="w-4 h-4 mr-2" />
          Search
        </Button>
      </form>

      {isLoading && <div className="text-sm text-muted-foreground">Searching workflows...</div>}

      {error && (
        <div className="text-sm text-red-500">
          Error searching workflows. Please try again.
        </div>
      )}

      {data && data.content && data.content.length > 0 ? (
        <div className="space-y-2">
          {data.content.map((workflow: any) => (
            <div key={workflow.eventId} className="p-4 border rounded-lg bg-background">
              <div className="flex justify-between items-start">
                <div>
                  <p className="font-medium">Ticket #{workflow.ticketId}</p>
                  <p className="text-sm text-muted-foreground">{workflow.definitionId}</p>
                </div>
                <div className="text-right">
                  <span className={`text-xs px-2 py-1 rounded-full ${
                    workflow.state === "COMPLETED" ? "bg-emerald-100 text-emerald-700" : 
                    workflow.state === "FAILED" ? "bg-red-100 text-red-700" : 
                    "bg-blue-100 text-blue-700"
                  }`}>
                    {workflow.state}
                  </span>
                  {workflow.durationMs && (
                    <p className="text-xs text-muted-foreground mt-1">{workflow.durationMs} ms</p>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-sm text-muted-foreground text-center py-8">
          No workflows found. Enter search criteria above.
        </div>
      )}
    </div>
  );
};
