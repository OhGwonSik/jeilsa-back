package com.common.auth.rolepermission.service;


import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.permission.domain.Permission;
import com.common.auth.permission.mapper.PermissionMapper;
import com.common.auth.role.domain.Role;
import com.common.auth.role.mapper.RoleMapper;
import com.common.auth.rolepermission.domain.RolePermission;
import com.common.auth.rolepermission.dto.*;
import com.common.auth.rolepermission.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionService {
    //----- DI Fields -----//
    private final RolePermissionMapper rolePermissionMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    //----- Methods -----//
    public List<RolePermissionResponse> findByRoleId(Integer roleId) {
        log.debug("Finding role permissions by role ID: {}", roleId);
        
        validateRoleExists(roleId);
        
        List<RolePermission> rolePermissions = rolePermissionMapper.selectRolePermissionsByRoleId(roleId);
        return rolePermissions.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public List<RolePermissionResponse> findByPermissionId(Integer permissionId) {
        log.debug("Finding role permissions by permission ID: {}", permissionId);
        
        validatePermissionExists(permissionId);
        
        List<RolePermission> rolePermissions = rolePermissionMapper.selectRolePermissionsByPermissionId(permissionId);
        return rolePermissions.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public RolePermissionResponse create(CreateRolePermissionRequest request) {
        log.info("Creating role permission: {}", request);
        
        validateRoleExists(request.getRoleId());
        validatePermissionExists(request.getPermissionId());
        
        if (rolePermissionMapper.selectExistsByRoleIdAndPermissionId(
                request.getRoleId(), request.getPermissionId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, 
                "Role permission already exists");
        }
        
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(request.getRoleId());
        rolePermission.setPermissionId(request.getPermissionId());
        rolePermission.setRegId(currentUserId);
        rolePermission.setChgId(currentUserId);
        rolePermission.setRegDt(LocalDateTime.now());
        rolePermission.setChgDt(LocalDateTime.now());
        
        rolePermissionMapper.insertRolePermission(rolePermission);
        
        log.info("Role permission created successfully: roleId={}, permissionId={}", 
            request.getRoleId(), request.getPermissionId());
        
        return convertToResponse(rolePermission);
    }

    public void createBatch(BatchRolePermissionRequest request) {
        log.info("Creating batch role permissions: {}", request);
        
        validateRoleExists(request.getRoleId());
        
        for (Integer permissionId : request.getPermissionIds()) {
            validatePermissionExists(permissionId);
        }
        
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        
        List<RolePermission> rolePermissions = request.getPermissionIds().stream()
            .filter(permissionId -> !rolePermissionMapper.selectExistsByRoleIdAndPermissionId(
                request.getRoleId(), permissionId))
            .map(permissionId -> {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(request.getRoleId());
                rolePermission.setPermissionId(permissionId);
                rolePermission.setRegId(currentUserId);
                rolePermission.setChgId(currentUserId);
                rolePermission.setRegDt(now);
                rolePermission.setChgDt(now);
                return rolePermission;
            })
            .collect(Collectors.toList());
        
        if (!rolePermissions.isEmpty()) {
            rolePermissionMapper.insertRolePermissionsBulk(rolePermissions);
            log.info("Batch role permissions created successfully: roleId={}, count={}", 
                request.getRoleId(), rolePermissions.size());
        }
    }

    public void deleteByRoleId(Integer roleId) {
        log.info("Deleting role permissions by role ID: {}", roleId);
        
        validateRoleExists(roleId);
        
        int deletedCount = rolePermissionMapper.deleteRolePermissionsByRoleId(roleId);
        
        log.info("Role permissions deleted successfully: roleId={}, count={}", 
            roleId, deletedCount);
    }

    public void deleteByPermissionId(Integer permissionId) {
        log.info("Deleting role permissions by permission ID: {}", permissionId);
        
        validatePermissionExists(permissionId);
        
        int deletedCount = rolePermissionMapper.deleteRolePermissionsByPermissionId(permissionId);
        
        log.info("Role permissions deleted successfully: permissionId={}, count={}", 
            permissionId, deletedCount);
    }

    public void delete(Integer roleId, Integer permissionId) {
        log.info("Deleting role permission: roleId={}, permissionId={}", roleId, permissionId);
        
        validateRoleExists(roleId);
        validatePermissionExists(permissionId);
        
        if (!rolePermissionMapper.selectExistsByRoleIdAndPermissionId(roleId, permissionId)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
                "Role permission not found");
        }
        
        int deletedCount = rolePermissionMapper.deleteRolePermissionByRoleIdAndPermissionId(roleId, permissionId);
        
        if (deletedCount == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
                "Failed to delete role permission");
        }
        
        log.info("Role permission deleted successfully: roleId={}, permissionId={}", 
            roleId, permissionId);
    }

    public boolean exists(Integer roleId, Integer permissionId) {
        return rolePermissionMapper.selectExistsByRoleIdAndPermissionId(roleId, permissionId);
    }

    public Optional<RolePermissionResponse> findOne(Integer roleId, Integer permissionId) {
        log.debug("Finding role permission: roleId={}, permissionId={}", roleId, permissionId);
        
        validateRoleExists(roleId);
        validatePermissionExists(permissionId);
        
        Optional<RolePermission> rolePermission = rolePermissionMapper.selectRolePermissionByRoleIdAndPermissionId(roleId, permissionId);
        return rolePermission.map(this::convertToResponse);
    }

    public List<RolePermissionResponse> findAll() {
        log.debug("Finding all role permissions");
        
        List<RolePermission> rolePermissions = rolePermissionMapper.selectAllRolePermissions();
        return rolePermissions.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    public RolePermissionResponse update(Integer roleId, Integer permissionId, UpdateRolePermissionRequest request) {
        log.info("Updating role permission: roleId={}, permissionId={}, request={}", roleId, permissionId, request);
        
        validateRoleExists(roleId);
        validatePermissionExists(permissionId);
        
        Optional<RolePermission> existingRolePermission = rolePermissionMapper.selectRolePermissionByRoleIdAndPermissionId(roleId, permissionId);
        if (existingRolePermission.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
                "Role permission not found");
        }
        
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        
        RolePermission rolePermission = existingRolePermission.get();
        if (request.getDelYn() != null) {
            rolePermission.setDelYn(request.getDelYn());
            // Only clear del fields when activating
            if (request.getDelYn().equals("N")) {
                rolePermission.setDelId(null);
                rolePermission.setDelDt(null);
            }
            // When deactivating or no change, don't touch del fields (preserve existing DB values)
        }
        rolePermission.setChgId(currentUserId);
        rolePermission.setChgDt(LocalDateTime.now());
        
        int updatedCount = rolePermissionMapper.updateRolePermission(rolePermission);
        
        if (updatedCount == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
                "Failed to update role permission");
        }
        
        log.info("Role permission updated successfully: roleId={}, permissionId={}", roleId, permissionId);
        
        return convertToResponse(rolePermission);
    }

    public void updateBatch(BatchUpdateRolePermissionRequest request) {
        log.info("Updating batch role permissions: {}", request);
        
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        
        List<RolePermission> rolePermissions = request.getUpdates().stream()
            .map(updateItem -> {
                validateRoleExists(updateItem.getRoleId());
                validatePermissionExists(updateItem.getPermissionId());
                
                Optional<RolePermission> existingRolePermission = rolePermissionMapper.selectRolePermissionByRoleIdAndPermissionId(
                    updateItem.getRoleId(), updateItem.getPermissionId());
                
                if (existingRolePermission.isEmpty()) {
                    throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
                        "Role permission not found: roleId=" + updateItem.getRoleId() + ", permissionId=" + updateItem.getPermissionId());
                }
                
                RolePermission rolePermission = existingRolePermission.get();
                if (updateItem.getDelYn() != null) {
                    rolePermission.setDelYn(updateItem.getDelYn());
                    // Only clear del fields when activating
                    if (updateItem.getDelYn().equals("N")) {
                        rolePermission.setDelId(null);
                        rolePermission.setDelDt(null);
                    }
                    // When deactivating or no change, don't touch del fields (preserve existing DB values)
                }
                rolePermission.setChgId(currentUserId);
                rolePermission.setChgDt(now);
                
                return rolePermission;
            })
            .collect(Collectors.toList());
        
        int updatedCount = rolePermissionMapper.updateRolePermissionsBulk(rolePermissions);
        
        log.info("Bulk role permissions updated successfully: count={}", updatedCount);
    }


    private void validateRoleExists(Integer roleId) {
        Optional<Role> role = roleMapper.selectRoleByRoleId(roleId);
        if (role.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
                "Role not found with ID: " + roleId);
        }
    }

    private void validatePermissionExists(Integer permissionId) {
        Optional<Permission> permission = permissionMapper.selectPermissionByPermissionId(permissionId);
        if (permission.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, 
                "Permission not found with ID: " + permissionId);
        }
    }

    private RolePermissionResponse convertToResponse(RolePermission rolePermission) {
        RolePermissionResponse response = new RolePermissionResponse(rolePermission);
        
        Optional<Role> role = roleMapper.selectRoleByRoleId(rolePermission.getRoleId());
        role.ifPresent(r -> response.setRoleName(r.getRoleName()));
        
        Optional<Permission> permission = permissionMapper.selectPermissionByPermissionId(rolePermission.getPermissionId());
        permission.ifPresent(p -> response.setPermissionName(p.getPermissionName()));
        
        return response;
    }
}