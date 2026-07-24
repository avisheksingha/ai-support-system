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

    private final KnowledgeArticleRepository repository;
    private final KnowledgeArticleMapper mapper;

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
        return repository.findById(id).map(existing -> {
            // Track whether content-bearing fields actually change.
            // Only title and content are included in embedding generation,
            // so only changes to these fields should invalidate embeddings.
            boolean contentChanged = false;

            if (dto.getTitle() != null) {
                if (!dto.getTitle().equals(existing.getTitle())) {
                    contentChanged = true;
                }
                existing.setTitle(dto.getTitle());
            }
            if (dto.getContent() != null) {
                if (!dto.getContent().equals(existing.getContent())) {
                    contentChanged = true;
                }
                existing.setContent(dto.getContent());
            }

            // Metadata-only updates — never invalidate embeddings
            if (dto.getCategory() != null) existing.setCategory(dto.getCategory());
            if (dto.getTags() != null) existing.setTags(dto.getTags());
            if (dto.getStatus() != null) existing.setStatus(dto.getStatus());

            if (contentChanged) {
                existing.setEmbeddingStatus(EmbeddingStatus.PENDING);
            }

            return mapper.toDto(repository.save(existing));
        }).orElseThrow(() -> new RuntimeException("Article not found: " + id));
    }

    @Transactional
    public void deleteArticle(Long id) {
        repository.deleteById(id);
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
            "publishedCount", published,
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
            return Map.of("publishedCount", 0L, "message", "No draft articles found.");
        }
        int published = repository.bulkPublishDraftArticles();
        log.info("Bulk published {} draft articles.", published);
        return Map.of("publishedCount", (long) published, "message", published + " articles published successfully.");
    }
}
