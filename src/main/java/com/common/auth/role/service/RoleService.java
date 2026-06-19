package com.common.auth.role.service;


import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.role.domain.Role;
import com.common.auth.role.dto.CreateRoleRequest;
import com.common.auth.role.dto.RoleResponse;
import com.common.auth.role.dto.UpdateRoleRequest;
import com.common.auth.role.mapper.RoleMapper;
import com.common.auth.user.domain.User;
import com.common.auth.userrole.domain.UserRole;
import com.common.auth.userrole.mapper.UserRoleMapper;
import com.jeil.delivery.domain.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {
    //----- Constant -----//
    private static final int SUCCESSFUL_UPDATE_COUNT = 0;

    @Value("${system.user-id:1}")
    private Integer systemId;

    //----- DI Fields -----//
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public Optional<Role> findById(Integer roleId) {
        log.debug("Finding role by ID: {}", roleId);
        return roleMapper.selectRoleByRoleId(roleId);
    }

    public Optional<Role> findByName(String roleName) {
        log.debug("Finding role by name: {}", roleName);
        return roleMapper.selectRoleByRoleName(roleName);
    }

    public List<Role> findAll() {
        log.debug("Finding all roles");
        return roleMapper.selectAllRoles();
    }

    public List<Role> findByMemberId(Integer memberId) {
        log.debug("Finding roles for user ID: {}", memberId);
        return roleMapper.selectRolesByMemberId(memberId);
    }

    public List<Role> findDefaultRoles() {
        log.debug("Finding default roles");
        return roleMapper.selectDefaultRoles();
    }

    public Role createRole(String roleName, String description) {
        log.info("Creating new role: {}", roleName);
        
        if (roleMapper.selectExistsByRoleName(roleName)) {
            throw new IllegalArgumentException("Role with name " + roleName + " already exists");
        }

        Role role = new Role(roleName, description);
        roleMapper.insertRole(role);
        
        log.info("Successfully created role: {}", roleName);
        return role;
    }

    public Role createRole(String name, String description, boolean isDefault) {
        log.info("Creating new role: {} (default: {})", name, isDefault);
        
        if (roleMapper.selectExistsByRoleName(name)) {
            throw new IllegalArgumentException("Role with name " + name + " already exists");
        }

        Role role = new Role(name, description);
        roleMapper.insertRole(role);
        
        log.info("Successfully created role: {} (default: {})", name, isDefault);
        return role;
    }

    public Role updateRole(Integer roleId, String roleName, String description) {
        log.info("Updating role with ID: {}", roleId);
        
        Optional<Role> existingRole = roleMapper.selectRoleByRoleId(roleId);
        if (existingRole.isEmpty()) {
            throw new IllegalArgumentException("Role not found with ID: " + roleId);
        }

        Role role = existingRole.get();
        
        if (!role.getRoleName().equals(roleName) && roleMapper.selectExistsByRoleName(roleName)) {
            throw new IllegalArgumentException("Role with name " + roleName + " already exists");
        }

        role.setRoleName(roleName);
        role.setDescription(description);
        role.setChgDt(LocalDateTime.now());

        int updatedRows = roleMapper.updateRole(role);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update role with ID: " + roleId);
        }

        log.info("Successfully updated role with ID: {}", roleId);
        return role;
    }

    public Role updateRole(Integer roleId, String roleName, String description, boolean isDefault) {
        log.info("Updating role with ID: {} (default: {})", roleId, isDefault);
        
        Optional<Role> existingRole = roleMapper.selectRoleByRoleId(roleId);
        if (existingRole.isEmpty()) {
            throw new IllegalArgumentException("Role not found with ID: " + roleId);
        }

        Role role = existingRole.get();
        
        if (!role.getRoleName().equals(roleName) && roleMapper.selectExistsByRoleName(roleName)) {
            throw new IllegalArgumentException("Role with name " + roleName + " already exists");
        }

        role.setRoleName(roleName);
        role.setDescription(description);
        role.setIsDefault(isDefault);
        role.setChgDt(LocalDateTime.now());

        int updatedRows = roleMapper.updateRole(role);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update role with ID: " + roleId);
        }

        log.info("Successfully updated role with ID: {} (default: {})", roleId, isDefault);
        return role;
    }

    public void deleteRole(Integer roleId) {
        log.info("Deleting role with ID: {}", roleId);
        
        // First remove all user-role associations
        userRoleMapper.deleteUserRolesByRoleId(roleId);
        
        int deletedRows = roleMapper.deleteRoleByRoleId(roleId);
        if (deletedRows == 0) {
            throw new IllegalArgumentException("Role not found with ID: " + roleId);
        }

        log.info("Successfully deleted role with ID: {}", roleId);
    }

    public void assignRoleToUser(Integer memberId, Integer roleId) {
        log.info("Assigning role {} to user {}", roleId, memberId);
        
        if (userRoleMapper.selectExistsByMemberIdAndRoleId(memberId, roleId)) {
            log.warn("User {} already has role {}", memberId, roleId);
            return;
        }

        UserRole userRole = new UserRole(memberId, roleId);
        userRoleMapper.insertUserRole(userRole);
        
        log.info("Successfully assigned role {} to user {}", roleId, memberId);
    }

    public void removeRoleFromUser(Integer memberId, Integer roleId) {
        log.info("Removing role {} from user {}", roleId, memberId);
        
        int deletedRows = userRoleMapper.deleteUserRoleByMemberIdAndRoleId(memberId, roleId);
        if (deletedRows == 0) {
            log.warn("User {} does not have role {}", memberId, roleId);
        } else {
            log.info("Successfully removed role {} from user {}", roleId, memberId);
        }
    }

    public void assignRolesToUser(Integer memberId, List<Integer> roleIds) {
        log.info("Assigning {} roles to user {}",roleIds.size(), memberId);
        
        // Remove existing roles
        userRoleMapper.deleteUserRolesByMemberId(memberId);
        
        // Add new roles
        if (!roleIds.isEmpty()) {
            // Get current authenticated user ID
            Integer currentUserId = SecurityUtil.getCurrentMemberId();
            Integer memberIdForInsert = currentUserId == null ? systemId : currentUserId;
            
            List<UserRole> userRoles = roleIds.stream()
                .map(roleId -> {
                    UserRole userRole = new UserRole(memberId, roleId);
                    userRole.setRegId(memberIdForInsert); // Set creator ID to current authenticated user
                    userRole.setChgId(memberIdForInsert); // Set modifier ID to current authenticated user
                    log.debug("Created UserRole: userId={}, roleId={}, regId={}",
                        userRole.getMemberId(), userRole.getRoleId(), userRole.getRegId());
                    return userRole;
                })
                .collect(Collectors.toList());
            
            int result = userRoleMapper.insertUserRolesBulk(userRoles);
        }
        
        log.info("Successfully assigned {} roles to user {}", roleIds.size(), memberId);
    }

    public int insertUsersDefaultRoles(List<MemberDTO> users) {
        log.info("insert {} roles to users {}", users.size(), users.stream().map(MemberDTO::getMemberId).collect(Collectors.toList()));
        LocalDateTime now = LocalDateTime.now();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        // Default Roles
        List<Role> selectDefaultRoleIds = roleMapper.selectDefaultRoles();
        List<UserRole> userRoles = new ArrayList<>();
        if(!selectDefaultRoleIds.isEmpty()){
            userRoles = users.stream()
            .flatMap(user -> selectDefaultRoleIds.stream()
                .map(role -> {
                    UserRole userRole = new UserRole(user.getMemberId(), role.getRoleId());
                    userRole.setRegId(currentUserId);
                    userRole.setRegDt(now);
                    userRole.setChgId(currentUserId);
                    userRole.setChgDt(now);
                    log.debug("Created UserRole: userId={}, roleId={}, regId={}", 
                        userRole.getMemberId(), userRole.getRoleId(), userRole.getRegId());
                    return userRole;
                })
            )
            .collect(Collectors.toList());
        }

        return userRoleMapper.insertUserRolesBulk(userRoles);
    }

    public boolean existsByName(String name) {
        return roleMapper.selectExistsByRoleName(name);
    }

    public int getRoleCount() {
        return roleMapper.selectCountRoles();
    }
    
    // ==================== DTO 기반 CRUD 메서드들 ====================
    
    /**
     * 역할 생성 (DTO 기반)
     */
    public RoleResponse createRole(CreateRoleRequest request) {
        log.info("Creating new role: {}", request.getRoleName());
        
        // 중복 체크
        if (roleMapper.selectExistsByRoleName(request.getRoleName())) {
            throw new BusinessException(ErrorCode.ROLE_ALREADY_EXISTS, "Role with name " + request.getRoleName() + " already exists");
        }
        
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        
        Role role = new Role(request.getRoleName(), request.getDescription());
        role.setIsDefault(request.getIsDefault());
        role.setRegId(currentUserId);
        role.setChgId(currentUserId);
        
        roleMapper.insertRole(role);
        
        log.info("Successfully created role: {}", request.getRoleName());
        return RoleResponse.from(role);
    }
    
    /**
     * 역할 수정 (DTO 기반)
     */
    public RoleResponse updateRole(Integer roleId, UpdateRoleRequest request) {
        log.info("Updating role with ID: {}", roleId);
        
        Optional<Role> existingRoleOpt = roleMapper.selectRoleByRoleId(roleId);
        if (existingRoleOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found with ID: " + roleId);
        }
        
        Role role = existingRoleOpt.get();
        
        // 역할명 변경 시 중복 체크
        if (!role.getRoleName().equals(request.getRoleName()) && roleMapper.selectExistsByRoleName(request.getRoleName())) {
            throw new BusinessException(ErrorCode.ROLE_ALREADY_EXISTS, "Role with name " + request.getRoleName() + " already exists");
        }
        
        // 값 업데이트
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        if (request.getIsDefault() != null) {
            role.setIsDefault(request.getIsDefault());
        }
        if (request.getDelYn() != null) {
            role.setDelYn(request.getDelYn());
            if (!request.getDelYn().isEmpty()) {
                role.setDelId(SecurityUtil.getCurrentMemberId());
                role.setDelDt(LocalDateTime.now());
            } else {
                role.setDelId(null);
                role.setDelDt(null);
            }
        }
        role.setChgId(SecurityUtil.getCurrentMemberId());
        role.setChgDt(LocalDateTime.now());
        
        int updatedRows = roleMapper.updateRole(role);
        if (updatedRows == SUCCESSFUL_UPDATE_COUNT) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to update role with ID: " + roleId);
        }
        
        log.info("Successfully updated role with ID: {}", roleId);
        return RoleResponse.from(role);
    }
    
    /**
     * 역할 조회 (DTO 기반)
     */
    public RoleResponse getRoleByRoleId(Integer roleId) {
        log.debug("Getting role by ID: {}", roleId);
        
        Optional<Role> roleOpt = roleMapper.selectRoleByRoleId(roleId);
        if (roleOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "Role not found with ID: " + roleId);
        }
        
        return RoleResponse.from(roleOpt.get());
    }
    
    /**
     * 전체 역할 목록 조회 (DTO 기반)
     */
    public List<RoleResponse> getAllRoles() {
        log.debug("Getting all roles");
        
        List<Role> roles = roleMapper.selectAllRoles();
        return roles.stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 역할 삭제 (DTO 기반)
     */
    public void deleteRoleById(Integer roleId) {
        log.info("Deleting role with ID: {}", roleId);
        
        // 존재 여부 확인
        Optional<Role> roleOpt = roleMapper.selectRoleByRoleId(roleId);
        if (roleOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "Role not found with ID: " + roleId);
        }
        
        int deletedRows = roleMapper.deleteRoleByRoleId(roleId);
        if (deletedRows == SUCCESSFUL_UPDATE_COUNT) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Role not found with ID: " + roleId);
        }
        
        log.info("Successfully deleted role with ID: {}", roleId);
    }
    
    /**
     * 역할명으로 조회 (DTO 기반)
     */
    public RoleResponse getRoleByRoleName(String roleName) {
        log.debug("Getting role by name: {}", roleName);
        
        Optional<Role> roleOpt = roleMapper.selectRoleByRoleName(roleName);
        if (roleOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND, "Role not found with name: " + roleName);
        }
        
        return RoleResponse.from(roleOpt.get());
    }
    
}