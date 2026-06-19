package com.jeil.delivery.security;

import com.jeil.delivery.domain.MemberDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final CustomUserDetailService customUserDetailService;
    private SecretKey accessSecretKey;
    private SecretKey refreshSecretKey;

    @Value("${jwt.accessToken}")
    private String accessToken;

    @Value("${jwt.accessToken-expire-time}")
    private int accessTokenExpireSeconds;

    @Value("${jwt.refreshToken}")
    private String refreshToken;

    @Value("${jwt.refreshToken-expire-time}")
    private int refreshTokenExpireSeconds;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    /**
     * Access/Refresh Token용 키 초기화
     */
    @PostConstruct
    public void init() {
        this.accessSecretKey = Keys.hmacShaKeyFor(accessToken.getBytes());
        this.refreshSecretKey = Keys.hmacShaKeyFor(refreshToken.getBytes());
    }

    /**
     * Access Token 생성 (Authentication 기반)
     */
    public String generateAccessToken(Authentication auth, List<String> permissions, List<?> menus) {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        MemberDTO member = userDetails.getMemberDTO();

        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, -5); // 5초 과거로 보정 (토큰 미래 에러)
        Date issuedAt = now.getTime();
        now.add(Calendar.SECOND, accessTokenExpireSeconds + 5);
        Date expiration = now.getTime();

        return Jwts.builder()
                .issuer(issuer)
                .claim("audience", audience)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .subject(String.valueOf(member.getMemberId()))
                .claim("userId", member.getUserId())
                .claim("name", member.getName())
                .claim("email", member.getEmail())
                .claim("tel", member.getTelNo())
                .claim("status", member.getDelYn())
                .claim("authorities", auth.getAuthorities()) // Spring Security 권한 목록
                .claim("permissions", permissions)           // ★ 권한 모듈 호환
                .claim("menus", menus)                       // ★ 메뉴 모듈 호환
                .claim("token_type", "access")               // ★ access 표시
                .signWith(accessSecretKey)
                .compact();
    }

    // ⬇️ JwtProvider에 추가
    public String generateAccessToken(MemberDTO memberDTO, List<String> permissions, List<?> menus) {
        // null 방어
        List<String> safePermissions = (permissions != null) ? permissions : List.of();
        List<?> safeMenus = (menus != null) ? menus : List.of();

        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, -5); // 5초 과거로 보정 (토큰 미래 에러)
        Date issuedAt = now.getTime();
        now.add(Calendar.SECOND, accessTokenExpireSeconds + 5);
        Date expiration = now.getTime();

        return Jwts.builder()
                .issuer(issuer)
                .claim("audience", audience)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .subject(String.valueOf(memberDTO.getMemberId()))                 // ★ 토큰 subject에 memberId 그대로 사용
                .claim("roles", memberDTO.getRoles())
                .claim("userCompanyId", memberDTO.getUserCompanyId())
                .claim("permissions", safePermissions)
                .claim("menus", safeMenus)
                .claim("token_type", "access")   // ★ isAccessToken()에서 쓰는 표식
                .signWith(accessSecretKey)
                .compact();
    }

    /**
     * Refresh Token 생성 (Authentication)
     */
    public String generateRefreshToken(Authentication auth) {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        MemberDTO member = userDetails.getMemberDTO();

        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND, -5); // 5초 과거로 보정 (토큰 미래 에러)
        Date issuedAt = now.getTime();
        now.add(Calendar.SECOND, refreshTokenExpireSeconds + 5);
        Date expiration = now.getTime();

        return Jwts.builder()
                .issuer(issuer)
                .claim("audience", audience)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .subject(String.valueOf(member.getMemberId()))
                .claim("token_type", "refresh") // ★ refresh 표시
                .signWith(refreshSecretKey)
                .compact();
    }

    /**
     * Access Token 유효성 검사
     */
    public boolean isValidAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(accessSecretKey)
                    .requireIssuer(this.issuer)
                    .clockSkewSeconds(60) // ← 시계 오차 허용(아래 3번 항목)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException se) {
            // 서명 검증 실패(비밀키 불일치)
            log.warn("[JWT] signature invalid: {}", se.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException ee) {
            log.warn("[JWT] token expired at: {}", ee.getClaims().getExpiration());
        } catch (io.jsonwebtoken.IncorrectClaimException ice) {
            // requireIssuer 불일치 등
            log.warn("[JWT] claim mismatch: {}", ice.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] parse failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Refresh Token 유효성 검사
     */
    public boolean isValidRefreshToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(refreshSecretKey)
                    .requireIssuer(this.issuer)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 공통 토큰 검증 (점검용)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(accessSecretKey)
                    .requireIssuer(this.issuer)
                    .clockSkewSeconds(60)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException se) {
            log.warn("[JWT] signature invalid: {}", se.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException ee) {
            log.warn("[JWT] token expired at: {}", ee.getClaims().getExpiration());
        } catch (io.jsonwebtoken.IncorrectClaimException ice) {
            log.warn("[JWT] claim mismatch: {}", ice.getMessage()); // issuer 등
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] parse failed: {}", e.getMessage());
        }
        return false;
    }


    /**
     * Access 토큰인지 여부
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = getAccessTokenClaims(token);
            return "access".equals(claims.get("token_type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh 토큰인지 여부
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getRefreshTokenClaims(token);
            return "refresh".equals(claims.get("token_type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 사용자 ID 추출
     */
    public String getUserId(String token) {
        try {
            return getAccessTokenClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 권한 목록 추출
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissions(String token) {
        try {
            return getAccessTokenClaims(token).get("permissions", List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * 메뉴 목록 추출
     */
    @SuppressWarnings("unchecked")
    public List<?> getMenus(String token) {
        try {
            return getAccessTokenClaims(token).get("menus", List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Access Token에서 Claims 추출
     */
    public Claims getAccessTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(accessSecretKey)
                .requireIssuer(this.issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Refresh Token에서 Claims 추출
     */
    public Claims getRefreshTokenClaims(String token) {
        return Jwts.parser()
                .verifyWith(refreshSecretKey)
                .requireIssuer(this.issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Refresh Token 재발급용 사용자 ID 추출
     */
    public Long getUserIdFromRefreshToken(String token) {
        Claims claims = getRefreshTokenClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 쿠키에서 refreshToken 추출
     */
    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public boolean isRefreshTokenExpired(Date expireDt) {
        return expireDt.before(new Date());
    }

    public String generateAccessToken(Authentication authentication) {
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        // permissions/menus는 기존 코드가 안 주므로 빈 리스트로 위임
        return generateAccessToken(authentication, roles, List.of());
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        return generateAccessToken(auth);
    }

    public long getRefreshTokenExpireSeconds() {
        // 필드 타입이 int라면 long으로 승격만 해주면 됨
        return (long) this.refreshTokenExpireSeconds;
    }

    public MemberDTO getMemberFromClaims(Claims claims) {
        MemberDTO m = new MemberDTO();

        // subject에는 memberId를 넣어두었음
        try {
            String sub = claims.getSubject();
            if (sub != null && !sub.isBlank()) {
                m.setMemberId(Integer.valueOf(sub));
            }
        } catch (Exception ignore) {}

        try {
            Object userId = claims.get("userId");
            if (userId != null) m.setUserId(String.valueOf(userId));
        } catch (Exception ignore) {}

        try {
            String name = claims.get("name", String.class);
            if (name != null) m.setName(name);
        } catch (Exception ignore) {}

        try {
            String email = claims.get("email", String.class);
            if (email != null) m.setEmail(email);
        } catch (Exception ignore) {}

        try {
            String tel = claims.get("tel", String.class);
            if (tel != null) m.setTelNo(tel);
        } catch (Exception ignore) {}

        try {
            // generateAccessToken에서 status에 delYn을 넣었음
            Object status = claims.get("status");
            if (status != null) m.setDelYn(String.valueOf(status));
        } catch (Exception ignore) {}


        try {
            Object companyId = claims.get("userCompanyId");
            if (companyId != null) {
                m.setUserCompanyId(Integer.valueOf(String.valueOf(companyId)));
            }
        } catch (Exception ignore) {}

        try {
            String role = claims.get("roles", String.class);
            if (role != null) {
                m.setRoles(role);
            }
        } catch (Exception ignore) {}

        return m;
    }
}
