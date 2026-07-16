package com.aisupport.auth.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.auth.dto.request.UpdateUserRoleRequest;
import com.aisupport.auth.dto.response.UserResponse;
import com.aisupport.auth.service.UserManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/v1/auth/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Endpoints for managing users, roles, and account statuses")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminController {

    private final UserManagementService userManagementService;

    @Operation(
        summary = "Get all users",
        description = "Retrieves a paginated list of all users in the system"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userManagementService.getAllUsers(pageable));
    }

    @Operation(
        summary = "Update user role",
        description = "Updates a user's role (e.g. CUSTOMER, AGENT, ADMIN)"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @Parameter(description = "Database ID of the user") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return ResponseEntity.ok(userManagementService.updateUserRole(id, request.getRole()));
    }

    @Operation(
        summary = "Lock user account",
        description = "Locks a user account, preventing them from logging in or refreshing tokens"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/lock")
    public ResponseEntity<UserResponse> lockUser(
            @Parameter(description = "Database ID of the user") @PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.lockUser(id));
    }

    @Operation(
        summary = "Unlock user account",
        description = "Unlocks a user account, restoring their ability to login"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/unlock")
    public ResponseEntity<UserResponse> unlockUser(
            @Parameter(description = "Database ID of the user") @PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.unlockUser(id));
    }
}
