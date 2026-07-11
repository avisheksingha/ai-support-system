package com.aisupport.orchestration.infrastructure.client.impl;

import org.springframework.stereotype.Service;

import com.aisupport.orchestration.domain.model.Result;
import com.aisupport.orchestration.infrastructure.client.TicketClient;

@Service
public class TicketClientImpl implements TicketClient {
    @Override
    public Result<Object> getTicket(Long ticketId) {
        return Result.success("Dummy ticket data");
    }
}
