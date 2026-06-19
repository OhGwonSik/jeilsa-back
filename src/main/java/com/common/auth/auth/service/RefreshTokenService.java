package com.common.auth.auth.service;

import com.common.auth.auth.domain.RefreshToken;
import com.common.auth.auth.mapper.RefreshTokenMapper;
import com.common.auth.common.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class RefreshTokenService {
    //----- Constant -----//
    private static final int TOKEN_BYTE_SIZE = 32;
    private static final int SUCCESS_COUNT_THRESHOLD = 0;

    @Value("${jwt.refresh-token.expiration:${jwt.refreshToken-expire-time:1209600}}")
    private long refreshTokenExpirationInSeconds;

    @Value("${system.user-id:1}")
    private Integer systemId;

    //----- DI Fields -----//
    private final RefreshTokenMapper refreshTokenMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    //----- Constructors -----//
    public RefreshTokenService(RefreshTokenMapper refreshTokenMapper) {
        this.refreshTokenMapper = refreshTokenMapper;
    }

    //----- Methods -----//
    public String createRefreshToken(Integer memberId) {
        return createRefreshToken(memberId, null, null);
    }

    public String createRefreshToken(Integer memberId, InetAddress ipAddress, String userAgent) {
        // Generate random token
        byte[] tokenBytes = new byte[TOKEN_BYTE_SIZE];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Hash the token for storage
        String tokenHash = hashToken(token);
        
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(refreshTokenExpirationInSeconds);
        
        RefreshToken refreshToken = new RefreshToken(tokenHash, memberId, expiryTime, ipAddress, userAgent);
        refreshTokenMapper.insertRefreshToken(refreshToken);
        
        log.debug("Created refresh token for user: {}", memberId);
        return token; // Return the original token, not the hash
    }

    public boolean validateRefreshToken(String token) {
        String tokenHash = hashToken(token);
        Optional<RefreshToken> refreshTokenOpt = refreshTokenMapper.findRefreshTokenByTokenHash(tokenHash);
        
        if (refreshTokenOpt.isEmpty()) {
            log.warn("Refresh token not found");
            return false;
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        
        if (!refreshToken.isValid()) {
            log.warn("Refresh token is invalid (revoked or expired)");
            return false;
        }
        
        return true;
    }

    public Integer getUserIdFromRefreshToken(String token) {
        String tokenHash = hashToken(token);
        Optional<RefreshToken> refreshTokenOpt = refreshTokenMapper.findRefreshTokenByTokenHash(tokenHash);
        return refreshTokenOpt.map(RefreshToken::getMemberId).orElse(null);
    }

    public void revokeRefreshToken(String token) {
        revokeRefreshToken(token, null);
    }

    public void revokeRefreshToken(String token, Integer rvkId) {
        String tokenHash = hashToken(token);
        LocalDateTime revokedAt = LocalDateTime.now();
        
        // rvkId가 null이면 현재 사용자 ID를 사용
        Integer actualRvkId = rvkId;
        if (actualRvkId == null) {
            try {
                Integer currentUserId = SecurityUtil.getCurrentMemberId();
                actualRvkId = currentUserId;
            } catch (IllegalStateException e) {
                log.warn("No authenticated user found for token revocation");
                actualRvkId = systemId;
            }
        }
        
        int updatedRows = refreshTokenMapper.revokeToken(tokenHash, revokedAt, actualRvkId);
        
        if (updatedRows > SUCCESS_COUNT_THRESHOLD) {
            log.debug("Revoked refresh token by user: {}", actualRvkId);
        } else {
            log.warn("Refresh token not found for revocation");
        }
    }

    public void revokeAllRefreshTokensByMemberId(Integer memberId) {
        revokeAllRefreshTokensByMemberId(memberId, null);
    }

    public void revokeAllRefreshTokensByMemberId(Integer memberId, Integer rvkId) {
        LocalDateTime revokedAt = LocalDateTime.now();
        
        // rvkId가 null이면 현재 사용자 ID를 사용
        Integer actualRvkId = rvkId;
        if (actualRvkId == null) {
            try {
                Integer currentUserId = SecurityUtil.getCurrentMemberId();
                actualRvkId = currentUserId;
            } catch (IllegalStateException e) {
                log.warn("No authenticated user found for token revocation");
                actualRvkId = systemId;
            }
        }
        
        int updatedRows = refreshTokenMapper.revokeAllUserTokens(memberId, revokedAt, actualRvkId);
        log.debug("Revoked {} refresh tokens for user: {} by: {}", updatedRows, memberId, actualRvkId);
    }

    public void deleteRefreshToken(String token) {
        String tokenHash = hashToken(token);
        int deletedRows = refreshTokenMapper.deleteRefreshTokenByTokenHash(tokenHash);
        
        if (deletedRows > SUCCESS_COUNT_THRESHOLD) {
            log.debug("Deleted refresh token");
        } else {
            log.warn("Refresh token not found for deletion");
        }
    }

    public void deleteAllRefreshTokensByMemberId(Integer memberId) {
        int deletedRows = refreshTokenMapper.deleteRefreshTokensByMemberId(memberId);
        log.debug("Deleted {} refresh tokens for user: {}", deletedRows, memberId);
    }

    public String rotateRefreshToken(String oldToken) {
        String oldTokenHash = hashToken(oldToken);
        Optional<RefreshToken> refreshTokenOpt = refreshTokenMapper.findRefreshTokenByTokenHash(oldTokenHash);
        
        if (refreshTokenOpt.isEmpty()) {
            log.warn("Old refresh token not found for rotation");
            return null;
        }
        
        RefreshToken oldRefreshToken = refreshTokenOpt.get();
        
        if (!oldRefreshToken.isValid()) {
            log.warn("Old refresh token is invalid for rotation");
            return null;
        }
        
        // Revoke old token
        revokeRefreshToken(oldToken);
        
        // Create new token
        return createRefreshToken(oldRefreshToken.getMemberId(), oldRefreshToken.getIpAddress(), oldRefreshToken.getUserAgent());
    }

    public List<RefreshToken> getValidTokensByMemberId(Integer memberId) {
        return refreshTokenMapper.findValidRefreshTokensByMemberId(memberId);
    }

    public List<RefreshToken> getAllTokensByMemberId(Integer memberId) {
        return refreshTokenMapper.findRefreshTokensByMemberId(memberId);
    }

    public void cleanupExpiredTokens() {
        int deletedCount = refreshTokenMapper.deleteExpiredTokens();
        if (deletedCount > SUCCESS_COUNT_THRESHOLD) {
            log.info("Cleaned up {} expired refresh tokens", deletedCount);
        }
    }

    public int countValidTokensByMemberId(Integer memberId) {
        return refreshTokenMapper.countValidTokensByMemberId(memberId);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}