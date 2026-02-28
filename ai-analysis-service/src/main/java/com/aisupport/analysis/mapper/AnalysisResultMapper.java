package com.aisupport.analysis.mapper;

import java.util.Arrays;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.aisupport.analysis.entity.AnalysisResult;
import com.aisupport.common.dto.AnalysisResultDTO;

@Mapper(
		componentModel = "spring",
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AnalysisResultMapper {

    @Mapping(target = "version", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "keywords", source = "keywords")
    AnalysisResult toEntity(AnalysisResultDTO dto);

    @Mapping(target = "analysisProvider", ignore = true)
    @Mapping(target = "analyzedAt", ignore = true)
    @Mapping(target = "keywords", source = "keywords")
    AnalysisResultDTO toDto(AnalysisResult entity);

    // Helper methods for converting between String[] and List<String>
    default List<String> arrayToList(String[] arr) {
        return arr != null ? Arrays.asList(arr) : List.of();
    }

    default String[] listToArray(List<String> list) {
        return list != null ? list.toArray(new String[0]) : null;
    }
}
