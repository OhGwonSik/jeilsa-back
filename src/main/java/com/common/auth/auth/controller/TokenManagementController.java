package com.common.auth.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.auth.domain.TokenStatistics;
import com.common.auth.auth.service.TokenCleanupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/tokens")
@Tag(name = "Token Management", description = "토큰 관리 API (관리자 전용)")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class TokenManagementController {
    //----- DI Fields -----//
    private final TokenCleanupService tokenCleanupService;

    //----- Methods -----//
    @GetMapping("/statistics")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'token', 'read')")
    @Operation(summary = "토큰 통계 조회", description = "전체 토큰 통계 정보를 조회합니다. (권한: admin 이상 token:read)")
    public ResponseEntity<?> getTokenStatistics() {
        try {
            TokenStatistics statistics = tokenCleanupService.getTokenStatistics();
            log.info("Token statistics requested: {}", statistics);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error retrieving token statistics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "server_error", "message", "Failed to retrieve token statistics"));
        }
    }

    @PostMapping("/cleanup")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'token', 'read', 'update')")
    @Operation(summary = "수동 토큰 정리", description = "만료된 토큰을 수동으로 정리합니다. (권한: admin 이상 token:read,update)")
    public ResponseEntity<?> manualCleanup() {
        try {
            int cleanedCount = tokenCleanupService.manualCleanup();
            log.info("Manual token cleanup completed. Cleaned {} tokens", cleanedCount);
            return ResponseEntity.ok(Map.of(
                    "message", "Token cleanup completed successfully",
                    "cleanedTokens", cleanedCount
            ));
        } catch (Exception e) {
            log.error("Error during manual token cleanup", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "server_error", "message", "Failed to cleanup tokens"));
        }
    }
}