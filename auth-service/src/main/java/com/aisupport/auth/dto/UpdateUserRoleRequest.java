package com.aisupport.auth.dto;

import com.aisupport.common.enums.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating a user's role", requiredProperties = {"role"})
public class UpdateUserRoleRequest {
    
    @Schema(description = "The new role to assign to the user", example = "AGENT")
    @NotNull(message = "Role is required")
    private UserRole role;
}
