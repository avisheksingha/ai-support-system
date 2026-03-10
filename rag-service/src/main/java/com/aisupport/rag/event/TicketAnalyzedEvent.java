package com.aisupport.rag.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAnalyzedEvent {

    private Long ticketId;
    private String intent;
    private String sentiment;
    private String urgency;
    private Double confidenceScore;
    private List<String> keywords;
    private String suggestedCategory;
    private LocalDateTime analyzedAt;
}
