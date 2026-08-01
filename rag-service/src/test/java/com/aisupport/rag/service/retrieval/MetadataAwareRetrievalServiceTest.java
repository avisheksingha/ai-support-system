package com.aisupport.rag.service.retrieval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import com.aisupport.rag.entity.EmbeddingStatus;
import com.aisupport.rag.entity.KnowledgeArticleStatus;
import com.aisupport.rag.repository.KnowledgeArticleRepository;

@ExtendWith(MockitoExtension.class)
class MetadataAwareRetrievalServiceTest {

    @Mock
    private VectorStore vectorStore;
    @Mock
    private KnowledgeArticleRepository articleRepo;

    private QueryMetadataExtractor metadataExtractor;
    private HybridDocumentRanker documentRanker;
    private MetadataPromptBuilder promptBuilder;
    private MetadataAwareRetrievalService retrievalService;

    @BeforeEach
    void setUp() {
        metadataExtractor = new QueryMetadataExtractor();
        documentRanker = new HybridDocumentRanker(0.50, 0.20, 0.15, 0.15);
        promptBuilder = new MetadataPromptBuilder();
        retrievalService = new MetadataAwareRetrievalService(
                vectorStore, articleRepo, metadataExtractor, documentRanker, promptBuilder);
    }

    private Document createDoc(String id, Long articleId, String title, String category, String tags, String status, String embStatus, double score, String text) {
        Map<String, Object> meta = new HashMap<>();
        if (articleId != null) meta.put("articleId", articleId);
        meta.put("title", title);
        meta.put("category", category);
        meta.put("tags", tags);
        meta.put("status", status);
        meta.put("embeddingStatus", embStatus);

        return Document.builder()
                .id(id)
                .text(text)
                .metadata(meta)
                .score(score)
                .build();
    }

