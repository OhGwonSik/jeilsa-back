package com.jeil.delivery.configuration.security;

import java.util.Arrays;
import java.util.List;

import com.jeil.delivery.domain.RefreshTokenVO;
import com.jeil.delivery.security.AuthorizationFilter;
import com.jeil.delivery.security.JwtProvider;
import com.jeil.delivery.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HierarchicalPermissionEvaluator hierarchicalPermissionEvaluator;

    @Value("${cors.allowed-origins:}")
    private String[] allowedOrigins; // 여러 개일 수 있음(권장)

    @Value("${cors.allowed-origin:}") // jeil 단일 문자열 호환
    private String singleAllowedOrigin;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:Authorization,Content-Type,Cache-Control,X-Requested-With}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(hierarchicalPermissionEvaluator);
        return expressionHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/code/**").permitAll()   // 임시
                        // 0) 프리플라이트
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 1) 공개(문서/프린트)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/waybill/print/**").permitAll()

                        // 2) 보호(Auth의 검증/로그아웃/일괄 revoke)
                        .requestMatchers("/auth/validate", "/auth/logout", "/auth/revoke-all").authenticated()

                        // 3) 공개(Auth 로그인/리프레시/리voke + 회원가입)
                        .requestMatchers("/auth/login", "/auth/refresh", "/auth/revoke").permitAll()
                        .requestMatchers("/user").permitAll()

                        // 4) 보호(그 외 사용doFilterInternal(HttpServletRequest자 API)
                        .requestMatchers("/user/**").authenticated()

                        //프린트 프로그램 내에서 리스트 조회
                        .requestMatchers("/waybill/print/**").permitAll()

                        // 헬스체크용
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/bill-print/page/company-invoice/**").permitAll()

                        // 5) 나머지
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new AuthorizationFilter(jwtProvider, tokenService, null),
                UsernamePasswordAuthenticationFilter.class);
            http.logout(l -> l.logoutUrl("/auth/logout").logoutSuccessHandler(jeilStyleLogoutSuccessHandler()));
        return http.build();
    }

    @Bean
     public LogoutSuccessHandler jeilStyleLogoutSuccessHandler() {
         return (request, response, authentication) -> {
             // 리프레시 토큰 제거
             if (request.getCookies() != null) {
                 for (Cookie cookie : request.getCookies()) {
                     if ("refreshToken".equals(cookie.getName())) {
                         //refreshToken이 유효한지 검증
                         String refreshToken = cookie.getValue();
                         if (jwtProvider.isValidRefreshToken(refreshToken)) {
                             //refreshToken에서 claims 꺼냄
                             Claims claims = jwtProvider.getRefreshTokenClaims(refreshToken);
                             int memberId = Integer.parseInt((String) claims.get("sub")); // subject에 멤버Id(PK)

                             RefreshTokenVO refreshTokenVO = RefreshTokenVO.builder()
                                     .memberId(memberId)
                                     .refreshToken(refreshToken)
                                     .build();

                             //DB에서 리프레시 토큰 삭제
                             tokenService.deleteRefreshToken(refreshTokenVO);
                         }
                     }
                 }
             }
             // 시큐리티 컨텍스트 클리어
             SecurityContextHolder.clearContext();

             // Refresh Token 쿠키 제거 (추후 엑세스토큰, 세션 쓸일 있으면 추가하면 됨)
             Cookie refreshTokenCookie = new Cookie("refreshToken", null);
             refreshTokenCookie.setHttpOnly(true);
             refreshTokenCookie.setSecure(true);
             refreshTokenCookie.setPath("/");
             refreshTokenCookie.setMaxAge(0);
             refreshTokenCookie.setAttribute("SameSite", "None");
             response.addCookie(refreshTokenCookie);

             // 4. 응답 반환 (프론트에서 accessToken 제거용)
             response.setContentType("application/json");
             response.setCharacterEncoding("UTF-8");
             response.getWriter().write("{\"message\":\"로그아웃 성공\", \"redirect\":\"/login\"}");
         };
     }

    @Configuration
    public class CorsConfig {

        @Value("${cors.allowed-origins}")
        private String allowedOrigins;

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration cfg = new CorsConfiguration();
            cfg.setAllowCredentials(true); // 꼭 필요
            cfg.setAllowedOrigins(List.of(allowedOrigins.split("\\s*,\\s*"))); // 여러 개면 콤마로 구분
            cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
            cfg.setAllowedHeaders(List.of("Origin","Content-Type","Accept","Authorization","X-Requested-With","X-CSRF-TOKEN"));
            cfg.setExposedHeaders(List.of("Set-Cookie","Authorization","Content-Disposition"));
            cfg.setMaxAge(3600L);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", cfg);
            return source;
        }
    }
}
