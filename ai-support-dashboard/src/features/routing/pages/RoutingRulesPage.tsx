import { useState } from "react";
import { 
  Network, 
  Plus, 
  Search, 
  CheckCircle2, 
  Users, 
  Clock, 
  MoreVertical, 
  Pencil, 
  Trash2, 
  AlertTriangle 
} from "lucide-react";
import { 
  useRoutingRulesQuery, 
  useRoutingRuleStatsQuery, 
  useCreateRoutingRule, 
  useUpdateRoutingRule, 
  useToggleRoutingRule, 
  useDeleteRoutingRule 
} from "../hooks/useRoutingRules";
import type { RoutingRule, RoutingRuleRequest } from "../types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from "@/components/ui/table";
import { 
  Select, 
  SelectContent, 
  SelectItem, 
  SelectTrigger, 
  SelectValue 
} from "@/components/ui/select";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { PaginationControls } from "@/components/ui/pagination-controls";
import { RuleFormDialog } from "../components/RuleFormDialog";

export function RoutingRulesPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [search, setSearch] = useState("");
  const [activeFilter, setActiveFilter] = useState<string>("ALL");
  const [teamFilter, setTeamFilter] = useState<string>("ALL");

  // Dialog states
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedRule, setSelectedRule] = useState<RoutingRule | null>(null);
  const [ruleToDelete, setRuleToDelete] = useState<RoutingRule | null>(null);

  // Queries & Mutations
  const filterParams = {
    page,
    size,
    ...(search.trim() ? { search: search.trim() } : {}),
    ...(activeFilter !== "ALL" ? { active: activeFilter === "ACTIVE" } : {}),
    ...(teamFilter !== "ALL" ? { team: teamFilter } : {}),
  };

  const { data, isLoading, isError, refetch } = useRoutingRulesQuery(filterParams);
  const { data: stats } = useRoutingRuleStatsQuery();

  const createMutation = useCreateRoutingRule();
  const updateMutation = useUpdateRoutingRule();
  const toggleMutation = useToggleRoutingRule();
  const deleteMutation = useDeleteRoutingRule();

  const totalElements = data?.page?.totalElements ?? data?.totalElements ?? (data?.content?.length ?? 0);
  const totalPages = data?.page?.totalPages ?? data?.totalPages ?? (totalElements > 0 ? Math.ceil(totalElements / size) : 0);

  const handleOpenCreate = () => {
    setSelectedRule(null);
    setIsFormOpen(true);
  };

  const handleOpenEdit = (rule: RoutingRule) => {
    setSelectedRule(rule);
    setIsFormOpen(true);
  };

  const handleSaveRule = (request: RoutingRuleRequest) => {
    if (selectedRule) {
      updateMutation.mutate(
        { id: selectedRule.id, request },
        {
          onSuccess: () => {
            setIsFormOpen(false);
            setSelectedRule(null);
          },
        }
      );
    } else {
      createMutation.mutate(request, {
        onSuccess: () => {
          setIsFormOpen(false);
        },
      });
    }
  };

  const handleConfirmDelete = () => {
    if (!ruleToDelete) return;
    deleteMutation.mutate(ruleToDelete.id, {
      onSuccess: () => {
        setRuleToDelete(null);
      },
    });
  };

  return (
    <div className="h-full overflow-auto p-6 space-y-6 bg-background text-foreground">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2.5">
            <div className="h-8 w-8 rounded bg-[#0C66E4] flex items-center justify-center text-white">
              <Network className="h-5 w-5" />
            </div>
            Routing Rules
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Configure rule-driven ticket assignments, priority adjustments, and SLA targets for support operations.
          </p>
        </div>

        <Button onClick={handleOpenCreate} variant="default" className="shadow-sm">
          <Plus className="h-4 w-4 mr-1.5" />
          Create Rule
        </Button>
      </div>

      {/* Summary Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-card border border-border rounded-xl p-4 shadow-sm flex items-center gap-3">
          <div className="h-10 w-10 rounded-lg bg-blue-500/10 text-blue-500 flex items-center justify-center shrink-0">
            <Network className="h-5 w-5" />
          </div>
          <div>
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">
              Total Rules
            </span>
            <span className="text-xl font-bold text-foreground">
              {stats?.totalRules ?? "-"}
            </span>
          </div>
        </div>

        <div className="bg-card border border-border rounded-xl p-4 shadow-sm flex items-center gap-3">
          <div className="h-10 w-10 rounded-lg bg-emerald-500/10 text-emerald-500 flex items-center justify-center shrink-0">
            <CheckCircle2 className="h-5 w-5" />
          </div>
          <div>
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">
              Active Rules
            </span>
            <span className="text-xl font-bold text-emerald-600">
              {stats?.activeRules ?? "-"}
            </span>
          </div>
        </div>

        <div className="bg-card border border-border rounded-xl p-4 shadow-sm flex items-center gap-3">
          <div className="h-10 w-10 rounded-lg bg-purple-500/10 text-purple-500 flex items-center justify-center shrink-0">
            <Users className="h-5 w-5" />
          </div>
          <div>
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">
              Target Teams
            </span>
            <span className="text-xl font-bold text-foreground">
              {stats?.totalTeamsCount ?? "-"}
            </span>
          </div>
        </div>

        <div className="bg-card border border-border rounded-xl p-4 shadow-sm flex items-center gap-3">
          <div className="h-10 w-10 rounded-lg bg-amber-500/10 text-amber-500 flex items-center justify-center shrink-0">
            <Clock className="h-5 w-5" />
          </div>
          <div>
            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">
              Default Fallback
            </span>
            <span className="text-xs font-bold text-foreground truncate block max-w-[140px]" title="general-support">
              general-support
            </span>
          </div>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <div className="flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative flex-1 w-full max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search by rule name, description, team..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            className="pl-9 bg-card border-border text-foreground text-xs"
          />
        </div>

        <div className="flex flex-wrap items-center gap-2.5 w-full sm:w-auto">
          {/* Status Filter */}
          <Select
            value={activeFilter}
            onValueChange={(val) => {
              if (val) {
                setActiveFilter(val);
                setPage(0);
              }
            }}
          >
            <SelectTrigger className="w-36 bg-card border-border text-xs">
              <SelectValue placeholder="All Status" />
            </SelectTrigger>
            <SelectContent className="bg-card border-border text-foreground">
              <SelectItem value="ALL" className="text-xs">All Statuses</SelectItem>
              <SelectItem value="ACTIVE" className="text-xs">Active Only</SelectItem>
              <SelectItem value="INACTIVE" className="text-xs">Inactive Only</SelectItem>
            </SelectContent>
          </Select>

          {/* Team Filter */}
          <Select
            value={teamFilter}
            onValueChange={(val) => {
              if (val) {
                setTeamFilter(val);
                setPage(0);
              }
            }}
          >
            <SelectTrigger className="w-44 bg-card border-border text-xs">
              <SelectValue placeholder="All Teams" />
            </SelectTrigger>
            <SelectContent className="bg-card border-border text-foreground">
              <SelectItem value="ALL" className="text-xs">All Teams</SelectItem>
              {(stats?.targetTeams || []).map((team) => (
                <SelectItem key={team} value={team} className="text-xs">
                  {team}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Rules Table */}
      {isLoading ? (
        <div className="h-64 flex items-center justify-center rounded-xl border border-border bg-card">
          <div className="flex flex-col items-center gap-3">
            <div className="h-8 w-8 rounded-full border-2 border-blue-500 border-t-transparent animate-spin" />
            <p className="text-sm text-muted-foreground font-medium animate-pulse">Loading routing rules...</p>
          </div>
        </div>
      ) : isError ? (
        <div className="h-64 flex flex-col items-center justify-center rounded-xl border border-red-500/20 bg-red-500/5 text-red-400 gap-2">
          <AlertTriangle className="h-8 w-8" />
          <p className="font-semibold">Failed to load routing rules</p>
          <p className="text-sm opacity-80">Verify routing-service connection and try again.</p>
          <Button variant="outline" size="sm" onClick={() => refetch()} className="mt-2 text-xs">
            Retry
          </Button>
        </div>
      ) : (
        <div className="rounded-xl border border-border bg-card overflow-hidden shadow-sm">
          <Table>
            <TableHeader className="bg-muted/30">
              <TableRow className="border-border hover:bg-transparent">
                <TableHead className="text-muted-foreground font-semibold text-xs">Rule & Description</TableHead>
                <TableHead className="text-muted-foreground font-semibold text-xs">Target Team</TableHead>
                <TableHead className="text-muted-foreground font-semibold text-xs">Conditions</TableHead>
                <TableHead className="text-muted-foreground font-semibold text-xs text-center">Priority</TableHead>
                <TableHead className="text-muted-foreground font-semibold text-xs text-center">SLA</TableHead>
                <TableHead className="text-muted-foreground font-semibold text-xs text-center">Status</TableHead>
                <TableHead className="text-muted-foreground font-semibold text-xs w-16 text-center">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {(!data?.content || data.content.length === 0) ? (
                <TableRow className="border-border hover:bg-transparent">
                  <TableCell colSpan={7} className="p-10 text-center text-muted-foreground">
                    <div className="max-w-sm mx-auto flex flex-col items-center gap-2">
                      <Network className="h-8 w-8 text-muted-foreground/50 mb-1" />
                      <p className="font-semibold text-foreground text-sm">No routing rules found</p>
                      <p className="text-xs text-muted-foreground">
                        {search || activeFilter !== "ALL" || teamFilter !== "ALL"
                          ? "Try clearing filters to find what you are looking for."
                          : "Get started by creating your first automated routing rule."}
                      </p>
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                data.content.map((rule) => (
                  <TableRow key={rule.id} className="border-border hover:bg-muted/20 transition-colors">
                    
                    {/* Name & Description */}
                    <TableCell className="py-3 max-w-[240px]">
                      <div className="flex flex-col">
                        <span className="font-semibold text-foreground text-xs leading-tight">
                          {rule.ruleName}
                        </span>
                        {rule.description && (
                          <span className="text-[11px] text-muted-foreground line-clamp-1 mt-0.5" title={rule.description}>
                            {rule.description}
                          </span>
                        )}
                      </div>
                    </TableCell>

                    {/* Target Team */}
                    <TableCell className="py-3">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold bg-blue-500/10 text-[#0C66E4] border border-blue-500/20">
                        {rule.assignToTeam}
                      </span>
                    </TableCell>

                    {/* Matching Conditions */}
                    <TableCell className="py-3">
                      <div className="flex flex-wrap gap-1 items-center max-w-[320px]">
                        {rule.intentPattern && (
                          <span className="px-1.5 py-0.5 rounded text-[10px] font-bold uppercase bg-indigo-500/10 text-indigo-600 border border-indigo-500/20">
                            Intent: {rule.intentPattern}
                          </span>
                        )}
                        {rule.urgencyPattern && (
                          <span className="px-1.5 py-0.5 rounded text-[10px] font-bold uppercase bg-rose-500/10 text-rose-600 border border-rose-500/20">
                            Urgency: {rule.urgencyPattern}
                          </span>
                        )}
                        {rule.sentimentPattern && (
                          <span className="px-1.5 py-0.5 rounded text-[10px] font-bold uppercase bg-amber-500/10 text-amber-600 border border-amber-500/20">
                            Sentiment: {rule.sentimentPattern}
                          </span>
                        )}
                        {rule.keywordPatterns && rule.keywordPatterns.length > 0 && (
                          <span className="px-1.5 py-0.5 rounded text-[10px] font-medium bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 border border-border">
                            {rule.keywordPatterns.length} keywords
                          </span>
                        )}
                        {!rule.intentPattern && !rule.urgencyPattern && !rule.sentimentPattern && (!rule.keywordPatterns || rule.keywordPatterns.length === 0) && (
                          <span className="text-[11px] text-muted-foreground italic">Wildcard (Matches All)</span>
                        )}
                      </div>
                    </TableCell>

                    {/* Priority */}
                    <TableCell className="py-3 text-center">
                      <span className="text-xs font-bold text-foreground">
                        {rule.priority}
                      </span>
                    </TableCell>

                    {/* SLA */}
                    <TableCell className="py-3 text-center">
                      <span className="text-xs font-semibold text-muted-foreground">
                        {rule.slaHours ? `${rule.slaHours}h` : "24h"}
                      </span>
                    </TableCell>

                    {/* Active Toggle Switch */}
                    <TableCell className="py-3 text-center">
                      <button
                        onClick={() => toggleMutation.mutate(rule.id)}
                        disabled={toggleMutation.isPending}
                        className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold transition-colors cursor-pointer border ${
                          rule.active
                            ? "bg-emerald-500/10 text-emerald-600 border-emerald-500/20 hover:bg-emerald-500/20"
                            : "bg-muted text-muted-foreground border-border hover:bg-muted/80"
                        }`}
                        title="Click to toggle rule status"
                      >
                        {rule.active ? (
                          <>
                            <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                            Active
                          </>
                        ) : (
                          <>
                            <span className="h-1.5 w-1.5 rounded-full bg-slate-400" />
                            Inactive
                          </>
                        )}
                      </button>
                    </TableCell>

                    {/* Actions Menu */}
                    <TableCell className="py-3 text-center">
                      <DropdownMenu>
                        <DropdownMenuTrigger className="h-8 w-8 p-0 text-muted-foreground hover:text-foreground hover:bg-muted rounded-md inline-flex items-center justify-center outline-none border-none">
                          <MoreVertical className="h-4 w-4" />
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="bg-card border-border text-foreground">
                          <DropdownMenuItem onClick={() => handleOpenEdit(rule)} className="text-xs cursor-pointer">
                            <Pencil className="h-3.5 w-3.5 mr-2" />
                            Edit Rule
                          </DropdownMenuItem>
                          <DropdownMenuItem 
                            onClick={() => setRuleToDelete(rule)} 
                            className="text-xs text-red-600 focus:text-red-600 cursor-pointer"
                          >
                            <Trash2 className="h-3.5 w-3.5 mr-2" />
                            Delete Rule
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>

          {/* Global Pagination Controls */}
          <div className="p-4">
            <PaginationControls
              currentPage={page}
              totalPages={totalPages}
              totalElements={totalElements}
              pageSize={size}
              entityName="rules"
              onPageChange={setPage}
              onPageSizeChange={(newSize) => {
                setSize(newSize);
                setPage(0);
              }}
            />
          </div>
        </div>
      )}

      {/* Create / Edit Rule Dialog */}
      <RuleFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        rule={selectedRule}
        onSave={handleSaveRule}
        isLoading={createMutation.isPending || updateMutation.isPending}
      />

      {/* Delete Rule Confirmation Dialog */}
      <AlertDialog open={!!ruleToDelete} onOpenChange={(open) => !open && setRuleToDelete(null)}>
        <AlertDialogContent className="bg-card border-border text-foreground">
          <AlertDialogHeader>
            <AlertDialogTitle className="text-base font-bold text-foreground">
              Delete Routing Rule?
            </AlertDialogTitle>
            <AlertDialogDescription className="text-xs text-muted-foreground">
              Are you sure you want to delete the rule <strong>"{ruleToDelete?.ruleName}"</strong>? This action cannot be undone and will stop automatic routing for this criteria.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel className="text-xs">Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleConfirmDelete}
              className="bg-red-600 hover:bg-red-700 text-white text-xs"
            >
              Delete Rule
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
