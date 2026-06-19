package com.common.auth.role.mapper;

import java.util.List;
import java.util.Optional;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.common.auth.role.domain.Role;
import com.common.auth.role.dto.RoleGridFilterDTO;
import com.common.auth.role.dto.RoleResponse;
import com.common.auth.role.dto.RoleUpsertItem;

@Mapper
public interface RoleMapper {
    Optional<Role> selectRoleByRoleId(@Param("roleId") Integer roleId);
    
    Optional<Role> selectRoleByRoleName(@Param("roleName") String roleName);
    
    List<Role> selectAllRoles();
    
    List<Role> selectRolesByMemberId(@Param("memberId") Integer memberId);
    
    List<Role> selectDefaultRoles();
    
    void insertRole(Role role);
    
    void insertRolesBatch(@Param("roles") List<Role> roles);
    
    int updateRole(Role role);
    
    int updateRolesBatch(@Param("roles") List<Role> roles);
    
    int deleteRoleByRoleId(@Param("roleId") Integer roleId);
    
    boolean selectExistsByRoleName(@Param("roleName") String roleName);
    
    int selectCountRoles();
    
    List<RoleResponse> selectRolesWithFilter(@Param("filter") RoleGridFilterDTO filter);
    
    // Bulk operations - 중복 체크용 메서드들
    List<Integer> selectExistRoleIdListByRoleIds(@Param("roleIds") List<Integer> roleIds);
    
    List<String> selectExistRoleNameListByRoleNames(@Param("roleNames") List<String> roleNames);
    
    List<Role> selectRoleListByRoleNamesAndNotInRoleIds(@Param("items") List<RoleUpsertItem> items);
    
    // Bulk operations - CRUD 메서드들
    int insertRolesBulk(@Param("roles") List<Role> roles);
    
    int updateRolesBulk(@Param("roles") List<Role> roles);
    
    int deleteRolesBulk(@Param("roles") List<Role> roles);
    
    int softDeleteRolesBulk(@Param("roles") List<Role> roles);

    String selectUserRoleDelYn(Integer memberId, Integer roleId);
}