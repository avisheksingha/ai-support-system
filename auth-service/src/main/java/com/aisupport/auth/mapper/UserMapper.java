package com.aisupport.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.aisupport.auth.dto.request.RegisterRequest;
import com.aisupport.auth.dto.response.UserResponse;
import com.aisupport.auth.entity.User;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "enabled", ignore = true)
	@Mapping(target = "locked", ignore = true)
	User toEntity(RegisterRequest request);
	
	UserResponse toResponse(User user);
	
}
