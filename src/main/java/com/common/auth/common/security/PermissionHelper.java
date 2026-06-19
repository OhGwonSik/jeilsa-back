package com.common.auth.common.security;

import com.jeil.delivery.configuration.security.HierarchicalPermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * SpEL에서 사용할 수 있는 권한 검사 헬퍼
 * @PreAuthorize("@permissionHelper.hasMinLevel('manager', 'user', 'read')")
 */
@Component("permissionHelper")
@RequiredArgsConstructor
public class PermissionHelper {
    private final HierarchicalPermissionEvaluator permissionEvaluator;
    
    /**
     * 단일 권한 검사
     */
    public boolean has(String domain, String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return permissionEvaluator.hasPermission(auth, null, domain + ":" + action);
    }
    
    /**
     * 최소 레벨 권한 검사
     */
    public boolean hasMinLevel(String minLevel, String domain, String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return permissionEvaluator.hasPermission(auth, null, minLevel + ":" + domain + ":" + action);
    }
    
    /**
     * 여러 권한 AND 검사
     */
    public boolean hasAll(String domain, String... actions) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        for (String action : actions) {
            if (!permissionEvaluator.hasPermission(auth, null, domain + ":" + action)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 최소 레벨 여러 권한 AND 검사
     */
    public boolean hasAllMinLevel(String minLevel, String domain, String... actions) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        for (String action : actions) {
            if (!permissionEvaluator.hasPermission(auth, null, minLevel + ":" + domain + ":" + action)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 여러 권한 OR 검사
     */
    public boolean hasAny(String domain, String... actions) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        for (String action : actions) {
            if (permissionEvaluator.hasPermission(auth, null, domain + ":" + action)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 자기 자신인지 확인
     */
    public boolean isSelf(String userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userId.equals(auth.getPrincipal().toString());
    }
    
    /**
     * 다중 도메인 권한 검사 (AND 조합)
     * 예: hasMultiDomain('manager', 'user:read', 'role:read')
     */
    public boolean hasMultiDomain(String minLevel, String... domainActions) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        for (String domainAction : domainActions) {
            if (!permissionEvaluator.hasPermission(auth, null, minLevel + ":" + domainAction)) {
                return false;
            }
        }
        return true;
    }
}