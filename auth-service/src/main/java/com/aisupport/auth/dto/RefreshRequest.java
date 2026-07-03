package com.aisupport.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Request payload for refreshing an access token", 
    requiredProperties = {"refreshToken"},
    example = "{\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n}"
)
public class RefreshRequest {

    @Schema(description = "The valid refresh token obtained during login", example = "eyJhbGciOiJIUzUxMiJ9...")
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
