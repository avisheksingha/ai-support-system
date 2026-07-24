import { Button } from "@/components/ui/button";

interface PaginationControlsProps {
  currentPage: number;
  totalPages: number;
  totalElements?: number | undefined;
  pageSize: number;
  onPageChange: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  pageSizeOptions?: number[];
  entityName?: string;
}

export function getPageNumbers(currentPage: number, totalPages: number): (number | 'ellipsis')[] {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  const pages: (number | 'ellipsis')[] = [];
  pages.push(0);

  if (currentPage > 2) {
    pages.push('ellipsis');
  }

  const start = Math.max(1, currentPage - 1);
  const end = Math.min(totalPages - 2, currentPage + 1);

  for (let i = start; i <= end; i++) {
    if (i > 0 && i < totalPages - 1) {
      pages.push(i);
    }
  }

  if (currentPage < totalPages - 3) {
    pages.push('ellipsis');
  }

  pages.push(totalPages - 1);

  return pages;
}

export function PaginationControls({
  currentPage,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
  onPageSizeChange,
  pageSizeOptions = [10, 20, 50, 100],
  entityName = "items",
}: PaginationControlsProps) {
  const startItem = totalElements ? currentPage * pageSize + 1 : 0;
  const endItem = totalElements ? Math.min((currentPage + 1) * pageSize, totalElements) : 0;

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-4 mt-4 border-t border-border">
      <div className="flex items-center gap-4 text-sm text-muted-foreground">
        <p>
          {totalElements
            ? `Showing ${startItem} to ${endItem} of ${totalElements} ${entityName}`
            : `No ${entityName} found`}
        </p>
        {onPageSizeChange && (
          <div className="flex items-center gap-2 border-l border-border pl-4">
            <span className="text-xs">Per page:</span>
            <select
              value={pageSize}
              onChange={(e) => onPageSizeChange(Number(e.target.value))}
              className="h-8 rounded-md border border-input bg-background px-2 py-1 text-xs text-foreground focus:outline-none focus:ring-1 focus:ring-blue-500 cursor-pointer"
            >
              {pageSizeOptions.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(Math.max(0, currentPage - 1))}
          disabled={currentPage === 0}
          className="h-8 px-3 text-xs shadow-sm bg-background border-border text-foreground hover:bg-muted transition-colors disabled:opacity-40"
        >
          Previous
        </Button>

        {totalPages > 0 && (
          <div className="flex items-center gap-1 mx-1">
            {getPageNumbers(currentPage, totalPages).map((pageIdx, idx) =>
              pageIdx === 'ellipsis' ? (
                <span key={`ellipsis-${idx}`} className="text-muted-foreground text-xs px-1.5 select-none">
                  ...
                </span>
              ) : (
                <button
                  key={pageIdx}
                  onClick={() => onPageChange(pageIdx)}
                  className={`h-8 min-w-8 px-2 rounded-md text-xs font-medium transition-colors ${
                    currentPage === pageIdx
                      ? "bg-[#0C66E4] text-white font-semibold shadow-sm"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground"
                  }`}
                >
                  {pageIdx + 1}
                </button>
              )
            )}
          </div>
        )}

        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(currentPage + 1)}
          disabled={currentPage >= totalPages - 1}
          className="h-8 px-3 text-xs shadow-sm bg-background border-border text-foreground hover:bg-muted transition-colors disabled:opacity-40"
        >
          Next
        </Button>
      </div>
    </div>
  );
}
