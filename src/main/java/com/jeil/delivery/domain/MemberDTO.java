package com.jeil.delivery.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberDTO implements Serializable{
	private static final long serialVersionUID = 5598343544119175224L;
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";

    private int memberId; // 회원 PK
    private int companyId; // 회사 PK
    private String companyNm; // 회사명
    private int groupId; // 그룹 PK
    private String userId; // 아이디
    private String userPw; // 비밀번호
    private String name; // 이름
    private String email; // 이메일
    private String telNo; // 전화번호
    private String groupVal; // 그룹값
    private int regId; // 등록자
    private LocalDateTime regDt; // 등록일시
    private int chgId; // 변경자
    private LocalDateTime chgDt; // 변경일시
    private int delId; // 삭제자
    private LocalDateTime delDt; // 삭제일시
    private String delYn; //삭제여부
    private LocalDateTime lastLoginDt;
    private Integer loginFailCnt;
    private String memberStatusCd;
    private String roles; // 권한명
    private Integer roleId;
    private Integer userCompanyId; //소속 회사 ID
    private boolean selfOnly; // 본인것만 조회 가능

    public MemberDTO() {
        this.regDt = LocalDateTime.now();
        this.chgDt = LocalDateTime.now();
        this.delYn = "N";
        this.loginFailCnt = 0;
    }

    public MemberDTO(String name, String email, String passwordHash) {
        this();
        this.name = name;
        this.email = email;
        this.userPw = passwordHash;
        this.memberStatusCd = STATUS_ACTIVE;
    }
}
