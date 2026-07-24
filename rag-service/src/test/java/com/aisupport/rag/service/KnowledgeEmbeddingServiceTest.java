package com.aisupport.rag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;

import com.aisupport.rag.entity.EmbeddingStatus;
import com.aisupport.rag.entity.KnowledgeArticle;
import com.aisupport.rag.repository.KnowledgeArticleRepository;

@ExtendWith(MockitoExtension.class)
class KnowledgeEmbeddingServiceTest {

    @Mock
    private KnowledgeArticleRepository repo;
    @Mock
    private VectorStore vectorStore;
    @Mock
    private TokenTextSplitter textSplitter;

    @Captor
    private ArgumentCaptor<List<Document>> docsCaptor;
    @Captor
    private ArgumentCaptor<List<Long>> idsCaptor;

    private KnowledgeEmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new KnowledgeEmbeddingService(repo, vectorStore, textSplitter);
    }

    private KnowledgeArticle article(Long id, String title, String content) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setId(id);
        article.setTitle(title);
        article.setContent(content);
        article.setEmbeddingStatus(EmbeddingStatus.PENDING);
        return article;
    }

    @Test
    void embedPendingArticles_shouldChunkEmbedAndMarkArticles() {
        KnowledgeArticle refundArticle = article(1L, "Refund Policy", "Refunds are processed within 5-7 business days.");
        KnowledgeArticle shippingArticle = article(2L, "Shipping Policy", "Orders ship within 2 business days.");
        List<KnowledgeArticle> unembedded = List.of(refundArticle, shippingArticle);

        when(repo.findByEmbeddingStatus(EmbeddingStatus.PENDING)).thenReturn(unembedded);

        List<Document> chunkedDocs = List.of(
                Document.builder().text("Refund Policy: Refunds are processed within 5-7 business days.").build(),
                Document.builder().text("Shipping Policy: Orders ship within 2 business days.").build()
        );
        when(textSplitter.apply(anyList())).thenReturn(chunkedDocs);

        int result = embeddingService.embedPendingArticles();

        assertThat(result).isEqualTo(2);

        verify(textSplitter).apply(docsCaptor.capture());
        assertThat(docsCaptor.getValue()).hasSize(2);
        assertThat(docsCaptor.getValue().get(0).getText())
                .isEqualTo("Refund Policy: Refunds are processed within 5-7 business days.");
        assertThat(docsCaptor.getValue().get(0).getMetadata())
                .containsEntry("articleId", 1L)
                .containsEntry("title", "Refund Policy");

        verify(vectorStore).add(chunkedDocs);

        verify(repo).updateEmbeddingStatus(idsCaptor.capture(), eq(EmbeddingStatus.PROCESSING));
        assertThat(idsCaptor.getValue()).containsExactly(1L, 2L);

        verify(repo).updateEmbeddingStatus(idsCaptor.capture(), eq(EmbeddingStatus.READY));
        assertThat(idsCaptor.getValue()).containsExactly(1L, 2L);
    }

    @Test
    void embedPendingArticles_whenNoPendingArticles_shouldReturnZeroAndSkipEmbedding() {
        when(repo.findByEmbeddingStatus(EmbeddingStatus.PENDING)).thenReturn(List.of());

        int result = embeddingService.embedPendingArticles();

        assertThat(result).isZero();

        verify(textSplitter, never()).apply(anyList());
        verify(vectorStore, never()).add(anyList());
        verify(repo, never()).updateEmbeddingStatus(anyList(), any(EmbeddingStatus.class));
    }

    @Test
    void embedPendingArticles_whenVectorStoreAddFails_shouldPropagateAndMarkAsFailed() {
        KnowledgeArticle refundArticle = article(1L, "Refund Policy", "Refunds are processed within 5-7 business days.");
        when(repo.findByEmbeddingStatus(EmbeddingStatus.PENDING)).thenReturn(List.of(refundArticle));

        List<Document> chunkedDocs = List.of(
                Document.builder().text("Refund Policy: Refunds are processed within 5-7 business days.").build()
        );
        when(textSplitter.apply(anyList())).thenReturn(chunkedDocs);
        doThrow(new RuntimeException("embedding provider unavailable"))
                .when(vectorStore).add(chunkedDocs);

        assertThatThrownBy(() -> embeddingService.embedPendingArticles())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("embedding provider unavailable");

        verify(repo).updateEmbeddingStatus(idsCaptor.capture(), eq(EmbeddingStatus.PROCESSING));
        assertThat(idsCaptor.getValue()).containsExactly(1L);

        verify(repo).updateEmbeddingStatus(idsCaptor.capture(), eq(EmbeddingStatus.FAILED));
        assertThat(idsCaptor.getValue()).containsExactly(1L);

        verify(repo, never()).updateEmbeddingStatus(anyList(), eq(EmbeddingStatus.READY));
    }

    @Test
    void embedPendingArticles_whenMarkAsReadyFails_shouldPropagateException() {
        KnowledgeArticle refundArticle = article(1L, "Refund Policy", "Refunds are processed within 5-7 business days.");
        when(repo.findByEmbeddingStatus(EmbeddingStatus.PENDING)).thenReturn(List.of(refundArticle));

        List<Document> chunkedDocs = List.of(
                Document.builder().text("Refund Policy: Refunds are processed within 5-7 business days.").build()
        );
        when(textSplitter.apply(anyList())).thenReturn(chunkedDocs);
        
        doThrow(new RuntimeException("db update failed"))
                .when(repo).updateEmbeddingStatus(List.of(1L), EmbeddingStatus.READY);

        assertThatThrownBy(() -> embeddingService.embedPendingArticles())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db update failed");

        verify(vectorStore, times(1)).add(chunkedDocs);
        verify(repo).updateEmbeddingStatus(List.of(1L), EmbeddingStatus.PROCESSING);
        
        // Ensure it rolls back to failed on exception (if handled that way)
        verify(repo).updateEmbeddingStatus(List.of(1L), EmbeddingStatus.FAILED);
    }
}