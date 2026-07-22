import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useCreateTicket } from "../hooks/useCustomerTickets";
import { useImproveWriting } from "../../writing/hooks/useWriting";
import { Loader2, Plus, Sparkles, Check, X, AlertTriangle } from "lucide-react";
import type { ValidationResult } from "../../writing/api/writingApi";

interface CreateTicketDialogProps {
  children?: React.ReactNode;
}

export function CreateTicketDialog({ children }: CreateTicketDialogProps = {}) {
  const [open, setOpen] = useState(false);
  const [subject, setSubject] = useState("");
  const [message, setMessage] = useState("");
  
  // AI Suggestion State
  const [suggestion, setSuggestion] = useState<{ subject: string, message: string, improved: boolean, changes: string[], qualityAssessment?: string, checklist?: string[] } | null>(null);
  const [lastCacheKey, setLastCacheKey] = useState<string>("");
  const [aiError, setAiError] = useState<string | null>(null);
  const [validationError, setValidationError] = useState<ValidationResult | null>(null);
  
  const createTicketMutation = useCreateTicket();
  const improveWritingMutation = useImproveWriting();

  const handleImprove = () => {
    if (!subject.trim() || !message.trim()) return;
    
    const currentKey = `${subject.trim()}|${message.trim()}`;

    setAiError(null);
    setSuggestion(null);

    improveWritingMutation.mutate(
      {
        context: "SUPPORT_TICKET",
        subject,
        content: message,
        language: navigator.language || "en-US"
      },
      {
        onSuccess: (data) => {
          if (data.validationResult && !data.validationResult.canProceed) {
            setValidationError(data.validationResult);
            return;
          }
          setSuggestion({
            subject: data.improvedSubject,
            message: data.improvedContent,
            improved: data.improved,
            changes: data.changes,
            qualityAssessment: data.qualityAssessment,
            checklist: data.checklist
          });
          setLastCacheKey(currentKey);
        },
        onError: () => {
          setAiError("Unable to improve text right now. You can continue submitting your ticket.");
        }
      }
    );
  };

  const applySuggestion = () => {
    if (suggestion) {
      setSubject(suggestion.subject);
      setMessage(suggestion.message);
      setSuggestion(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!subject.trim() || !message.trim()) return;

    createTicketMutation.mutate(
      { subject, message, bypassSoftValidation: validationError?.isSoftValidation },
      {
        onSuccess: () => {
          setOpen(false);
          setSubject("");
          setMessage("");
          setSuggestion(null);
          setLastCacheKey("");
          setAiError(null);
          setValidationError(null);
        },
        onError: (error: any) => {
          if (error.response?.data?.outcome) {
            setValidationError(error.response.data);
          }
        }
      }
    );
  };

  return (
    <Dialog open={open} onOpenChange={(isOpen) => {
      setOpen(isOpen);
      if (!isOpen && !createTicketMutation.isPending) {
        // Reset when closed
        setSuggestion(null);
        setAiError(null);
      }
    }}>
      <DialogTrigger render={children ? (children as React.ReactElement) : (
        <Button className="shadow-sm h-9 px-4 text-sm font-medium gap-1.5 pb-0.5">
          <Plus className="h-4 w-4" />
          Create Ticket
        </Button>
      )} />
      <DialogContent className="bg-background border-border text-foreground sm:max-w-[550px]">
        <DialogHeader>
          <DialogTitle className="text-lg font-semibold pr-8">
            Create New Ticket
          </DialogTitle>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          {aiError && (
            <div className="text-xs text-amber-600 bg-amber-50 p-2 rounded border border-amber-200 flex items-start gap-2">
              <AlertTriangle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{aiError}</span>
            </div>
          )}
          
          {validationError && (
            <div className={`text-sm p-3 rounded border flex flex-col gap-1 ${validationError.isSoftValidation ? 'text-amber-800 bg-amber-50 border-amber-200' : 'text-red-700 bg-red-50 border-red-200'}`}>
              <div className="flex items-center gap-2 font-semibold">
                <AlertTriangle className="h-4 w-4" />
                {validationError.title || `Validation Failed: ${validationError.outcome}`}
              </div>
              <p className={`text-xs pl-6 ${validationError.isSoftValidation ? 'text-amber-700' : 'text-red-600'}`}>
                {validationError.userMessage || validationError.reason}
              </p>
            </div>
          )}
          
          <div className="space-y-2">
            <label htmlFor="subject" className="text-sm font-medium text-foreground">
              Subject
            </label>
            <Input
              id="subject"
              placeholder="Brief summary of your issue"
              className="bg-card border-border focus-visible:ring-[#0C66E4]"
              value={subject}
              onChange={(e) => {
                setSubject(e.target.value);
                setValidationError(null);
                setAiError(null);
              }}
              required
              maxLength={150}
            />
          </div>
          
          <div className="space-y-2 flex flex-col">
            <div className="flex items-center justify-between">
              <label htmlFor="message" className="text-sm font-medium text-foreground">
                Description
              </label>
              <span className="text-[10px] text-muted-foreground">
                {message.length} / 5000
              </span>
            </div>
            <textarea
              id="message"
              placeholder="Please describe the issue in detail so we can best assist you."
              className="min-h-[120px] bg-card border border-border rounded-md p-3 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#0C66E4] resize-none"
              value={message}
              onChange={(e) => {
                setMessage(e.target.value);
                setValidationError(null);
                setAiError(null);
              }}
              required
              minLength={10}
            />
            <div className="flex justify-end mt-1">
              <Button
                type="button"
                variant="outline"
                size="sm"
                className="h-7 px-3 gap-1.5 text-[11px] text-blue-600 border-blue-200 hover:bg-blue-50/50 shadow-sm"
                onClick={handleImprove}
                disabled={improveWritingMutation.isPending || !subject.trim() || !message.trim()}
                title="Improves grammar and clarity without changing the meaning of your request."
              >
                {improveWritingMutation.isPending ? (
                  <>
                    <Loader2 className="h-3 w-3 animate-spin" />
                    Improving...
                  </>
                ) : (
                  <>
                    <Sparkles className="h-3 w-3" />
                    {suggestion && lastCacheKey === `${subject.trim()}|${message.trim()}` ? "Improve Again" : "Improve Draft"}
                  </>
                )}
              </Button>
            </div>
          </div>

          {suggestion && (
            <div className="bg-white border border-blue-200/60 shadow-sm rounded-md p-4 text-sm relative max-h-[320px] overflow-y-auto">
              {suggestion.improved ? (
                <>
                  <h4 className="font-semibold text-[#0C66E4] mb-4 flex items-center gap-1.5">
                    <Sparkles className="h-4 w-4" /> Suggested Improvements
                  </h4>
                  
                  <div className="space-y-3 mb-5">
                    <div className="bg-blue-50/40 rounded-lg p-3 border border-blue-100/50">
                      <span className="text-xs font-semibold text-blue-800 uppercase tracking-wider mb-2 block">Suggested Subject</span>
                      <div className="space-y-1">
                        <p className="text-blue-900/50 line-through text-xs">{subject}</p>
                        <p className="text-blue-950 font-medium">{suggestion.subject}</p>
                      </div>
                    </div>
                    
                    <div className="bg-blue-50/40 rounded-lg p-3 border border-blue-100/50">
                      <span className="text-xs font-semibold text-blue-800 uppercase tracking-wider mb-2 block">Suggested Description</span>
                      <div className="space-y-1">
                        <p className="text-blue-900/50 line-through text-xs whitespace-pre-wrap">{message}</p>
                        <p className="text-blue-950 whitespace-pre-wrap">{suggestion.message}</p>
                      </div>
                    </div>
                  </div>

                  {suggestion.changes.length > 0 && (
                    <div className="mb-5">
                      <span className="text-xs font-medium text-muted-foreground uppercase tracking-wider block mb-2">Changes</span>
                      <ul className="text-xs text-foreground/80 space-y-1.5">
                        {suggestion.changes.map((change, idx) => (
                          <li key={idx} className="flex items-start gap-1.5">
                            <Check className="h-3.5 w-3.5 text-green-600 mt-0.5 shrink-0" /> {change}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                  {suggestion.qualityAssessment && (
                    <div className="mb-4 bg-purple-50/50 p-3 rounded-lg border border-purple-100">
                      <span className="text-xs font-semibold text-purple-800 uppercase tracking-wider mb-1 block">Ticket Quality Assessment</span>
                      <p className="text-xs text-purple-900 leading-relaxed">{suggestion.qualityAssessment}</p>
                    </div>
                  )}

                  {suggestion.checklist && suggestion.checklist.length > 0 && (
                    <div className="mb-5 bg-amber-50/50 p-3 rounded-lg border border-amber-100">
                      <span className="text-xs font-semibold text-amber-800 uppercase tracking-wider mb-2 block">Before Submit Checklist</span>
                      <ul className="text-xs text-amber-900 space-y-1.5 list-disc pl-4">
                        {suggestion.checklist.map((item, idx) => (
                          <li key={idx}>{item}</li>
                        ))}
                      </ul>
                    </div>
                  )}

                  <div className="flex items-center gap-2">
                    <Button 
                      type="button" 
                      size="sm" 
                      className="bg-[#0C66E4] hover:bg-[#0052CC] h-8 gap-1"
                      onClick={applySuggestion}
                    >
                      <Check className="h-3.5 w-3.5" /> Apply
                    </Button>
                    <Button 
                      type="button" 
                      size="sm" 
                      variant="outline" 
                      className="h-8 gap-1 text-muted-foreground"
                      onClick={() => setSuggestion(null)}
                    >
                      <X className="h-3.5 w-3.5" /> Dismiss
                    </Button>
                  </div>
                </>
              ) : (
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-semibold text-green-700 mb-0.5 flex items-center gap-1.5">
                      <Check className="h-4 w-4" /> Looks good!
                    </h4>
                    <p className="text-muted-foreground text-xs">Your ticket is already clear enough to send.</p>
                  </div>
                  <Button 
                    type="button" 
                    size="sm" 
                    variant="ghost" 
                    className="h-8 w-8 p-0 text-muted-foreground"
                    onClick={() => setSuggestion(null)}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              )}
            </div>
          )}

          <DialogFooter className="border-t border-border pt-4 flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <p className="text-[10px] text-muted-foreground sm:text-left text-center leading-relaxed">
                AI improves grammar and clarity without changing the intended meaning whenever possible.
              </p>
            </div>
            <div className="flex gap-2 justify-end">
              <Button 
                type="button" 
                variant="outline" 
                onClick={() => setOpen(false)}
                className="border-border hover:bg-muted text-foreground shadow-sm"
                disabled={createTicketMutation.isPending || improveWritingMutation.isPending}
              >
                Cancel
              </Button>
              <Button 
                type="submit" 
                className="min-w-[120px]"
                disabled={createTicketMutation.isPending || improveWritingMutation.isPending || !subject.trim() || !message.trim()}
              >
                {createTicketMutation.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : validationError?.isSoftValidation ? (
                  "Submit Anyway"
                ) : (
                  "Submit Ticket"
                )}
              </Button>
            </div>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
