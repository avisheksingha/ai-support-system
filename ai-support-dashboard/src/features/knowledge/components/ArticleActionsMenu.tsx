import { useState } from "react";
import { MoreHorizontal, Edit3 } from "lucide-react";
import type { KnowledgeArticle } from "../types";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface ArticleActionsMenuProps {
  article: KnowledgeArticle;
  onEdit: () => void;
}

export function ArticleActionsMenu({ onEdit }: ArticleActionsMenuProps) {
  const [open, setOpen] = useState(false);

  return (
    <DropdownMenu open={open} onOpenChange={setOpen}>
      <DropdownMenuTrigger 
        className="h-8 w-8 p-0 text-muted-foreground hover:text-foreground hover:bg-muted rounded-md inline-flex items-center justify-center outline-none border-none transition-colors"
      >
        <span className="sr-only">Open menu</span>
        <MoreHorizontal className="h-4 w-4" />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-40 bg-background border-border text-foreground">
        <DropdownMenuItem
          onClick={onEdit}
          className="focus:bg-card cursor-pointer"
        >
          <Edit3 className="mr-2 h-4 w-4" />
          <span>Edit Article</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
