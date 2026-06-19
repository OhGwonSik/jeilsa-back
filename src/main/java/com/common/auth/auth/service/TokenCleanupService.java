package com.common.auth.auth.service;


import com.common.auth.auth.domain.TokenStatistics;
import com.common.auth.auth.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {
    //----- DI Fields -----//
    private final RefreshTokenMapper refreshTokenMapper;
    
    //----- Constant -----//
    @Value("${system.uuid:00000000-0000-0000-0000-000000000000}")
    private Integer systemUuid;

    /**
     * 매일 새벽 2시에 만료된 토큰들을 revoked 상태로 변경
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void revokeExpiredTokens() {
        log.info("Starting scheduled revoke of expired refresh tokens");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            int revokedCount = refreshTokenMapper.revokeExpiredTokens(now, systemUuid);
            
            if (revokedCount > 0) {
                log.info("Successfully revoked {} expired refresh tokens", revokedCount);
            } else {
                log.debug("No expired refresh tokens found to revoke");
            }
        } catch (Exception e) {
            log.error("Error occurred while revoking expired tokens", e);
        }
    }

    /**
     * 매주 일요일 새벽 3시에 30일 이상 된 revoked 토큰들을 물리적으로 삭제
     * 감사 추적을 위해 일정 기간 보관 후 삭제
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void deleteOldRevokedTokens() {
        log.info("Starting cleanup of old revoked refresh tokens");
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deletedCount = refreshTokenMapper.deleteOldRevokedTokens(cutoffDate);
            
            if (deletedCount > 0) {
                log.info("Successfully deleted {} old revoked refresh tokens", deletedCount);
            } else {
                log.debug("No old revoked refresh tokens found to delete");
            }
        } catch (Exception e) {
            log.error("Error occurred while deleting old revoked tokens", e);
        }
    }

    /**
     * 수동 실행용 메서드 - 관리자가 필요시 호출
     */
    public int manualCleanup() {
        log.info("Manual cleanup of expired refresh tokens requested");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            int revokedCount = refreshTokenMapper.revokeExpiredTokens(now, systemUuid);
            
            log.info("Manual cleanup completed. Revoked {} expired tokens", revokedCount);
            return revokedCount;
        } catch (Exception e) {
            log.error("Error occurred during manual cleanup", e);
            throw new RuntimeException("Manual cleanup failed", e);
        }
    }

    /**
     * 시스템 통계 정보 제공
     */
    public TokenStatistics getTokenStatistics() {
        try {
            int totalTokens = refreshTokenMapper.countAllTokens();
            int validTokens = refreshTokenMapper.countValidTokens();
            int expiredTokens = refreshTokenMapper.countExpiredTokens();
            int revokedTokens = refreshTokenMapper.countRevokedTokens();
            
            return new TokenStatistics(totalTokens, validTokens, expiredTokens, revokedTokens);
        } catch (Exception e) {
            log.error("Error occurred while fetching token statistics", e);
            return new TokenStatistics(0, 0, 0, 0);
        }
    }
}