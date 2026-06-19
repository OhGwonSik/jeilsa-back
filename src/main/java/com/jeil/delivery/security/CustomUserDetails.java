package com.jeil.delivery.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.jeil.delivery.domain.MemberDTO;

import lombok.Getter;

/**
 * Spring Security가 이해할 수 있도록 memberDTO를 Wrapping한 클래스
 */
public class CustomUserDetails implements UserDetails {
	private static final long serialVersionUID = -965292052058204506L;

    @Getter
    private final MemberDTO memberDTO;

    public CustomUserDetails(MemberDTO memberDTO) {
        this.memberDTO = memberDTO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // MEMBER_STATUS_CODE를 권한으로 사용 (예: ROLE_USER, ROLE_ADMIN 등)
        String role = "ROLE_" + memberDTO.getGroupVal();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return memberDTO.getUserPw();
    }

    @Override
    public String getUsername() {
        return memberDTO.getUserId(); // 로그인 ID
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 정책 없다면 true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 정책 없다면 true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 정책 없다면 true
    }

    @Override
    public boolean isEnabled() {
    	return "N".equals(memberDTO.getDelYn()); // 활성화 상태 여부 N이면 true라 가능
    }
}
