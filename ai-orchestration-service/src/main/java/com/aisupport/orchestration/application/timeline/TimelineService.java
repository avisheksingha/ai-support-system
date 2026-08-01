package com.aisupport.orchestration.application.timeline;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.application.timeline.dto.AIInsightResponse;
import com.aisupport.orchestration.application.timeline.dto.AiDecisionContextDTO;
import com.aisupport.orchestration.application.timeline.dto.AiDecisionDTO;
import com.aisupport.orchestration.application.timeline.dto.KnowledgeInsightDTO;
import com.aisupport.orchestration.application.timeline.dto.KnowledgeSourceDTO;
import com.aisupport.orchestration.application.timeline.dto.PipelineProgressDTO;
import com.aisupport.orchestration.application.timeline.dto.RoutingInsightDTO;
import com.aisupport.orchestration.application.timeline.dto.TimelineEvent;
import com.aisupport.orchestration.application.timeline.dto.TimelinePageResponse;
import com.aisupport.orchestration.application.timeline.dto.WorkflowMetadataDTO;
import com.aisupport.orchestration.application.timeline.dto.WorkspaceDataResponse;
import com.aisupport.orchestration.infrastructure.persistence.entity.AiExecutionRecordEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowCheckpointEntity;
import com.aisupport.orchestration.infrastructure.persistence.entity.WorkflowExecutionEntity;
import com.aisupport.orchestration.infrastructure.persistence.repository.AiExecutionRecordRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowCheckpointRepository;
import com.aisupport.orchestration.infrastructure.persistence.repository.WorkflowExecutionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineService {

	private static final String MODEL_KEY = "model";
    private static final String DEFAULT_MODEL_ID = "unavailable";
    private static final String DEFAULT_TEAM = "General Support";
    
    private static final String KNOWLEDGE_CONTEXT_KEY = "knowledgeContext";
    private static final String ROUTING_DECISION_KEY = "routingDecision";
    private static final String AI_DECISION_KEY = "aiDecision";
    private static final String ANALYSIS_RESULT_KEY = "analysisResult";
    private static final String CONFIDENCE_KEY = "confidence";
    private static final String KNOWLEDGE_SUMMARY_KEY = "knowledgeSummary";
    private static final String ASSIGN_TO_TEAM_KEY = "assignToTeam";
    private static final String PRIORITY_KEY = "priority";
    private static final String SLA_HOURS_KEY = "slaHours";
    private static final String AI_SUMMARY_KEY = "aiSummary";
    private static final String SUGGESTED_REPLY_KEY = "suggestedReply";
    private static final String INTENT_KEY = "intent";
    private static final String SENTIMENT_KEY = "sentiment";
    private static final String URGENCY_KEY = "urgency";
    
	private static final String MATCHED_ARTICLE_TITLES_KEY = "matchedArticleTitles";

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowCheckpointRepository workflowCheckpointRepository;
    private final AiExecutionRecordRepository aiExecutionRecordRepository;
    private final TimelineMapper timelineMapper;

    @Transactional(readOnly = true)
    public TimelinePageResponse getTimelineForTicket(Long ticketId, int page, int size) {
        List<TimelineEvent> allEvents = new ArrayList<>();

        workflowExecutionRepository.findByTicketId(ticketId).ifPresent(execution -> {
            // 1. Add Workflow Execution Event
            allEvents.add(timelineMapper.toEvent(execution));

            // 2. Add Checkpoint Events
            List<WorkflowCheckpointEntity> checkpoints = workflowCheckpointRepository
                    .findByExecutionIdOrderByCreatedAtDesc(execution.getId());
            for (WorkflowCheckpointEntity checkpoint : checkpoints) {
                allEvents.add(timelineMapper.toEvent(checkpoint));
            }

            // 3. Add AI Execution Events
            List<AiExecutionRecordEntity> aiRecords = aiExecutionRecordRepository
                    .findByCorrelationId(execution.getCorrelationId());
            for (AiExecutionRecordEntity aiRecord : aiRecords) {
                allEvents.add(timelineMapper.toEvent(aiRecord));
            }
        });

        // Sort chronologically
        allEvents.sort(Comparator.comparing(TimelineEvent::getTimestamp));

        // In-memory pagination
        int totalElements = allEvents.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);

        List<TimelineEvent> pageContent = allEvents.subList(start, end);

        return TimelinePageResponse.builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(end >= totalElements)
                .build();
    }

    @Transactional(readOnly = true)
    public TimelinePageResponse getTimelineForWorkflowId(String workflowId, int page, int size) {
        List<TimelineEvent> allEvents = new ArrayList<>();

        workflowExecutionRepository.findById(workflowId).ifPresent(execution -> {
            // 1. Add Workflow Execution Event
            allEvents.add(timelineMapper.toEvent(execution));

            // 2. Add Checkpoint Events
            List<WorkflowCheckpointEntity> checkpoints = workflowCheckpointRepository
                    .findByExecutionIdOrderByCreatedAtDesc(execution.getId());
            for (WorkflowCheckpointEntity checkpoint : checkpoints) {
                allEvents.add(timelineMapper.toEvent(checkpoint));
            }

            // 3. Add AI Execution Events
            List<AiExecutionRecordEntity> aiRecords = aiExecutionRecordRepository
                    .findByCorrelationId(execution.getCorrelationId());
            for (AiExecutionRecordEntity aiRecord : aiRecords) {
                allEvents.add(timelineMapper.toEvent(aiRecord));
            }
        });

        // Sort chronologically
        allEvents.sort(Comparator.comparing(TimelineEvent::getTimestamp));

        // In-memory pagination
        int totalElements = allEvents.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int start = Math.min(page * size, totalElements);
        int end = Math.min(start + size, totalElements);

        List<TimelineEvent> pageContent = allEvents.subList(start, end);

        return TimelinePageResponse.builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLast(end >= totalElements)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<AIInsightResponse> getTicketInsights(Long ticketId) {
        return fetchTicketInsights(ticketId);
    }

    @Transactional(readOnly = true)
    public Optional<WorkspaceDataResponse> getWorkspaceData(Long ticketId) {
        try {
            Optional<WorkflowExecutionEntity> executionOpt = workflowExecutionRepository.findByTicketId(ticketId);
            if (executionOpt.isEmpty()) {
                return Optional.empty();
            }

            WorkflowExecutionEntity execution = executionOpt.get();

            List<WorkflowCheckpointEntity> checkpoints = workflowCheckpointRepository
                    .findByExecutionIdOrderByCreatedAtDesc(execution.getId());
            if (checkpoints.isEmpty()) {
                return Optional.empty();
            }

            WorkflowCheckpointEntity latestCheckpoint = checkpoints.get(0);
            Map<String, Object> attributes = latestCheckpoint.getAttributesSnapshot();

            WorkspaceDataResponse.WorkspaceDataResponseBuilder responseBuilder = WorkspaceDataResponse.builder();

            // --- Analysis ---
            AIInsightResponse analysis = fetchTicketInsights(ticketId).orElse(null);
            if (analysis != null) {
                responseBuilder.analysis(analysis);
            }

            // --- Knowledge ---
            KnowledgeInsightDTO knowledge = null;
            if (attributes.containsKey(KNOWLEDGE_CONTEXT_KEY)) {
                knowledge = mapKnowledgeInsight(attributes);
                responseBuilder.knowledge(knowledge);
            }

            // --- Routing ---
            RoutingInsightDTO routing = null;
            if (attributes.containsKey(ROUTING_DECISION_KEY)) {
                routing = mapRoutingInsight(attributes, analysis, knowledge);
                responseBuilder.routing(routing);
            }

            // --- AI Decision ---
            AiDecisionDTO aiDecision = null;
            if (attributes.containsKey(AI_DECISION_KEY)) {
                aiDecision = mapAiDecision(attributes);
                responseBuilder.aiDecision(aiDecision);
            }

            // --- AI Decision Context (Unified Source of Truth) ---
            responseBuilder.aiDecisionContext(buildAiDecisionContext(analysis, knowledge, routing, aiDecision));

            // --- Workflow Metadata ---
            responseBuilder.workflowMetadata(mapWorkflowMetadata(execution));

            // --- Pipeline Progress ---
            responseBuilder.pipelineProgress(mapPipelineProgress(attributes));

            return Optional.of(responseBuilder.build());
        } catch (Exception e) {
            log.error("Failed to build workspace data for ticketId={}", ticketId, e);
            return Optional.empty();
        }
    }

    // ── Private Mapping Methods ──────────────────────────────────────────────

    private Optional<AIInsightResponse> fetchTicketInsights(Long ticketId) {
        try {
            Optional<WorkflowExecutionEntity> executionOpt = workflowExecutionRepository.findByTicketId(ticketId);
            if (executionOpt.isEmpty()) {
                return Optional.empty();
            }

            List<WorkflowCheckpointEntity> checkpoints = workflowCheckpointRepository
                    .findByExecutionIdOrderByCreatedAtDesc(executionOpt.get().getId());
            if (checkpoints.isEmpty()) {
                return Optional.empty();
            }

            WorkflowCheckpointEntity latestCheckpoint = checkpoints.get(0);
            Map<String, Object> attributes = latestCheckpoint.getAttributesSnapshot();

            Object analysisObj = attributes.get(ANALYSIS_RESULT_KEY);
            if (!(analysisObj instanceof Map<?, ?>)) {
                return Optional.empty();
            }
            Map<?, ?> analysisMap = (Map<?, ?>) analysisObj;

            String model = aiExecutionRecordRepository.findTopByTicketIdOrderByExecutedAtDesc(ticketId)
                    .map(AiExecutionRecordEntity::getModelId)
                    .filter(Objects::nonNull)
                    .orElse(DEFAULT_MODEL_ID);
                    
            if (DEFAULT_MODEL_ID.equals(model)) {
                Object kcObj = attributes.get(KNOWLEDGE_CONTEXT_KEY);
                if (kcObj instanceof Map<?, ?> kc && kc.get(MODEL_KEY) != null) {
                    model = kc.get(MODEL_KEY).toString();
                }
            }

            Double confidence = 0.0;
            Object aiDecisionObj = attributes.get(AI_DECISION_KEY);
            if (aiDecisionObj instanceof Map<?, ?> aiDecision && aiDecision.get(CONFIDENCE_KEY) instanceof Number num) {
                confidence = num.doubleValue();
            }

            List<String> keywordsList = analysisMap.get("keywords") instanceof List<?> list
                    ? list.stream().map(Objects::toString).toList()
                    : Collections.emptyList();

            return Optional.of(AIInsightResponse.builder()
                    .intent(Objects.toString(analysisMap.get(INTENT_KEY), null))
                    .sentiment(Objects.toString(analysisMap.get(SENTIMENT_KEY), null))
                    .urgency(Objects.toString(analysisMap.get(URGENCY_KEY), null))
                    .confidenceScore(confidence)
                    .analysisProvider(model)
                    .keywords(keywordsList)
                    .suggestedCategory(Objects.toString(analysisMap.get("suggestedCategory"), null))
                    .build());
        } catch (Exception e) {
            log.error("Failed to fetch ticket insights for ticketId={}", ticketId, e);
            return Optional.empty();
        }
    }

    private KnowledgeInsightDTO mapKnowledgeInsight(Map<String, Object> attributes) {
        Object kcObj = attributes.get(KNOWLEDGE_CONTEXT_KEY);
        if (!(kcObj instanceof Map<?, ?>)) {
            return null;
        }
        Map<?, ?> kc = (Map<?, ?>) kcObj;

        boolean knowledgeFound = Boolean.TRUE.equals(kc.get("knowledgeFound"));
        String model = Objects.toString(kc.get(MODEL_KEY), null);
        Integer retrievedDocumentCount = kc.get("retrievedDocumentCount") instanceof Number n ? n.intValue() : null;
        
        List<String> matchedArticleTitles = Collections.emptyList();
        Object rawTitles = kc.get(MATCHED_ARTICLE_TITLES_KEY);
        
        // Safely cast
        if (rawTitles instanceof List<?> titleList) {
            matchedArticleTitles = titleList.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        
        List<KnowledgeSourceDTO> sources = Collections.emptyList();
        Object rawSources = kc.get("sources");
        if (rawSources instanceof List<?> sourceList) {
            sources = sourceList.stream()
                    .filter(Map.class::isInstance)
                    .map(obj -> {
                        Map<?, ?> map = (Map<?, ?>) obj;
                        return KnowledgeSourceDTO.builder()
                                .id(Objects.toString(map.get("id"), null))
                                .title(Objects.toString(map.get("title"), "Unknown"))
                                .similarityScore(map.get("similarityScore") instanceof Number n ? n.doubleValue() : null)
                                .category(Objects.toString(map.get("category"), null))
                                .tags(Objects.toString(map.get("tags"), null))
                                .vectorScore(map.get("vectorScore") instanceof Number n ? n.doubleValue() : null)
                                .hybridScore(map.get("hybridScore") instanceof Number n ? n.doubleValue() : null)
                                .build();
                    })
                    .toList();
        }

        return KnowledgeInsightDTO.builder()
                .knowledgeSummary(Objects.toString(kc.get(KNOWLEDGE_SUMMARY_KEY), null))
                .confidence(knowledgeFound ? 1.0 : 0.0)
                .sources(sources)
                .knowledgeFound(knowledgeFound)
                .model(model)
                .retrievedDocumentCount(retrievedDocumentCount)
                .matchedArticleTitles(matchedArticleTitles)
                .build();
    }

    private RoutingInsightDTO mapRoutingInsight(Map<String, Object> attributes, AIInsightResponse analysis, KnowledgeInsightDTO knowledge) {
        Object rdObj = attributes.get(ROUTING_DECISION_KEY);
        if (!(rdObj instanceof Map<?, ?>)) {
            return null;
        }
        Map<?, ?> rd = (Map<?, ?>) rdObj;

        String assignedTeam = Objects.toString(rd.get(ASSIGN_TO_TEAM_KEY), null);
        String priority = rd.get(PRIORITY_KEY) != null ? rd.get(PRIORITY_KEY).toString() : null;
        Integer slaHours = rd.get(SLA_HOURS_KEY) instanceof Number n ? n.intValue() : null;

        String explanation = generateRoutingExplanation(analysis, knowledge, assignedTeam);

        return RoutingInsightDTO.builder()
                .assignedTeam(assignedTeam)
                .priority(priority)
                .slaHours(slaHours)
                .routingExplanation(explanation)
                .build();
    }

    private AiDecisionDTO mapAiDecision(Map<String, Object> attributes) {
        Object adObj = attributes.get(AI_DECISION_KEY);
        if (!(adObj instanceof Map<?, ?>)) {
            return null;
        }
        Map<?, ?> ad = (Map<?, ?>) adObj;

        return AiDecisionDTO.builder()
                .aiSummary(Objects.toString(ad.get(AI_SUMMARY_KEY), null))
                .suggestedReply(Objects.toString(ad.get(SUGGESTED_REPLY_KEY), null))
                .confidence(ad.get(CONFIDENCE_KEY) instanceof Number n ? n.doubleValue() : null)
                .decisionReason(Objects.toString(ad.get("decisionReason"), null))
                .build();
    }

    private WorkflowMetadataDTO mapWorkflowMetadata(WorkflowExecutionEntity execution) {
        Long durationMs = null;
        if (execution.getCreatedAt() != null && execution.getCompletedAt() != null) {
            durationMs = Duration.between(execution.getCreatedAt(), execution.getCompletedAt()).toMillis();
        }

        return WorkflowMetadataDTO.builder()
                .workflowExecutionId(execution.getId())
                .workflowState(execution.getState() != null ? execution.getState().name() : null)
                .workflowDurationMs(durationMs)
                .build();
    }

    private PipelineProgressDTO mapPipelineProgress(Map<String, Object> attributes) {
        return PipelineProgressDTO.builder()
                .analysisCompleted(attributes.containsKey(ANALYSIS_RESULT_KEY))
                .knowledgeCompleted(attributes.containsKey(KNOWLEDGE_CONTEXT_KEY))
                .routingCompleted(attributes.containsKey(ROUTING_DECISION_KEY))
                .decisionCompleted(attributes.containsKey(AI_DECISION_KEY))
                .build();
    }

    private AiDecisionContextDTO buildAiDecisionContext(AIInsightResponse analysis, KnowledgeInsightDTO knowledge, RoutingInsightDTO routing, AiDecisionDTO aiDecision) {
        String[] refined = refineIntentAndCategory(analysis);
        String intentStr = refined[0];
        String category = refined[1];

        double confidence = resolveConfidence(analysis, aiDecision);
        int docCount = resolveDocCount(knowledge);
        List<String> matchedArticles = knowledge != null && knowledge.getMatchedArticleTitles() != null
                ? knowledge.getMatchedArticleTitles()
                : Collections.emptyList();
        String team = routing != null && routing.getAssignedTeam() != null ? routing.getAssignedTeam() : DEFAULT_TEAM;
        boolean fallbackUsed = isFallbackUsed(knowledge);

        String reason = String.format(
            "Detected %s issue with high confidence (%.0f%%). Retrieved %d relevant knowledge article(s) from the Knowledge Base. Based on AI analysis and organizational routing rules, the ticket was assigned to the %s team.",
            intentStr, confidence * 100, docCount, team
        );

        if (aiDecision != null && shouldUpdateDecisionReason(aiDecision.getDecisionReason())) {
            aiDecision.setDecisionReason(reason);
        }

        String routingExpl = routing != null && routing.getRoutingExplanation() != null
                ? routing.getRoutingExplanation()
                : generateRoutingExplanation(analysis, knowledge, team);

        return AiDecisionContextDTO.builder()
                .intent(intentStr)
                .category(category)
                .confidence(confidence)
                .retrievedArticleCount(docCount)
                .matchedArticles(matchedArticles)
                .routingDecision(team)
                .decisionReason(reason)
                .routingExplanation(routingExpl)
                .retrievalFallbackUsed(fallbackUsed)
                .build();
    }

    private boolean shouldUpdateDecisionReason(String reason) {
        if (reason == null) return true;
        return reason.contains("0 relevant knowledge article") || reason.contains(DEFAULT_MODEL_ID);
    }

    private String[] refineIntentAndCategory(AIInsightResponse analysis) {
        String intent = analysis != null && analysis.getIntent() != null ? analysis.getIntent() : "support issue";
        String category = analysis != null && analysis.getSuggestedCategory() != null ? analysis.getSuggestedCategory() : DEFAULT_TEAM;

        if (!isSemanticOverlap(intent, category)) {
            return new String[]{intent.replace("_", " "), category};
        }

        String lower = intent.toLowerCase();
        if (lower.contains("auth") || lower.contains("login") || lower.contains("oauth") || lower.contains("security")) {
            return new String[]{"Account Access & Login Verification Failure", "Identity & Access Management (IAM)"};
        }
        if (lower.contains("bill") || lower.contains("pay") || lower.contains("invoice")) {
            return new String[]{"Billing Discrepancy & Payment Verification", "Billing & Account Services"};
        }
        if (lower.contains("network") || lower.contains("connect") || lower.contains("latency") || lower.contains("technical")) {
            return new String[]{"Network Latency & Connectivity Troubleshooting", "Core Network & Infrastructure"};
        }
        if (lower.contains("bug") || lower.contains("crash") || lower.contains("error")) {
            return new String[]{"Software Defect & Crash Investigation", "Software Engineering & Defect Resolution"};
        }
        return new String[]{intent.replace("_", " ") + " Inquiry", category.replace("_", " ") + " Operations"};
    }

    private boolean isSemanticOverlap(String intent, String category) {
        return intent.equalsIgnoreCase(category)
            || intent.toLowerCase().contains(category.toLowerCase())
            || category.toLowerCase().contains(intent.toLowerCase());
    }

    private double resolveConfidence(AIInsightResponse analysis, AiDecisionDTO aiDecision) {
        if (aiDecision != null && aiDecision.getConfidence() != null) {
            return aiDecision.getConfidence();
        }
        if (analysis != null && analysis.getConfidenceScore() != null) {
            return analysis.getConfidenceScore();
        }
        return 0.85;
    }

    private int resolveDocCount(KnowledgeInsightDTO knowledge) {
        if (knowledge == null) return 0;
        if (knowledge.getRetrievedDocumentCount() != null) {
            return knowledge.getRetrievedDocumentCount();
        }
        if (knowledge.getMatchedArticleTitles() != null) {
            return knowledge.getMatchedArticleTitles().size();
        }
        return 0;
    }

    private boolean isFallbackUsed(KnowledgeInsightDTO knowledge) {
        if (knowledge == null || !knowledge.isKnowledgeFound()) {
            return true;
        }
        return knowledge.getModel() != null && knowledge.getModel().contains("Fallback");
    }

    private String generateRoutingExplanation(AIInsightResponse analysis, KnowledgeInsightDTO knowledge, String assignedTeam) {
        String category = resolveCategory(analysis);
        double confidence = resolveConfidence(analysis, null);
        String confLabel = resolveConfidenceLabel(confidence);
        String s1 = String.format("Detected a %s-related support request with %s confidence (%.0f%%).", category, confLabel, confidence * 100);

        String s2 = buildKeywordsSentence(analysis, category);

        int docCount = resolveDocCount(knowledge);
        String s3 = docCount > 0
                ? String.format("The Metadata-Aware Retrieval Engine retrieved %d relevant Knowledge Base article(s).", docCount)
                : "The Metadata-Aware Retrieval Engine retrieved 0 relevant Knowledge Base articles, triggering general domain routing.";

        String team = assignedTeam != null ? assignedTeam : DEFAULT_TEAM;
        boolean fallbackUsed = isFallbackUsed(knowledge);
        String s4 = fallbackUsed
                ? String.format("Due to retrieval fallback or routing resiliency rules, this ticket was assigned to the %s team for investigation.", team)
                : String.format("According to the routing policy, this ticket was assigned to the %s team for initial investigation.", team);

        return String.join(" ", s1, s2, s3, s4);
    }

    private String resolveCategory(AIInsightResponse analysis) {
        if (analysis == null) return DEFAULT_TEAM;
        if (analysis.getSuggestedCategory() != null && !analysis.getSuggestedCategory().isBlank() && !"None".equalsIgnoreCase(analysis.getSuggestedCategory())) {
            return analysis.getSuggestedCategory();
        }
        if (analysis.getIntent() != null) {
            return analysis.getIntent().replace("_", " ");
        }
        return DEFAULT_TEAM;
    }

    private String resolveConfidenceLabel(double confidence) {
        if (confidence >= 0.85) return "High";
        if (confidence >= 0.70) return "Medium";
        return "Low";
    }

    private String buildKeywordsSentence(AIInsightResponse analysis, String category) {
        if (analysis != null && analysis.getKeywords() != null && !analysis.getKeywords().isEmpty()) {
            String formattedKws = formatKeywordsList(analysis.getKeywords());
            return String.format("Keywords such as %s matched the %s category.", formattedKws, category);
        }
        return String.format("Analysis of intent and urgency matched the %s category.", category);
    }

    private String formatKeywordsList(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return "";
        if (keywords.size() == 1) return "\"" + keywords.get(0) + "\"";
        if (keywords.size() == 2) return "\"" + keywords.get(0) + "\" and \"" + keywords.get(1) + "\"";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keywords.size(); i++) {
            if (i > 0) sb.append(", ");
            if (i == keywords.size() - 1) sb.append("and ");
            sb.append("\"").append(keywords.get(i)).append("\"");
        }
        return sb.toString();
    }
}