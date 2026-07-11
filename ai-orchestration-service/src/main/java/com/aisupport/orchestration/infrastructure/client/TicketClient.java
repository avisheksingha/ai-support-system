package com.aisupport.orchestration.infrastructure.client;

import com.aisupport.orchestration.domain.model.Result;

public interface TicketClient {
    Result<Object> getTicket(Long ticketId); // Placeholder signature
}
