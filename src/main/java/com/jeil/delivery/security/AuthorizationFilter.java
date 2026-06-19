package com.jeil.delivery.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.service.TokenService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT AccessToken 인증 필터
 *
 * - 모든 요청에 대해 JWT 토큰을 검사함
 * - 유효한 AccessToken이 있을 경우 SecurityContext에 인증 정보 등록
 * - Spring Security의 OncePerRequestFilter를 확장하여 한 요청당 한 번만 실행됨
 */
@RequiredArgsConstructor
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

	 private final JwtProvider jwtProvider;
	 private final TokenService tokenService;
	 private final CustomUserDetailService customUserDetailService;

	 	/**
	    * 요청마다 실행되며, JWT 토큰을 검증하고 SecurityContext에 사용자 인증 정보를 설정함 STATELESS 형식이라 매 요청 마다 SecurityContextHolder에 넣어줘야하나봄
	    */
	    @Override
		// JwtAuthenticationFilter.java (핵심 부분만 예시)
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
				throws ServletException, IOException {

			String header = request.getHeader(HttpHeaders.AUTHORIZATION);
			if (header != null && header.startsWith("Bearer ")) {
				String token = header.substring(7);
				try {
					Claims claims = jwtProvider.getAccessTokenClaims(token); // 서명/만료 검증 포함

					// 1) 우선 token_type 검사(선택)
					if (!"access".equals(String.valueOf(claims.get("token_type")))) {
						throw new JwtException("Not an access token");
					}

					// 2) 권한 추출: authorities(표준 형태) 또는 permissions(문자열 리스트) 모두 허용
					List<GrantedAuthority> authorities = new ArrayList<>();

					Object auths = claims.get("authorities");
					if (auths instanceof List<?>) {
						for (Object o : (List<?>) auths) {
							if (o instanceof Map<?, ?> map && map.get("authority") != null) {
								authorities.add(new SimpleGrantedAuthority(String.valueOf(map.get("authority"))));
							} else if (o instanceof String s) {
								authorities.add(new SimpleGrantedAuthority(s));
							}
						}
					} else {
						// 👉 여기: permissions 지원
						List<String> perms = claims.get("permissions", List.class);
						if (perms != null) {
							authorities = perms.stream()
									.filter(Objects::nonNull)
									.map(String::valueOf)
									.map(SimpleGrantedAuthority::new)
									.collect(Collectors.toList());
						}
					}

		            // 3) JWT → MemberDTO 변환 (JwtProvider 유틸 활용)
		            MemberDTO member = jwtProvider.getMemberFromClaims(claims);
		            log.info("member123{}=>",member);
		            // 4) CustomUserDetails 생성
		            CustomUserDetails userDetails = new CustomUserDetails(member);
		            log.info("userDetails{}=>",userDetails);
		            // 5) Authentication 생성 및 SecurityContext 등록
		            UsernamePasswordAuthenticationToken auth =
		                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

		            SecurityContextHolder.getContext().setAuthentication(auth);

//					UsernamePasswordAuthenticationToken auth =
//							new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
//					SecurityContextHolder.getContext().setAuthentication(auth);
				} catch (JwtException | IllegalArgumentException e) {
					log.warn("Invalid JWT token for request: {} - {}", request.getRequestURI(), e.getMessage());
				}
			}

			chain.doFilter(request, response);
		}


	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getServletPath();  // ex) "/auth/login"  (컨텍스트패스 제거됨)
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

		// 로그인/토큰 재발급/토큰 폐기 등 공개 엔드포인트만 스킵
		return path.equals("/auth/login")
				|| path.equals("/auth/refresh")
				|| path.equals("/auth/revoke")
				|| path.equals("/auth/revoke-all")
				|| path.startsWith("/swagger-ui")
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/waybill/print")
				|| path.equals("/actuator/health")
				|| path.startsWith("/bill-print/page/company-invoice"); // 청구상세내역 전체공개
	}
}
