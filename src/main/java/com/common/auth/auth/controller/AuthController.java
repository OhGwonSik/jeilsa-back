package com.common.auth.auth.controller;

import com.common.auth.auth.domain.TokenPair;
import com.common.auth.auth.dto.LoginRequest;
import com.common.auth.auth.dto.LoginResponse;
import com.common.auth.auth.dto.RefreshTokenRequest;
import com.common.auth.auth.dto.TokenResponse;
import com.common.auth.auth.service.AuthService;
import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.util.RequestUtil;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "인증 관련 API")
@RequiredArgsConstructor
public class AuthController {
    private final JwtProvider jwtProvider;
    //----- Fields -----//
    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpirationInSeconds;

    //----- DI Fields -----//
    private final AuthService authService;

    //----- Methods -----//
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        log.info("Login attempt for email: {}", loginRequest.getUserId());

        InetAddress ipAddress = RequestUtil.getClientIpAddress(request);
        String userAgent = RequestUtil.getUserAgent(request);

        try {
            LoginResponse response = authService.login(loginRequest, ipAddress, userAgent);
            log.info("Login successful for email: {}", loginRequest.getUserId());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            // 아이디/비번 틀린 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(
                            "INVALID_CREDENTIALS",
                            "아이디 또는 비밀번호를 확인하세요."
                    ));
        }
    }


    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃하고 리프레시 토큰을 무효화합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Logged out successfully")));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPair tokenPair = authService.refreshTokens(request.getRefreshToken());
        
        TokenResponse response = new TokenResponse(
            tokenPair.getAccessToken(),
            tokenPair.getRefreshToken(),
            accessTokenExpirationInSeconds
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/revoke")
    @Operation(summary = "토큰 무효화", description = "리프레시 토큰을 무효화합니다.")
    public ResponseEntity<ApiResponse<Map<String, String>>> revokeToken(@Valid @RequestBody RefreshTokenRequest request) {
        authService.revokeRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Token revoked successfully")));
    }

    @PostMapping("/revoke-all")
    public ResponseEntity<ApiResponse<Map<String, String>>> revokeAllTokens(Authentication auth) {
        Integer memberId =
                (auth.getPrincipal() instanceof Integer)
                        ? (Integer) auth.getPrincipal()
                        : Integer.valueOf(auth.getName()); // 혹시 String으로 온 경우 대비

        authService.revokeAllUserTokens(memberId); // ← AuthService도 Integer 받도록 바꿔둬
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "All tokens revoked successfully")));
    }

    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        if (authorization == null || authorization.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Authorization header is missing",
                            "헤더에 Bearer 토큰이 없습니다."
                    ));
        }

        String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
        if (token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Bearer token is empty",
                            "Authorization: Bearer <token> 형식으로 전달해야 합니다."
                    ));
        }

        if (!jwtProvider.isValidAccessToken(token)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Invalid or expired token",
                            "서명 불일치/만료/issuer 불일치 등으로 유효성 검증에 실패했습니다."
                    ));
        }

        if (!jwtProvider.isAccessToken(token)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Token is not an access token",
                            "token_type != access"
                    ));
        }

        Claims claims = jwtProvider.getAccessTokenClaims(token);
        MemberDTO member = jwtProvider.getMemberFromClaims(claims);
        List<String> permissions = jwtProvider.getPermissions(token);
        List<?> menus = jwtProvider.getMenus(token);
        String roles = claims.get("roles", String.class);

        Map<String, Object> data = Map.of(
                "valid", true,
                "memberId", member.getMemberId(),
                "roles", roles,
                "permissions", permissions,
                "menus", menus
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}