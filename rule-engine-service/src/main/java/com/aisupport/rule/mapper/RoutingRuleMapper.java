package com.aisupport.rule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.aisupport.rule.dto.RuleRequest;
import com.aisupport.rule.dto.RuleResponse;
import com.aisupport.rule.model.RoutingRule;
import org.mapstruct.Mapping;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RoutingRuleMapper {
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	RoutingRule toEntity(RuleRequest request);
	
	RuleResponse toResponse(RoutingRule routingRule);

}
