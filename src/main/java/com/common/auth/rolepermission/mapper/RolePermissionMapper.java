package com.common.auth.rolepermission.mapper;

import com.common.auth.common.dto.PageRequestDTO;
import com.common.auth.rolepermission.domain.RolePermission;
import com.common.auth.rolepermission.dto.RolePermissionGridFilterDTO;
import com.common.auth.rolepermission.dto.RolePermissionResponse;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RolePermissionMapper {
    
    List<RolePermission> selectRolePermissionsByRoleId(@Param("roleId") Integer roleId);
    
    List<RolePermission> selectRolePermissionsByPermissionId(@Param("permissionId") Integer permissionId);
    
    Optional<RolePermission> selectRolePermissionByRoleIdAndPermissionId(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);
    
    List<RolePermission> selectAllRolePermissions();
    
    List<RolePermission> selectAllRolePermissionsWithPaging(@Param("pageRequest") PageRequestDTO pageRequest);
    
    List<RolePermissionResponse> selectRolePermissionsWithFilter(
        @Param("filter") RolePermissionGridFilterDTO filter
    );
    
    void insertRolePermission(RolePermission rolePermission);
    
    void insertRolePermissionsBulk(@Param("rolePermissions") List<RolePermission> rolePermissions);
    
    int updateRolePermission(RolePermission rolePermission);
    
    int updateRolePermissionsBulk(@Param("rolePermissions") List<RolePermission> rolePermissions);
    
    int deleteRolePermissionsByRoleId(@Param("roleId") Integer roleId);
    
    int deleteRolePermissionsByPermissionId(@Param("permissionId") Integer permissionId);
    
    int deleteRolePermissionByRoleIdAndPermissionId(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);
    
    boolean selectExistsByRoleIdAndPermissionId(@Param("roleId") Integer roleId, @Param("permissionId") Integer permissionId);
    
    List<RolePermission> selectExistingRolePermissionsByCompositeKeys(@Param("rolePermissions") List<RolePermission> rolePermissions);
    
    int softDeleteRolePermissionsBulk(@Param("rolePermissions") List<RolePermission> rolePermissions);
}