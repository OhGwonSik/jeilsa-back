package com.common.auth.permission.service;


import com.common.auth.common.enums.PermissionHierarchyLevel;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.permission.domain.Permission;
import com.common.auth.permission.dto.CreatePermissionRequest;
import com.common.auth.permission.dto.PermissionResponse;
import com.common.auth.permission.dto.UpdatePermissionRequest;
import com.common.auth.permission.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {
    //----- DI Fields -----//
    private final PermissionMapper permissionMapper;

    public Optional<Permission> findById(Integer permissionId) {
        log.debug("Finding permission by ID: {}", permissionId);
        return permissionMapper.selectPermissionByPermissionId(permissionId);
    }

    public Optional<Permission> findByName(String permissionName) {
        log.debug("Finding permission by name: {}", permissionName);
        return permissionMapper.selectPermissionByPermissionName(permissionName);
    }

    public List<Permission> findAll() {
        log.debug("Finding all permissions");
        return permissionMapper.selectAllPermissions();
    }

    public List<Permission> findByRoleId(Integer roleId) {
        log.debug("Finding permissions for role ID: {}", roleId);
        return permissionMapper.selectPermissionsByRoleId(roleId);
    }

    public List<Permission> findByMemberId(Integer memberId) {
        log.debug("Finding permissions for user ID: {}", memberId);
        return permissionMapper.selectPermissionsByMemberId(memberId);
    }

    public PermissionResponse createPermission(CreatePermissionRequest request) {
        log.info("Creating new permission: {}", request.getPermissionName());
        
        if (permissionMapper.selectExistsByPermissionName(request.getPermissionName())) {
            throw new IllegalArgumentException("Permission with name " + request.getPermissionName() + " already exists");
        }

        Integer currentUserId = SecurityUtil.getCurrentMemberId();

        Permission permission = new Permission(request.getPermissionName(), request.getDescription());
        permission.setRegId(currentUserId);
        permission.setChgId(currentUserId);
        
        permissionMapper.insertPermission(permission);
        
        log.info("Successfully created permission: {}", request.getPermissionName());
        return PermissionResponse.from(permission);
    }

    public PermissionResponse updatePermission(Integer permissionId, UpdatePermissionRequest request) {
        log.info("Updating permission with ID: {}", permissionId);
        
        Optional<Permission> existingPermission = permissionMapper.selectPermissionByPermissionId(permissionId);
        if (existingPermission.isEmpty()) {
            throw new IllegalArgumentException("Permission not found with ID: " + permissionId);
        }

        Permission permission = existingPermission.get();
        
        // Check if name is being changed and if new name already exists
        if (!permission.getPermissionName().equals(request.getPermissionName()) && 
            permissionMapper.selectExistsByPermissionName(request.getPermissionName())) {
            throw new IllegalArgumentException("Permission with name " + request.getPermissionName() + " already exists");
        }

        permission.setPermissionName(request.getPermissionName());
        permission.setDescription(request.getDescription());
        permission.setChgId(SecurityUtil.getCurrentMemberId());
        permission.setChgDt(LocalDateTime.now());

        int updatedRows = permissionMapper.updatePermission(permission);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update permission with ID: " + permissionId);
        }

        log.info("Successfully updated permission with ID: {}", permissionId);
        return PermissionResponse.from(permission);
    }

    public void deletePermission(Integer permissionId) {
        log.info("Deleting permission with ID: {}", permissionId);
        
        int deletedRows = permissionMapper.deletePermissionByPermissionId(permissionId);
        if (deletedRows == 0) {
            throw new IllegalArgumentException("Permission not found with ID: " + permissionId);
        }

        log.info("Successfully deleted permission with ID: {}", permissionId);
    }

    public boolean existsPermissionByPermissionName(String permissionName) {
        return permissionMapper.selectExistsByPermissionName(permissionName);
    }

    public int getPermissionCount() {
        return permissionMapper.selectCountPermissions();
    }

    public List<String> getPermissionNamesByMemberId(Integer memberId) {
        log.debug("Getting permission names for user ID: {}", memberId);
        return permissionMapper.selectPermissionsByMemberId(memberId).stream()
                .map(Permission::getPermissionName)
                .toList();
    }
    
    /**
     * 사용자의 최상위 권한만 반환 (권한 계층 최적화)
     * 예: admin:user:read, manager:user:read가 있으면 admin:user:read만 반환
     */
    public List<String> getHighestLevelPermissionsByMemberId(Integer userId) {
        log.debug("Getting highest level permissions for user ID: {}", userId);
        
        List<String> allPermissions = permissionMapper.selectPermissionsByMemberId(userId).stream()
                .map(Permission::getPermissionName)
                .toList();
                
        return filterToHighestLevelPermissions(allPermissions);
    }
    
    /**
     * 권한 리스트에서 최상위 레벨 권한만 필터링
     */
    private List<String> filterToHighestLevelPermissions(List<String> permissions) {
        return permissions.stream()
                .collect(Collectors.groupingBy(this::getPermissionDomainAndAction))
                .values()
                .stream()
                .map(this::getHighestLevelPermission)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * 권한에서 도메인과 액션 부분 추출 (레벨 제외)
     * 예: "admin:user:read" -> "user:read"
     */
    private String getPermissionDomainAndAction(String permission) {
        String[] parts = permission.split(":");
        if (parts.length >= 2) {
            return String.join(":", Arrays.copyOfRange(parts, 1, parts.length));
        }
        return permission;
    }
    
    /**
     * 같은 도메인-액션의 권한들 중에서 가장 높은 레벨의 권한 반환
     */
    private Optional<String> getHighestLevelPermission(List<String> sameTypePermissions) {
        return sameTypePermissions.stream()
                .max((p1, p2) -> {
                    PermissionHierarchyLevel level1 = getPermissionLevel(p1);
                    PermissionHierarchyLevel level2 = getPermissionLevel(p2);
                    
                    if (level1 == null && level2 == null) return 0;
                    if (level1 == null) return -1;
                    if (level2 == null) return 1;
                    
                    return Integer.compare(level1.getLevel(), level2.getLevel());
                });
    }
    
    /**
     * 권한 문자열에서 레벨 추출
     */
    private PermissionHierarchyLevel getPermissionLevel(String permission) {
        String[] parts = permission.split(":");
        if (parts.length == 0) {
            return null;
        }
        
        try {
            return PermissionHierarchyLevel.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            log.debug("Unknown permission level: {}", parts[0]);
            return null;
        }
    }
    
    public PermissionResponse getPermissionByPermissionId(Integer permissionId) {
        log.debug("Getting permission by ID: {}", permissionId);
        return findById(permissionId)
                .map(PermissionResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + permissionId));
    }
    
    public PermissionResponse getPermissionByPermissionName(String permissionName) {
        log.debug("Getting permission by name: {}", permissionName);
        return findByName(permissionName)
                .map(PermissionResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with name: " + permissionName));
    }
    
    public List<PermissionResponse> getAllPermissions() {
        log.debug("Getting all permissions");
        return findAll().stream()
                .map(PermissionResponse::from)
                .toList();
    }
}