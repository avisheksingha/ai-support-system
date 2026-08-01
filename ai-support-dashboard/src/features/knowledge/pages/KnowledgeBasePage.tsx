import { useState } from "react";
import { 
  BookOpen, 
  Search, 
  Filter, 
  Plus, 
  BarChart3, 
  Database,
  FileText,
  CheckCircle2,
  FileEdit,
  FolderTree,
  Clock,
  Send,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useSearchArticles, useCreateArticle, useUpdateArticle, useSyncEmbeddings, useKnowledgeStats, useBulkPublish } from "../api/useKnowledge";
import type { ArticleSearchRequest, KnowledgeArticleStatus, KnowledgeArticle } from "../types";
import { KnowledgeArticleFormDialog } from "../components/KnowledgeArticleFormDialog";
import { ArticleActionsMenu } from "../components/ArticleActionsMenu";
import { PaginationControls } from "@/components/ui/pagination-controls";
import { formatDateStr } from "@/shared/utils/date";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

const BUSINESS_CATEGORIES = [
  "Authentication",
  "Billing",
  "Payments",
  "User Management",
  "Security",
  "API",
  "Networking",
  "Compliance",
  "General"
];



export function KnowledgeBasePage() {
  const [searchRequest, setSearchRequest] = useState<ArticleSearchRequest>({
    page: 0,
    size: 10,
    sortBy: 'accessCount',
    sortDirection: 'desc'
  });

  const currentPage = searchRequest.page ?? 0;
  const currentSize = searchRequest.size ?? 10;

  const { data, isLoading, isError, refetch } = useSearchArticles(searchRequest);
  const { data: statsData } = useKnowledgeStats();
  
  const createMutation = useCreateArticle();
  const updateMutation = useUpdateArticle();
  const syncMutation = useSyncEmbeddings();
  const bulkPublishMutation = useBulkPublish();

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingArticle, setEditingArticle] = useState<KnowledgeArticle | null>(null);
  const [showBulkPublishConfirm, setShowBulkPublishConfirm] = useState(false);

  const handleCreateClick = () => {
    setEditingArticle(null);
    setIsFormOpen(true);
  };

  const handleEditClick = (article: KnowledgeArticle) => {
    setEditingArticle(article);
    setIsFormOpen(true);
  };

  const handleFormSubmit = (formData: Partial<KnowledgeArticle>) => {
    if (editingArticle) {
      updateMutation.mutate({ id: editingArticle.id, article: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleSyncClick = () => {
    syncMutation.mutate(undefined, {
      onSuccess: () => {
        refetch();
      }
    });
  };

  const getStatusColor = (status: KnowledgeArticleStatus) => {
    switch (status) {
      case 'PUBLISHED': return 'bg-emerald-500/15 text-emerald-700 border-emerald-500/20';
      case 'DRAFT': return 'bg-amber-500/15 text-amber-700 border-amber-500/20';
      case 'ARCHIVED': return 'bg-zinc-500/15 text-zinc-700 border-zinc-500/20';
      case 'DEPRECATED': return 'bg-red-500/15 text-red-700 border-red-500/20';
      default: return 'bg-zinc-100 text-zinc-800';
    }
  };

  // Global Summary Metrics (from DB stats across all 121 articles)
  const totalArticles = statsData?.totalArticles ?? data?.totalElements ?? 0;
  const publishedCount = statsData?.publishedCount ?? 0;
  const draftCount = statsData?.draftCount ?? totalArticles;
  const embeddedCount = statsData?.embeddedCount ?? totalArticles;
  const categoriesCount = statsData?.categoriesCount ?? 1;
  const pendingOrFailedCount = statsData?.pendingCount ?? 0;

  return (
    <div className="h-full overflow-auto p-6 space-y-6">
      {/* Header Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <BookOpen className="h-6 w-6 text-blue-400" />
            Enterprise Knowledge Base
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Manage knowledge articles, retrieval quality, and AI knowledge lifecycle.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button 
            variant="outline" 
            className="w-full sm:w-auto border-border bg-background text-foreground hover:bg-card hover:text-foreground gap-2" 
            onClick={handleSyncClick} 
            disabled={syncMutation.isPending || pendingOrFailedCount === 0}
          >
            <Database className="h-4 w-4 text-blue-500" />
            {syncMutation.isPending ? 'Processing...' : pendingOrFailedCount === 0 ? 'Embeddings Up to Date' : 'Process Pending Embeddings'}
          </Button>
          {draftCount > 0 && (
            <Button 
              variant="outline"
              className="w-full sm:w-auto border-emerald-500/30 bg-emerald-500/5 text-emerald-700 hover:bg-emerald-500/15 hover:text-emerald-800 gap-2"
              onClick={() => setShowBulkPublishConfirm(true)}
              disabled={bulkPublishMutation.isPending}
            >
              <Send className="h-4 w-4" />
              {bulkPublishMutation.isPending ? 'Publishing...' : `Publish All Drafts (${draftCount})`}
            </Button>
          )}
          <Button className="w-full sm:w-auto bg-[#0C66E4] hover:bg-[#0052CC] text-white gap-2" onClick={handleCreateClick}>
            <Plus className="h-4 w-4" />
            Create Article
          </Button>
        </div>
      </div>

      {/* KPI Summary Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
        <div className="p-3.5 rounded-xl border border-border bg-card flex flex-col justify-between shadow-sm">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium">Total Articles</span>
            <FileText className="h-4 w-4 text-blue-500" />
          </div>
          <span className="text-xl font-bold text-foreground mt-2">{totalArticles}</span>
        </div>

        <div className="p-3.5 rounded-xl border border-border bg-card flex flex-col justify-between shadow-sm">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium">Published</span>
            <CheckCircle2 className="h-4 w-4 text-emerald-500" />
          </div>
          <span className="text-xl font-bold text-foreground mt-2">{publishedCount}</span>
        </div>

        <div className="p-3.5 rounded-xl border border-border bg-card flex flex-col justify-between shadow-sm">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium">Draft</span>
            <FileEdit className="h-4 w-4 text-amber-500" />
          </div>
          <span className="text-xl font-bold text-foreground mt-2">{draftCount}</span>
        </div>

        <div className="p-3.5 rounded-xl border border-border bg-card flex flex-col justify-between shadow-sm">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium">Embedded</span>
            <Database className="h-4 w-4 text-indigo-500" />
          </div>
          <span className="text-xl font-bold text-foreground mt-2">{embeddedCount}</span>
        </div>

        <div className="p-3.5 rounded-xl border border-border bg-card flex flex-col justify-between shadow-sm">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium">Categories</span>
            <FolderTree className="h-4 w-4 text-purple-500" />
          </div>
          <span className="text-xl font-bold text-foreground mt-2">{categoriesCount}</span>
        </div>

        <div className="p-3.5 rounded-xl border border-border bg-card flex flex-col justify-between shadow-sm">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-medium">Pending Sync</span>
            <Clock className="h-4 w-4 text-amber-500" />
          </div>
          <span className="text-xl font-bold text-foreground mt-2">{pendingOrFailedCount}</span>
        </div>
      </div>

      {/* Filters Bar */}
      <div className="flex flex-col sm:flex-row gap-3 items-center">
        <div className="relative flex-1 w-full max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search by title, content, category or tags..."
            className="pl-9 bg-background border-border text-foreground placeholder:text-muted-foreground focus-visible:ring-blue-500"
            value={searchRequest.query || ''}
            onChange={(e) => setSearchRequest(prev => ({ ...prev, query: e.target.value, page: 0 }))}
          />
        </div>
        
        <Select 
          value={searchRequest.category || searchRequest.status || 'ALL'} 
          onValueChange={(val: string | null) => {
            setSearchRequest(prev => {
              const req: ArticleSearchRequest = { ...prev, page: 0, category: undefined, status: undefined };
              if (val && val !== 'ALL') {
                if (BUSINESS_CATEGORIES.includes(val)) {
                  req.category = val;
                } else {
                  req.status = val as KnowledgeArticleStatus;
                }
              }
              return req;
            });
          }}
        >
          <SelectTrigger className="w-[190px] bg-background border-border text-foreground focus-visible:ring-blue-500">
            <SelectValue placeholder="All Categories" />
          </SelectTrigger>
          <SelectContent className="bg-background border-border text-foreground">
            <SelectItem value="ALL">All Categories & Statuses</SelectItem>
            <div className="px-2 py-1.5 text-xs font-semibold text-muted-foreground uppercase border-t border-border mt-1">Categories</div>
            {BUSINESS_CATEGORIES.map(cat => (
              <SelectItem key={cat} value={cat}>{cat}</SelectItem>
            ))}
            <div className="px-2 py-1.5 text-xs font-semibold text-muted-foreground uppercase border-t border-border mt-1">Statuses</div>
            <SelectItem value="PUBLISHED">Published</SelectItem>
            <SelectItem value="DRAFT">Draft</SelectItem>
            <SelectItem value="ARCHIVED">Archived</SelectItem>
            <SelectItem value="DEPRECATED">Deprecated</SelectItem>
          </SelectContent>
        </Select>

        <Button variant="outline" className="w-full sm:w-auto border-border bg-background text-foreground hover:bg-card hover:text-foreground gap-2">
          <Filter className="h-4 w-4" />
          Filters
        </Button>
      </div>

      {/* Table / Content */}
      {isLoading ? (
        <div className="h-64 flex items-center justify-center rounded-xl border border-border bg-background">
          <div className="flex flex-col items-center gap-3">
            <div className="h-8 w-8 rounded-full border-2 border-blue-500 border-t-transparent animate-spin" />
            <p className="text-sm text-muted-foreground font-medium animate-pulse">Loading articles...</p>
          </div>
        </div>
      ) : isError ? (
        <div className="h-64 flex flex-col items-center justify-center rounded-xl border border-red-500/20 bg-red-500/5 text-red-400 gap-2">
          <p className="font-semibold">Failed to load knowledge articles</p>
          <p className="text-sm opacity-80">Please check backend connection.</p>
        </div>
      ) : (
        <>
          <div className="rounded-xl border border-border bg-background overflow-hidden shadow-sm">
            <div className="overflow-x-auto">
              <Table>
                <TableHeader className="bg-muted/30">
                  <TableRow className="border-border hover:bg-transparent">
                    <TableHead className="text-muted-foreground font-medium w-2/5">Article</TableHead>
                    <TableHead className="text-muted-foreground font-medium">Status</TableHead>
                    <TableHead className="text-muted-foreground font-medium">Category</TableHead>
                    <TableHead className="text-muted-foreground font-medium">Access / Sync</TableHead>
                    <TableHead className="text-muted-foreground font-medium">Last Updated</TableHead>
                    <TableHead className="text-muted-foreground font-medium text-center w-20">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {!data || !data.content || data.content.length === 0 ? (
                    <TableRow className="border-border hover:bg-transparent">
                      <TableCell colSpan={6} className="p-6">
                        <div className="text-slate-500 p-8 text-sm text-center border-2 border-dashed border-slate-200 rounded-xl max-w-md mx-auto bg-white">
                          <span className="block text-2xl mb-2">📭</span>
                          <p className="font-semibold text-foreground">No articles found.</p>
                          <p className="text-xs text-muted-foreground mt-1">Try adjusting your search terms or filter criteria.</p>
                        </div>
                      </TableCell>
                    </TableRow>
                  ) : (
                    data.content.map((article) => (
                      <TableRow key={article.id} className="border-border hover:bg-card transition-colors group">
                        <TableCell className="py-3">
                          <div className="flex flex-col gap-0.5">
                            <div 
                              className="font-medium text-foreground hover:text-blue-500 transition-colors cursor-pointer text-sm" 
                              onClick={() => handleEditClick(article)}
                            >
                              {article.title}
                            </div>
                            {article.content && (
                              <p className="text-xs text-muted-foreground max-w-lg font-normal truncate">
                                {article.content.length > 85
                                  ? `${article.content.substring(0, 85).trim()}...`
                                  : article.content
                                }
                              </p>
                            )}
                            {article.tags && article.tags.length > 0 && (
                              <div className="flex flex-wrap gap-1.5 mt-1">
                                {article.tags.map(tag => (
                                  <span key={tag} className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-muted text-muted-foreground">
                                    {tag}
                                  </span>
                                ))}
                              </div>
                            )}
                          </div>
                        </TableCell>
                        <TableCell className="py-3">
                          <Badge variant="outline" className={`text-[10px] font-semibold border ${getStatusColor(article.status)}`}>
                            {article.status}
                          </Badge>
                        </TableCell>
                        <TableCell className="py-3">
                          <span className="text-xs font-medium px-2 py-0.5 rounded bg-muted text-muted-foreground border border-border">
                            {article.category || 'General'}
                          </span>
                        </TableCell>
                        <TableCell className="py-3">
                          <div className="flex flex-col gap-1">
                            <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                              <BarChart3 className="h-3.5 w-3.5" />
                              {article.accessCount || 0} views
                            </div>
                            <div className="flex items-center gap-1.5 text-xs">
                              <div className={`h-1.5 w-1.5 rounded-full ${article.embeddingStatus === 'READY' ? 'bg-emerald-500' : article.embeddingStatus === 'PROCESSING' ? 'bg-blue-500' : article.embeddingStatus === 'FAILED' ? 'bg-red-500' : 'bg-amber-500'}`} />
                              <span className={article.embeddingStatus === 'READY' ? 'text-emerald-700' : article.embeddingStatus === 'PROCESSING' ? 'text-blue-700' : article.embeddingStatus === 'FAILED' ? 'text-red-700' : 'text-amber-700'}>
                                {article.embeddingStatus || 'PENDING'}
                              </span>
                            </div>
                          </div>
                        </TableCell>
                        <TableCell className="py-3 text-xs text-muted-foreground">
                          {article.updatedAt || article.createdAt ? formatDateStr(article.updatedAt || article.createdAt) : '—'}
                        </TableCell>
                        <TableCell className="py-3 text-center">
                          <ArticleActionsMenu 
                            article={article} 
                            onEdit={() => handleEditClick(article)} 
                          />
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          </div>

          {/* Global Pagination Controls */}
          <PaginationControls
            currentPage={currentPage}
            totalPages={data?.totalPages ?? 0}
            totalElements={data?.totalElements}
            pageSize={currentSize}
            entityName="articles"
            onPageChange={(page) => setSearchRequest(prev => ({ ...prev, page }))}
            onPageSizeChange={(size) => setSearchRequest(prev => ({ ...prev, size, page: 0 }))}
          />
        </>
      )}

      <KnowledgeArticleFormDialog
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        initialData={editingArticle}
        onSubmit={handleFormSubmit}
      />

      {/* Bulk Publish Confirmation Dialog */}
      {showBulkPublishConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-background border border-border rounded-xl shadow-xl p-6 max-w-md w-full mx-4 space-y-4">
            <h3 className="text-lg font-semibold text-foreground">Publish All Draft Articles</h3>
            <p className="text-sm text-muted-foreground">
              This will publish <span className="font-semibold text-foreground">{draftCount}</span> draft articles.
              Embedding status will not be affected.
            </p>
            <div className="flex justify-end gap-3 pt-2">
              <Button
                variant="outline"
                className="border-border hover:bg-muted text-foreground"
                onClick={() => setShowBulkPublishConfirm(false)}
                disabled={bulkPublishMutation.isPending}
              >
                Cancel
              </Button>
              <Button
                className="bg-[#0C66E4] hover:bg-[#0052CC] text-white gap-2"
                disabled={bulkPublishMutation.isPending}
                onClick={() => {
                  bulkPublishMutation.mutate(undefined, {
                    onSuccess: (result) => {
                      setShowBulkPublishConfirm(false);
                      refetch();
                      alert(`${result.publishedCount} articles published successfully.`);
                    },
                    onError: () => {
                      setShowBulkPublishConfirm(false);
                    }
                  });
                }}
              >
                <Send className="h-4 w-4" />
                {bulkPublishMutation.isPending ? 'Publishing...' : 'Confirm Publish'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
