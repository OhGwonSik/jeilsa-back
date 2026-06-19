package com.common.auth.common.util;

import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

public class SecurityUtil {
    /**
     * 현재 인증된 사용자의 ID를 SecurityContext에서 가져옵니다.
     * JWT 인증 필터에서 설정된 사용자 ID를 반환합니다.
     * 
     * @return 현재 사용자의 Integer
     * @throws IllegalStateException 인증되지 않은 사용자이거나 잘못된 형식의 사용자 ID인 경우
     */
    public static Integer getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal == null) {
            throw new IllegalStateException("Principal is null");
        }

        // 이미 숫자면 바로
        if (principal instanceof Integer i) return i;
        if (principal instanceof Long l) return Math.toIntExact(l);

        // CustomUserDetails/MemberDTO 에서 원시 int를 꺼내서 박싱
        try {
            if (principal instanceof CustomUserDetails cud) {
                MemberDTO m = cud.getMemberDTO();
                if (m != null) return Integer.valueOf(m.getMemberId()); // int → Integer
            }
            if (principal instanceof MemberDTO m) {
                return Integer.valueOf(m.getMemberId()); // int → Integer
            }
        } catch (NoClassDefFoundError ignore) {
            // 해당 타입이 없으면 그냥 넘어감
        }

        // 문자열이면 숫자 파싱
        if (principal instanceof String s) {
            s = s.trim();
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                String name = auth.getName();
                try {
                    return Integer.valueOf(name.trim());
                } catch (Exception e2) {
                    throw new IllegalStateException("Invalid numeric user ID format: " + s, e);
                }
            }
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass());
    }
    
    /**
     * 현재 사용자가 인증되어 있는지 확인합니다.
     * 
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}