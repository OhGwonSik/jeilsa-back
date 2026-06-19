package com.jeil.delivery.billing.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BillCompanyVO {

	private Integer billCompanyId;         // 송장 연동용 회사 ID
    private String companyNm;              // 화주명
    private String representativeNm;       // 대표자명
    private String bizNo;                  // 사업자번호
    private String address;                // 주소
    private String postNo;                // 우편번호
    private String bizType;               // 업태
    private String bizItem;               // 종목
    private String telNo;                 // 전화번호
    private String faxNo;                 // 팩스번호
    private String email;                 // 이메일
    private String bankNm;                // 은행명
    private String depositorNm;           // 예금주명
    private String accountNo;             // 계좌번호
    private String rmk;                   // 비고

    private Integer regId;                // 등록자 ID
    private LocalDateTime regDt;          // 등록일시
    private Integer chgId;                // 수정자 ID
    private LocalDateTime chgDt;          // 수정일시
    private String delYn;                 // 삭제 여부 (Soft Delete)
}
