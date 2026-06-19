package com.jeil.delivery.mapper;

import com.common.auth.user.dto.UserGridFilterDTO;
import com.common.auth.user.dto.UserResponse;
import com.common.auth.userrole.domain.UserRole;
import com.common.auth.userrole.dto.UserRoleGridFilterDTO;
import com.common.auth.userrole.dto.UserRoleResponse;
import org.apache.ibatis.annotations.Mapper;

import com.jeil.delivery.domain.MemberDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberMapper {
	public MemberDTO selectMemberDetailById(String userId); // 로그인 ID 기준으로 사용자 조회

	List<MemberDTO> selectMemberList(MemberDTO dto);

	int insertMember(MemberDTO dto);

	int updateMember(MemberDTO dto);

	int deleteMember(MemberDTO dto);

	boolean checkUserId(String userId);

	Optional<MemberDTO> selectUserByUserId(String userId);

	Optional<MemberDTO> selectUserBymemberId(Integer memberId);

	boolean selectExistsByUserId(String userId);
	int selectMemberIdByUserId(String userId);

	List<UserResponse> selectUsersWithFilter(@Param("filter") UserGridFilterDTO filter);

	List<UserRoleResponse> selectUserRolesWithFilter(
			@Param("filter") UserRoleGridFilterDTO filter
	);

	List<UserRole> selectExistingUserRolesByCompositeKeys(@Param("userRoles") List<UserRole> userRoles);

	int insertUserRolesBulk(@Param("userRoles") List<UserRole> toInsert);

	int updateUserRolesBulk(@Param("userRoles") List<UserRole> userRoles);

	int softDeleteUserRolesBulk(@Param("userRoles") List<UserRole> userRoles);
}
