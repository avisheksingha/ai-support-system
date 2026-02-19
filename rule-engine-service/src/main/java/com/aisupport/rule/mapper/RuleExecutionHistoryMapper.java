package com.aisupport.rule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.aisupport.rule.dto.RuleExecutionHistoryResponse;
import com.aisupport.rule.model.RuleExecutionHistory;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RuleExecutionHistoryMapper {

	RuleExecutionHistoryResponse toResponse(RuleExecutionHistory history);
}
