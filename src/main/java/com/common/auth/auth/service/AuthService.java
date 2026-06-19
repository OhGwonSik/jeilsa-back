package com.common.auth.auth.service;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.common.auth.auth.domain.TokenPair;
import com.common.auth.auth.dto.LoginRequest;
import com.common.auth.auth.dto.LoginResponse;
import com.common.auth.common.exception.AuthenticationException;
import com.common.auth.common.exception.ErrorCode;
import com.jeil.delivery.security.JwtProvider;
import com.common.auth.menu.service.MenuService;
import com.common.auth.permission.service.PermissionService;
import com.common.auth.user.domain.User;
import com.common.auth.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    //----- DI Fields -----//
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final MemberService memberService;
    private final PermissionService permissionService;
    private final MenuService menuService;

    //----- Fields -----//
    @Value("${auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    //----- Methods -----//
    public LoginResponse login(LoginRequest loginRequest) {
        return login(loginRequest, null, null);
    }

    public LoginResponse login(LoginRequest loginRequest, InetAddress ipAddress, String userAgent) {
        log.info("Attempting login for email: {}", loginRequest.getUserId());
        int memberId = memberService.selectMemberIdByUserId(loginRequest.getUserId());
        if (memberId==0) {
            throw new AuthenticationException(ErrorCode.USER_NOT_FOUND);
        }
        // 계정 잠금 여부 확인
        if (userService.isAccountLocked(memberId, maxLoginAttempts)) {
            log.warn("Account locked due to too many failed attempts: {}", loginRequest.getUserId());
            throw new AuthenticationException(ErrorCode.ACCOUNT_LOCKED);
        }

        Optional<MemberDTO> userOpt = userService.selectUserByMemberId(loginRequest.getUserId());
        if (userOpt.isEmpty()) {
            log.warn("User not found for email: {}", loginRequest.getUserId());
            throw new AuthenticationException(ErrorCode.AUTHENTICATION_FAILED);
        }

        MemberDTO memberDTO = userOpt.get();
        log.debug("Found user: {}", memberDTO);
        log.debug("User delYn: {}", memberDTO.getDelYn());

        if (memberDTO.getDelYn().equals("Y")) {
            log.warn("Disabled user attempted login: {}", loginRequest.getUserId());
            throw new AuthenticationException(ErrorCode.ACCOUNT_DISABLED);
        }

        boolean passwordMatches = userService.validatePassword(loginRequest.getUserPw(), memberDTO.getUserPw());
        log.debug("Password validation result: {}", passwordMatches);

        if (!passwordMatches) {
            log.warn("Invalid password for user: {}", loginRequest.getUserId());
            throw new AuthenticationException(ErrorCode.AUTHENTICATION_FAILED);
        }

        // 로그인 성공 처리
        Integer loginMemberId = memberDTO.getMemberId();
        userService.recordLoginSuccess(memberDTO.getMemberId());

        List<String> permissions = fetchUserPermissions(loginMemberId);
        List<Map<String, Object>> menus = fetchUserMenus(loginMemberId);

        TokenPair tokenPair = generateTokenPair(memberDTO, permissions, menus, ipAddress, userAgent);

        log.info("Successful login for user: {}", loginRequest.getUserId());

        return new LoginResponse(
            tokenPair.getAccessToken(),
            tokenPair.getRefreshToken(),
            String.valueOf(memberId),
            memberDTO.getEmail(),
            memberDTO.getTelNo(),
            memberDTO.getName(),
            memberDTO.getMemberStatusCd(),
            memberDTO.getRoles(),
            memberDTO.getCompanyId(),
            permissions,
            menus
        );
    }

    public void logout(String refreshToken) {
        log.info("Logging out user");
        revokeRefreshToken(refreshToken);
    }

    public TokenPair generateTokenPair(MemberDTO memberDTO, List<String> permissions, List<Map<String, Object>> menus) {
        return generateTokenPair(memberDTO, permissions, menus, null, null);
    }

    public TokenPair generateTokenPair(MemberDTO memberDTO, List<String> permissions, List<Map<String, Object>> menus, InetAddress ipAddress, String userAgent) {
        log.debug("Generating token pair for user: {} with {} permissions and {} menus", memberDTO.getMemberId(), permissions.size(), menus.size());
        String accessToken = jwtProvider.generateAccessToken(memberDTO, permissions, menus);
        String refreshToken = refreshTokenService.createRefreshToken(memberDTO.getMemberId(), ipAddress, userAgent);

        log.info("Generated token pair for user: {} - Access token length: {}, Refresh token length: {}",
        		memberDTO.getMemberId(), accessToken.length(), refreshToken.length());
        return new TokenPair(accessToken, refreshToken);
    }

    public TokenPair refreshTokens(String refreshToken) {
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            log.warn("Invalid refresh token provided");
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }

        Integer memberId = refreshTokenService.getUserIdFromRefreshToken(refreshToken);
        if (memberId == null) {
            log.warn("No user found for refresh token");
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }

        Optional<MemberDTO> userOpt = userService.selectUserBymemberId(memberId);
        MemberDTO memberDTO = userOpt.get();
        List<String> permissions = fetchUserPermissions(memberId);
        List<Map<String, Object>> menus = fetchUserMenus(memberId);

        // 토큰 갱신
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
        String newAccessToken = jwtProvider.generateAccessToken(memberDTO, permissions, menus);

        log.info("Refreshed tokens for user: {}", memberId);
        return new TokenPair(newAccessToken, newRefreshToken);
    }

    public void revokeRefreshToken(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("Revoked refresh token");
    }

    public void revokeAllUserTokens(Integer memberId) {
        refreshTokenService.revokeAllRefreshTokensByMemberId(memberId);
        log.info("Revoked all tokens for user: {}", memberId);
    }

    private List<String> fetchUserPermissions(Integer memberId) {
        log.debug("Fetching permissions for user: {}", memberId);

        // 최상위 권한만 가져와서 JWT 크기 최적화
        List<String> permissions = permissionService.getHighestLevelPermissionsByMemberId(memberId);
        log.debug("Retrieved {} highest-level permissions for user {}: {}", permissions.size(), memberId, permissions);

        // 디버깅을 위해 전체 권한도 로깅
        List<String> allPermissions = permissionService.getPermissionNamesByMemberId(memberId);
        log.debug("Full permissions ({}): {}", allPermissions.size(), allPermissions);

        return permissions;
    }

    private List<Map<String, Object>> fetchUserMenus(Integer memberId) {
        log.debug("Fetching menus for user: {}", memberId);
        List<Map<String, Object>> menus = menuService.getMenusByMemberId(memberId, true);
        log.debug("Retrieved {} menus for user: {}", menus.size(), memberId);

        return menus;
    }
}