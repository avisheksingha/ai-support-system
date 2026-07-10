import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useCreateTicket } from "../hooks/useCustomerTickets";
import { Loader2, Plus } from "lucide-react";

export function CreateTicketDialog() {
  const [open, setOpen] = useState(false);
  const [subject, setSubject] = useState("");
  const [message, setMessage] = useState("");
  
  const createTicketMutation = useCreateTicket();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!subject.trim() || !message.trim()) return;

    createTicketMutation.mutate(
      { subject, message },
      {
        onSuccess: () => {
          setOpen(false);
          setSubject("");
          setMessage("");
        }
      }
    );
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      {/* @ts-ignore: Radix UI asChild type incompatibility */}
      <DialogTrigger asChild>
        <Button className="shadow-sm h-9 px-4 text-sm font-medium gap-1.5">
          <Plus className="h-4 w-4" />
          Create Ticket
        </Button>
      </DialogTrigger>
      <DialogContent className="bg-background border-border text-foreground sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle className="text-lg font-semibold">Create New Ticket</DialogTitle>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div className="space-y-2">
            <label htmlFor="subject" className="text-sm font-medium text-foreground">
              Subject
            </label>
            <Input
              id="subject"
              placeholder="Brief summary of your issue"
              className="bg-card border-border focus-visible:ring-[#0C66E4]"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              required
              maxLength={150}
            />
          </div>
          
          <div className="space-y-2 flex flex-col">
            <label htmlFor="message" className="text-sm font-medium text-foreground">
              Description
            </label>
            <textarea
              id="message"
              placeholder="Please describe the issue in detail so we can best assist you."
              className="min-h-[150px] bg-card border border-border rounded-md p-3 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#0C66E4] resize-none"
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              required
              minLength={10}
            />
          </div>

          <DialogFooter className="border-t border-border pt-4">
            <Button 
              type="button" 
              variant="outline" 
              onClick={() => setOpen(false)}
              className="border-border hover:bg-muted text-foreground shadow-sm"
              disabled={createTicketMutation.isPending}
            >
              Cancel
            </Button>
            <Button 
              type="submit" 
              className="min-w-[120px]"
              disabled={createTicketMutation.isPending || !subject.trim() || !message.trim()}
            >
              {createTicketMutation.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                "Submit Ticket"
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
