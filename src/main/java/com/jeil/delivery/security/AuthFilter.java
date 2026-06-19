package com.jeil.delivery.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeil.delivery.configuration.error.InsertCheckedException;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.domain.RefreshTokenVO;
import com.jeil.delivery.service.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 요청을 처리하고, AccessToken을 발급하는 필터
 *
 * - /auth/login 경로에서 POST 요청을 처리함
 * - 인증 성공 시 JWT 발급 후 JSON으로 반환
 */
@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends UsernamePasswordAuthenticationFilter {

	private final JwtProvider jwtProvider;
	private final TokenService tokenService;
	private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider, TokenService tokenService) {
        this.jwtProvider = jwtProvider;
        this.tokenService = tokenService;
        setAuthenticationManager(authenticationManager);
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/auth/login", "POST"));
    }

	/**
	 * 로그인 요청 시 username/password를 추출하고 인증 시도
	 */
	@Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        try {
            MemberDTO credentials = objectMapper.readValue(request.getInputStream(), MemberDTO.class);
            log.info("로그인 시도 - ID: {}, PW: {}", credentials.getUserId(), credentials.getUserPw());

            if (credentials == null || credentials.getUserId() == null || credentials.getUserPw() == null) {
                throw new RuntimeException("아이디 또는 비밀번호가 누락되었습니다.");
            }

            return this.getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getUserId(), credentials.getUserPw())
            );

        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 중 오류가 발생했습니다.", e);
        }
    }

	/**
	 * 인증 성공 시 AccessToken 생성 후 JSON 응답
	 */
	@Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {

		 String accessToken = jwtProvider.generateAccessToken(authResult);

		    CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
		    MemberDTO memberDTO = userDetails.getMemberDTO();

		    // 1. 기존 리프레시 토큰 조회
		    RefreshTokenVO existingToken = tokenService.getRefreshTokenByMemberId(memberDTO);
		    String refreshToken;
		    Date refreshExpireDt;

		    // 2. 기존 토큰이 있고 아직 유효하면 → 재사용
		    if (existingToken != null && !jwtProvider.isRefreshTokenExpired(existingToken.getExpireDt())){
		    	log.info("existingToken 존재=>{}",existingToken);
		        refreshToken = existingToken.getRefreshToken();
		        refreshExpireDt = existingToken.getExpireDt();
		    }else{
		        // 3. 없거나 만료되었으면 → 새로 발급 후 저장
		    	log.info("existingToken 미존재=>{}",existingToken);
		        refreshToken = jwtProvider.generateRefreshToken(authResult);
		        refreshExpireDt = new Date(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpireSeconds() * 1000L);

		        RefreshTokenVO refreshTokenVO = RefreshTokenVO.builder()
		                .memberId(memberDTO.getMemberId())
		                .refreshToken(refreshToken)
		                .expireDt(refreshExpireDt)
		                .build();

		        int count = tokenService.insertRefreshToken(refreshTokenVO);
		        if (count == 0) {
		            throw new InsertCheckedException("리프레시 토큰 저장에 실패했습니다.");
		        }
		    }

        //Refresh 토큰을 http only(js조작불가) 쿠키로 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // HTTPS 환경일 경우 true -> 개발중에는 false
                .path("/")    // 전체 경로 적용
                .maxAge(Duration.ofSeconds(jwtProvider.getRefreshTokenExpireSeconds()))
                .sameSite("Lax") // Strict, Lax, None
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        Map<String, String> tokenResponse = new HashMap<>();
		tokenResponse.put("accessToken", accessToken);
		tokenResponse.put("refreshToken", refreshToken);
		tokenResponse.put("memberId", String.valueOf(memberDTO.getMemberId()));
		tokenResponse.put("userId", memberDTO.getUserId());
		tokenResponse.put("name", memberDTO.getName());
		tokenResponse.put("email", memberDTO.getEmail());
		tokenResponse.put("telNo", memberDTO.getTelNo());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), tokenResponse);
    }

	/**
	 * 인증 실패 시 401 Unauthorized 반환
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request,
	                                          HttpServletResponse response,
	                                          AuthenticationException failed) throws IOException {

	    response.setContentType("application/json;charset=UTF-8");
	    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
	    response.setHeader("Access-Control-Allow-Credentials", "true");

	    String message;
	    if (failed instanceof BadCredentialsException) {
	        message = "아이디 또는 비밀번호가 일치하지 않습니다.";
	    } else if (failed instanceof UsernameNotFoundException) {
	        message = "존재하지 않는 아이디입니다.";
	    } else if (failed instanceof DisabledException) {
	        message = "삭제된 계정입니다. 관리자에게 문의하세요.";
	    }else {
	        message = "로그인 처리 중 오류가 발생했습니다.";
	    }

	    Map<String, Object> res = new HashMap<>();
	    res.put("success", false);
	    res.put("message", message);

	    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    objectMapper.writeValue(response.getWriter(), res);
	}
}