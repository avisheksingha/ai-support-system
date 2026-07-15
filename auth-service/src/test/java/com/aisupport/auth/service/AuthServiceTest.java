package com.aisupport.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aisupport.auth.config.JwtConfig;
import com.aisupport.auth.dto.LoginRequest;
import com.aisupport.auth.dto.RegisterRequest;
import com.aisupport.auth.entity.User;
import com.aisupport.auth.exception.AuthException;
import com.aisupport.auth.mapper.UserMapper;
import com.aisupport.auth.repository.LoginAuditRepository;
import com.aisupport.auth.repository.RefreshTokenRepository;
import com.aisupport.auth.repository.UserRepository;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private RefreshTokenRepository refreshTokenRepository;
    private LoginAuditRepository loginAuditRepository;
    private AuthService authService;
    private JwtConfig jwtConfig;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        loginAuditRepository = mock(LoginAuditRepository.class);
        jwtConfig = mock(JwtConfig.class);
        userMapper = mock(UserMapper.class);
        
        authService = new AuthService(
                userRepository, 
                passwordEncoder, 
                jwtService, 
                refreshTokenRepository, 
                loginAuditRepository,
                jwtConfig,
                userMapper
        );
    }

    @Test
    void register_shouldRejectExistingEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Email is already in use");
    }

    @Test
    void login_shouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedpassword");
        user.setEnabled(true);
        user.setLocked(false);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashedpassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Invalid email or password");
    }
}
