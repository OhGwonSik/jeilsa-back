package com.jeil.delivery.system.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ShipperVO {

    private int shipperId;              // 화주 ID (PK, auto increment)
    private int companyId;              // 회사 ID

    private String useYn;               // 사용 여부
    private String shipperCd;           // 화주 코드
    private String vatYn;               // 부가세 여부
    private String billStdrCd;          // 청구 기준 코드
    private String taxbilOutputYn;      // 세금계산서 출력 여부

    private Integer communicationFee;      // 통신료
    private Integer untpc;               // 수량단가 (택배)
    private Integer weightUntpc;          // 무게 단가 (통신)

    private int billCompanyId;          // 청구 회사 ID
    private Integer excclcCompanyId;    // 정산 회사 ID (nullable)

    private Integer regId;              // 등록자 ID
    private LocalDateTime regDt;        // 등록 일시
    private Integer chgId;              // 변경자 ID
    private LocalDateTime chgDt;        // 변경 일시
    private String delYn;               // 삭제 여부
}
