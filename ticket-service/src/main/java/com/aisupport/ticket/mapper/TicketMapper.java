package com.aisupport.ticket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.aisupport.ticket.dto.TicketRequest;
import com.aisupport.ticket.dto.TicketResponse;
import com.aisupport.ticket.entity.Ticket;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TicketMapper {
	
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "ticketNumber", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "priority", ignore = true)
	@Mapping(target = "assignedTo", ignore = true)
	@Mapping(target = "intent", ignore = true)
	@Mapping(target = "sentiment", ignore = true)
	@Mapping(target = "urgency", ignore = true)
	@Mapping(target = "slaHours", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Ticket toEntity(TicketRequest request);
    
    @Mapping(source = "status", target = "status")
    @Mapping(source = "priority", target = "priority")
    TicketResponse toResponse(Ticket ticket);
}