    @Test
    void retrieveAndRank_whenHighConfidenceAndCategoryMatch_shouldFilterByCategory() {
        String query = """
                Customer Message: My wifi router is blinking red
                Keywords: wifi, router, red
                Suggested Category: TECHNICAL
                Confidence Score: 0.85
                """;

        Document doc = createDoc("1", 101L, "Router Setup", "TECHNICAL", "wifi,router", "PUBLISHED", "READY", 0.8, "Reset the router.");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));
        when(articleRepo.findIdsByStatusAndEmbeddingStatusAndIdIn(eq(KnowledgeArticleStatus.PUBLISHED), eq(EmbeddingStatus.READY), anyList()))
                .thenReturn(List.of(101L));

        RetrievalResult result = retrievalService.retrieveAndRank(query);

        assertThat(result.retrievedDocumentCount()).isEqualTo(1);
        assertThat(result.matchedArticleTitles()).isEqualTo("Router Setup");
        assertThat(result.formattedContext()).contains("Category: TECHNICAL", "Tags: wifi,router", "Title: Router Setup");

        ArgumentCaptor<SearchRequest> reqCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(reqCaptor.capture());
        assertThat(String.valueOf(reqCaptor.getValue().getFilterExpression()))
                .contains("key=status", "value=PUBLISHED", "key=embeddingStatus", "value=READY", "key=category", "value=TECHNICAL");
    }

    @Test
    void retrieveAndRank_whenCategorySearchReturnsZero_shouldFallbackToGlobalSearch() {
        String query = """
                Customer Message: Unknown account issue
                Suggested Category: BILLING
                Confidence Score: 0.90
                """;

        Document doc = createDoc("2", 102L, "General Account Help", "GENERAL", "account", "PUBLISHED", "READY", 0.7, "Contact support.");
        
        // First call returns empty (category search), second call returns doc (fallback global search)
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of())
                .thenReturn(List.of(doc));
        when(articleRepo.findIdsByStatusAndEmbeddingStatusAndIdIn(any(), any(), anyList()))
                .thenReturn(List.of(102L));

        RetrievalResult result = retrievalService.retrieveAndRank(query);

        assertThat(result.retrievedDocumentCount()).isEqualTo(1);
        assertThat(result.matchedArticleTitles()).isEqualTo("General Account Help");

        ArgumentCaptor<SearchRequest> reqCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore, times(2)).similaritySearch(reqCaptor.capture());
        assertThat(String.valueOf(reqCaptor.getAllValues().get(0).getFilterExpression()))
                .contains("key=category", "value=BILLING");
        assertThat(String.valueOf(reqCaptor.getAllValues().get(1).getFilterExpression()))
                .contains("key=status", "value=PUBLISHED", "key=embeddingStatus", "value=READY")
                .doesNotContain("key=category");
    }

    @Test
    void retrieveAndRank_whenLowConfidence_shouldSkipCategoryAndDoGlobalSearch() {
        String query = """
                Customer Message: My system is slow
                Suggested Category: TECHNICAL
                Confidence Score: 0.45
                """;

        Document doc = createDoc("3", 103L, "System Optimization", "GENERAL", "slow,performance", "PUBLISHED", "READY", 0.75, "Clear cache.");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));
        when(articleRepo.findIdsByStatusAndEmbeddingStatusAndIdIn(any(), any(), anyList())).thenReturn(List.of(103L));

        RetrievalResult result = retrievalService.retrieveAndRank(query);

        assertThat(result.retrievedDocumentCount()).isEqualTo(1);

        ArgumentCaptor<SearchRequest> reqCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore, times(1)).similaritySearch(reqCaptor.capture());
        // Verify it skipped category restriction
        assertThat(String.valueOf(reqCaptor.getValue().getFilterExpression()))
                .contains("key=status", "value=PUBLISHED", "key=embeddingStatus", "value=READY")
                .doesNotContain("key=category");
    }

    @Test
    void retrieveAndRank_shouldFilterOutDraftOrPendingArticlesViaGovernanceCheck() {
        String query = "Customer Message: Help with login";

        Document docPublished = createDoc("4", 104L, "Login Help", "TECHNICAL", "login", "PUBLISHED", "READY", 0.9, "Click forgot password.");
        Document docDraft = createDoc("5", 105L, "Secret Draft", "TECHNICAL", "login", "DRAFT", "READY", 0.95, "Internal draft info.");

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(docPublished, docDraft));
        // Live DB only returns ID 104 (docPublished), rejecting ID 105
        when(articleRepo.findIdsByStatusAndEmbeddingStatusAndIdIn(any(), any(), anyList())).thenReturn(List.of(104L));

        RetrievalResult result = retrievalService.retrieveAndRank(query);

        assertThat(result.retrievedDocumentCount()).isEqualTo(1);
        assertThat(result.matchedArticleTitles()).isEqualTo("Login Help");
        assertThat(result.formattedContext()).doesNotContain("Secret Draft", "Internal draft info");
    }

    @Test
    void hybridRanker_shouldTreatTagsAsFirstClassRankingSignal() {
        String query = """
                Customer Message: I need help with VPN
                Keywords: vpn, remote, access
                """;

        // doc1 has lower vector score (0.6) but perfect tag match (vpn,remote) -> high tag score
        Document doc1 = createDoc("6", 106L, "Remote Access Guide", "TECHNICAL", "vpn,remote,access", "PUBLISHED", "READY", 0.60, "Use the VPN client.");
        // doc2 has higher vector score (0.7) but zero tag match -> zero tag score
        Document doc2 = createDoc("7", 107L, "General Network", "TECHNICAL", "ethernet,cable", "PUBLISHED", "READY", 0.70, "Plug in the cable.");

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc1, doc2));
        when(articleRepo.findIdsByStatusAndEmbeddingStatusAndIdIn(any(), any(), anyList())).thenReturn(List.of(106L, 107L));

        RetrievalResult result = retrievalService.retrieveAndRank(query);

        assertThat(result.documents()).hasSize(2);
        // doc1 should be ranked FIRST because tags are a first-class signal!
        assertThat(result.documents().get(0).getId()).isEqualTo("6");
        assertThat(result.documents().get(1).getId()).isEqualTo("7");
    }
}
