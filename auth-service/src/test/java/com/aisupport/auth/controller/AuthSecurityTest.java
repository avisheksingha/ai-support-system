package com.aisupport.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aisupport.auth.config.SecurityConfig;
import com.aisupport.auth.service.AuthService;
import com.aisupport.auth.service.UserManagementService;
import com.aisupport.common.auth.SecurityConstants;

import lombok.RequiredArgsConstructor;

@WebMvcTest({AuthController.class, AdminController.class})
@Import(SecurityConfig.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class AuthSecurityTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserManagementService userManagementService;

    @Test
    void currentUserRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/auth/admin/users")
                        .header(SecurityConstants.HEADER_USER_ID, "7")
                        .header(SecurityConstants.HEADER_USER_ROLE, "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminEndpoints() throws Exception {
        when(userManagementService.getAllUsers(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/auth/admin/users")
                        .header(SecurityConstants.HEADER_USER_ID, "1")
                        .header(SecurityConstants.HEADER_USER_ROLE, "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void missingRoleHeaderShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/admin/users")
                        .header(SecurityConstants.HEADER_USER_ID, "1"))
                .andExpect(status().isUnauthorized());
    }
}