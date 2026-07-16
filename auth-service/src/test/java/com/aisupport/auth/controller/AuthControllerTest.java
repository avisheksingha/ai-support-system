package com.aisupport.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.aisupport.auth.dto.request.LoginRequest;
import com.aisupport.auth.dto.request.RegisterRequest;
import com.aisupport.auth.dto.response.AuthResponse;
import com.aisupport.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
    }

    @Test
    void register_shouldReturnOkStatus() {
        RegisterRequest request = new RegisterRequest();
        AuthResponse response = AuthResponse.builder().accessToken("token").build();

        when(authService.register(request)).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.register(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(authService).register(request);
    }

    @Test
    void login_shouldReturnOkStatusAndPassIpAddress() {
        LoginRequest request = new LoginRequest();
        AuthResponse response = AuthResponse.builder().accessToken("token").build();
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        when(servletRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(authService.login(request, "192.168.1.1")).thenReturn(response);

        ResponseEntity<AuthResponse> result = authController.login(request, servletRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
        verify(authService).login(request, "192.168.1.1");
    }
}
