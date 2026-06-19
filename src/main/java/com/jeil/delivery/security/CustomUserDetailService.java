package com.jeil.delivery.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security가 로그인 시 사용자 정보를 로드할 때 사용하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailService implements UserDetailsService {
	private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ID로 사용자 정보 조회
        MemberDTO member = memberMapper.selectMemberDetailById(username);

        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        log.info("DB에서 조회된 사용자 ID: {}, password: {}", member.getUserId(), member.getUserPw());

        return new CustomUserDetails(member);
    }
}
