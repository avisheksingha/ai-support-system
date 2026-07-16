package com.aisupport.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.aisupport.auth.dto.request.UpdateUserRoleRequest;
import com.aisupport.auth.dto.response.UserResponse;
import com.aisupport.auth.service.UserManagementService;
import com.aisupport.common.enums.UserRole;

class AdminControllerTest {

    private UserManagementService userManagementService;
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        userManagementService = mock(UserManagementService.class);
        adminController = new AdminController(userManagementService);
    }

    @Test
    void updateUserRole_shouldReturnUpdatedUser() {
        UserResponse userDto = UserResponse.builder().id(1L).role(UserRole.AGENT).build();

        when(userManagementService.updateUserRole(1L, UserRole.AGENT)).thenReturn(userDto);

        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRole(UserRole.AGENT);
        
        ResponseEntity<UserResponse> result = adminController.updateUserRole(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getRole()).isEqualTo(UserRole.AGENT);
        verify(userManagementService).updateUserRole(1L, UserRole.AGENT);
    }

    @Test
    void lockUser_shouldReturnUpdatedUser() {
        UserResponse userDto = UserResponse.builder().id(2L).locked(true).build();

        when(userManagementService.lockUser(2L)).thenReturn(userDto);

        ResponseEntity<UserResponse> result = adminController.lockUser(2L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getLocked()).isTrue();
        verify(userManagementService).lockUser(2L);
    }
}
