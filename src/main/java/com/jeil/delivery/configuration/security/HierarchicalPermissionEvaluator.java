package com.jeil.delivery.configuration.security;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.common.auth.common.enums.PermissionHierarchyLevel;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * 권한 계층을 고려한 권한 검사기
 * 상위 권한을 가지고 있으면 하위 권한도 자동으로 허용
 */
@Slf4j
@Component
public class HierarchicalPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        
        String permissionStr = permission.toString();
        return hasHierarchicalPermission(authentication, permissionStr);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        
        String permissionStr = permission.toString();
        return hasHierarchicalPermission(authentication, permissionStr);
    }
    
    /**
     * 권한 계층을 고려한 권한 검사
     * 예: admin:user:read 권한 요청시 -> admin 레벨 권한이 있으면 허용
     * 최소 레벨 지정시: manager:user:read -> admin 이상 레벨만 허용
     */
    private boolean hasHierarchicalPermission(Authentication authentication, String requiredPermission) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> userPermissions = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
                
        log.debug("Checking permission: {} against user permissions: {}", requiredPermission, userPermissions);
        
        // 최소 레벨이 지정된 경우 (형태: "minLevel:domain:action")
        if (isMinLevelPermission(requiredPermission)) {
            return hasMinimumLevelPermission(userPermissions, requiredPermission);
        }
        
        // 직접 권한 확인
        if (userPermissions.contains(requiredPermission)) {
            log.debug("Direct permission match found for: {}", requiredPermission);
            return true;
        }
        
        // 계층 권한 확인
        return hasHigherLevelPermission(userPermissions, requiredPermission);
    }
    
    /**
     * 최소 레벨 권한 형태인지 확인
     * 예: "manager:user:read" 형태
     */
    private boolean isMinLevelPermission(String permission) {
        String[] parts = permission.split(":");
        if (parts.length != 3) {
            return false;
        }
        
        // 첫 번째 부분이 권한 레벨인지 확인
        try {
            PermissionHierarchyLevel.valueOf(parts[0].toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 최소 레벨 권한 검사
     * 예: "manager:user:read" 요청시 manager 이상 레벨의 user:read 권한이 있는지 확인
     */
    private boolean hasMinimumLevelPermission(Set<String> userPermissions, String requiredPermission) {
        String[] parts = requiredPermission.split(":");
        if (parts.length != 3) {
            return false;
        }
        
        PermissionHierarchyLevel minLevel;
        try {
            minLevel = PermissionHierarchyLevel.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        
        String domain = parts[1];
        String action = parts[2];
        
        log.debug("Checking minimum level {} for {}:{}", minLevel, domain, action);
        
        // 사용자의 권한 중에서 해당 도메인:액션에 대해 최소 레벨 이상의 권한이 있는지 확인
        for (String userPermission : userPermissions) {
            String[] userParts = userPermission.split(":");
            if (userParts.length >= 3) {
                String userLevel = userParts[0];
                String userDomain = userParts[1];
                String userAction = userParts[2];
                
                // 도메인과 액션이 일치하는지 확인
                if (domain.equals(userDomain) && action.equals(userAction)) {
                    PermissionHierarchyLevel userLevelEnum = getUserLevel(userLevel);
                    if (userLevelEnum != null && userLevelEnum.isAtLeast(minLevel)) {
                        log.debug("User has sufficient level {} for required minimum {}", userLevelEnum, minLevel);
                        return true;
                    }
                }
            }
        }
        
        log.debug("User does not have minimum level {} permission for {}:{}", minLevel, domain, action);
        return false;
    }
    
    /**
     * 상위 레벨 권한이 있는지 확인
     * 예: admin:user:read 요청시 admin 권한이 있는지 확인
     */
    private boolean hasHigherLevelPermission(Set<String> userPermissions, String requiredPermission) {
        String[] parts = requiredPermission.split(":");
        if (parts.length < 2) {
            return false;
        }
        
        String domain = parts[1]; // user, role, permission 등
        String action = parts.length > 2 ? parts[2] : null; // read, create, update, delete
        
        // 현재 요청 권한의 레벨 확인
        PermissionHierarchyLevel requiredLevel = getPermissionLevel(requiredPermission);
        if (requiredLevel == null) {
            return false;
        }
        
        // 사용자의 권한 중에서 같은 도메인의 더 높은 레벨 권한이 있는지 확인
        for (String userPermission : userPermissions) {
            if (isHigherLevelPermissionForDomain(userPermission, domain, action, requiredLevel)) {
                log.debug("Higher level permission found: {} for required: {}", userPermission, requiredPermission);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 특정 도메인에 대한 상위 레벨 권한인지 확인
     */
    private boolean isHigherLevelPermissionForDomain(String userPermission, String requiredDomain, String requiredAction, PermissionHierarchyLevel requiredLevel) {
        String[] parts = userPermission.split(":");
        if (parts.length < 2) {
            return false;
        }
        
        String userLevel = parts[0];
        String userDomain = parts[1];
        String userAction = parts.length > 2 ? parts[2] : null;
        
        // 도메인이 다르면 false
        if (!userDomain.equals(requiredDomain)) {
            return false;
        }
        
        // 액션이 지정된 경우 액션도 같아야 함 (또는 상위 액션)
        if (requiredAction != null && userAction != null && !isActionCompatible(userAction, requiredAction)) {
            return false;
        }
        
        // 권한 레벨 확인
        PermissionHierarchyLevel userLevelEnum = getUserLevel(userLevel);
        return userLevelEnum != null && userLevelEnum.isHigherThan(requiredLevel);
    }
    
    /**
     * 권한 문자열에서 레벨 추출
     */
    private PermissionHierarchyLevel getPermissionLevel(String permission) {
        String[] parts = permission.split(":");
        if (parts.length == 0) {
            return null;
        }
        
        return getUserLevel(parts[0]);
    }
    
    /**
     * 레벨 문자열을 enum으로 변환
     */
    private PermissionHierarchyLevel getUserLevel(String levelStr) {
        try {
            return PermissionHierarchyLevel.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * 액션 호환성 확인
     * 상위 액션(예: admin)이 하위 액션(예: read)을 포함하는지 확인
     */
    private boolean isActionCompatible(String userAction, String requiredAction) {
        // 현재는 단순 매칭, 필요시 액션 계층도 구현 가능
        return userAction.equals(requiredAction);
    }
}