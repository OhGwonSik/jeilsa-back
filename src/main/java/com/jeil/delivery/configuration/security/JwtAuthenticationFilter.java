package com.jeil.delivery.configuration.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.common.auth.common.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jeil.delivery.security.JwtProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    //----- Constants -----//
    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final int TOKEN_LOG_MAX_LENGTH = 20;
    private static final String TOKEN_LOG_SUFFIX = "...";

    //----- DI Fields -----//
    private final JwtProvider jwtProvider;

    // @Value("${jwt-filter.token-invalid-chars:,\"' }")
    @Value("${jwt-filter.token-invalid-chars}")
    private String tokenInvalidChars;

    // 공개 엔드포인트 목록 (임시로 하드코딩, 추후 설정으로 이동 예정)
    private final List<String> publicEndpoints = Arrays.asList(
            "/auth/login",
            "/auth/refresh",
            "/auth/revoke",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator/health"

    );

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("JWT Filter processing request: {} {}", request.getMethod(), path);

        try {
            // 1) 추출 + 추적 로그
            String jwt = getJwtFromRequest(request, path);

            if (!StringUtils.hasText(jwt)) {
                log.debug("[JWT] no token → continue chain (unauthenticated), path={}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // 2) 형식 검증(파서 전) — 지금 네 에러 “Found: 1”을 여기서 걸러낸다
            int dots = dotCount(jwt);
            if (dots != 2) {
                log.warn("[JWT] invalid compact form: dots={} (must be 2), preview={}, path={}",
                        dots, preview(jwt), path);
                filterChain.doFilter(request, response);
                return;
            }

            // 3) 서명/클레임 검증
            log.debug("[JWT] validating token..., path={}", path);
            if (!jwtProvider.validateToken(jwt)) {
                log.warn("[JWT] validation failed (signature/issuer/exp). path={}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // 4) access 토큰 확인
            if (!jwtProvider.isAccessToken(jwt)) {
                log.warn("[JWT] not an access token (maybe refresh) → reject. path={}", path);
                filterChain.doFilter(request, response);
                return;
            }

            // 5) 주체/권한 세팅
            String userId = jwtProvider.getUserId(jwt);
            List<String> permissions = jwtProvider.getPermissions(jwt);
            log.debug("[JWT] validated. userId={}, permissions={}", userId, permissions);

            List<SimpleGrantedAuthority> authorities = (permissions == null) ? List.of()
                    : permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            log.debug("[JWT] authorities built: {}", authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("[JWT] SecurityContext set for user={}", userId);

        } catch (Exception ex) {
            log.error("[JWT] auth error. path={}, msg={}", path, ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }


    // 점검용

    private static int dotCount(String s) {
        return (s == null) ? -1 : (int) s.chars().filter(ch -> ch == '.').count();
    }
    private static String preview(String s) {
        if (s == null) return "null";
        int len = s.length();
        return s.substring(0, Math.min(10, len)) + "..." + s.substring(Math.max(0, len - 10));
    }

    private String getJwtFromRequest(HttpServletRequest request, String path) {
        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = header.substring(7).trim();
            if ((token.startsWith("\"") && token.endsWith("\"")) || (token.startsWith("'") && token.endsWith("'"))) {
                token = token.substring(1, token.length() - 1).trim();
            }
            log.debug("[JWT][extract] from=Authorization header, dots={}, preview={}",
                    dotCount(token), preview(token));
        } else {
            log.debug("[JWT][extract] Authorization header missing for {}", path);
        }

        // refresh 엔드포인트에서만 쿠키 fallback 허용
        if ((token == null || token.isBlank()) && path.startsWith("/auth/refresh")) {
            token = jwtProvider.extractRefreshTokenFromCookie(request);
            log.debug("[JWT][extract] from=cookie(refreshToken), dots={}, preview={}",
                    dotCount(token), preview(token));
        }

        return token;
    }

    private String cleanToken(String token) {
        // 성능 최적화: 한 번의 스캔으로 불필요한 문자 제거
        StringBuilder cleanToken = new StringBuilder(token.length());
        boolean foundInvalidChar = false;

        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (tokenInvalidChars.indexOf(c) >= 0 || Character.isWhitespace(c)) {
                foundInvalidChar = true;
                break;
            }
            cleanToken.append(c);
        }

        return foundInvalidChar ? cleanToken.toString() : token;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();                 // ★ "/auth/login"
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        return isPublicEndpoint(path);                          // ★ publicEndpoints만 스킵
    }

    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream().anyMatch(path::startsWith);
    }


}
