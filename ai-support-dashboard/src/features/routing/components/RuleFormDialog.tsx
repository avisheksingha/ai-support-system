import { useState, useEffect } from "react";
import type { RoutingRule, RoutingRuleRequest } from "../types";
import type { TicketPriority } from "@/shared/types/ticket";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Dialog,
  DialogContent,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Network, Plus, Trash2 } from "lucide-react";

interface RuleFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  rule: RoutingRule | null;
  onSave: (data: RoutingRuleRequest) => void;
  isLoading?: boolean;
}

const COMMON_TEAMS = [
  "account-team",
  "billing-team",
  "developer-support",
  "integration-team",
  "general-support",
];

const INTENT_OPTIONS = [
  { value: "ALL", label: "Any Intent (Wildcard)" },
  { value: "ACCOUNT_ACCESS", label: "Account Access" },
  { value: "PAYMENT_ISSUE", label: "Payment / Billing" },
  { value: "TECHNICAL_SUPPORT", label: "Technical Support" },
  { value: "API_INTEGRATION", label: "API & Developer" },
  { value: "FEATURE_REQUEST", label: "Feature Request" },
];

const URGENCY_OPTIONS = [
  { value: "ALL", label: "Any Urgency" },
  { value: "CRITICAL", label: "Critical" },
  { value: "HIGH", label: "High" },
  { value: "MEDIUM", label: "Medium" },
  { value: "LOW", label: "Low" },
];

const SENTIMENT_OPTIONS = [
  { value: "ALL", label: "Any Sentiment" },
  { value: "NEGATIVE", label: "Negative" },
  { value: "FRUSTRATED", label: "Frustrated" },
  { value: "NEUTRAL", label: "Neutral" },
  { value: "POSITIVE", label: "Positive" },
];

