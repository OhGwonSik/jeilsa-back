package com.common.auth.userorganization.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.common.auth.userorganization.domain.UserOrganization;
import com.common.auth.userorganization.dto.UserOrganizationGridFilterDTO;
import com.common.auth.userorganization.dto.UserOrganizationResponse;

@Mapper
public interface UserOrganizationMapper {
    
    Optional<UserOrganization> findUserOrganizationByUserOrganizationId(@Param("userOrganizationId") Integer userOrganizationId);
    
    List<UserOrganization> findAllUserOrganizations();

    List<UserOrganizationResponse> selectUserOrganizationsWithFilter(
        @Param("filter") UserOrganizationGridFilterDTO filter
    );
    
    void insertUserOrganization(UserOrganization userOrganization);
    
    void insertUserOrganizationsBulk(@Param("userOrganizations") List<UserOrganization> userOrganizations);

    int updateUserOrganization(UserOrganization userOrganization);

    int updateUserOrganizationsBulk(@Param("userOrganizations") List<UserOrganization> userOrganizations);

    int deleteUserOrganization(UserOrganization userOrganization);

    int deleteUserOrganizationByMemberIdAndOrganizationId(@Param("userId") Integer userId, @Param("organizationId") Integer organizationId);
    
    /**
     * 사용자-조직 매핑을 일괄 삭제합니다.
     *
     * @param userOrganizations 삭제할 사용자-조직 매핑 목록
     * @return 삭제된 레코드 수
     */
    int deleteUserOrganizationsBulk(@Param("userOrganizations") List<UserOrganization> userOrganizations);
    
    List<UserOrganization> findByMemberId(@Param("userId") Integer userId);
    
    List<UserOrganization> findByOrganizationId(@Param("organizationId") Integer organizationId);
    
    void insert(UserOrganization userOrganization);
    
    int deleteByMemberId(@Param("memberId") Integer memberId);
    
    int deleteByOrganizationId(@Param("organizationId") Integer organizationId);
    
    int deleteByMemberIdAndOrganizationId(@Param("memberId") Integer memberId, @Param("organizationId") Integer organizationId);
    
    int deleteUserOrganizationsByMemberIdAndOrganizationId(@Param("memberId") Integer memberId, @Param("organizationId") Integer organizationId);
    
    boolean existsByMemberIdAndOrganizationId(@Param("memberId") Integer memberId, @Param("organizationId") Integer organizationId);
    
    List<UserOrganization> selectExistingUserOrganizationsByCompositeKeys(@Param("userOrganizations") List<UserOrganization> userOrganizations);
    
    int softDeleteUserOrganizationsBulk(@Param("userOrganizations") List<UserOrganization> userOrganizations);
}
