export type KnowledgeArticleStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'DEPRECATED';

export type EmbeddingStatus = 'PENDING' | 'PROCESSING' | 'READY' | 'FAILED';

export interface KnowledgeArticle {
  id: number;
  title: string;
  content: string;
  embeddingStatus: EmbeddingStatus;
  accessCount: number;
  status: KnowledgeArticleStatus;
  category: string;
  tags: string[];
  lastAccessedAt: string;
  authorId: string;
  version: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ArticleSearchRequest {
  query?: string | undefined;
  status?: KnowledgeArticleStatus | undefined;
  category?: string | undefined;
  tags?: string[] | undefined;
  page?: number | undefined;
  size?: number | undefined;
  sortBy?: string | undefined;
  sortDirection?: 'asc' | 'desc' | undefined;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
