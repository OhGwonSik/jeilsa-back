package com.jeil.delivery.system.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CompanyVO {

    private int companyId;               // 회사 ID (PK, auto increment)
    private String regionCd;
    private String regionDtlCd;
    private String bizNo;                // 사업자 번호
    private String companyNm;            // 회사명
    private String representativeNm;     // 대표자명
    private String bizType;              // 업태
    private String bizItem;              // 업종
    private String address;              // 주소
    private String telNo;                // 전화번호
    private String faxNo;                // 팩스번호
    private String email;                // 이메일
    private String managerNm;           // 담당자명
    private String managerTelNo;           // 담당자번호
    private String rmk;                  // 비고
    private String shipperYn;           // 화주업체 여부
    private Integer untpc;
    private Integer weightUntpc;
    private Integer regId;               // 등록자 ID
    private LocalDateTime regDt;         // 등록 일시
    private Integer chgId;               // 변경자 ID
    private LocalDateTime chgDt;         // 변경 일시
    private String delYn;                // 삭제 여부
    private String shipperCd;
    private String deliveryRouteNm;
}
