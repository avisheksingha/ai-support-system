package com.aisupport.rule.mapper;

import java.util.Arrays;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.aisupport.rule.dto.RuleRequest;
import com.aisupport.rule.dto.RuleResponse;
import com.aisupport.rule.entity.RoutingRule;

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
	@Mapping(target = "keywordPatterns", source = "keywordPatterns")
	RoutingRule toEntity(RuleRequest request);

	@Mapping(target = "keywordPatterns", source = "keywordPatterns")
	RuleResponse toResponse(RoutingRule routingRule);

	// Helper methods for converting between String[] (DB) and List<String> (DTO)
	default List<String> arrayToList(String[] arr) {
		return arr != null ? Arrays.asList(arr) : List.of();
	}

	default String[] listToArray(List<String> list) {
		return list != null ? list.toArray(new String[0]) : null;
	}
}
