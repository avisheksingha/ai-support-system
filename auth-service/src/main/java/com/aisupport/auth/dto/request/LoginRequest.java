package com.aisupport.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
    description = "Request payload for user login", 
    requiredProperties = {"email", "password"},
    example = "{\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"SecureP@ssw0rd\"\n}"
)
public class LoginRequest {

    @Schema(description = "User's registered email address", example = "john.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is not valid")
    private String email;

    @Schema(description = "User's password", example = "SecureP@ssw0rd")
    @NotBlank(message = "Password is required")
    private String password;
}
