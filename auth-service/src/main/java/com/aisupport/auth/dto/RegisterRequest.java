package com.aisupport.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Request payload for user registration", 
    requiredProperties = {"email", "password", "fullName"},
    example = "{\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"SecureP@ssw0rd\",\n  \"fullName\": \"John Doe\"\n}"
)
public class RegisterRequest {
    
    @Schema(description = "User's email address which will be used for login", example = "john.doe@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is not valid")
    private String email;

    @Schema(description = "Strong password for the account", example = "SecureP@ssw0rd")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Schema(description = "User's full name", example = "John Doe")
    @NotBlank(message = "Full name is required")
    private String fullName;
}
