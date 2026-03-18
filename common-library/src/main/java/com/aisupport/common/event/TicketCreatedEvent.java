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
public class TicketCreatedEvent {

    private Long ticketId;
    private String ticketNumber;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
}
