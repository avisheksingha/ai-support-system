import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { knowledgeApi } from './knowledgeApi';
import type { ArticleSearchRequest } from '../types';

export const useSearchArticles = (request: ArticleSearchRequest) => {
  return useQuery({
    queryKey: ['knowledge-articles', request],
    queryFn: () => knowledgeApi.searchArticles(request),
    // Keep previous data while fetching new pages
    placeholderData: (previousData) => previousData
  });
};

export const useKnowledgeArticle = (id: number) => {
  return useQuery({
    queryKey: ['knowledge-article', id],
    queryFn: () => knowledgeApi.getArticle(id),
    enabled: !!id
  });
};

export const useCreateArticle = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: knowledgeApi.createArticle,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['knowledge-articles'] });
      queryClient.invalidateQueries({ queryKey: ['knowledge-stats'] });
    }
  });
};

export const useUpdateArticle = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, article }: { id: number, article: Partial<import('../types').KnowledgeArticle> }) => 
      knowledgeApi.updateArticle(id, article),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['knowledge-articles'] });
      queryClient.invalidateQueries({ queryKey: ['knowledge-article', variables.id] });
      queryClient.invalidateQueries({ queryKey: ['knowledge-stats'] });
    }
  });
};

export const useDeleteArticle = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: knowledgeApi.deleteArticle,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['knowledge-articles'] });
      queryClient.invalidateQueries({ queryKey: ['knowledge-stats'] });
    }
  });
};

export const useSyncEmbeddings = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: knowledgeApi.syncEmbeddings,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['knowledge-articles'] });
      queryClient.invalidateQueries({ queryKey: ['knowledge-stats'] });
    }
  });
};

export const useKnowledgeStats = () => {
  return useQuery({
    queryKey: ['knowledge-stats'],
    queryFn: knowledgeApi.getStats,
  });
};

export const useBulkPublish = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: knowledgeApi.bulkPublish,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['knowledge-articles'] });
      queryClient.invalidateQueries({ queryKey: ['knowledge-stats'] });
    }
  });
};
