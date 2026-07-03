package com.aisupport.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.auth.dto.AuthResponse;
import com.aisupport.auth.dto.LoginRequest;
import com.aisupport.auth.dto.RefreshRequest;
import com.aisupport.auth.dto.RegisterRequest;
import com.aisupport.auth.dto.UserResponse;
import com.aisupport.auth.service.AuthService;
import com.aisupport.common.auth.SecurityConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, token refresh, and logout")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Register a new user",
        description = "Registers a new user account with basic details and assigns a default role"
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
        summary = "Login user",
        description = "Authenticates a user with email and password, returning JWT access and refresh tokens"
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request, 
            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.login(request, ipAddress));
    }

    @Operation(
        summary = "Refresh access token",
        description = "Generates a new JWT access token using a valid refresh token"
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(
        summary = "Logout user",
        description = "Invalidates the user's current session and access tokens"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Internal user ID header provided by gateway") @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = true) Long userId) {
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the profile information of the currently authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @Parameter(description = "Internal user ID header provided by gateway") @RequestHeader(value = SecurityConstants.HEADER_USER_ID, required = true) Long userId) {
        return ResponseEntity.ok(authService.getCurrentUser(userId));
    }
}
