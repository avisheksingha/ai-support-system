package com.aisupport.auth.service;

import java.time.Instant;
import java.util.Date; // NOSONAR (JJWT requires java.util.Date)
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.aisupport.auth.config.JwtConfig;
import com.aisupport.auth.entity.RefreshToken;
import com.aisupport.auth.entity.User;
import com.aisupport.auth.exception.TokenExpiredException;
import com.aisupport.auth.repository.RefreshTokenRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(jwtConfig.getAccessTokenExpirationMs())))
                .signWith(getSignInKey())
                .compact();
    }

    public RefreshToken generateRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(hashRefreshToken(rawToken))
                .rawToken(rawToken)
                .expiresAt(Instant.now().plusMillis(jwtConfig.getRefreshTokenExpirationMs()))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public String hashRefreshToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    public RefreshToken verifyRefreshTokenExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenExpiredException("Refresh token has expired. Please sign in again");
        }
        if (token.isRevoked()) {
            throw new TokenExpiredException("Refresh token has been revoked");
        }
        return token;
    }
}
