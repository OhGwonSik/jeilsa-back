package com.jeil.delivery.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityUtils {
	public static SecurityContext getContext() {
		return SecurityContextHolder.getContext();
	}

	public static Authentication getAuthentication() {
		return getContext().getAuthentication();
	}

	public static Object getPrincipal() {
		return getAuthentication().getPrincipal();
	}

	public static CustomUserDetails getCustomUserDetails() {
		if(getPrincipal() instanceof CustomUserDetails customUserDetails) {
			customUserDetails = (CustomUserDetails)getPrincipal();
			return customUserDetails;
		}

		return null;
	}


	/*
	 *  AccessToken 혹은 RefreshToken으로 사용자 정보(MemberDTO)를 복원한 이후 사용
	 * 요청(request)와 함께 인증 객체를 SecurityContextHolder에 설정함
	 * */

    public void setAuthentication(MemberDTO member, HttpServletRequest request) {
        CustomUserDetails userDetails = new CustomUserDetails(member);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
