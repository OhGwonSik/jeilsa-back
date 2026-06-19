package com.common.auth.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    //----- Fields -----//
    private String accessToken;
    private String refreshToken;
    private String memberId;
    private String email;
    private String telNo;
    private String name;
    private String userStatusCd;
    private String roles;
    private Integer userCompanyId; // 소속 회사 ID
    private List<String> permissions;
    private List<Map<String, Object>> menus;
}