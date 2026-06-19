package com.common.auth.user.mapper;

import java.util.List;
import java.util.Optional;


import com.jeil.delivery.domain.MemberDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.common.auth.user.domain.User;
import com.common.auth.user.dto.UserGridFilterDTO;
import com.common.auth.user.dto.UserResponse;
import com.common.auth.user.dto.UserUpsertItem;

@Mapper
public interface UserMapper {
    
    
    Optional<MemberDTO> selectUserByLoginId(String loginId);
    
    Optional<MemberDTO> selectUserByMemberId(Integer memberId);
    
    List<MemberDTO> selectAllUsers();
    
    void insertUser(MemberDTO memberDTO);

    int updateUser(MemberDTO memberDTO);
    
    int deleteUserByMemberId(@Param("memberId") Integer memberId);
    
    boolean selectExistsByUserEmail(@Param("email") String email);
    
    int selectCountUsers();
    
    // Grid 관련 메서드
//    List<UserResponse> selectUsersWithFilter(@Param("filter") UserGridFilterDTO filter);
    
    void insertUsersBatch(@Param("users") List<MemberDTO> users);
    
    void updateUsersBatch(@Param("users") List<MemberDTO> users);
    
    // Bulk operations - 중복 체크용 메서드들
    List<Integer> selectExistUserIdListByMemberIds(@Param("userIds") List<Integer> userIds);
    
    List<String> selectExistEmailListByEmails(@Param("emails") List<String> emails);
    
    List<User> selectUserListByEmailsAndNotInUserIds(@Param("items") List<UserUpsertItem> items);
    
    // Bulk operations - CRUD 메서드들
    int insertUsersBulk(@Param("users") List<MemberDTO> users);
    
    int updateUsersBulk(@Param("users") List<MemberDTO> users);
    
    int deleteUsersBulk(@Param("users") List<MemberDTO> users);
    
    int softDeleteUsersBulk(@Param("users") List<MemberDTO> users);
}