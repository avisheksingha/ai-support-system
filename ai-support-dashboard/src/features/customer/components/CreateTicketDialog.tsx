import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
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
        <Button className="bg-indigo-600 hover:bg-indigo-700 text-white gap-2">
          <Plus className="h-4 w-4" />
          Create Ticket
        </Button>
      </DialogTrigger>
      <DialogContent className="bg-zinc-950 border-zinc-800 text-zinc-100 sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold">How can we help?</DialogTitle>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          <div className="space-y-2">
            <label htmlFor="subject" className="text-sm font-medium text-zinc-400">
              Subject
            </label>
            <Input
              id="subject"
              placeholder="Brief summary of your issue"
              className="bg-zinc-900 border-zinc-800 focus-visible:ring-indigo-500"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              required
              maxLength={150}
            />
          </div>
          
          <div className="space-y-2 flex flex-col">
            <label htmlFor="message" className="text-sm font-medium text-zinc-400">
              Details
            </label>
            <textarea
              id="message"
              placeholder="Please describe the issue in detail so we can best assist you."
              className="min-h-[150px] bg-zinc-900 border border-zinc-800 rounded-md p-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 resize-none"
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              required
              minLength={10}
            />
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-zinc-800/60">
            <Button 
              type="button" 
              variant="outline" 
              onClick={() => setOpen(false)}
              className="border-zinc-700 hover:bg-zinc-800 text-zinc-300"
              disabled={createTicketMutation.isPending}
            >
              Cancel
            </Button>
            <Button 
              type="submit" 
              className="bg-indigo-600 hover:bg-indigo-700 text-white min-w-[100px]"
              disabled={createTicketMutation.isPending || !subject.trim() || !message.trim()}
            >
              {createTicketMutation.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                "Submit Ticket"
              )}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
