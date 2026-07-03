package com.aisupport.auth.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aisupport.auth.dto.UpdateUserRoleRequest;
import com.aisupport.auth.dto.UserResponse;
import com.aisupport.auth.service.UserManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/auth/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Endpoints for managing users, roles, and account statuses")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminController {

    private final UserManagementService userManagementService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users in the system")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userManagementService.getAllUsers(pageable));
    }

    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Updates a user's role (e.g. CUSTOMER, AGENT, ADMIN)")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @Parameter(description = "New user role", example = "{\"role\":\"AGENT\"}")
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return ResponseEntity.ok(userManagementService.updateUserRole(id, request.getRole()));
    }

    @PostMapping("/users/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lock user account", description = "Locks a user account, preventing them from logging in or refreshing tokens")
    public ResponseEntity<UserResponse> lockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.lockUser(id));
    }

    @PostMapping("/users/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlock user account", description = "Unlocks a user account")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.unlockUser(id));
    }
}
