package com.common.auth.user.service;


import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.exception.UserManagementException;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.role.domain.Role;
import com.common.auth.role.service.RoleService;
import com.common.auth.user.domain.User;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    //----- Constants -----//
    private static final int SUCCESSFUL_UPDATE_COUNT = 0;
    private static final int FAILED_COUNT_INCREMENT = 1;
    private static final int INITIAL_FAIL_COUNT = 0;

    //----- DI Fields -----//
//    private final UserMapper userMapper;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public Optional<MemberDTO> selectUserByMemberId(String loginId) {
        log.debug("Finding user by loginId: {}", loginId);
        return memberMapper.selectUserByUserId(loginId);
    }

    public Optional<MemberDTO> selectUserBymemberId(Integer memberId) {
        log.debug("Finding user by email: {}", memberId);
        return memberMapper.selectUserBymemberId(memberId);
    }

    public MemberDTO createMember(String name, String userId, String userPw) {
        log.info("Creating new user with userId: {}", userId);
        
        if (memberMapper.selectExistsByUserId(userId)) {
            throw new UserManagementException(ErrorCode.USER_ALREADY_EXISTS, "Email: " + userId);
        }

        String encodedPassword = passwordEncoder.encode(userPw);
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setName(name);
        memberDTO.setUserId(userId);
        memberDTO.setUserPw(encodedPassword);

        memberMapper.insertMember(memberDTO);
        log.info("Successfully inserted user with userId: {}", userId);
        
        // Retrieve the user with the generated userId from database
        MemberDTO createdMember = memberMapper.selectUserByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Failed to retrieve created user with userId: " + userId));
        
        log.info("Successfully created user with userId: {} and userId: {}", userId, createdMember.getUserId());

        return createdMember;
    }

    public MemberDTO createMemberWithRoles(String name, String userId, String userPw, List<Integer> roleIds) {
        log.info("Creating new user with userId and roles: {}", userId);
        
        // Create user
        MemberDTO memberDTO = createMember(name, userId, userPw);
        
        // Assign roles if provided, otherwise assign default roles
        if (roleIds != null && !roleIds.isEmpty()) {
            roleService.assignRolesToUser(memberDTO.getMemberId(), roleIds);
        } else {
            // Find and assign default roles
            List<Role> defaultRoles = roleService.findDefaultRoles();
            if (!defaultRoles.isEmpty()) {
                List<Integer> defaultRoleIds = defaultRoles.stream()
                    .map(Role::getRoleId)
                    .collect(java.util.stream.Collectors.toList());
                roleService.assignRolesToUser(memberDTO.getMemberId(), defaultRoleIds);
                log.info("Assigned {} default roles to user: {}", defaultRoles.size(), userId);
            }
        }
        
        log.info("Successfully created user with roles: {}", userId);
        return memberDTO;
    }

    public void updateUserStatusToEnable(Integer memberId) {
        log.info("Enabling user with ID: {}", memberId);
        updateUserStatus(memberId, true);
    }

    public void updateUserStatusToDisable(Integer memberId) {
        log.info("Disabling user with ID: {}", memberId);
        updateUserStatus(memberId, false);
    }

    private void updateUserStatus(Integer memberId, boolean active) {
        Optional<MemberDTO> existingMember = memberMapper.selectUserBymemberId(memberId);
        if (existingMember.isEmpty()) {
            throw new UserManagementException(ErrorCode.USER_NOT_FOUND, "User ID: " + memberId);
        }

        MemberDTO memberDTO = existingMember.get();
        memberDTO.setDelYn("N");
        memberDTO.setChgId(SecurityUtil.getCurrentMemberId());

        if (!active) {
            memberDTO.setDelId(SecurityUtil.getCurrentMemberId());
            memberDTO.setDelDt(LocalDateTime.now());
        } else {
            memberDTO.setDelId(0);
            memberDTO.setDelDt(null);
        }

        int updatedRows = memberMapper.updateMember(memberDTO);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update memberDTO status for ID: " + memberId);
        }
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        log.debug("Validating password. Raw: '{}', Encoded: '{}'", rawPassword, encodedPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        log.debug("Password validation result: {}", matches);
       
        return matches;
    }

    /**
     * 로그인 성공 시 호출 - 마지막 로그인 시간 업데이트 및 실패 카운트 초기화
     */
    public void recordLoginSuccess(Integer memberId) {
        log.info("Recording login success for user ID: {}", memberId);
        
        Optional<MemberDTO> userOpt = memberMapper.selectUserBymemberId(memberId);
        if (userOpt.isEmpty()) {
            log.error("User not found with ID: {}", memberId);
            throw new UserManagementException(ErrorCode.USER_NOT_FOUND, "User ID: " + memberId);
        }


        MemberDTO memberDTO = userOpt.get();
        memberDTO.setLastLoginDt(LocalDateTime.now());
        memberDTO.setLoginFailCnt(0); // 실패 카운트 초기화

        int updatedRows = memberMapper.updateMember(memberDTO);
        if (updatedRows == 0) {
            log.error("Failed to update login success for user ID: {}", memberId);
            throw new RuntimeException("Failed to update login success for user ID: " + memberId);
        }

        log.info("Successfully recorded login success for user ID: {}", memberId);
    }

    /**
     * 로그인 실패 시 호출 - 실패 카운트 증가
     */
    public void recordLoginFailure(String userId) {
        log.info("Recording login failure for userId: {}", userId);
        
        Optional<MemberDTO> userOpt = memberMapper.selectUserByUserId(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found with userId during login failure: {}", userId);
            return; // 사용자가 없는 경우 무시 (보안상 사용자 존재 여부 노출 방지)
        }

        MemberDTO memberDTO = new MemberDTO();
        int currentFailCount = memberDTO.getLoginFailCnt() != null ? memberDTO.getLoginFailCnt() : INITIAL_FAIL_COUNT;
        memberDTO.setLoginFailCnt(currentFailCount + FAILED_COUNT_INCREMENT);

        int updatedRows = memberMapper.updateMember(memberDTO);
        if (updatedRows == SUCCESSFUL_UPDATE_COUNT) {
            log.error("Failed to update login failure count for userId: {}", userId);
            throw new RuntimeException("Failed to update login failure count for userId: " + userId);
        }

        log.info("Successfully recorded login failure for userId: {} (fail count: {})",
                   userId, memberDTO.getLoginFailCnt());
    }

    /**
     * 계정 잠금 처리 (최대 실패 횟수 도달 시)
     */
    public void lockAccount(String userId) {
        log.info("Locking account for userId: {}", userId);
        
        Optional<MemberDTO> userOpt = memberMapper.selectUserByUserId(userId);
        if (userOpt.isEmpty()) {
            log.warn("User not found with userId during account lock: {}", userId);
            return;
        }

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberStatusCd(User.STATUS_LOCKED);

        int updatedRows = memberMapper.updateMember(memberDTO);
        if (updatedRows == 0) {
            log.error("Failed to lock account for userId: {}", userId);
            throw new RuntimeException("Failed to lock account for userId: " + userId);
        }

        log.info("Successfully locked account for userId: {}", userId);
    }

    /**
     * 사용자 계정 잠금 여부 확인 (USER_STATUS_CD와 로그인 실패 횟수 기준)
     */
    public boolean isAccountLocked(Integer memberId, int maxFailAttempts) {
        Optional<MemberDTO> userOpt = memberMapper.selectUserBymemberId(memberId);
        if (userOpt.isEmpty()) {
            return false; // 사용자가 없으면 잠금되지 않은 것으로 처리
        }

        MemberDTO memberDTO = new MemberDTO();
        // USER_STATUS_CD가 LOCKED인 경우 바로 잠김 처리
        if (User.STATUS_LOCKED.equals(memberDTO.getMemberStatusCd())) {
            log.warn("Account locked by status for email: {} (status: {})", memberId, memberDTO.getMemberStatusCd());
            return true;
        }
        
        // 로그인 실패 횟수 체크
        int failCount = memberDTO.getLoginFailCnt() != null ? memberDTO.getLoginFailCnt() : 0;
        boolean isLocked = failCount >= maxFailAttempts;
        
        if (isLocked) {
            log.warn("Account locked for email: {} (fail count: {})", memberId, failCount);
        }
        
        return isLocked;
    }

    /**
     * 계정 잠금 해제 (관리자 기능)
     */
    public void unlockAccount(Integer memberId) {
        log.info("Unlocking account for user ID: {}", memberId);
        
        Optional<MemberDTO> userOpt = memberMapper.selectUserBymemberId(memberId);
        if (userOpt.isEmpty()) {
            throw new UserManagementException(ErrorCode.USER_NOT_FOUND, "User ID: " + memberId);
        }

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setLoginFailCnt(0);
        // 상태가 LOCKED였다면 ACTIVE로 복원
        if (User.STATUS_LOCKED.equals(memberDTO.getMemberStatusCd())) {
            memberDTO.setMemberStatusCd(User.STATUS_ACTIVE);
        }

        int updatedRows = memberMapper.updateMember(memberDTO);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to unlock account for user ID: " + memberId);
        }

        log.info("Successfully unlocked account for user ID: {}", memberId);
    }
}
