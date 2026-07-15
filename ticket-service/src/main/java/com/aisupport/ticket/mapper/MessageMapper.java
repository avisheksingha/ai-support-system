package com.aisupport.ticket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.aisupport.ticket.dto.MessageRequest;
import com.aisupport.ticket.dto.MessageResponse;
import com.aisupport.ticket.entity.Message;

@Mapper(componentModel = "spring")
public interface MessageMapper {
	
    @Mapping(target = "id", ignore = true)
	@Mapping(target = "ticket", ignore = true)
	@Mapping(target = "type", ignore = true)
	@Mapping(target = "isInternal", ignore = true)
	Message toEntity(MessageRequest request);

    @Mapping(target = "isInternal", ignore = true)
	@Mapping(source = "ticket.ticketNumber", target = "ticketNumber")
    MessageResponse toResponse(Message message);
}
