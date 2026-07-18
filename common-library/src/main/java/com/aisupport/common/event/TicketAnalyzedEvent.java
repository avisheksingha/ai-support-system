package com.aisupport.common.event;

import java.time.Instant;

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
    private String ticketDescription;
    private AnalysisResult analysis;
    private Instant analyzedAt;
}
