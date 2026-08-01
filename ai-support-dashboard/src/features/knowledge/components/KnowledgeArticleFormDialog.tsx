import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import type { KnowledgeArticle, KnowledgeArticleStatus } from '../types';

interface KnowledgeArticleFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialData?: KnowledgeArticle | null;
  onSubmit: (data: Partial<KnowledgeArticle>) => void;
}

export function KnowledgeArticleFormDialog({
  open,
  onOpenChange,
  initialData,
  onSubmit,
}: KnowledgeArticleFormDialogProps) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState('');
  const [tags, setTags] = useState('');
  const [status, setStatus] = useState<KnowledgeArticleStatus>('DRAFT');

  useEffect(() => {
    if (initialData) {
      setTitle(initialData.title || '');
      setContent(initialData.content || '');
      setCategory(initialData.category || '');
      setTags((initialData.tags || []).join(', '));
      setStatus(initialData.status || 'DRAFT');
    } else {
      setTitle('');
      setContent('');
      setCategory('');
      setTags('');
      setStatus('DRAFT');
    }
  }, [initialData, open]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      title,
      content,
      category,
      tags: tags.split(',').map((t) => t.trim()).filter(Boolean),
      status,
    });
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="bg-background border-border text-foreground sm:max-w-[550px]">
        <DialogHeader>
          <DialogTitle className="text-lg font-semibold pr-8">
            {initialData ? 'Edit Knowledge Article' : 'Create Knowledge Article'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 mt-2">
          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">
              Title
            </label>
            <Input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Enter article title"
              className="bg-card border-border focus-visible:ring-[#0C66E4]"
              required
            />
          </div>

          <div className="space-y-2 flex flex-col">
            <div className="flex items-center justify-between">
              <label className="text-sm font-medium text-foreground">
                Content
              </label>
            </div>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Write the article content..."
              className="min-h-[120px] bg-card border border-border rounded-md p-3 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#0C66E4] resize-none"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-foreground">
                Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-card px-3 py-2 text-sm ring-offset-background text-foreground focus:outline-none focus:ring-2 focus:ring-[#0C66E4] focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <option value="">Select Category</option>
                <option value="Authentication">Authentication</option>
                <option value="Billing">Billing</option>
                <option value="Payments">Payments</option>
                <option value="User Management">User Management</option>
                <option value="Security">Security</option>
                <option value="API">API</option>
                <option value="Networking">Networking</option>
                <option value="Compliance">Compliance</option>
                <option value="General">General</option>
              </select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-foreground">
                Status
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as KnowledgeArticleStatus)}
                className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-card px-3 py-2 text-sm ring-offset-background text-foreground focus:outline-none focus:ring-2 focus:ring-[#0C66E4] focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <option value="DRAFT">Draft</option>
                <option value="PUBLISHED">Published</option>
                <option value="ARCHIVED">Archived</option>
                <option value="DEPRECATED">Deprecated</option>
              </select>
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-foreground">
              Tags (comma-separated)
            </label>
            <Input
              value={tags}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setTags(e.target.value)}
              placeholder="e.g. sso, authentication, enterprise"
              className="bg-card border-border focus-visible:ring-[#0C66E4]"
            />
          </div>

          <DialogFooter className="border-t border-border pt-4 flex-col sm:flex-row gap-3">
            <div className="flex gap-2 justify-end w-full">
              <Button 
                type="button" 
                variant="outline" 
                onClick={() => onOpenChange(false)}
                className="border-border hover:bg-muted text-foreground shadow-sm"
              >
                Cancel
              </Button>
              <Button 
                type="submit" 
                className="min-w-[120px] bg-[#0C66E4] hover:bg-[#0052CC] text-white"
              >
                {initialData ? 'Save Changes' : 'Create Article'}
              </Button>
            </div>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
