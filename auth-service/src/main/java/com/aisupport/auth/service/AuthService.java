package com.aisupport.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.auth.dto.AuthResponse;
import com.aisupport.auth.dto.LoginRequest;
import com.aisupport.auth.dto.RefreshRequest;
import com.aisupport.auth.dto.RegisterRequest;
import com.aisupport.auth.dto.UserResponse;
import com.aisupport.auth.entity.LoginAudit;
import com.aisupport.auth.entity.RefreshToken;
import com.aisupport.auth.entity.User;
import com.aisupport.common.enums.UserRole;
import com.aisupport.auth.exception.AuthException;
import com.aisupport.auth.repository.LoginAuditRepository;
import com.aisupport.auth.repository.RefreshTokenRepository;
import com.aisupport.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_SUCCESS = "SUCCESS";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAuditRepository loginAuditRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email is already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.CUSTOMER) // Default role
                .build();

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    auditLogin(null, ipAddress, STATUS_FAILED);
                    return new AuthException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditLogin(user, ipAddress, STATUS_FAILED);
            throw new AuthException("Invalid email or password");
        }

        if (!user.isEnabled() || user.isLocked()) {
            auditLogin(user, ipAddress, STATUS_FAILED);
            throw new AuthException("Account is locked or disabled");
        }

        auditLogin(user, ipAddress, STATUS_SUCCESS);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        return refreshTokenRepository.findByToken(request.getRefreshToken())
                .map(jwtService::verifyRefreshTokenExpiration)
                .map(oldToken -> {
                    User user = oldToken.getUser();
                    AuthResponse response = buildAuthResponse(user);
                    refreshTokenRepository.delete(oldToken);
                    return response;
                })
                .orElseThrow(() -> new AuthException("Refresh token is not in database!"));
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(900L) // 15 mins
                .build();
    }

    private void auditLogin(User user, String ipAddress, String status) {
        loginAuditRepository.save(LoginAudit.builder()
                .user(user)
                .ipAddress(ipAddress)
                .loginStatus(status)
                .build());
    }
}
