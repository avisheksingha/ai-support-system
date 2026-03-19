package com.aisupport.common.event;

import java.time.LocalDateTime;

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
public class TicketRagResponseEvent {

    private Long ticketId;
    private String query;
    private String response;
    private String model;
    private LocalDateTime generatedAt;
}
