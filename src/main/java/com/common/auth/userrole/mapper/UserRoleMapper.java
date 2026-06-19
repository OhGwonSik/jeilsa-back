package com.common.auth.userrole.mapper;

import java.util.List;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.common.auth.userrole.domain.UserRole;
import com.common.auth.userrole.dto.UserRoleGridFilterDTO;
import com.common.auth.userrole.dto.UserRoleResponse;

@Mapper
public interface UserRoleMapper {
    
    List<UserRole> selectUserRolesByMemberId(@Param("memberId") Integer memberId);
    
    List<UserRole> selectUserRolesByRoleId(@Param("roleId") Integer roleId);
    
    List<UserRole> selectAllUserRoles();

    List<UserRoleResponse> selectUserRolesWithFilter(
            @Param("filter") UserRoleGridFilterDTO filter
    );
    
    void insertUserRole(UserRole userRole);
    
    int deleteUserRolesByMemberId(Integer memberId);
    
    int deleteUserRolesByRoleId(@Param("roleId") Integer roleId);

    /**
     * 사용자-역할 매핑을 일괄 삭제합니다.
     *
     * @param userRoles 삭제할 사용자-역할 매핑 목록
     * @return 삭제된 레코드 수
     */
    int deleteUserRolesBulk(@Param("userRoles") List<UserRole> userRoles);
    
    int deleteUserRoleByMemberIdAndRoleId(@Param("memberId") Integer memberId, @Param("roleId") Integer roleId);
    
    boolean selectExistsByMemberIdAndRoleId(@Param("memberId") Integer memberId, @Param("roleId") Integer roleId);
    
    int insertUserRolesBulk(@Param("userRoles") List<UserRole> userRoles);

    int updateUserRolesBulk(@Param("userRoles") List<UserRole> userRoles);

    int softDeleteUserRolesBulk(@Param("userRoles") List<UserRole> userRoles);
    
    List<UserRole> selectExistingUserRolesByCompositeKeys(@Param("userRoles") List<UserRole> userRoles);
}