package com.common.auth.organizationrole.mapper;

import java.util.List;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.common.auth.organizationrole.domain.OrganizationRole;
import com.common.auth.organizationrole.dto.OrganizationRoleGridFilterDTO;
import com.common.auth.organizationrole.dto.OrganizationRoleResponse;

@Mapper
public interface OrganizationRoleMapper {
    
    List<OrganizationRole> findByOrganizationId(@Param("organizationId") Integer organizationId);
    
    List<OrganizationRole> findByRoleId(@Param("roleId") Integer roleId);
    
    List<OrganizationRoleResponse> selectOrganizationRolesWithFilter(
        @Param("filter") OrganizationRoleGridFilterDTO filter
    );

    List<OrganizationRole> findAllOrganizationRoles();

    void insert(OrganizationRole organizationRole);
    
    int insertOrganizationRolesBulk(@Param("organizationRoles") List<OrganizationRole> organizationRoles);
    
    int updateOrganizationRolesBulk(@Param("organizationRoles") List<OrganizationRole> organizationRoles);

    int deleteOrganizationRolesBulk(@Param("organizationRoles") List<OrganizationRole> organizationRoles);
    
    int deleteByOrganizationId(@Param("organizationId") Integer organizationId);
    
    int deleteByRoleId(@Param("roleId") Integer roleId);
    
    int deleteByOrganizationIdAndRoleId(@Param("organizationId") Integer organizationId, @Param("roleId") Integer roleId);
    
    void deleteOrganizationRoleByOrganizationIdAndRoleId(@Param("organizationId") Integer organizationId, @Param("roleId") Integer roleId);

    boolean existsByOrganizationIdAndRoleId(@Param("organizationId") Integer organizationId, @Param("roleId") Integer roleId);

    List<OrganizationRole> selectExistingOrganizationRolesByCompositeKeys(@Param("organizationRoles") List<OrganizationRole> organizationRoles);
    
    int softDeleteOrganizationRolesBulk(@Param("organizationRoles") List<OrganizationRole> organizationRoles);
}