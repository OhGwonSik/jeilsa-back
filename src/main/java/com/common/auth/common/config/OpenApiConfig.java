package com.common.auth.common.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = false)
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;
    
    @Value("${swagger.server.url:}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Common Auth Backend API")
                        .description("공통 인증 백엔드 시스템 API 문서")
                        .version("1.0.0")
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(createServerList())
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private List<Server> createServerList() {
        List<Server> servers = new java.util.ArrayList<>();
        
        // 환경변수로 서버 URL이 설정된 경우 우선 사용
        if (!swaggerServerUrl.isEmpty()) {
            servers.add(new Server()
                    .url(swaggerServerUrl)
                    .description("Configured Server"));
        }
        
        // 기본 로컬 개발 서버 추가 (환경변수 미설정 시에만)
        if (servers.isEmpty()) {
            servers.add(new Server()
                    .url("http://localhost:" + serverPort + contextPath)
                    .description("Local Development Server"));
        }
        
        return servers;
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("JWT 토큰을 입력하세요 (Bearer 접두사 없이)");
    }
}