package com.common.auth.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenUtil {
    // todo 미사용 확인후 삭제
    //----- Constants -----//
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpirationInSeconds;

    @Value("${jwt.refresh-token.expiration:${jwt.refreshToken-expire-time:1209600}}")
    private long refreshTokenExpirationInSeconds;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String userId, List<String> permissions, List<Map<String, Object>> menus) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationInSeconds * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim("permissions", permissions)
                .claim("menus", menus)
                .claim("token_type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInSeconds * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim("token_type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUserIdFromToken(String token) {
        try {
            String trimmedToken = token != null ? token.trim() : null;
            if (trimmedToken == null || trimmedToken.isEmpty()) {
                log.error("JWT token is null or empty for user ID extraction");
                return null;
            }
            
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(trimmedToken)
                    .getPayload();
            String userId = claims.getSubject();
            log.debug("Extracted user ID from token: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("Failed to extract user ID from token: {}", e.getMessage(), e);
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        try {
            String trimmedToken = token != null ? token.trim() : null;
            if (trimmedToken == null || trimmedToken.isEmpty()) {
                log.error("JWT token is null or empty for permissions extraction");
                return null;
            }
            
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(trimmedToken)
                    .getPayload();
            List<String> permissions = claims.get("permissions", List.class);
            log.debug("Extracted permissions from token: {}", permissions);
            return permissions;
        } catch (Exception e) {
            log.error("Failed to extract permissions from token: {}", e.getMessage(), e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMenusFromToken(String token) {
        try {
            String trimmedToken = token != null ? token.trim() : null;
            if (trimmedToken == null || trimmedToken.isEmpty()) {
                log.error("JWT token is null or empty for menus extraction");
                return null;
            }
            
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(trimmedToken)
                    .getPayload();
            List<Map<String, Object>> menus = claims.get("menus", List.class);
            log.debug("Extracted menus from token: {}", menus);
            return menus;
        } catch (Exception e) {
            log.error("Failed to extract menus from token: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            String trimmedToken = authToken != null ? authToken.trim() : null;
            if (trimmedToken == null || trimmedToken.isEmpty()) {
                log.error("JWT token is null or empty after trimming");
                return false;
            }
            
            log.debug("Validating JWT token...");
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(trimmedToken);
            log.debug("JWT token validation successful");
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage(), ex);
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token format: {}", ex.getMessage(), ex);
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage(), ex);
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage(), ex);
        }
        return false;
    }

    public boolean isAccessToken(String token) {
        try {
            String trimmedToken = token != null ? token.trim() : null;
            if (trimmedToken == null || trimmedToken.isEmpty()) {
                log.error("JWT token is null or empty for access token check");
                return false;
            }
            
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(trimmedToken)
                    .getPayload();
            String tokenType = claims.get("token_type", String.class);
            log.debug("Extracted token type: {}", tokenType);
            boolean isAccess = "access".equals(tokenType);
            log.debug("Token is access token: {}", isAccess);
            return isAccess;
        } catch (Exception ex) {
            log.error("Error checking token type: {}", ex.getMessage(), ex);
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            String trimmedToken = token != null ? token.trim() : null;
            if (trimmedToken == null || trimmedToken.isEmpty()) {
                log.error("JWT token is null or empty for refresh token check");
                return false;
            }
            
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(trimmedToken)
                    .getPayload();
            String tokenType = claims.get("token_type", String.class);
            log.debug("Extracted token type: {}", tokenType);
            boolean isRefresh = "refresh".equals(tokenType);
            log.debug("Token is refresh token: {}", isRefresh);
            return isRefresh;
        } catch (Exception ex) {
            log.error("Error checking token type: {}", ex.getMessage(), ex);
            return false;
        }
    }
}
