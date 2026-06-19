package com.common.auth.auth.mapper;


import com.common.auth.auth.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface RefreshTokenMapper {
    Optional<RefreshToken> findRefreshTokenByTokenHash(@Param("refreshToken") String refreshToken);

    List<RefreshToken> findRefreshTokensByMemberId(@Param("memberId") Integer memberId);

    List<RefreshToken> findValidRefreshTokensByMemberId(@Param("memberId") Integer memberId);

    void insertRefreshToken(RefreshToken refreshToken);

    int revokeToken(@Param("refreshToken") String refreshToken, @Param("expireDt") LocalDateTime revokedAt, @Param("delId") Integer rvkId);

    int revokeAllUserTokens(Integer memberId, @Param("revokedAt") LocalDateTime revokedAt, Integer rvkId);

    int deleteRefreshTokenByTokenHash(@Param("refreshToken") String refreshToken);

    int deleteExpiredTokens();

    int deleteRefreshTokensByMemberId(@Param("memberId") Integer memberId);

    boolean existsRefreshTokenByTokenHash(@Param("refreshToken") String refreshToken);

    int countValidTokensByMemberId(@Param("memberId") Integer memberId);

    int countExpiredTokens();

    List<RefreshToken> findExpiredTokens();

    int revokeExpiredTokens(@Param("revokedAt") LocalDateTime revokedAt, @Param("rvkId") Integer rvkId);

    int deleteOldRevokedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    int countAllTokens();

    int countValidTokens();

    int countRevokedTokens();
}