export function RuleFormDialog({
  open,
  onOpenChange,
  rule,
  onSave,
  isLoading = false,
}: RuleFormDialogProps) {
  const isEdit = !!rule;

  const [ruleName, setRuleName] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<number>(50);
  const [assignToTeam, setAssignToTeam] = useState("general-support");
  const [customTeam, setCustomTeam] = useState("");
  const [isCustomTeam, setIsCustomTeam] = useState(false);
  const [slaHours, setSlaHours] = useState<number>(24);
  const [priorityOverride, setPriorityOverride] = useState<string>("NONE");
  const [intentPattern, setIntentPattern] = useState("ALL");
  const [urgencyPattern, setUrgencyPattern] = useState("ALL");
  const [sentimentPattern, setSentimentPattern] = useState("ALL");
  const [keywordInput, setKeywordInput] = useState("");
  const [keywords, setKeywords] = useState<string[]>([]);
  const [active, setActive] = useState(true);

  useEffect(() => {
    if (rule) {
      setRuleName(rule.ruleName || "");
      setDescription(rule.description || "");
      setPriority(rule.priority ?? 50);
      if (COMMON_TEAMS.includes(rule.assignToTeam)) {
        setAssignToTeam(rule.assignToTeam);
        setIsCustomTeam(false);
        setCustomTeam("");
      } else {
        setAssignToTeam("CUSTOM");
        setIsCustomTeam(true);
        setCustomTeam(rule.assignToTeam);
      }
      setSlaHours(rule.slaHours ?? 24);
      setPriorityOverride(rule.priorityOverride || "NONE");
      setIntentPattern(rule.intentPattern || "ALL");
      setUrgencyPattern(rule.urgencyPattern || "ALL");
      setSentimentPattern(rule.sentimentPattern || "ALL");
      setKeywords(rule.keywordPatterns || []);
      setActive(rule.active ?? true);
    } else {
      setRuleName("");
      setDescription("");
      setPriority(50);
      setAssignToTeam("general-support");
      setIsCustomTeam(false);
      setCustomTeam("");
      setSlaHours(24);
      setPriorityOverride("NONE");
      setIntentPattern("ALL");
      setUrgencyPattern("ALL");
      setSentimentPattern("ALL");
      setKeywords([]);
      setActive(true);
    }
  }, [rule, open]);

  const handleAddKeyword = () => {
    const trimmed = keywordInput.trim().toLowerCase();
    if (trimmed && !keywords.includes(trimmed)) {
      setKeywords([...keywords, trimmed]);
      setKeywordInput("");
    }
  };

  const handleRemoveKeyword = (kw: string) => {
    setKeywords(keywords.filter(k => k !== kw));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!ruleName.trim()) return;

    const targetTeam = isCustomTeam ? customTeam.trim() : assignToTeam;
    if (!targetTeam) return;

    const payload: RoutingRuleRequest = {
      ruleName: ruleName.trim(),
      description: description.trim() || undefined,
      priority: Number(priority) || 0,
      active,
      assignToTeam: targetTeam,
      slaHours: Number(slaHours) || 24,
      priorityOverride: priorityOverride !== "NONE" ? (priorityOverride as TicketPriority) : undefined,
      intentPattern: intentPattern !== "ALL" ? intentPattern : undefined,
      urgencyPattern: urgencyPattern !== "ALL" ? urgencyPattern : undefined,
      sentimentPattern: sentimentPattern !== "ALL" ? sentimentPattern : undefined,
      keywordPatterns: keywords.length > 0 ? keywords : undefined,
    };

    onSave(payload);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-xl max-h-[90vh] overflow-y-auto bg-card border-border text-foreground p-0 gap-0">
        
        {/* Cover banner */}
        <div className="h-20 bg-gradient-to-r from-[#0C66E4]/20 via-[#0C66E4]/10 to-transparent relative shrink-0">
          <div
            className="absolute inset-0 opacity-30"
            style={{ backgroundImage: "radial-gradient(circle, #0C66E4 1px, transparent 1px)", backgroundSize: "20px 20px" }}
          />
        </div>

        {/* Header Icon + Titles */}
        <div className="px-6 pb-6">
          <div className="flex items-end gap-3 -mt-8 mb-4">
            <div className="h-14 w-14 rounded-full bg-[#0C66E4] flex items-center justify-center shadow-md ring-4 ring-card text-white shrink-0">
              <Network className="h-7 w-7" />
            </div>
            <div className="pb-1">
              <h2 className="text-lg font-bold text-foreground leading-tight">
                {isEdit ? "Edit Routing Rule" : "Create Routing Rule"}
              </h2>
              <p className="text-xs text-muted-foreground mt-0.5">
                {isEdit ? `Updating configuration for ${rule?.ruleName}` : "Define ticket assignment criteria, target team, and SLA targets."}
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4 border-t border-border pt-4">
            
            {/* Rule Name & Priority */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="sm:col-span-2 space-y-1.5">
                <label className="text-xs font-semibold text-foreground">Rule Name *</label>
                <Input
                  required
                  placeholder="e.g. Critical Billing Issues"
                  value={ruleName}
                  onChange={(e) => setRuleName(e.target.value)}
                  className="bg-background border-border text-foreground text-xs"
                />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-foreground">Evaluation Priority</label>
                <Input
                  type="number"
                  min={0}
                  max={1000}
                  value={priority}
                  onChange={(e) => setPriority(parseInt(e.target.value) || 0)}
                  className="bg-background border-border text-foreground text-xs"
                  title="Higher numbers evaluate first"
                />
              </div>
            </div>

            {/* Description */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-foreground">Description</label>
              <Input
                placeholder="Optional explanation of when this rule applies"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="bg-background border-border text-foreground text-xs"
              />
            </div>

            {/* Target Team & SLA */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-foreground">Assign To Team *</label>
                <Select
                  value={isCustomTeam ? "CUSTOM" : assignToTeam}
                  onValueChange={(val) => {
                    if (val === "CUSTOM") {
                      setIsCustomTeam(true);
                    } else if (val) {
                      setIsCustomTeam(false);
                      setAssignToTeam(val);
                    }
                  }}
                >
                  <SelectTrigger className="w-full bg-background border-border text-xs">
                    <SelectValue placeholder="Select target team" />
                  </SelectTrigger>
                  <SelectContent className="bg-card border-border text-foreground">
                    {COMMON_TEAMS.map((t) => (
                      <SelectItem key={t} value={t} className="text-xs">
                        {t}
                      </SelectItem>
                    ))}
                    <SelectItem value="CUSTOM" className="text-xs font-semibold text-blue-500">
                      + Custom Team Name...
                    </SelectItem>
                  </SelectContent>
                </Select>
                {isCustomTeam && (
                  <Input
                    required
                    placeholder="Enter custom team name"
                    value={customTeam}
                    onChange={(e) => setCustomTeam(e.target.value)}
                    className="mt-2 bg-background border-border text-foreground text-xs"
                  />
                )}
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-foreground">SLA Target (Hours)</label>
                <Input
                  type="number"
                  min={1}
                  max={720}
                  value={slaHours}
                  onChange={(e) => setSlaHours(parseInt(e.target.value) || 24)}
                  className="bg-background border-border text-foreground text-xs"
                />
              </div>
            </div>

            {/* Priority Override & Active Status */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 items-center">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-foreground">Priority Override</label>
                <Select
                  value={priorityOverride}
                  onValueChange={(val) => { if (val) setPriorityOverride(val); }}
                >
                  <SelectTrigger className="w-full bg-background border-border text-xs">
                    <SelectValue placeholder="Keep ticket priority" />
                  </SelectTrigger>
                  <SelectContent className="bg-card border-border text-foreground">
                    <SelectItem value="NONE" className="text-xs text-muted-foreground">Keep ticket priority (No override)</SelectItem>
                    <SelectItem value="LOW" className="text-xs text-slate-500">LOW</SelectItem>
                    <SelectItem value="MEDIUM" className="text-xs text-blue-500">MEDIUM</SelectItem>
                    <SelectItem value="HIGH" className="text-xs text-amber-500 font-semibold">HIGH</SelectItem>
                    <SelectItem value="CRITICAL" className="text-xs text-red-500 font-bold">CRITICAL</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="flex items-center gap-3 pt-4">
                <input
                  type="checkbox"
                  id="ruleActiveCheck"
                  checked={active}
                  onChange={(e) => setActive(e.target.checked)}
                  className="h-4 w-4 rounded border-border text-blue-600 focus:ring-blue-500"
                />
                <label htmlFor="ruleActiveCheck" className="text-xs font-medium text-foreground cursor-pointer select-none">
                  Rule is Active & Enabled
                </label>
              </div>
            </div>

            {/* Condition Patterns */}
            <div className="border-t border-border pt-3 space-y-3">
              <p className="text-[11px] font-bold text-muted-foreground uppercase tracking-wider">
                Matching Conditions (All matching conditions are evaluated)
              </p>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="space-y-1">
                  <label className="text-[11px] font-medium text-muted-foreground">Intent</label>
                  <Select value={intentPattern} onValueChange={(val) => { if (val) setIntentPattern(val); }}>
                    <SelectTrigger className="w-full bg-background border-border text-xs">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="bg-card border-border text-foreground">
                      {INTENT_OPTIONS.map((opt) => (
                        <SelectItem key={opt.value} value={opt.value} className="text-xs">
                          {opt.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-1">
                  <label className="text-[11px] font-medium text-muted-foreground">Urgency</label>
                  <Select value={urgencyPattern} onValueChange={(val) => { if (val) setUrgencyPattern(val); }}>
                    <SelectTrigger className="w-full bg-background border-border text-xs">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="bg-card border-border text-foreground">
                      {URGENCY_OPTIONS.map((opt) => (
                        <SelectItem key={opt.value} value={opt.value} className="text-xs">
                          {opt.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-1">
                  <label className="text-[11px] font-medium text-muted-foreground">Sentiment</label>
                  <Select value={sentimentPattern} onValueChange={(val) => { if (val) setSentimentPattern(val); }}>
                    <SelectTrigger className="w-full bg-background border-border text-xs">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent className="bg-card border-border text-foreground">
                      {SENTIMENT_OPTIONS.map((opt) => (
                        <SelectItem key={opt.value} value={opt.value} className="text-xs">
                          {opt.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {/* Keywords */}
              <div className="space-y-1.5">
                <label className="text-[11px] font-medium text-muted-foreground">Keywords Match</label>
                <div className="flex gap-2">
                  <Input
                    placeholder="Add keyword (e.g. refund, password, outage)..."
                    value={keywordInput}
                    onChange={(e) => setKeywordInput(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        e.preventDefault();
                        handleAddKeyword();
                      }
                    }}
                    className="bg-background border-border text-foreground text-xs"
                  />
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleAddKeyword}
                    className="shrink-0 text-xs border-border"
                  >
                    <Plus className="h-3.5 w-3.5 mr-1" /> Add
                  </Button>
                </div>

                {keywords.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 mt-2">
                    {keywords.map((kw) => (
                      <span
                        key={kw}
                        className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] bg-muted border border-border text-foreground"
                      >
                        {kw}
                        <button
                          type="button"
                          onClick={() => handleRemoveKeyword(kw)}
                          className="hover:text-red-500 transition-colors"
                        >
                          <Trash2 className="h-3 w-3" />
                        </button>
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Footer Buttons */}
            <div className="flex justify-end gap-2 border-t border-border pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => onOpenChange(false)}
                disabled={isLoading}
                className="text-xs"
              >
                Cancel
              </Button>
              <Button
                type="submit"
                variant="default"
                disabled={isLoading || !ruleName.trim()}
                className="text-xs"
              >
                {isLoading ? "Saving..." : isEdit ? "Update Rule" : "Create Rule"}
              </Button>
            </div>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  );
}
