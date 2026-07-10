package com.aisupport.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.aisupport.auth.config.JwtConfig;
import com.aisupport.auth.entity.RefreshToken;
import com.aisupport.auth.entity.User;
import com.aisupport.auth.exception.TokenExpiredException;
import com.aisupport.auth.repository.RefreshTokenRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Bridges java.time to JJWT's builder API, which still requires
     * java.util.Date as of jjwt 0.13.x (no Instant overload exists for
     * issuedAt/expiration/notBefore). Centralizing the conversion here means
     * there's exactly one place to update if a future JJWT release adds
     * native Instant support — and one clear, documented reason for the
     * otherwise-legacy Date import in this file (see SonarLint "use
     * java.time" info-level finding).
     *
     * @param instant the point in time to convert
     * @return the equivalent java.util.Date
     */
     private static Date toJwtDate(Instant instant) {
    	    return Date.from(instant); // NOSONAR — JJWT 0.13.0 builder API requires java.util.Date, no Instant overload exists
     }

    /**
     * Gets the sign-in key for the JWT.
     *
     * @return The sign-in key.
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        } catch (DecodingException e) {
            try {
                keyBytes = MessageDigest.getInstance("SHA-256")
                        .digest(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException ex) {
                keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a new access token for the given user.
     *
     * @param user The user for whom to generate an access token.
     * @return The generated access token.
     */
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .claim("name", user.getFullName())
                .issuedAt(toJwtDate(Instant.now()))
                .expiration(toJwtDate(Instant.now().plusMillis(jwtConfig.getAccessTokenExpirationMs())))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Generates a new refresh token for the given user.
     *
     * @param user The user for whom to generate a refresh token.
     * @return The generated refresh token.
     */
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

    /**
     * Hashes the provided refresh token using SHA-256.
     *
     * @param token The refresh token to hash.
     * @return The hashed representation of the refresh token.
     */
    public String hashRefreshToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    /**
     * Verifies the expiration of a refresh token.
     * If the token has expired, it will be deleted from the repository and a TokenExpiredException will be thrown.
     * If the token has been revoked, a TokenExpiredException will be thrown.
     *
     * @param token The refresh token to verify.
     * @return The valid refresh token if it has not expired or been revoked.
     * @throws TokenExpiredException if the token has expired or been revoked.
     */
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