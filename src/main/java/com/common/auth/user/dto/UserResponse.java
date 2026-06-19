package com.common.auth.user.dto;


import com.common.auth.role.domain.Role;
import com.jeil.delivery.domain.MemberDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {
    private Integer memberId;
    private String email;
    private String userId;
    private String telNo;
    private String name;
    private String memberStatusCd;
    private String delYn;
    private Integer delId;
    private LocalDateTime regDt;
    private Integer regId;
    private LocalDateTime chgDt;
    private Integer chgId;
    private List<Role> roles;

    public UserResponse(MemberDTO memberDTO) {
        this.memberId = memberDTO.getMemberId();
        this.userId = memberDTO.getUserId();
        this.email = memberDTO.getEmail();
        this.telNo = memberDTO.getTelNo();
        this.name = memberDTO.getName();
        this.memberStatusCd = memberDTO.getMemberStatusCd();
        this.delYn = memberDTO.getDelYn();
        this.delId = memberDTO.getDelId();
        this.regId = memberDTO.getRegId();
        this.regDt = memberDTO.getRegDt();
        this.chgId = memberDTO.getChgId();
        this.chgDt = memberDTO.getChgDt();
    }
}