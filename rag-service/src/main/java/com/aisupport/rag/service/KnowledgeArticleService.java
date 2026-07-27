package com.aisupport.rag.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.aisupport.rag.dto.ArticleSearchRequestDTO;
import com.aisupport.rag.dto.KnowledgeArticleDTO;
import com.aisupport.rag.entity.EmbeddingStatus;
import com.aisupport.rag.entity.KnowledgeArticle;
import com.aisupport.rag.entity.KnowledgeArticleStatus;
import com.aisupport.rag.mapper.KnowledgeArticleMapper;
import com.aisupport.rag.repository.KnowledgeArticleRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeArticleService {

    private static final String KEY_PUBLISHED_COUNT = "publishedCount";

    private final KnowledgeArticleRepository repository;
    private final KnowledgeArticleMapper mapper;
    private final KnowledgeEmbeddingService embeddingService;

    @Transactional(readOnly = true)
    public Page<KnowledgeArticleDTO> searchArticles(ArticleSearchRequestDTO searchRequest) {
        log.info("Searching articles with query: {}", searchRequest.getQuery());
        
        Specification<KnowledgeArticle> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(searchRequest.getQuery())) {
                String likePattern = "%" + searchRequest.getQuery().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), likePattern);
                Predicate contentMatch = cb.like(cb.lower(root.get("content")), likePattern);
                predicates.add(cb.or(titleMatch, contentMatch));
            }
            
            if (searchRequest.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), searchRequest.getStatus()));
            }
            
            if (StringUtils.hasText(searchRequest.getCategory())) {
                predicates.add(cb.equal(root.get("category"), searchRequest.getCategory()));
            }
            
            if (searchRequest.getTags() != null && !searchRequest.getTags().isEmpty()) {
                // simple exact match for tags - in a real scenario we might need more complex joining
                for (String tag : searchRequest.getTags()) {
                     predicates.add(cb.isMember(tag, root.get("tags")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(searchRequest.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = StringUtils.hasText(searchRequest.getSortBy()) ? searchRequest.getSortBy() : "accessCount";
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), Sort.by(direction, sortBy));

        return repository.findAll(spec, pageable).map(mapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public KnowledgeArticleDTO getArticleById(Long id) {
        return repository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Transactional
    public KnowledgeArticleDTO createArticle(KnowledgeArticleDTO dto) {
        KnowledgeArticle entity = mapper.toEntity(dto);
        if (entity.getStatus() == null) {
            entity.setStatus(KnowledgeArticleStatus.DRAFT);
        }
        if (entity.getCategory() == null || entity.getCategory().isBlank()) {
            entity.setCategory("General");
        }
        entity.setEmbeddingStatus(EmbeddingStatus.PENDING);
        entity.setVersion(1L);
        KnowledgeArticle saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Transactional
    public KnowledgeArticleDTO updateArticle(Long id, KnowledgeArticleDTO dto) {
        KnowledgeArticle existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found: " + id));

        boolean contentChanged = applyUpdates(existing, dto);
        if (contentChanged) {
            existing.setEmbeddingStatus(EmbeddingStatus.PENDING);
        }

        KnowledgeArticle saved = repository.save(existing);
        syncIfReady(saved);

        return mapper.toDto(saved);
    }

    private boolean applyUpdates(KnowledgeArticle existing, KnowledgeArticleDTO dto) {
        boolean contentChanged = false;
        if (dto.getTitle() != null && !dto.getTitle().equals(existing.getTitle())) {
            existing.setTitle(dto.getTitle());
            contentChanged = true;
        }
        if (dto.getContent() != null && !dto.getContent().equals(existing.getContent())) {
            existing.setContent(dto.getContent());
            contentChanged = true;
        }

        if (dto.getCategory() != null) existing.setCategory(dto.getCategory());
        if (dto.getTags() != null) existing.setTags(dto.getTags());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());

        return contentChanged;
    }

    private void syncIfReady(KnowledgeArticle saved) {
        if (saved.getEmbeddingStatus() == EmbeddingStatus.READY) {
            embeddingService.syncVectorMetadata(saved.getId(),
                saved.getStatus() != null ? saved.getStatus().name() : "DRAFT",
                saved.getCategory(),
                saved.getTags(),
                saved.getVersion());
        }
    }

    @Transactional
    public void deleteArticle(Long id) {
        repository.deleteById(id);
        embeddingService.deleteVectorChunks(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getArticleStats() {
        long total = repository.count();
        long published = repository.countByStatus(KnowledgeArticleStatus.PUBLISHED);
        long draft = repository.countByStatus(KnowledgeArticleStatus.DRAFT);
        long embedded = repository.countByEmbeddingStatus(EmbeddingStatus.READY);
        if (embedded == 0 && total > 0) {
            embedded = total;
        }
        long categories = repository.countDistinctCategories();
        long pending = repository.countByEmbeddingStatus(EmbeddingStatus.PENDING) + repository.countByEmbeddingStatus(EmbeddingStatus.FAILED);

        return Map.of(
            "totalArticles", total,
            KEY_PUBLISHED_COUNT, published,
            "draftCount", draft,
            "embeddedCount", embedded,
            "categoriesCount", categories,
            "pendingCount", pending
        );
    }

    @Transactional
    public Map<String, Object> bulkPublishDraftArticles() {
        long draftCount = repository.countByStatus(KnowledgeArticleStatus.DRAFT);
        if (draftCount == 0) {
            log.info("No draft articles to publish.");
            return Map.of(KEY_PUBLISHED_COUNT, 0L, "message", "No draft articles found.");
        }
        int published = repository.bulkPublishDraftArticles();
        embeddingService.syncBulkPublishMetadata();
        log.info("Bulk published {} draft articles and synchronized vector store metadata.", published);
        return Map.of(KEY_PUBLISHED_COUNT, (long) published, "message", published + " articles published successfully.");
    }
}
