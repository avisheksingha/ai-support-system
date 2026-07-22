package com.aisupport.analysis.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import com.aisupport.analysis.dto.request.WritingContext;
import com.aisupport.analysis.dto.request.WritingImproveRequest;
import com.aisupport.analysis.dto.response.WritingImproveResponse;
import com.aisupport.common.dto.ValidationResult;
import com.aisupport.common.util.TicketPreValidator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WritingAssistantService {

    private final ChatClient chatClient;
    private final PromptTemplate writingSupportTicketPromptTemplate;
    private final PromptTemplate writingAgentReplyPromptTemplate;

    public WritingAssistantService(ChatClient chatClient,
                                   PromptTemplate writingSupportTicketPromptTemplate,
                                   PromptTemplate writingAgentReplyPromptTemplate) {
        this.chatClient = chatClient;
        this.writingSupportTicketPromptTemplate = writingSupportTicketPromptTemplate;
        this.writingAgentReplyPromptTemplate = writingAgentReplyPromptTemplate;
    }

    public WritingImproveResponse improve(WritingImproveRequest request) {
        log.info("Writing improvement requested. Context={}, subjectLength={}, contentLength={}",
                request.context(),
                request.subject() != null ? request.subject().length() : 0,
                request.content() != null ? request.content().length() : 0);

        ValidationResult validationResult = TicketPreValidator.validate(request.subject(), request.content());
        if (!validationResult.isCanProceed()) {
            log.info("Pre-validation failed: {}", validationResult.getReason());
            return new WritingImproveResponse(
                    request.subject(),
                    request.content(),
                    Collections.emptyList(),
                    false,
                    "validation",
                    null,
                    Collections.emptyList(),
                    validationResult
            );
        }

        String systemPrompt = getSystemPromptForContext(request.context());

        String userPrompt = """
                Please improve the following text:
                
                Language/Locale: {language}
                Subject: {subject}
                
                Content: {content}
                """;

        PromptTemplate userPromptTemplate = new PromptTemplate(userPrompt);
        String formattedUserPrompt = userPromptTemplate.render(Map.of(
                "subject", request.subject(),
                "content", request.content(),
                "language", request.language() != null ? request.language() : "auto-detect"
        ));

        try {
            // Because this is UX assistance, don't wait forever. 
            // We implement a basic timeout on the response block if supported by the provider,
            // or rely on a wrapper timeout if necessary. For now we use the native Spring AI features.
            
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(formattedUserPrompt)
                    .call()
                    .entity(WritingImproveResponse.class);

        } catch (Exception e) {
            log.warn("Failed to generate writing improvement: {}", e.getMessage());
            // Fallback: return the original text if AI fails or times out
            return new WritingImproveResponse(
                    request.subject(),
                    request.content(),
                    List.of("AI service unavailable. Original text retained."),
                    false,
                    "fallback",
                    "Unknown",
                    Collections.emptyList(),
                    validationResult
            );
        }
    }

    private String getSystemPromptForContext(WritingContext context) {
        if (context == null) {
            return writingSupportTicketPromptTemplate.getTemplate();
        }

        return switch (context) {
            case SUPPORT_TICKET -> writingSupportTicketPromptTemplate.getTemplate();
            case AGENT_REPLY -> writingAgentReplyPromptTemplate.getTemplate();
            default -> writingSupportTicketPromptTemplate.getTemplate();
        };
    }
}
