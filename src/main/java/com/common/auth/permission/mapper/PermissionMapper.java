package com.common.auth.permission.mapper;

import java.util.List;
import java.util.Optional;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.common.auth.permission.domain.Permission;
import com.common.auth.permission.dto.PermissionGridFilterDTO;
import com.common.auth.permission.dto.PermissionResponse;
import com.common.auth.permission.dto.PermissionUpsertItem;

@Mapper
public interface PermissionMapper {
    Optional<Permission> selectPermissionByPermissionId(@Param("permissionId") Integer permissionId);
    
    Optional<Permission> selectPermissionByPermissionName(@Param("permissionName") String permissionName);
    
    List<Permission> selectAllPermissions();
    
    List<PermissionResponse> selectPermissionsWithFilter(@Param("filter") PermissionGridFilterDTO filter);

    List<Permission> selectPermissionsByRoleId(@Param("roleId") Integer roleId);
    
    List<Permission> selectPermissionsByMemberId(Integer memberId);
    
    void insertPermission(Permission permission);
    
    int updatePermission(Permission permission);
    
    int deletePermissionByPermissionId(@Param("permissionId") Integer permissionId);
    
    boolean selectExistsByPermissionName(@Param("permissionName") String permissionName);
    
    int selectCountPermissions();
    
    // Bulk operations - 중복 체크용 메서드들
    List<Integer> selectExistPermissionIdListByPermissionIds(@Param("permissionIds") List<Integer> permissionIds);
    
    List<String> selectExistPermissionNameListByPermissionNames(@Param("permissionNames") List<String> permissionNames);
    
    List<Permission> selectPermissionListByPermissionNamesAndNotInPermissionIds(@Param("items") List<PermissionUpsertItem> items);
    
    // Bulk operations - CRUD 메서드들
    int insertPermissionsBulk(@Param("permissions") List<Permission> permissions);
    
    int updatePermissionsBulk(@Param("permissions") List<Permission> permissions);
    
    int deletePermissionsBulk(@Param("permissions") List<Permission> permissions);
    
    int softDeletePermissionsBulk(@Param("permissions") List<Permission> permissions);
}