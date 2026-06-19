package com.common.auth.organization.mapper;

import java.util.List;
import java.util.Optional;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.common.auth.organization.domain.Organization;
import com.common.auth.organization.dto.OrganizationGridFilterDTO;
import com.common.auth.organization.dto.OrganizationResponse;
import com.common.auth.organization.dto.OrganizationUpsertItem;

@Mapper
public interface OrganizationMapper {
    Optional<Organization> selectOrganizationByOrganizationId(@Param("organizationId") Integer organizationId);

    Optional<Organization> selectOrganizationByOrganizationName(@Param("organizationName") String organizationName);

    List<Organization> selectAllOrganizations();

    void insertOrganization(Organization organization);

    void updateOrganization(Organization organization);

    void deleteOrganizationByOrganizationId(@Param("organizationId") Integer organizationId);
    
    // Grid 관련 메서드
    List<OrganizationResponse> selectOrganizationsWithFilter(@Param("filter") OrganizationGridFilterDTO filter);
    
    void insertOrganizationsBatch(@Param("organizations") List<Organization> organizations);
    
    void updateOrganizationsBatch(@Param("organizations") List<Organization> organizations);
    
    // Bulk operations - 중복 체크용 메서드들
    List<Integer> selectExistOrganizationIdListByOrganizationIds(@Param("organizationIds") List<Integer> organizationIds);
    
    List<String> selectExistOrganizationNameListByOrganizationNames(@Param("organizationNames") List<String> organizationNames);
    
    List<Organization> selectOrganizationListByOrganizationNamesAndNotInOrganizationIds(@Param("items") List<OrganizationUpsertItem> items);
    
    // Bulk operations - CRUD 메서드들
    int insertOrganizationsBulk(@Param("organizations") List<Organization> organizations);
    
    int updateOrganizationsBulk(@Param("organizations") List<Organization> organizations);
    
    int deleteOrganizationsBulk(@Param("organizations") List<Organization> organizations);
    
    int softDeleteOrganizationsBulk(@Param("organizations") List<Organization> organizations);
}