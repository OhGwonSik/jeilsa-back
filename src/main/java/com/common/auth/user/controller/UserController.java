package com.common.auth.user.controller;

import java.util.Map;


import com.jeil.delivery.domain.MemberDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.role.service.RoleService;
import com.common.auth.user.domain.User;
import com.common.auth.user.dto.CreateUserRequest;
import com.common.auth.user.dto.UserResponse;
import com.common.auth.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "User Management", description = "사용자 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class UserController {
    //----- DI Fields -----//
    private final UserService userService;
    private final RoleService roleService;

    //----- Constants -----//
    // PermissionHelper 방식으로 변경됨

    // 관리자용 사용자 생성
    @PostMapping
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'user', 'read', 'create')")
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다. (권한: admin 이상 user:read+create)")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            MemberDTO memberDTO = userService.createMemberWithRoles(
                request.getUserName(),
                request.getEmail(),
                request.getPassword(),
                request.getRoleIds()
            );

            UserResponse response = new UserResponse(memberDTO);
            response.setRoles(roleService.findByMemberId(memberDTO.getMemberId()));
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "validation_error", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "server_error", "message", "Failed to create user"));
        }
    }

    @PostMapping("/{memberId}/enable")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'user', 'read', 'update')")
    @Operation(summary = "사용자 활성화", description = "특정 사용자를 활성화합니다. (권한: admin 이상 user:read+update)")
    public ResponseEntity<?> enableUser(
        @Parameter(description = "사용자 ID (Integer 형식)", required = true, example = "550e8400-e29b-41d4-a716-446655440000", in = ParameterIn.PATH)
        @PathVariable("memberId") Integer memberId) {
        try {
            userService.updateUserStatusToEnable(memberId);
            return ResponseEntity.ok(Map.of("message", "User enabled successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to enable user {}: {}", memberId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error enabling user with ID: {}", memberId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "server_error", "message", "Failed to enable user"));
        }
    }

    @PostMapping("/{memberId}/disable")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'user', 'read', 'update')")
    @Operation(summary = "사용자 비활성화", description = "특정 사용자를 비활성화합니다. (권한: admin 이상 user:read+update)")
    public ResponseEntity<?> disableUser(
        @Parameter(description = "사용자 ID (Integer 형식)", required = true, example = "550e8400-e29b-41d4-a716-446655440000", in = ParameterIn.PATH)
        @PathVariable("memberId") Integer memberId) {
        try {
            userService.updateUserStatusToDisable(memberId);
            return ResponseEntity.ok(Map.of("message", "User disabled successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to disable user {}: {}", memberId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error disabling user with ID: {}", memberId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "server_error", "message", "Failed to disable user"));
        }
    }

    @PostMapping("/{memberId}/unlock")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'user', 'read', 'update')")
    @Operation(summary = "사용자 계정 잠금 해제", description = "특정 사용자의 계정 잠금을 해제합니다. (권한: admin 이상 user:read+update)")
    public ResponseEntity<?> unlockUserAccount(
        @Parameter(description = "사용자 ID (Integer 형식)", required = true, example = "550e8400-e29b-41d4-a716-446655440000", in = ParameterIn.PATH)
        @PathVariable("memberId") Integer memberId) {
        try {
            userService.unlockAccount(memberId);
            return ResponseEntity.ok(Map.of("message", "Account unlocked successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to unlock account {}: {}", memberId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error unlocking account with ID: {}", memberId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "server_error", "message", "Failed to unlock account"));
        }
    }
}
