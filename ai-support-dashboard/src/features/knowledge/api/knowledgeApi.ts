import { apiClient } from '@/lib/api-client';
import type { KnowledgeArticle, ArticleSearchRequest, Page } from '../types';

const API_BASE_URL = '/orchestration/knowledge-base';

export const knowledgeApi = {
  searchArticles: async (request: ArticleSearchRequest): Promise<Page<KnowledgeArticle>> => {
    const response = await apiClient.post(`${API_BASE_URL}/search`, request);
    return response.data;
  },

  getArticle: async (id: number): Promise<KnowledgeArticle> => {
    const response = await apiClient.get(`${API_BASE_URL}/articles/${id}`);
    return response.data;
  },

  createArticle: async (article: Partial<KnowledgeArticle>): Promise<KnowledgeArticle> => {
    const response = await apiClient.post(`${API_BASE_URL}/articles`, article);
    return response.data;
  },

  updateArticle: async (id: number, article: Partial<KnowledgeArticle>): Promise<KnowledgeArticle> => {
    const response = await apiClient.put(`${API_BASE_URL}/articles/${id}`, article);
    return response.data;
  },

  deleteArticle: async (id: number): Promise<void> => {
    await apiClient.delete(`${API_BASE_URL}/articles/${id}`);
  },

  syncEmbeddings: async (): Promise<{ embeddedCount: number }> => {
    const response = await apiClient.post(`${API_BASE_URL}/articles/sync-embeddings`);
    return response.data;
  },

  getStats: async (): Promise<{
    totalArticles: number;
    publishedCount: number;
    draftCount: number;
    embeddedCount: number;
    categoriesCount: number;
    pendingCount: number;
  }> => {
    const response = await apiClient.get(`${API_BASE_URL}/stats`);
    return response.data;
  },

  bulkPublish: async (): Promise<{ publishedCount: number; message: string }> => {
    const response = await apiClient.post(`${API_BASE_URL}/articles/bulk-publish`);
    return response.data;
  }
};
