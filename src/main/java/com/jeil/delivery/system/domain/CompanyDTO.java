package com.jeil.delivery.system.domain;

import com.jeil.delivery.domain.CommonColumnDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CompanyDTO extends CommonColumnDTO{

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

    private int shipperId;              // 화주 ID (PK, auto increment)
    private String useYn;               // 사용 여부
    private String shipperCd;           // 화주 코드
    private String shipperNm;
    private String vatYn;               // 부가세 여부
    private String billStdrCd;          // 청구 기준 코드
    private String billCharCd;			// 요금 청구 코드
    private String taxbilOutputYn;      // 세금계산서 출력 여부

    private Integer communicationFee;      // 통신료
    private Integer untpc;               // 수량단가 (택배)
    private Integer weightUntpc;          // 무게 단가 (통신)

    private int billCompanyId;          // 청구 회사 ID
    private Integer excclcCompanyId;    // 정산 회사 ID (nullable)
    private Integer calculationCompanyId;

    private int deliveryRouteId;
    private String deliveryRouteCd;
    private String deliveryRouteLabel;

    private int origDeliveryRouteId;

    private String regionNm;
    private String regionDtlNm;
    private String billCompanyNm;
    private String excclcCompanyNm;
    private String deliveryRouteNm;

    private String startDate;
    private String endDate;

    private Integer userCompanyId;
    private Boolean isUser;

}
